# Lotto Pattern Generator 코드 설명서

이 문서는 프로젝트 코드를 처음 읽는 사람을 위해 작성한 설명서입니다.

목표는 두 가지입니다.

1. 내가 이 프로젝트의 구조를 이해할 수 있게 한다.
2. 면접에서 "이 기능은 어떻게 동작하나요?"라는 질문을 받았을 때 대답할 수 있게 한다.

## 전체 구조

이 프로젝트는 Spring Boot MVC 기반 웹 애플리케이션입니다.

```text
브라우저
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
H2 DB 또는 AWS RDS MySQL
```

| 계층 | 역할 |
| --- | --- |
| Controller | URL 요청을 받고, 화면에 필요한 데이터를 Model에 담아 JSP로 넘김 |
| Service | 실제 비즈니스 로직 처리 |
| Repository | DB 접근 담당 |
| Domain | DB 테이블과 매핑되는 JPA Entity |
| Model | 화면 요청/응답 전달용 객체 |
| JSP | 사용자에게 보여지는 화면 |

## 애플리케이션 시작 흐름

시작 파일은 `LottoPatternGeneratorApplication.java`입니다.

`@SpringBootApplication`은 Spring Boot 앱을 실행한다는 뜻이고, `@EnableScheduling`은 `@Scheduled`가 붙은 메서드를 정해진 시간에 자동 실행할 수 있게 합니다.

```text
1. Spring Boot 실행
2. Spring Bean 등록
3. SecurityConfig 적용
4. UserAccountService에서 기본 admin 계정 생성
5. WinningNumberService에서 DB가 비어 있으면 CSV 초기 적재
6. 웹 요청 대기
```

## 로그인/회원가입 흐름

관련 파일:

```text
AuthController.java
UserAccountService.java
DatabaseUserDetailsService.java
SecurityConfig.java
login.jsp
register.jsp
```

회원가입 흐름:

```text
사용자 /register 접속
  ↓
register.jsp 표시
  ↓
사용자 ID/PW 입력 후 POST /register
  ↓
AuthController.register(POST)
  ↓
UserAccountService.register()
  ↓
아이디 길이, 비밀번호 길이, 비밀번호 확인, 중복 아이디 검증
  ↓
BCrypt로 비밀번호 암호화
  ↓
USER 권한으로 DB 저장
  ↓
/login?registered 로 이동
```

로그인 검증은 Controller가 직접 처리하지 않고 Spring Security가 처리합니다.

```text
사용자 POST /login
  ↓
Spring Security가 username/password 검증
  ↓
DatabaseUserDetailsService가 DB에서 사용자 조회
  ↓
비밀번호 해시 비교
  ↓
성공 시 /generate 이동
```

면접 답변 예시:

> 로그인은 Spring Security의 formLogin을 사용했습니다. 사용자 조회는 UserDetailsService 구현체가 담당하며, 비밀번호는 BCrypt 해시로 비교합니다.

## 권한 분리 흐름

관련 파일:

```text
SecurityConfig.java
UserRole.java
UserAccount.java
```

권한은 `ADMIN`, `USER`로 나뉩니다.

```text
PUBLIC:
  /login
  /register
  /error
  /css/**

ADMIN:
  /admin
  /admin/**
  /h2-console/**

USER 또는 ADMIN:
  그 외 대부분의 서비스 화면
```

면접 답변 예시:

> Spring Security에서 URL별 접근 권한을 설정했습니다. 관리자 기능은 ADMIN 권한만 접근할 수 있고, 일반 사용자는 번호 생성과 통계 화면을 사용할 수 있습니다.

## 사용자 메인 화면 흐름

관련 파일:

```text
HomeController.java
index.jsp
LottoGenerationService.java
GenerationHistoryService.java
RulePresetService.java
WinningNumberService.java
```

사용자 메인 화면은 `/generate`입니다.

