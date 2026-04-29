# STEP 1~19 대화형 복습 기록

이 문서는 STEP 1부터 STEP 19까지 튜터링하면서 주고받은 핵심 질문과 답변을 복습용으로 정리한 문서입니다.

목적은 단순히 코드를 외우는 것이 아니라, 면접에서 질문을 받았을 때 내 말로 설명할 수 있게 만드는 것입니다.

---

## STEP 1. 프로젝트 구조와 역할 나누기

### 배운 내용

프로젝트는 Controller, Service, Repository, Entity, DTO/Model로 역할을 나누는 것이 좋습니다.

```text
Controller = 요청 받기
Service = 실제 작업하기
Repository = DB 저장/조회하기
Entity = DB 테이블과 연결되는 객체
DTO/Model = 데이터를 주고받기 위한 객체
```

### 확인 질문

CSV 파일을 실제로 읽고 파싱하는 로직은 어디에 두는 게 좋을까?

### 내 답변

Service.

### 정리 답변

CSV 파일을 읽고 파싱하는 것은 실제 비즈니스 로직이므로 Service에 두는 것이 좋습니다.

### 면접 답변

> CSV 파일을 읽고 파싱하는 로직은 비즈니스 로직에 해당한다고 생각해서 Service 계층에 배치했습니다. Controller는 MultipartFile을 전달받아 Service를 호출하는 역할만 하도록 분리했습니다.

---

## STEP 2. CSV 파일 업로드 요청 받기

### 배운 내용

파일 업로드는 HTML form에서 `multipart/form-data` 방식으로 전송하고, Spring Controller에서 `MultipartFile`로 받습니다.

```html
<form method="post" action="/winning-numbers/upload" enctype="multipart/form-data">
    <input type="file" name="file">
</form>
```

```java
@PostMapping("/winning-numbers/upload")
public String upload(@RequestParam MultipartFile file) {
    return "redirect:/admin";
}
```

### 확인 질문

파일 업로드 form에서 파일을 서버로 보내기 위해 반드시 넣어야 하는 속성은 무엇일까?

### 내 답변

MultipartFile 안에 enctype.

### 정리 답변

`MultipartFile`은 Java Controller에서 파일을 받을 때 쓰는 타입이고, `enctype`은 HTML form에서 파일을 보낼 때 필요한 설정입니다.

정확히는 form 태그에 아래 속성을 넣어야 합니다.

```html
enctype="multipart/form-data"
```

### 면접 답변

> 파일 업로드는 HTML form에 `enctype="multipart/form-data"`를 설정하고, Spring Controller에서 `MultipartFile` 타입으로 파일을 받았습니다.

---

## STEP 3. Controller에서 Service로 파일 전달하기

### 배운 내용

Controller는 업로드 요청을 받고, 실제 CSV 처리는 Service에 맡깁니다.

```java
@PostMapping("/winning-numbers/upload")
public String uploadWinningNumbers(@RequestParam MultipartFile file) {
    winningNumberService.importCsv(file);
    return "redirect:/admin";
}
```

### 확인 질문

CSV 파일을 읽고 파싱하는 코드를 Controller가 아니라 Service에 두는 이유는 무엇일까?

### 내 답변

Service는 구현을 목적으로 한 단계이다.

### 정리 답변

Service는 실제 비즈니스 로직을 구현하는 계층입니다.

CSV 파싱, DB 저장, 번호 생성처럼 실제 기능의 핵심 로직은 Service에 작성하는 것이 좋습니다.

### 면접 답변

> Controller는 사용자의 요청을 받고 응답을 반환하는 역할에 집중시키고, CSV 파싱과 저장 같은 실제 비즈니스 로직은 Service 계층에 작성했습니다. 이렇게 역할을 분리하면 코드가 복잡해지는 것을 막고 테스트와 유지보수가 쉬워집니다.

---

## STEP 4. MultipartFile을 문자열로 읽기

### 배운 내용

