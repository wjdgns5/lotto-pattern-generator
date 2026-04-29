# 로또 CSV 업로드 기반 번호 추출 프로젝트 학습 기록

이 문서는 STEP 1부터 STEP 19까지 진행한 튜터링 내용을 Notion이나 면접 준비용으로 다시 보기 좋게 정리한 문서입니다.

## 전체 목표

AI 도움으로 만든 로또 번호 추출 프로젝트를 직접 이해하고, 면접에서 아래 질문에 답할 수 있도록 기능 흐름 중심으로 학습했습니다.

- 왜 이렇게 작성했나요?
- 어떤 오류가 있었나요?
- 직접 구현할 수 있나요?
- CSV 업로드는 어떻게 처리했나요?
- DB에는 왜 저장했나요?
- 번호 생성 로직은 어떻게 만들었나요?
- 과거 당첨번호는 어떻게 제외했나요?

## 전체 흐름 요약

```text
CSV 업로드
  ↓
MultipartFile로 파일 받기
  ↓
Service로 전달
  ↓
CSV 문자열 읽기
  ↓
BufferedReader로 한 줄씩 읽기
  ↓
CSV 한 줄을 컬럼으로 파싱
  ↓
회차/날짜/번호/보너스번호로 변환
  ↓
Entity로 변환
  ↓
DB 저장
  ↓
저장된 과거 당첨번호 조회
  ↓
1~45 중 서로 다른 6개 번호 생성
  ↓
과거 당첨번호 동일 조합 제외
  ↓
홀짝/합계/제외번호/하위빈도/마킹 규칙 필터 적용
  ↓
화면에 결과 출력
```

---

## STEP 1. 프로젝트 구조와 역할 나누기

### 핵심 내용

프로젝트는 Controller, Service, Repository, Entity, DTO/Model로 역할을 나눕니다.

```text
사용자
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
DB
```

### 역할

| 계층 | 역할 |
| --- | --- |
| Controller | 사용자의 요청을 받는 입구 |
| Service | 실제 비즈니스 로직 처리 |
| Repository | DB 저장/조회 담당 |
| Entity | DB 테이블과 연결되는 객체 |
| DTO / Model | 요청값 또는 결과값을 담는 객체 |

### 왜 필요한가

Controller에 모든 로직을 넣으면 코드가 길어지고 유지보수가 어려워집니다.

```text
Controller = 요청 받기
Service = 실제 작업하기
Repository = DB와 대화하기
```

### 면접 답변

> 프로젝트는 Controller, Service, Repository 계층으로 나누었습니다. Controller는 사용자의 요청을 받고, Service는 CSV 파싱이나 번호 생성 같은 비즈니스 로직을 처리하며, Repository는 JPA를 통해 DB 저장과 조회를 담당합니다. 이렇게 역할을 분리해서 코드가 길어지는 것을 막고 유지보수하기 쉽게 구성했습니다.

---

## STEP 2. CSV 파일 업로드 요청 받기

### 핵심 내용

사용자가 CSV 파일을 선택하고 서버로 업로드하는 흐름을 만듭니다.

```text
사용자 CSV 파일 선택
  ↓
업로드 버튼 클릭
  ↓
Spring Boot Controller가 MultipartFile로 파일 받기
```

### 핵심 HTML 코드

```html
<form method="post" action="/winning-numbers/upload" enctype="multipart/form-data">
    <input type="file" name="file" accept=".csv" required>
    <button type="submit">CSV 업로드</button>
</form>
```

### 핵심 Controller 코드

```java
@PostMapping("/winning-numbers/upload")
public String uploadWinningNumbers(@RequestParam MultipartFile file) {
    System.out.println(file.getOriginalFilename());
    return "redirect:/admin";
}
```

### 주의할 점

파일 업로드 form에는 반드시 아래 속성이 필요합니다.

```html
enctype="multipart/form-data"
```

### 면접 답변

