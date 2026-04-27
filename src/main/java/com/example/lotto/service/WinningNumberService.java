package com.example.lotto.service;

import com.example.lotto.domain.WinningDrawEntity;
import com.example.lotto.model.WinningDraw;
import com.example.lotto.model.WinningNumberUpdateRequest;
import com.example.lotto.repository.WinningDrawRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class WinningNumberService {

    private final WinningDrawRepository winningDrawRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Path seedCsvPath;
    private final boolean autoUpdateEnabled;

    public WinningNumberService(
            WinningDrawRepository winningDrawRepository,
            @Value("${app.lotto.seed-csv:data/winning-numbers.csv}") String seedCsvPath,
            @Value("${app.lotto.auto-update.enabled:true}") boolean autoUpdateEnabled
    ) {
        this.winningDrawRepository = winningDrawRepository;
        this.seedCsvPath = Path.of(seedCsvPath);
        this.autoUpdateEnabled = autoUpdateEnabled;
    }

    @PostConstruct
    @Transactional
    public void importSeedCsvIfEmpty() {
        // 앱 시작 시 DB가 비어 있으면 seed CSV를 한 번 읽어 초기 당첨번호 데이터를 채웁니다.
        // 이미 DB에 데이터가 있으면 운영 중 데이터가 덮어써지지 않도록 아무 작업도 하지 않습니다.
        if (winningDrawRepository.count() > 0 || Files.notExists(seedCsvPath)) {
            return;
        }
        try {
            importCsvText(Files.readString(seedCsvPath, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("초기 당첨번호 CSV를 읽을 수 없습니다.", exception);
        }
    }

    @Transactional(readOnly = true)
    public List<WinningDraw> findAll() {
        return winningDrawRepository.findAll().stream()
                .sorted(Comparator.comparing(WinningDrawEntity::getDrawNumber).reversed())
                .map(this::toModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WinningDraw> findLatest(int count) {
        return winningDrawRepository.findAll().stream()
                .sorted(Comparator.comparing(WinningDrawEntity::getDrawNumber).reversed())
                .limit(count)
                .map(this::toModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public long count() {
        return winningDrawRepository.count();
    }

    @Transactional(readOnly = true)
    public Optional<WinningDraw> findLatestDraw() {
        return winningDrawRepository.findTopByOrderByDrawNumberDesc().map(this::toModel);
    }

    @Transactional(readOnly = true)
    public boolean isHistoricalWinningCombination(List<Integer> candidateNumbers) {
        List<Integer> sortedCandidate = candidateNumbers.stream().sorted().toList();
        return winningDrawRepository.findAll().stream()
                .map(WinningDrawEntity::getNumbers)
                .anyMatch(sortedCandidate::equals);
    }

    @Transactional
    public WinningDraw saveOrUpdate(WinningNumberUpdateRequest request) {
        WinningDraw draw = toWinningDraw(request);
        winningDrawRepository.save(new WinningDrawEntity(
                draw.getDrawNumber(),
                draw.getDrawDate(),
                draw.getNumbers(),
                draw.getBonusNumber()
        ));
        return draw;
    }

    @Transactional
    public int importCsv(MultipartFile file) {
        // 관리자 화면에서 업로드한 MultipartFile을 문자열로 바꾼 뒤 공통 CSV 파서로 넘깁니다.
        try {
            return importCsvText(new String(file.getBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalArgumentException("CSV 파일을 읽을 수 없습니다.");
        }
    }

    @Transactional
    public int importCsvText(String csvText) {
        int importedCount = 0;
        try (BufferedReader reader = new BufferedReader(new StringReader(csvText))) {
            // 첫 줄은 헤더로 읽고, 그 다음 줄부터 회차 데이터를 WinningDraw로 변환해 저장합니다.
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0;
            }
            List<String> headers = parseCsvLine(headerLine);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                WinningDraw draw = parseUploadedCsvLine(headers, parseCsvLine(line));
                winningDrawRepository.save(new WinningDrawEntity(
                        draw.getDrawNumber(),
                        draw.getDrawDate(),
                        draw.getNumbers(),
                        draw.getBonusNumber()
                ));
                importedCount++;
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("CSV 파일을 처리할 수 없습니다.");
        }
        return importedCount;
    }

    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Seoul")
    @Transactional
    public void updateLatestDrawsOnSchedule() {
        // 서버가 켜져 있으면 매주 월요일 오전 9시에 외부 API 업데이트를 자동 실행합니다.
        if (autoUpdateEnabled) {
            updateFromExternalApi();
        }
    }

    @Transactional
    public int updateFromExternalApi() {
        // DB에 저장된 최신 회차 다음 번호부터 API를 조회합니다.
        // 존재하지 않는 회차가 나오면 returnValue가 success가 아니므로 반복을 멈춥니다.
        int latestDrawNumber = winningDrawRepository.findTopByOrderByDrawNumberDesc()
                .map(WinningDrawEntity::getDrawNumber)
                .orElse(0);
        int updatedCount = 0;

        for (int drawNumber = latestDrawNumber + 1; drawNumber <= latestDrawNumber + 20; drawNumber++) {
            Map<String, Object> response = fetchDraw(drawNumber);
            if (!"success".equals(response.get("returnValue"))) {
                break;
            }
            List<Integer> numbers = List.of(
                    toInt(response.get("drwtNo1")),
                    toInt(response.get("drwtNo2")),
                    toInt(response.get("drwtNo3")),
                    toInt(response.get("drwtNo4")),
                    toInt(response.get("drwtNo5")),
                    toInt(response.get("drwtNo6"))
            );
            winningDrawRepository.save(new WinningDrawEntity(
                    toInt(response.get("drwNo")),
                    parseDate((String) response.get("drwNoDate")),
                    numbers,
                    toInt(response.get("bnusNo"))
            ));
            updatedCount++;
        }

        return updatedCount;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchDraw(int drawNumber) {
        String url = "https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=" + drawNumber;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody() == null ? Map.of("returnValue", "fail") : response.getBody();
    }

    private WinningDraw parseUploadedCsvLine(List<String> headers, List<String> columns) {
        // 이 프로젝트 전용 영문 헤더와 사용자가 준 로또 CSV 형식을 모두 받아들이기 위한 파서입니다.
        Map<String, String> row = new LinkedHashMap<>();
        for (int index = 0; index < headers.size() && index < columns.size(); index++) {
            row.put(headers.get(index), columns.get(index));
        }

        if (row.containsKey("drawNumber")) {
            return new WinningDraw(
                    toInt(row.get("drawNumber")),
                    parseDate(row.get("drawDate")),
                    sortedNumbers(List.of(
                            toInt(row.get("number1")),
                            toInt(row.get("number2")),
                            toInt(row.get("number3")),
                            toInt(row.get("number4")),
                            toInt(row.get("number5")),
                            toInt(row.get("number6"))
                    )),
                    row.get("bonusNumber") == null || row.get("bonusNumber").isBlank() ? null : toInt(row.get("bonusNumber"))
            );
        }

        String drawNumberText = row.getOrDefault("회차", "");
        List<Integer> numbers = new ArrayList<>();
        for (int index = 2; index <= 7 && index < columns.size(); index++) {
            numbers.add(toInt(columns.get(index)));
        }
        Integer bonusNumber = columns.size() > 8 && !columns.get(8).isBlank() ? toInt(columns.get(8)) : null;
        return new WinningDraw(toInt(drawNumberText.replace(",", "")), null, sortedNumbers(numbers), bonusNumber);
    }

    private WinningDraw toWinningDraw(WinningNumberUpdateRequest request) {
        // 관리자 수동 입력값을 검증한 뒤 DB 저장용 모델로 변환합니다.
        if (request.getDrawNumber() < 1) {
            throw new IllegalArgumentException("회차는 1 이상이어야 합니다.");
        }
        List<Integer> numbers = sortedNumbers(List.of(
                required(request.getNumber1(), "1번"),
                required(request.getNumber2(), "2번"),
                required(request.getNumber3(), "3번"),
                required(request.getNumber4(), "4번"),
                required(request.getNumber5(), "5번"),
                required(request.getNumber6(), "6번")
        ));
        validateNumbers(numbers);

        Integer bonusNumber = request.getBonusNumber();
        if (bonusNumber != null) {
            validateRange(bonusNumber, "보너스 번호");
        }

        return new WinningDraw(request.getDrawNumber(), parseDate(request.getDrawDate()), numbers, bonusNumber);
    }

    private WinningDraw toModel(WinningDrawEntity entity) {
        return new WinningDraw(
                entity.getDrawNumber(),
                entity.getDrawDate(),
                entity.getNumbers(),
                entity.getBonusNumber()
        );
    }

    private Integer required(Integer number, String label) {
        if (number == null) {
            throw new IllegalArgumentException(label + " 당첨번호를 입력해 주세요.");
        }
        return number;
    }

    private List<Integer> sortedNumbers(List<Integer> numbers) {
        return numbers.stream().sorted().toList();
    }

    private void validateNumbers(List<Integer> numbers) {
        Set<Integer> uniqueNumbers = new HashSet<>();
        for (Integer number : numbers) {
            validateRange(number, "당첨번호");
            if (!uniqueNumbers.add(number)) {
                throw new IllegalArgumentException("당첨번호 6개는 서로 달라야 합니다.");
            }
        }
    }

    private void validateRange(Integer number, String label) {
        if (number < 1 || number > 45) {
            throw new IllegalArgumentException(label + "는 1부터 45 사이여야 합니다: " + number);
        }
    }

    private LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(rawDate);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("추첨일은 yyyy-MM-dd 형식으로 입력해 주세요.");
        }
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value).replace(",", "").trim());
    }

    private List<String> parseCsvLine(String line) {
        // 따옴표 안의 쉼표는 실제 구분자가 아니므로 quoted 상태를 추적하며 CSV 한 줄을 나눕니다.
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                quoted = !quoted;
            } else if (character == ',' && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        values.add(current.toString().trim());
        return values;
    }
}