업로드된 파일은 byte 배열로 읽히므로 문자열로 변환해야 합니다.

```java
String csvText = new String(file.getBytes(), StandardCharsets.UTF_8);
```

### 확인 질문

`new String(file.getBytes(), StandardCharsets.UTF_8)`에서 `UTF_8`을 지정하는 이유는 무엇일까?

### 내 답변

데이터를 byte나 문자로 통신하기 위해서 UTF-8 인코딩을 통해 한글이 깨지는 것을 방지해야 한다.

### 정리 답변

파일은 byte 형태로 읽히기 때문에, byte를 문자열로 바꿀 때 어떤 문자 규칙으로 해석할지 지정해야 합니다.

`UTF-8`을 사용하면 한글이나 특수문자가 깨지는 것을 줄일 수 있습니다.

### 면접 답변

> MultipartFile의 내용은 byte 배열로 읽히기 때문에 문자열로 변환할 때 인코딩을 지정해야 합니다. CSV에 한글 헤더나 한글 데이터가 있을 수 있어서 `UTF-8`을 사용해 문자 깨짐을 방지했습니다.

---

## STEP 5. CSV 문자열을 한 줄씩 읽기

### 배운 내용

CSV 문자열은 `BufferedReader`로 한 줄씩 읽습니다.

```java
try (BufferedReader reader = new BufferedReader(new StringReader(csvText))) {
    String headerLine = reader.readLine();

    String line;
    while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
            continue;
        }
    }
}
```

### 확인 질문

CSV 파일에서 첫 번째 줄을 바로 데이터로 저장하지 않고 따로 읽는 이유는 무엇일까?

### 내 답변

첫 번째 줄은 숫자 데이터가 아닌 문자 데이터이므로 읽기만 하고 실제 숫자 데이터는 두 번째부터 읽는다.

### 정리 답변

CSV의 첫 번째 줄은 보통 컬럼명을 담은 헤더입니다.

예:

```text
drawNumber,drawDate,number1
```

따라서 실제 데이터 저장은 두 번째 줄부터 처리합니다.

### 면접 답변

> CSV의 첫 번째 줄은 컬럼명을 담은 헤더이기 때문에 실제 당첨번호 데이터로 저장하지 않습니다. 숫자 변환이나 DB 저장은 두 번째 줄부터 처리합니다.

---

## STEP 6. CSV 한 줄을 컬럼으로 나누기

### 배운 내용

CSV 한 줄은 쉼표 기준으로 나눌 수 있지만, 따옴표 안의 쉼표는 조심해야 합니다.

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

### 확인 질문

`split(",")`만 사용하면 CSV 값 안에 쉼표가 들어 있을 때 어떤 문제가 생길까?

### 내 답변

`"1,000"`인 경우 `"1`, `000"` 이렇게 된다.

### 정리 답변

단순히 `split(",")`을 사용하면 따옴표 안에 있는 쉼표도 구분자로 처리되어 값이 잘못 쪼개질 수 있습니다.

### 면접 답변

> 단순히 `split(",")`을 사용하면 `"1,000"`처럼 따옴표 안에 쉼표가 포함된 값도 잘못 분리될 수 있습니다. 그래서 따옴표 내부 여부를 체크해서 따옴표 밖의 쉼표만 구분자로 처리했습니다.

---

## STEP 7. CSV 컬럼을 회차, 날짜, 번호로 변환하기

### 배운 내용

CSV에서 읽은 값은 모두 문자열이므로 숫자와 날짜 타입으로 변환해야 합니다.

```java
int drawNumber = Integer.parseInt(columns.get(0));
LocalDate drawDate = LocalDate.parse(columns.get(1));
Integer number = Integer.parseInt(columns.get(2));
```

### 확인 질문

CSV에서 읽은 `"10"`이라는 값은 왜 바로 숫자처럼 사용할 수 없고 `Integer.parseInt()`로 변환해야 할까?

### 내 답변