> CSV 파일 업로드는 HTML form에서 `multipart/form-data` 방식으로 전송하고, Spring Boot Controller에서 `MultipartFile`로 받도록 구현했습니다.

---

## STEP 3. Controller에서 Service로 파일 전달하기

### 핵심 내용

Controller가 받은 CSV 파일을 Service로 넘깁니다.

```text
Controller가 MultipartFile 받기
  ↓
winningNumberService.importCsv(file) 호출
  ↓
Service가 파일 처리 준비
```

### 핵심 코드

```java
@PostMapping("/winning-numbers/upload")
public String uploadWinningNumbers(@RequestParam MultipartFile file) {
    winningNumberService.importCsv(file);
    return "redirect:/admin";
}
```

```java
@Service
public class WinningNumberService {

    public int importCsv(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV 파일을 선택해주세요.");
        }

        return 0;
    }
}
```

### 왜 필요한가

CSV 읽기, 파싱, DB 저장은 실제 비즈니스 로직이므로 Controller가 아니라 Service에서 처리하는 것이 좋습니다.

### 면접 답변

> Controller는 사용자의 요청을 받고 응답을 반환하는 역할에 집중시키고, CSV 파싱과 저장 같은 실제 비즈니스 로직은 Service 계층에 작성했습니다.

---

## STEP 4. MultipartFile을 문자열로 읽기

### 핵심 내용

업로드된 파일을 문자열로 변환합니다.

```text
MultipartFile
  ↓
byte[]
  ↓
String csvText
```

### 핵심 코드

```java
public int importCsv(MultipartFile file) {
    if (file.isEmpty()) {
        throw new IllegalArgumentException("CSV 파일을 선택해주세요.");
    }

    try {
        String csvText = new String(file.getBytes(), StandardCharsets.UTF_8);
        return importCsvText(csvText);
    } catch (IOException exception) {
        throw new IllegalArgumentException("CSV 파일을 읽을 수 없습니다.");
    }
}
```

### 왜 UTF-8을 쓰는가

파일은 byte 형태로 읽히기 때문에 문자열로 바꿀 때 문자 규칙인 인코딩이 필요합니다. CSV에 한글이 들어 있을 수 있으므로 UTF-8을 지정해 문자 깨짐을 방지합니다.

### 면접 답변

> MultipartFile의 내용은 byte 배열로 읽히기 때문에 문자열로 변환할 때 인코딩을 지정해야 합니다. CSV에 한글 헤더나 한글 데이터가 있을 수 있어서 `UTF-8`을 사용해 문자 깨짐을 방지했습니다.

---

## STEP 5. CSV 문자열을 한 줄씩 읽기

### 핵심 내용

CSV 문자열 전체를 `BufferedReader`로 한 줄씩 읽습니다.

```text
CSV 문자열 전체
  ↓
BufferedReader
  ↓
첫 줄은 헤더
  ↓
두 번째 줄부터 데이터
```

### 핵심 코드

```java
public int importCsvText(String csvText) {
    int importedCount = 0;

    try (BufferedReader reader = new BufferedReader(new StringReader(csvText))) {
        String headerLine = reader.readLine();

        if (headerLine == null) {
            return 0;
        }

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }

            importedCount++;
        }
    } catch (IOException exception) {
        throw new IllegalArgumentException("CSV 파일을 처리할 수 없습니다.");
    }

    return importedCount;
}
```

### 주의할 점

첫 번째 줄은 보통 `drawNumber`, `drawDate`, `number1` 같은 헤더이므로 실제 데이터로 저장하지 않습니다.

### 면접 답변

> 업로드받은 MultipartFile을 UTF-8 문자열로 변환한 뒤, StringReader와 BufferedReader를 사용해 한 줄씩 읽었습니다. 첫 번째 줄은 헤더로 처리하고, 두 번째 줄부터 실제 데이터로 반복 처리했습니다.

---

