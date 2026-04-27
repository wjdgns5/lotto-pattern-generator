# AWS Deployment Notes

이 프로젝트는 Spring Profile로 로컬 개발 환경과 AWS 운영 환경을 분리합니다.

## 설정 파일 구조

| 파일 | 용도 |
| --- | --- |
| `src/main/resources/application.yml` | 공통 설정, 기본 프로필 `local` |
| `src/main/resources/application-local.yml` | 로컬 개발용 H2 DB 설정 |
| `src/main/resources/application-prod.yml` | AWS 운영용 MySQL/RDS 설정 |

## Local 실행

기본 프로필은 `local`입니다.

```bash
mvnw.cmd spring-boot:run
```

명시적으로 실행하려면:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

로컬에서는 H2 파일 DB를 사용합니다.

```text
jdbc:h2:file:./data/lotto-db
```

## AWS/prod 실행

prod 프로필은 MySQL 호환 DB를 사용하도록 구성되어 있습니다.

```bash
java -jar lotto-pattern-generator.war --spring.profiles.active=prod
```

필요한 환경변수:

```text
DB_HOST=your-rds-endpoint.amazonaws.com
DB_PORT=3306
DB_NAME=lotto_db
DB_USERNAME=your-db-user
DB_PASSWORD=your-db-password
```

## AWS DB 선택

운영 환경에서는 H2보다 AWS RDS MySQL을 권장합니다.

권장 구성:

```text
EC2 또는 Docker 컨테이너: Spring Boot 애플리케이션 실행
RDS MySQL: 회원, 당첨번호, 프리셋, 생성 이력, 감사 로그 저장
Security Group: 애플리케이션 서버에서만 RDS 3306 포트 접근 허용
```

RDS 생성 시 기본 예시:

```text
Engine: MySQL
DB name: lotto_db
Port: 3306
Public access: No 권장
Character set: utf8mb4 권장
```

운영에서는 DB 계정과 비밀번호를 Git에 저장하지 않고 환경변수나 AWS Parameter Store/Secrets Manager로 관리합니다.