`"10"`은 우리 눈으로 봤을 때는 숫자이지만 컴퓨터는 문자 String으로 인식하므로 int로 변환하기 위해서는 `Integer.parseInt()`를 사용해야 한다.

### 정리 답변

CSV에서 읽은 값은 모두 문자열입니다.

```text
"10" = 문자열
10 = 숫자
```

합계 계산이나 범위 검사를 하려면 숫자 타입으로 변환해야 합니다.

### 면접 답변

> CSV에서 읽은 값은 모두 문자열이기 때문에 `"10"`처럼 숫자로 보이는 값도 Java에서는 `String` 타입입니다. 합계 계산이나 범위 검사를 하려면 숫자 타입이 필요하므로 `Integer.parseInt()`를 사용해 변환했습니다.

---

## STEP 8. Entity 만들고 DB 테이블 구조 잡기

### 배운 내용

DB에 저장할 데이터를 Entity로 만듭니다.

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

### 확인 질문

로또 당첨번호 Entity에서 `drawNumber`, 즉 회차 번호를 `@Id`로 사용하기 좋은 이유는 무엇일까?

### 내 답변

회차 번호는 중복이 되지 않기에 `@Id` 기본키로 사용하기 적합하다.

### 정리 답변

로또 회차 번호는 각 회차를 고유하게 식별할 수 있고 중복되지 않습니다.

따라서 별도의 자동 증가 ID를 만들지 않고 `drawNumber`를 기본키로 사용할 수 있습니다.

### 면접 답변

> 로또 회차 번호는 각 회차를 고유하게 식별할 수 있고 중복되지 않기 때문에 Entity의 기본키로 사용하기 적합합니다. 그래서 별도의 자동 증가 ID를 만들지 않고 `drawNumber`를 `@Id`로 지정했습니다.

---

## STEP 9. Repository 만들기

### 배운 내용

Repository는 DB 저장과 조회를 담당합니다.

```java
public interface WinningDrawRepository
        extends JpaRepository<WinningDrawEntity, Integer> {
}
```

### 확인 질문

`JpaRepository<WinningDrawEntity, Integer>`에서 `Integer`를 쓰는 이유는 무엇일까?

### 내 답변

기본키로 설정한 `drawNumber` 타입이 `Integer`이기 때문이다.

### 정리 답변

`JpaRepository`의 두 번째 제네릭 타입은 Entity의 기본키 타입입니다.

`drawNumber`가 `Integer`이므로 Repository도 `Integer`로 지정합니다.

### 면접 답변

> `JpaRepository`의 두 번째 제네릭 타입은 Entity의 기본키 타입입니다. 당첨번호 Entity에서는 회차 번호인 `drawNumber`를 `@Id`로 사용했고, 그 타입이 `Integer`이기 때문에 Repository도 `Integer`로 지정했습니다.

---

## STEP 10. 파싱한 데이터를 Entity로 변환하고 DB에 저장하기

### 배운 내용

CSV에서 파싱한 데이터를 Entity로 변환하고 DB에 저장합니다.

```java
winningDrawRepository.save(new WinningDrawEntity(
        draw.getDrawNumber(),
        draw.getDrawDate(),
        draw.getNumbers(),
        draw.getBonusNumber()
));
```

### 확인 질문

CSV에서 파싱한 데이터를 바로 버리지 않고 DB에 저장해야 하는 이유는 무엇일까?

### 내 답변

잘 모르겠어.

### 정리 답변

CSV는 데이터를 처음 넣는 통로이고, DB는 계속 조회하고 사용할 저장소입니다.

DB에 저장해두면 다음 기능에서 계속 재사용할 수 있습니다.

```text
과거 당첨번호와 같은 조합 제외
통계 대시보드
최신 회차 조회
관리자 수정
외부 API 업데이트
```

### 다시 확인한 답변

과거 당첨번호를 DB에 저장해두는 게 좋다.

### 면접 답변