## STEP 6. CSV 한 줄을 컬럼으로 나누기

### 핵심 내용

CSV 한 줄을 쉼표 기준으로 컬럼으로 나눕니다.

### 단순 방식

```java
String[] columns = line.split(",");
```

하지만 `"1,000"`처럼 따옴표 안에 쉼표가 들어 있는 경우 문제가 생깁니다.

### 개선 코드

```java
private List<String> parseCsvLine(String line) {
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
```

### 면접 답변

> CSV를 한 줄씩 읽은 뒤 쉼표를 기준으로 컬럼을 분리했습니다. 단순 `split(",")`은 따옴표 안의 쉼표까지 잘못 나눌 수 있기 때문에, 현재 문자가 따옴표 내부인지 여부를 `quoted` 변수로 추적하면서 파싱했습니다.

---

## STEP 7. CSV 컬럼을 회차, 날짜, 번호로 변환하기

### 핵심 내용

CSV에서 읽은 값은 모두 문자열이므로 필요한 타입으로 변환합니다.

```text
회차 → int
날짜 → LocalDate
번호 6개 → List<Integer>
보너스번호 → Integer
```

### 핵심 코드

```java
private WinningDraw parseUploadedCsvLine(List<String> columns) {
    int drawNumber = Integer.parseInt(columns.get(0));
    LocalDate drawDate = LocalDate.parse(columns.get(1));

    List<Integer> numbers = List.of(
            Integer.parseInt(columns.get(2)),
            Integer.parseInt(columns.get(3)),
            Integer.parseInt(columns.get(4)),
            Integer.parseInt(columns.get(5)),
            Integer.parseInt(columns.get(6)),
            Integer.parseInt(columns.get(7))
    );

    Integer bonusNumber = Integer.parseInt(columns.get(8));

    return new WinningDraw(drawNumber, drawDate, numbers, bonusNumber);
}
```

### 면접 답변

> CSV에서 읽은 값은 모두 문자열이기 때문에 회차와 번호는 `Integer.parseInt()`로 숫자로 변환하고, 추첨일은 `LocalDate.parse()`를 사용해 날짜 타입으로 변환했습니다.

---

## STEP 8. Entity 만들고 DB 테이블 구조 잡기

### 핵심 내용

CSV에서 파싱한 당첨번호 데이터를 DB에 저장하기 위해 Entity를 만듭니다.

### 핵심 구조

```java
@Entity
@Table(name = "winning_draws")
public class WinningDrawEntity {

    @Id
    private Integer drawNumber;

    private LocalDate drawDate;

    private List<Integer> numbers;

    private Integer bonusNumber;
}
```

### 왜 drawNumber를 @Id로 쓰는가

로또 회차 번호는 중복되지 않습니다.

```text
1회차는 하나만 존재
2회차도 하나만 존재
```

따라서 `drawNumber`는 각 데이터를 고유하게 구분하는 기본키로 적합합니다.

### 면접 답변

> 로또 당첨번호는 회차가 중복될 수 없기 때문에 회차 번호를 기본키로 사용했습니다. Entity에는 회차 번호, 추첨일, 당첨번호 6개, 보너스번호를 저장했습니다.

---

## STEP 9. Repository 만들기

### 핵심 내용

Entity를 DB에 저장하고 조회하기 위해 Repository를 만듭니다.

### 핵심 코드

```java
public interface WinningDrawRepository
        extends JpaRepository<WinningDrawEntity, Integer> {
}
```

### 코드 설명

```text
WinningDrawEntity = 관리할 Entity
Integer = Entity의 기본키 타입
```

`drawNumber`가 `Integer` 타입의 기본키이므로 Repository의 ID 타입도 `Integer`입니다.

### JPA 기본 기능

```text
save()
findAll()
findById()
delete()
count()
```

### 면접 답변