```text
번호 생성
내 프리셋 목록
프리셋 저장/삭제
최근 생성 이력
관리자 버튼 조건부 노출
당첨번호 저장 개수 요약
```

번호 생성 흐름:

```text
사용자가 조건 입력
  ↓
POST /generate
  ↓
HomeController.generate()
  ↓
LottoGenerationService.generate()
  ↓
조건 검증
  ↓
랜덤 후보 생성
  ↓
홀짝 비율 검사
  ↓
허용 패턴 검사
  ↓
합계 범위 검사
  ↓
저빈도 번호 과다 포함 여부 검사
  ↓
마커 규칙 검사
  ↓
과거 당첨번호와 동일한 조합인지 검사
  ↓
GenerationResult 반환
  ↓
GenerationHistoryService.save()
  ↓
index.jsp에 결과 표시
```

번호 생성 규칙:

| 조건 | 설명 |
| --- | --- |
| 3:3 | 홀수 3개, 짝수 3개 |
| 4:2 | 홀수 4개, 짝수 2개 |
| 패턴 | O/E 문자열로 허용된 패턴만 통과 |
| 합계 | 사용자가 입력한 최소/최대 합계 사이 |
| 제외수 | 사용자가 입력한 번호는 후보에서 제외 |
| 과거 당첨 제외 | DB에 저장된 당첨번호 조합과 같으면 제외 |
| 중복 후보 제외 | 같은 후보가 한 번 더 나오면 제외 |

면접 답변 예시:

> 번호 생성은 단순 랜덤이 아니라 후보를 만든 뒤 여러 필터를 통과시키는 방식입니다. 사용자가 입력한 제외수와 합계 조건을 반영하고, DB에 저장된 과거 당첨번호와 동일한 조합은 제외합니다.

## 프리셋 흐름

관련 파일:

```text
RulePresetService.java
RulePreset.java
RulePresetRepository.java
index.jsp
```

프리셋 저장:

```text
사용자가 생성 조건 입력
  ↓
프리셋 이름 입력
  ↓
POST /presets
  ↓
RulePresetService.save()
  ↓
username과 함께 DB 저장
```

프리셋 적용:

```text
내 프리셋 클릭
  ↓
GET /presets/{id}/apply
  ↓
RulePresetService.findMine()
  ↓
본인 프리셋인지 확인
  ↓
GenerationRequest로 변환
  ↓
LottoGenerationService.generate()
  ↓
오른쪽 영역에 번호 표시
```

중요한 점은 프리셋 조회/삭제 시 username을 확인한다는 것입니다. 다른 사용자의 프리셋을 id만 알고 접근하는 것을 막기 위한 구조입니다.

## 최근 생성 이력 흐름

관련 파일:

```text
GenerationHistoryService.java
GenerationHistory.java
GenerationHistoryRepository.java
```

```text
번호 생성 성공
  ↓
GenerationHistoryService.save()
  ↓
요청 조건 요약 저장
  ↓
생성 결과 텍스트 저장
  ↓
trimOldHistories()
  ↓
최근 3개 초과 이력 삭제
```

면접 답변 예시:

> 생성 이력은 사용자별로 최근 3개만 유지합니다. 새 이력을 저장한 후 최신순으로 조회해서 4번째 이후 데이터는 삭제하도록 구현했습니다.

## 관리자 화면 흐름

관련 파일:

```text
HomeController.java
admin.jsp
WinningNumberService.java
AdminAuditLogService.java
```

관리자 화면은 `/admin`입니다.

```text
CSV 일괄 업로드
외부 API 최신 회차 업데이트
당첨번호 수동 저장
최근 저장 회차 확인
관리자 감사 로그 확인
```

CSV 업로드:

```text
관리자가 CSV 선택
  ↓
POST /admin/winning-numbers/upload
  ↓
WinningNumberService.importCsv()
  ↓
importCsvText()
  ↓
CSV 한 줄씩 파싱
  ↓
WinningDrawEntity로 변환
  ↓
DB 저장
  ↓
AdminAuditLogService.record()
```