> CSV 파일은 초기 당첨번호 데이터를 입력하기 위한 수단이고, 실제 서비스에서는 데이터를 계속 조회하고 활용해야 하기 때문에 DB에 저장했습니다. DB에 저장해두면 번호 생성 시 과거 당첨 조합을 제외할 수 있고, 통계 대시보드나 관리자 수정 기능에서도 같은 데이터를 재사용할 수 있습니다.

---

## STEP 11. 저장된 당첨번호 데이터 조회하기

### 배운 내용

DB에 저장된 Entity를 조회한 뒤 서비스나 화면에서 쓰기 좋은 Model로 변환합니다.

```java
@Transactional(readOnly = true)
public List<WinningDraw> findAll() {
    return winningDrawRepository.findAll().stream()
            .map(this::toModel)
            .toList();
}
```

### 확인 질문

DB에서 조회한 `WinningDrawEntity`를 `WinningDraw` 모델로 변환하는 이유는 무엇일까?

### 내 답변

데이터를 이동하면서 사용하려면 model로 변환해서 사용한다.

### 정리 답변

`WinningDrawEntity`는 DB 테이블과 직접 연결된 객체이고, `WinningDraw` 모델은 서비스나 화면에서 사용하기 좋은 객체입니다.

DB 구조와 서비스 로직을 분리하기 위해 Entity를 Model로 변환합니다.

### 면접 답변

> Entity는 DB와 매핑되는 객체이기 때문에 서비스나 화면에서 그대로 사용하기보다, 필요한 데이터만 담은 모델로 변환했습니다. 이렇게 하면 DB 구조와 비즈니스 로직을 분리할 수 있고, 코드 유지보수도 쉬워집니다.

---

## STEP 12. 1~45 중 서로 다른 6개 번호 생성하기

### 배운 내용

1~45 숫자 풀을 만들고 섞은 뒤 앞에서 6개를 선택하면 중복 없는 번호를 만들 수 있습니다.

```java
List<Integer> pool = IntStream.rangeClosed(1, 45)
        .boxed()
        .collect(Collectors.toCollection(ArrayList::new));

Collections.shuffle(pool, random);
```

### 확인 질문

`random.nextInt(45)`를 6번 반복하는 방식보다, 1~45 리스트를 섞고 앞에서 6개를 뽑는 방식이 더 좋은 이유는 무엇일까?

### 내 답변

중복 여부.

### 정리 답변

`random.nextInt(45)`를 6번 반복하면 같은 숫자가 여러 번 나올 수 있습니다.

1~45 리스트를 섞고 앞에서 6개를 뽑으면 리스트 안에 숫자가 하나씩만 있으므로 중복이 생기지 않습니다.

### 면접 답변

> `random.nextInt()`를 6번 호출하는 방식은 중복 번호가 나올 수 있기 때문에, 1부터 45까지의 번호 풀을 만든 뒤 섞고 앞에서 6개를 선택하는 방식으로 구현했습니다.

---

## STEP 13. 과거 당첨번호와 완전히 같은 조합 제외하기

### 배운 내용

과거 당첨번호를 `Set`으로 만들어두고 새 후보 번호가 포함되어 있는지 검사합니다.

```java
Set<String> historicalWinningKeys = winningNumberService.findAll().stream()
        .map(draw -> draw.getNumbers().toString())
        .collect(Collectors.toSet());
```

```java
if (historicalWinningKeys.contains(key)) {
    continue;
}
```

### 확인 질문

과거 당첨번호를 `List`가 아니라 `Set`으로 만들어두면 어떤 점이 좋을까?

### 내 답변

Set은 데이터 중복을 허용하지 않기 때문에 중복된 데이터를 거를 수 있다.

### 정리 답변

`Set`을 쓰는 이유는 두 가지입니다.

```text
1. 중복된 과거 조합을 하나로 관리할 수 있음
2. 특정 조합이 이미 있는지 contains()로 빠르게 확인할 수 있음
```

### 면접 답변