> Spring Data JPA의 `JpaRepository`를 사용해 Repository 계층을 구성했습니다. `WinningDrawEntity`와 기본키 타입인 `Integer`를 지정해서 저장, 전체 조회, 단건 조회, 개수 조회 같은 기본 DB 기능을 사용할 수 있게 했습니다.

---

## STEP 10. 파싱한 데이터를 Entity로 변환하고 DB에 저장하기

### 핵심 내용

CSV에서 파싱한 데이터를 Entity로 변환하고 Repository로 DB에 저장합니다.

### 핵심 코드

```java
WinningDraw draw = parseUploadedCsvLine(columns);

winningDrawRepository.save(new WinningDrawEntity(
        draw.getDrawNumber(),
        draw.getDrawDate(),
        draw.getNumbers(),
        draw.getBonusNumber()
));
```

### 왜 DB에 저장하는가

CSV를 읽고 바로 버리면 데이터를 다시 사용할 수 없습니다. DB에 저장해두면 아래 기능에서 계속 재사용할 수 있습니다.

```text
과거 당첨번호와 같은 조합 제외
통계 대시보드
최신 회차 조회
관리자 수정
외부 API 업데이트
```

### 면접 답변

> CSV 한 줄을 읽어 회차, 추첨일, 당첨번호 6개, 보너스번호로 파싱한 뒤, 이를 `WinningDrawEntity`로 변환해 JPA Repository의 `save()` 메서드로 저장했습니다.

---

## STEP 11. 저장된 당첨번호 데이터 조회하기

### 핵심 내용

DB에 저장된 과거 당첨번호를 조회하고, 서비스에서 사용하기 좋은 모델로 변환합니다.

### 핵심 코드

```java
@Transactional(readOnly = true)
public List<WinningDraw> findAll() {
    return winningDrawRepository.findAll().stream()
            .sorted(Comparator.comparing(WinningDrawEntity::getDrawNumber).reversed())
            .map(this::toModel)
            .toList();
}
```

### Entity를 Model로 변환

```java
private WinningDraw toModel(WinningDrawEntity entity) {
    return new WinningDraw(
            entity.getDrawNumber(),
            entity.getDrawDate(),
            entity.getNumbers(),
            entity.getBonusNumber()
    );
}
```

### 왜 변환하는가

```text
Entity = DB 구조에 가까운 객체
Model = 서비스나 화면에서 사용하기 좋은 객체
```

DB 구조와 서비스 로직을 분리하기 위해 Entity를 Model로 변환합니다.

### 면접 답변

> 당첨번호 데이터는 JPA Repository를 통해 조회했습니다. `findAll()`로 전체 당첨번호를 가져온 뒤, Service 계층에서 최신 회차 순으로 정렬하고 Entity를 서비스에서 사용하기 좋은 모델로 변환했습니다.

---

## STEP 12. 1~45 중 서로 다른 6개 번호 생성하기

### 핵심 내용

1부터 45까지의 숫자 중 서로 다른 6개를 랜덤으로 뽑고 오름차순 정렬합니다.

### 핵심 코드

```java
private final SecureRandom random = new SecureRandom();

private List<Integer> randomNumbers() {
    List<Integer> pool = IntStream.rangeClosed(1, 45)
            .boxed()
            .collect(Collectors.toCollection(ArrayList::new));

    Collections.shuffle(pool, random);

    return pool.stream()
            .limit(6)
            .sorted()
            .toList();
}
```

### 왜 이 방식인가

`random.nextInt(45)`를 6번 반복하면 중복 번호가 나올 수 있습니다.

1~45 리스트를 섞고 앞에서 6개를 뽑으면 애초에 숫자가 하나씩만 있으므로 중복이 생기지 않습니다.

### 면접 답변

> 1부터 45까지의 번호 풀을 만든 뒤 `Collections.shuffle()`로 섞고, 앞에서 6개를 선택하는 방식으로 중복 없는 번호 조합을 생성했습니다. 마지막에는 사용자가 보기 쉽도록 오름차순 정렬했습니다.

