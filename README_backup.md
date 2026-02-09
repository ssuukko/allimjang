# 📝 스마트 알림장 (Alrimjang Project)

> 교사와 학생, 학부모를 하나로 잇는 디지털 소통 플랫폼

---

## 📌 1. 프로젝트 개요

**목적**: 기존 종이 알림장이나 단체 채팅방의 불편함을 해소하고, 공지 사항 전달 및 확인 여부를 체계적으로 관리합니다.

**주요 타겟**: 학급 운영 효율화를 원하는 교사, 자녀의 공지를 놓치고 싶지 않은 학부모.

**핵심 가치**: 정보 전달의 정확성, 열람 확인의 투명성, 데이터 중심의 학급 관리.

---

## 🛠 2. 기술 스택 (Tech Stack)

| 구분 | 기술 | 상세 |
|------|------|------|
| **Backend** | Java 17 | Spring Boot 3.x |
| **Database** | PostgreSQL | MyBatis (Persistence Framework) |
| **Frontend** | Thymeleaf | Bootstrap 5, jQuery |
| **Security** | Spring Security | Session-based Authentication |
| **Build** | Maven | Project Object Model |

---

## 📁 3. 프로젝트 구조 (Project Structure)

```
src
 ┣ main/java/com/alrimjang
 ┃ ┣ config        # 보안 및 시스템 설정
 ┃ ┣ controller    # HTTP 요청 처리
 ┃ ┣ service       # 비즈니스 로직 및 트랜잭션 관리
 ┃ ┣ mapper        # MyBatis 인터페이스
 ┃ ┣ model         # Entity 및 DTO
 ┃ ┗ AlrimjangApplication.java
 ┣ main/resources
 ┃ ┣ mapper        # MyBatis SQL XML 파일
 ┃ ┣ static        # CSS, JS, Image 등 정적 자원
 ┃ ┣ templates     # Thymeleaf 뷰 템플릿
 ┃ ┗ application.yml
 ┗ .env.example    # 환경변수 설정 템플릿 (보안 가이드)
```

---

## 🚀 4. 설치 및 실행 가이드 (Setup & Installation)

### 4-1. 환경 변수 설정

본 프로젝트는 **보안을 위해 DB 계정 정보를 환경변수로 관리**합니다.

1. 프로젝트 루트의 `.env.example` 파일을 복사하여 `.env` 파일을 생성합니다.
2. `.env` 파일에 본인의 로컬 PostgreSQL 계정 정보를 입력합니다.
   - `DB_USERNAME`: PostgreSQL 사용자 ID
   - `DB_PASSWORD`: PostgreSQL 비밀번호

### 4-2. 실행 방법

```bash
# 리포지토리 클론
git clone https://github.com/ssuukko/alrimjang.git

# 메이븐 빌드 및 실행
./mvnw spring-boot:run
```

---

## ✨ 5. 핵심 기능 (Features)

- **공지 사항 관리**: 교사의 공지 작성, 수정, 삭제 및 파일 첨부 기능.
- **열람 확인 시스템**: 학생/학부모의 공지 확인 여부를 실시간으로 체크.
- **권한별 접근 제어**: 교사(작성/관리)와 학생/학부모(열람)의 명확한 역할 분리.
- **데이터 시각화**: 공지별 열람률 통계 대시보드 제공 (예정).

---

## 🗄️ 6. DB 설계 (ERD)

> 📌 **ERD 다이어그램은 2일차 작업에서 추가될 예정입니다.**

---

## 📝 7. 커밋 컨벤션 (Commit Convention)

| 타입 | 설명 |
|------|------|
| `init` | 초기 설정 |
| `feat` | 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 리팩토링 |
| `docs` | 문서 수정 |

---

## 👨‍💻 8. 개발 환경

- **IDE**: IntelliJ IDEA
- **Database**: PostgreSQL (Local)
- **Version Control**: Git / GitHub

---

## 📄 License

This project is licensed under the MIT License.

---

## 👤 Author

**ssuukko** - [GitHub Profile](https://github.com/ssuukko)