> 과거 당첨번호 조합을 `Set`으로 저장한 이유는 중복을 제거하고, 새로 생성한 후보 조합이 과거 당첨번호에 포함되어 있는지 `contains()`로 빠르게 확인하기 위해서입니다.

---

## STEP 14. 필터 조건 적용하기

### 배운 내용

조건에 맞지 않는 후보는 `continue`로 제외하고 다음 후보를 검사합니다.

```java
if (!hasOddEvenRatio(numbers, oddCount, evenCount)) {
    continue;
}

if (candidate.getSum() < request.getMinSum()
        || candidate.getSum() > request.getMaxSum()) {
    continue;
}
```

### 확인 질문

조건에 맞지 않는 후보를 만났을 때 `continue`를 사용하는 이유는 무엇일까?

### 내 답변

조건이랑 맞지 않기 때문에 continue로 무시하고 다시 조건을 찾으려고 한다.

### 정리 답변

조건에 맞지 않는 후보는 결과에 추가하면 안 되기 때문에 `continue`로 현재 반복을 건너뛰고, 다음 후보 번호를 다시 생성합니다.

### 면접 답변

> 생성된 후보 번호가 조건을 만족하지 않으면 `continue`로 현재 반복을 건너뛰고 다음 후보를 생성하도록 했습니다. 이렇게 해서 모든 필터를 통과한 조합만 결과 리스트에 추가되도록 했습니다.

---

## STEP 15. 생성 결과를 화면에 반환하기

### 배운 내용

Service에서 생성한 결과를 Controller가 Model에 담아 JSP로 전달합니다.

```java
model.addAttribute("result", result);
```

JSP에서는 아래처럼 접근합니다.

```jsp
${result}
```

### 확인 질문

Controller에서 `model.addAttribute("result", result)`를 해주는 이유는 무엇일까?

### 내 답변

result로 값을 jsp로 넘겨서 사용하기 위함.

### 정리 답변

Service에서 생성한 결과 객체를 JSP 화면에서 사용할 수 있도록 `Model`에 `"result"`라는 이름으로 담아 전달합니다.

### 면접 답변

> Service에서 생성한 결과 객체를 JSP 화면에서 사용할 수 있도록 `Model`에 `"result"`라는 이름으로 담아 전달했습니다. 그래서 JSP에서는 `${result}`로 생성 결과에 접근할 수 있습니다.

---

## STEP 16. 전체 흐름 정리와 면접 답변 만들기

### 배운 내용

전체 흐름은 다음과 같습니다.

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

### 확인 질문

이 프로젝트에서 CSV 파일을 DB에 저장한 뒤 번호 생성 로직에서 다시 DB를 조회하는 가장 큰 이유는 무엇일까?

### 내 답변

DB에 넣은 데이터를 가져와서 번호 생성 로직에서 데이터를 빠르게 가져와서 중복 확인 여부를 확인하려고?

### 정리 답변

CSV에서 읽은 당첨번호를 DB에 저장해두고, 번호 생성 시 DB에서 과거 당첨번호를 조회해서 새로 만든 후보 조합이 과거 1등 조합과 완전히 같은지 확인하기 위해서입니다.

### 면접 답변

> CSV는 초기 입력 수단이고, 실제 번호 생성에서는 DB에 저장된 과거 당첨번호를 조회해 사용합니다. 조회한 당첨번호 조합을 Set으로 변환한 뒤, 새로 생성한 후보 조합이 과거 1등 조합과 완전히 같은지 `contains()`로 검사해 중복 조합을 제외했습니다.

---

## STEP 17. 통계 대시보드 만들기

### 배운 내용

DB에 저장된 당첨번호 데이터를 기준으로 번호별 출현 빈도, 홀짝 비율, 합계 구간을 계산합니다.

```java
Map<Integer, Long> numberCounts = new LinkedHashMap<>();
```

### 확인 질문