---

## STEP 13. 과거 당첨번호와 완전히 같은 조합 제외하기

### 핵심 내용

DB에 저장된 과거 1등 당첨번호와 새 후보 번호가 완전히 같으면 제외합니다.

### 핵심 코드

```java
Set<String> historicalWinningKeys = winningNumberService.findAll().stream()
        .map(draw -> draw.getNumbers().toString())
        .collect(Collectors.toSet());
```

```java
String key = candidate.getNumbers().toString();

if (historicalWinningKeys.contains(key)) {
    continue;
}
```

### 왜 Set을 쓰는가

```text
1. 중복 제거
2. contains()로 빠르게 포함 여부 확인
```

### 주의할 점

번호 비교 전에는 정렬되어 있어야 합니다.

```text
[1, 2, 3, 4, 5, 6]
[6, 5, 4, 3, 2, 1]
```

이 둘은 같은 조합이지만 정렬하지 않으면 다르게 보일 수 있습니다.

### 면접 답변

> DB에 저장된 과거 당첨번호 목록을 조회한 뒤, 각 조합을 정렬된 문자열 key로 변환해 `Set`에 저장했습니다. 새로 생성한 후보 번호도 같은 방식으로 key를 만들고, `Set.contains()`로 이미 존재하는 조합인지 검사했습니다. 같은 조합이 있으면 제외했습니다.

---

## STEP 14. 필터 조건 적용하기

### 핵심 내용

랜덤 후보 번호에 여러 조건을 적용해 조건에 맞는 조합만 통과시킵니다.

필터 조건:

```text
제외번호 제거
홀짝 비율 검사
합계 범위 검사
하위 빈도 숫자 제한
일자 마킹 방지 검사
과거 당첨번호 동일 조합 제외
```

### 핵심 코드

```java
if (!hasOddEvenRatio(numbers, oddCount, evenCount)) {
    continue;
}

if (candidate.getSum() < request.getMinSum()
        || candidate.getSum() > request.getMaxSum()) {
    continue;
}

if (candidate.getLowFrequencyNumbers().size() > 1) {
    continue;
}

if (!passesMarkerRules(candidate.getNumbers())) {
    continue;
}
```

### continue를 쓰는 이유

조건에 맞지 않는 후보는 결과에 넣으면 안 되기 때문에 `continue`로 무시하고 다음 후보를 검사합니다.

### 면접 답변

> 먼저 1~45 번호 풀에서 사용자가 입력한 제외번호를 제거한 뒤 랜덤으로 후보 6개를 생성했습니다. 이후 후보에 대해 홀짝 비율, 허용 패턴, 합계 범위, 하위 빈도 숫자 개수, 마킹 규칙을 순서대로 검사했습니다. 조건을 통과하지 못한 후보는 `continue`로 제외하고, 모든 조건을 통과한 후보만 결과에 추가했습니다.

---

## STEP 15. 생성 결과를 화면에 반환하기

### 핵심 내용

Service에서 만든 번호 생성 결과를 Controller가 Model에 담아 JSP 화면에 보여줍니다.

### 흐름

```text
사용자가 번호 생성 버튼 클릭
  ↓
Controller가 요청 받음
  ↓
Service가 번호 생성
  ↓
GenerationResult 반환
  ↓
Controller가 Model에 결과 담음
  ↓
JSP에서 번호 출력
```

### 핵심 Controller 코드

```java
@PostMapping("/generate")
public String generate(@ModelAttribute GenerationRequest request, Model model) {
    GenerationResult result = lottoGenerationService.generate(request);

    model.addAttribute("generationRequest", request);
    model.addAttribute("result", result);

    return "index";
}
```

### 핵심 JSP 코드

```jsp
<c:if test="${not empty result}">
    <c:forEach var="candidate" items="${result.threeOddThreeEvenCandidates}">
        <div>
            <c:forEach var="number" items="${candidate.numbers}">
                <span>${number}</span>
            </c:forEach>
        </div>
    </c:forEach>
</c:if>
```