외부 API 업데이트:

```text
관리자가 최신 회차 가져오기 클릭
  ↓
WinningNumberService.updateFromExternalApi()
  ↓
DB의 최신 회차 조회
  ↓
최신 회차 + 1부터 API 호출
  ↓
success면 저장
  ↓
fail이면 아직 없는 회차이므로 중단
```

면접 답변 예시:

> 관리자 기능은 단순 저장뿐 아니라 누가 어떤 작업을 했는지 감사 로그를 남깁니다. CSV 업로드, 수동 저장, 외부 API 업데이트 같은 관리자 작업은 AdminAuditLog에 기록됩니다.

## 스케줄러 흐름

관련 파일:

```text
LottoPatternGeneratorApplication.java
WinningNumberService.java
application.yml
```

```java
@Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Seoul")
```

뜻:

```text
매주 월요일 오전 9시 0분 0초
한국 시간 기준
```

```text
Spring Boot 서버 실행 중
  ↓
월요일 오전 9시 도달
  ↓
updateLatestDrawsOnSchedule()
  ↓
app.lotto.auto-update.enabled 값 확인
  ↓
true면 updateFromExternalApi() 실행
```

주의할 점은 서버가 꺼져 있으면 스케줄러도 실행되지 않는다는 것입니다. AWS나 Docker로 운영 배포해서 앱이 계속 켜져 있어야 자동 업데이트가 의미 있습니다.

## 통계 화면 흐름

관련 파일:

```text
StatsService.java
StatsSummary.java
NumberStat.java
stats.jsp
```

```text
GET /stats
  ↓
HomeController.stats()
  ↓
StatsService.summarize()
  ↓
저장된 전체 당첨번호 조회
  ↓
번호별 출현 빈도 계산
  ↓
홀짝 비율 계산
  ↓
합계 구간 계산
  ↓
StatsSummary 반환
  ↓
stats.jsp에서 차트 형태로 표시
```

면접 답변 예시:

> 통계 화면은 DB에 저장된 당첨번호 전체를 기준으로 번호별 빈도, 홀짝 비율, 합계 구간을 계산합니다. 계산 결과는 StatsSummary DTO에 담아 JSP에서 시각화합니다.

## 오류 처리 흐름

관련 파일:

```text
GlobalExceptionHandler.java
ErrorPageController.java
error.jsp
SecurityConfig.java
```

```text
잘못된 요청 또는 예외 발생
  ↓
GlobalExceptionHandler가 예외 종류별로 처리
  ↓
status/title/message를 Model에 담음
  ↓
error.jsp 표시
```

403 접근 거부:

```text
USER가 /admin 접근
  ↓
Spring Security 접근 거부
  ↓
/error-page?status=403 이동
  ↓
error.jsp 표시
```

면접 답변 예시:

> ControllerAdvice를 사용해 예외를 한 곳에서 처리하도록 만들었습니다. 검증 실패는 400, 권한 없음은 403, 없는 페이지는 404, 예상하지 못한 오류는 500으로 나누어 공통 오류 화면에 표시합니다.

## DB와 CSV 흐름

관련 파일:

```text
application-local.yml
application-prod.yml
WinningNumberService.java
WinningDrawEntity.java
WinningDrawRepository.java
data/winning-numbers.csv
```

로컬에서는 H2 파일 DB를 사용합니다.

```text
data/lotto-db.mv.db
```

초기 CSV:

```text
data/winning-numbers.csv
```

앱 시작 시:

```text
DB에 당첨번호가 있는지 확인
  ↓
있으면 CSV를 읽지 않음
  ↓
없고 CSV가 있으면 CSV를 읽어 DB에 저장
```

중요한 점:

```text
번호 생성/통계는 CSV 파일을 매번 읽지 않는다.
CSV는 초기 적재 또는 관리자 업로드에만 사용한다.
실제 조회 기준은 DB다.
```

## Local / Prod 설정 분리

관련 파일:

```text
application.yml
application-local.yml
application-prod.yml
docs/AWS_DEPLOYMENT.md
```

```text
application.yml
  공통 설정
  기본 프로필 local

application-local.yml
  H2 DB
  H2 Console 활성화

application-prod.yml
  MySQL/RDS
  H2 Console 비활성화
  secure cookie 설정
```

운영 환경변수:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

면접 답변 예시:

> 로컬과 운영 설정을 Spring Profile로 분리했습니다. 로컬에서는 H2를 사용하고, 운영에서는 환경변수로 RDS MySQL 접속 정보를 주입받도록 구성했습니다.

## JSP 화면별 역할

| JSP | 역할 |
| --- | --- |
| `login.jsp` | 로그인 화면 |
| `register.jsp` | 회원가입 화면 |
| `index.jsp` | 사용자 번호 생성 메인 화면 |
| `admin.jsp` | 관리자 당첨번호 관리 화면 |
| `stats.jsp` | 통계 대시보드 화면 |
| `error.jsp` | 공통 오류 화면 |

CSS는 JSP에 직접 넣지 않고 아래 파일로 분리했습니다.

```text
src/main/resources/static/css/app.css
```

## 프로젝트 소개 답변

면접에서 프로젝트 소개를 요청받으면 이렇게 말할 수 있습니다.

> 로또 당첨번호 데이터를 기반으로 조건에 맞는 후보 번호를 생성하는 Spring Boot MVC 프로젝트입니다. 단순 랜덤 생성이 아니라 과거 당첨번호 제외, 홀짝 패턴, 합계 범위, 제외수, 프리셋 저장, 생성 이력, 통계 대시보드, 관리자 당첨번호 관리 기능을 구현했습니다. 인증과 권한은 Spring Security로 처리했고, 로컬은 H2 DB, 운영은 RDS MySQL을 사용할 수 있도록 Spring Profile로 설정을 분리했습니다.

## 자주 받을 수 있는 질문

### Q. 왜 Controller와 Service를 나눴나요?

Controller는 요청과 응답을 담당하고, Service는 실제 비즈니스 로직을 담당하게 하기 위해서입니다. 이렇게 나누면 화면 요청 처리와 핵심 로직이 분리되어 테스트와 유지보수가 쉬워집니다.

### Q. 왜 CSV를 매번 읽지 않고 DB에 저장하나요?

CSV를 매번 읽으면 조회, 통계, 중복 검사 기능을 확장하기 어렵습니다. DB에 저장하면 JPA Repository로 조회할 수 있고, 관리자 수동 저장이나 외부 API 업데이트와도 같은 저장 구조를 사용할 수 있습니다.

### Q. 왜 운영에서는 H2가 아니라 RDS를 쓰나요?

H2는 로컬 개발과 테스트에는 편하지만 운영 DB로는 적합하지 않습니다. AWS 운영 환경에서는 안정성, 백업, 접근 제어, 확장성을 위해 RDS MySQL 같은 관리형 DB를 사용하는 것이 좋습니다.

### Q. 이 프로젝트에서 Redis는 왜 아직 안 썼나요?

현재 기능 규모에서는 DB 조회만으로 충분합니다. Redis는 통계 결과 캐싱, 세션 공유, 조회 성능 개선이 필요해지는 시점에 도입하는 것이 적절합니다.

### Q. Docker는 왜 나중에 적용할 수 있나요?

Docker는 애플리케이션 실행 환경을 이미지로 고정해 로컬과 운영의 차이를 줄여줍니다. 현재는 EC2에서 직접 실행할 수 있고, 다음 단계에서 Dockerfile을 추가해 컨테이너 기반 배포로 개선할 수 있습니다.
