# Game Community

## 1. 프로젝트 소개

Spring Framework 기말과제로 제작한 게임 커뮤니티 웹 서비스입니다.

사용자는 게임별 공간에서 게시글과 댓글을 작성하고,
다른 사용자와 게임 관련 정보를 공유할 수 있습니다.

회원뿐만 아니라 비회원도 커뮤니티에 참여할 수 있도록
구현하였습니다.

## 2. 개발 목적

- Spring Boot 기반 웹 애플리케이션 구현
- Spring Data JPA를 활용한 데이터베이스 연동
- Spring Security 기반 회원 인증 및 접근 권한 관리
- MVC 구조를 활용한 웹 서비스 개발
- 게시글 및 댓글 CRUD 기능 구현

## 3. 개발 환경

| 구분 | 사용 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.2.0 |
| Security | Spring Security |
| ORM | Spring Data JPA, Hibernate |
| Database | MySQL |
| Frontend | HTML, CSS, Thymeleaf |
| Build Tool | Gradle |
| IDE | IntelliJ IDEA |
| Version Control | Git, GitHub |

## 4. 주요 기능

### 회원 관리
- 회원가입
- 로그인 및 로그아웃
- 회원 정보 조회
- 중복 아이디 및 닉네임 검사
- BCrypt 비밀번호 해시 저장

### 게임별 공간
- 종합 게임 공간
- 오버워치 공간
- 리그 오브 레전드 공간
- 발로란트 공간
- 스팀 게임 공간
- 자유 공간

### 게시글 관리
- 회원 및 비회원 게시글 작성
- 게시글 목록 및 상세 조회
- 게시글 수정 및 삭제
- 작성자 권한 검사
- 비회원 게시글 비밀번호 인증

### 댓글 관리
- 회원 및 비회원 댓글 작성
- 댓글 목록 조회
- 댓글 삭제
- 댓글 작성자 권한 검사

### 커뮤니티 편의 기능
- 게시글 제목 검색
- 게시글 조회수 증가
- 게시글 추천
- 동일 세션 내 중복 추천 방지
- 인기 게시글 및 최신 게시글 조회
- 페이지 분할

### 사용자 인터페이스
- 빨간색과 흰색을 활용한 통일된 디자인
- 모바일 반응형 화면
- 400, 403, 404 오류 안내 페이지
- 브라우저 상세 오류 정보 노출 방지

## 5. 시스템 구조

Browser
-> Spring MVC Controller
-> Service
-> JPA Repository
-> MySQL Database

Controller는 사용자 요청을 처리하고,
Service는 비즈니스 로직을 수행합니다.

Repository는 JPA를 활용하여 데이터베이스에 접근하며,
Thymeleaf를 통해 처리 결과를 화면에 출력합니다.

## 6. 데이터베이스 구성

| 테이블 | 설명 |
|---|---|
| members | 회원 정보 |
| spaces | 게임별 공간 정보 |
| posts | 게시글 정보 |
| comments | 댓글 정보 |

회원, 게임 공간, 게시글, 댓글 사이의 관계는
외래키를 활용하여 구성하였습니다.

## 7. 프로젝트 구조

```text
gamecommunity/
├── src/
│   ├── main/
│   │   ├── java/com/example/gamecommunity/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── static/css/
│   │       ├── templates/
│   │       └── application.properties
│   └── test/
├── gradle/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

## 8. 실행 방법

### 8-1. 사전 준비

- Java 21 설치
- MySQL 설치 및 실행
- IntelliJ IDEA 설치 또는 Gradle Wrapper 사용

### 8-2. 프로젝트 다운로드

```bash
git clone https://github.com/jeaghojun/SpringFrameworkTermProject.git
cd SpringFrameworkTermProject
```

### 8-3. 데이터베이스 설정

MySQL에서 gamecommunity 데이터베이스를 생성합니다.

```sql
CREATE DATABASE gamecommunity
CHARACTER SET utf8mb4;

CREATE USER 'gamecommunity_user'@'localhost'
IDENTIFIED BY 'YOUR_DB_PASSWORD';

GRANT ALL PRIVILEGES ON gamecommunity.*
TO 'gamecommunity_user'@'localhost';
```

YOUR_DB_PASSWORD는 사용자가 지정한 비밀번호로 변경합니다.

이미 데이터베이스와 계정이 존재한다면
기존 설정을 확인하여 사용합니다.

### 8-4. 환경변수 설정

데이터베이스 비밀번호는 소스 코드에 직접 저장하지 않고
DB_PASSWORD 환경변수를 사용합니다.

IntelliJ 실행 설정에서 DB_PASSWORD 환경변수를 지정하거나,
Windows PowerShell에서 다음과 같이 설정합니다.

```powershell
$env:DB_PASSWORD="YOUR_DB_PASSWORD"
```

### 8-5. 프로젝트 실행

Windows PowerShell 기준:

```powershell
.\gradlew.bat bootRun
```

또는 IntelliJ IDEA에서
GamecommunityApplication.java를 실행합니다.

### 8-6. 홈페이지 접속

http://localhost:8080

## 9. 개발 결과

Spring Boot와 Spring Data JPA를 활용하여
회원 인증, 게시글 관리, 댓글 관리 및 검색 기능을 포함한
게임 커뮤니티 웹 서비스를 구현하였습니다.

## 10. 참고 자료

- Spring Boot: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Thymeleaf: https://www.thymeleaf.org/
- MySQL: https://dev.mysql.com/doc/