### 면접 답변

> 사용자가 번호 생성 조건을 입력하면 Controller에서 `GenerationRequest`로 값을 바인딩하고, `LottoGenerationService`를 호출해 `GenerationResult`를 받았습니다. 이후 결과를 `Model`에 담아 JSP로 전달했고, JSP에서는 JSTL의 `c:forEach`를 사용해 후보 번호 목록을 반복 출력했습니다.

---

## STEP 16. 전체 흐름 정리와 면접 답변 만들기

### 전체 흐름

```text
CSV 업로드
  ↓
CSV 읽기
  ↓
CSV 파싱
  ↓
Entity 변환
  ↓
DB 저장
  ↓
저장된 당첨번호 조회
  ↓
랜덤 번호 생성
  ↓
필터 조건 적용
  ↓
과거 당첨번호와 동일 조합 제외
  ↓
화면에 결과 출력
```

### 전체 면접 답변

> 이 프로젝트는 로또 당첨번호 CSV를 업로드받아 DB에 저장하고, 저장된 과거 당첨번호를 기준으로 조건에 맞는 번호 조합을 생성하는 Spring Boot MVC 프로젝트입니다. CSV 파일은 `MultipartFile`로 업로드받고, Service에서 UTF-8 문자열로 변환한 뒤 `BufferedReader`로 한 줄씩 읽어 회차, 날짜, 번호를 파싱했습니다. 파싱한 데이터는 `WinningDrawEntity`로 변환해 JPA Repository로 저장했습니다. 번호 생성 시에는 1~45 번호 풀에서 제외번호를 제거하고 랜덤으로 6개를 뽑은 뒤, 홀짝 비율, 합계 범위, 하위 빈도 숫자, 마킹 규칙, 과거 당첨번호 중복 여부를 검사해 조건을 통과한 조합만 화면에 반환했습니다.

---

## STEP 17. 통계 대시보드 만들기

### 핵심 내용

DB에 저장된 과거 당첨번호를 이용해 통계 대시보드를 만듭니다.

대시보드 항목:

```text
전체 저장 회차 수
번호별 출현 빈도
가장 많이 나온 번호
가장 적게 나온 번호
홀짝 비율 분포
번호 합계 구간 분포
```

### 핵심 코드

```java
List<WinningDraw> draws = winningNumberService.findAll();
```

```java
Map<Integer, Long> numberCounts = new LinkedHashMap<>();

IntStream.rangeClosed(1, 45)
        .forEach(number -> numberCounts.put(number, 0L));
```

```java
for (WinningDraw draw : draws) {
    for (Integer number : draw.getNumbers()) {
        numberCounts.compute(number, (key, value) -> value + 1L);
    }
}
```

### Map 사용 이유

```text
Integer = 로또 번호 1~45
Long = 해당 번호가 출현한 횟수
```

### 면접 답변

> DB에 저장된 전체 당첨번호를 조회한 뒤, `StatsService`에서 번호별 출현 횟수, 홀짝 비율, 합계 구간을 계산했습니다. 번호별 빈도는 1부터 45까지를 key로 가지는 Map을 만들고, 각 회차의 번호를 순회하면서 카운트를 증가시키는 방식으로 구현했습니다.

---

## STEP 18. 관리자 감사 로그 만들기

### 핵심 내용

관리자가 어떤 작업을 했는지 기록하는 감사 로그 기능입니다.

### 감사 로그 예시

```text
admin / CSV_UPLOAD / 1180개 회차 CSV 업로드 / 2026-04-29 10:00
admin / MANUAL_SAVE / 1170회차 당첨번호 수동 저장 / 2026-04-29 10:05
admin / EXTERNAL_UPDATE / 1개 회차 외부 API 업데이트 / 2026-04-29 10:10
```

### 필요한 데이터