번호별 출현 빈도를 계산할 때 `Map<Integer, Long>`을 사용하는 이유는 무엇일까?

### 내 답변

`Map<Integer, Long>`에서 1~45번이 몇 번 들어가는지 알기 위해서 Integer는 각각의 1~45이며 Long은 그 몇 개가 들어갔는지에 대한 숫자이다.

### 정리 답변

```text
Integer = 로또 번호 1~45
Long = 해당 번호가 과거 당첨번호에서 나온 횟수
```

### 면접 답변

> 번호별 출현 빈도를 계산하기 위해 `Map<Integer, Long>`을 사용했습니다. key인 `Integer`는 1부터 45까지의 로또 번호를 의미하고, value인 `Long`은 해당 번호가 전체 당첨번호 데이터에서 몇 번 등장했는지를 의미합니다.

---

## STEP 18. 관리자 감사 로그 만들기

### 배운 내용

관리자 감사 로그는 누가, 언제, 어떤 작업을 했는지 기록하는 기능입니다.

```text
username   = 누가 작업했는지
actionType = 어떤 종류의 작업인지
detail     = 어떤 내용을 처리했는지
createdAt  = 언제 작업했는지
```

### 확인 질문

관리자 감사 로그에서 `username`, `actionType`, `detail`, `createdAt`을 저장하는 이유는 무엇일까?

### 내 답변

작업 이력의 흔적을 추적하기 위함이다.

### 정리 답변

이 네 가지를 저장하면 나중에 문제가 생겼을 때 누가, 언제, 어떤 작업을 했는지 추적할 수 있습니다.

### 면접 답변

> 관리자 감사 로그는 관리자 작업 이력을 추적하기 위한 기능입니다. 누가, 언제, 어떤 작업을 했는지 확인할 수 있도록 username, actionType, detail, createdAt을 저장했습니다. 이를 통해 데이터 변경 이슈가 생겼을 때 원인을 추적할 수 있습니다.

---

## STEP 19. 스케줄러로 외부 API 자동 업데이트하기

### 배운 내용

Spring Scheduler는 서버가 실행 중일 때 정해진 시간에 메서드를 자동 실행합니다.

```java
@Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Seoul")
public void updateLatestDrawsOnSchedule() {
    if (autoUpdateEnabled) {
        updateFromExternalApi();
    }
}
```

### 확인 질문

Spring Scheduler로 만든 자동 업데이트 기능은 왜 서버가 계속 실행 중이어야 동작할까?

### 내 답변

서버가 실행되어야 특정 시간이 되면 작동하기 때문이다.

### 정리 답변

Spring Scheduler는 애플리케이션 프로세스 내부에서 동작합니다. 서버가 꺼져 있으면 스케줄러도 함께 동작하지 않기 때문에 자동 업데이트가 실행되지 않습니다.

### 면접 답변

> Spring Scheduler는 애플리케이션 프로세스 내부에서 동작하기 때문에, 서버가 실행 중일 때만 정해진 시간에 메서드를 실행할 수 있습니다. 서버가 꺼져 있으면 스케줄러도 함께 동작하지 않기 때문에 자동 업데이트가 실행되지 않습니다.

---

## 최종 복습용 답변

> 이 프로젝트는 CSV 업로드를 통해 로또 당첨번호 데이터를 DB에 저장하고, 저장된 과거 당첨번호를 기준으로 조건에 맞는 번호 조합을 생성하는 Spring Boot MVC 프로젝트입니다. Controller는 요청을 받고, Service는 CSV 파싱과 번호 생성 로직을 처리하며, Repository는 JPA를 통해 DB 저장과 조회를 담당합니다. 번호 생성 시에는 1~45 번호 풀을 만들고 제외번호를 제거한 뒤 랜덤으로 6개를 뽑으며, 홀짝 비율, 합계 범위, 하위 빈도 숫자, 마킹 규칙, 과거 당첨번호 중복 여부를 검사해 조건을 통과한 조합만 화면에 반환합니다.