```text
username   = 누가 작업했는지
actionType = 어떤 종류의 작업인지
detail     = 어떤 내용을 처리했는지
createdAt  = 언제 작업했는지
```

### 핵심 Service 코드

```java
@Transactional
public void record(String username, String actionType, String detail) {
    adminAuditLogRepository.save(new AdminAuditLog(
            username,
            actionType,
            detail,
            LocalDateTime.now()
    ));
}
```

### 면접 답변

> 관리자 기능은 당첨번호 데이터에 직접 영향을 주기 때문에 누가 어떤 작업을 했는지 추적할 필요가 있다고 생각했습니다. 그래서 CSV 업로드, 수동 저장, 외부 API 업데이트 같은 작업이 성공하면 현재 로그인한 관리자 ID, 작업 유형, 상세 내용, 시간을 감사 로그 테이블에 저장하도록 구현했습니다.

---

## STEP 19. 스케줄러로 외부 API 자동 업데이트하기

### 핵심 내용

매주 정해진 시간에 자동으로 최신 로또 당첨번호를 가져오는 기능입니다.

### 전체 흐름

```text
Spring Boot 서버 실행 중
  ↓
매주 월요일 오전 9시
  ↓
스케줄러 자동 실행
  ↓
DB에 저장된 최신 회차 확인
  ↓
동행복권 API 호출
  ↓
새 회차가 있으면 DB 저장
```

### 핵심 코드

```java
@EnableScheduling
@SpringBootApplication
public class LottoPatternGeneratorApplication {
}
```

```java
@Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Seoul")
@Transactional
public void updateLatestDrawsOnSchedule() {
    if (autoUpdateEnabled) {
        updateFromExternalApi();
    }
}
```

### cron 의미

```text
초 분 시 일 월 요일
0  0  9  *  *  MON
```

뜻:

```text
매주 월요일 오전 9시
한국 시간 기준
```

### 중요한 점

Spring Scheduler는 애플리케이션이 실행 중일 때만 동작합니다.

```text
Spring Boot 서버 실행 중
  ↓
스케줄러도 실행 중
  ↓
정해진 시간이 되면 자동 실행
```

서버가 꺼져 있으면 스케줄러도 동작하지 않습니다.

### 면접 답변

> Spring Scheduler를 사용해 매주 월요일 오전 9시에 자동 업데이트 메서드가 실행되도록 구현했습니다. DB에서 현재 저장된 최신 회차를 조회한 뒤, 최신 회차 다음 번호부터 동행복권 API를 호출합니다. API 응답이 success이면 당첨번호를 Entity로 변환해 DB에 저장하고, success가 아니면 아직 존재하지 않는 회차로 판단해 반복을 중단합니다.

---

## 최종 프로젝트 소개 답변

> 이 프로젝트는 로또 당첨번호 CSV를 업로드받아 DB에 저장하고, 저장된 과거 당첨번호를 기준으로 조건에 맞는 번호 조합을 생성하는 Spring Boot MVC 프로젝트입니다. CSV 파일은 `MultipartFile`로 업로드받고, Service에서 UTF-8 문자열로 변환한 뒤 `BufferedReader`로 한 줄씩 읽어 회차, 날짜, 번호를 파싱했습니다. 파싱한 데이터는 `WinningDrawEntity`로 변환해 JPA Repository로 저장했습니다. 번호 생성 시에는 1~45 번호 풀에서 제외번호를 제거하고 랜덤으로 6개를 뽑은 뒤, 홀짝 비율, 합계 범위, 하위 빈도 숫자, 마킹 규칙, 과거 당첨번호 중복 여부를 검사해 조건을 통과한 조합만 화면에 반환했습니다. 또한 통계 대시보드, 관리자 감사 로그, 스케줄러 기반 외부 API 자동 업데이트 기능을 추가해 단순 번호 생성기를 넘어 운영 관점의 기능도 포함했습니다.
