# madi (마디)

시술과 시술 사이, 회복의 마디마디를 기록하는 시술 후 회복 관리 앱의 백엔드 서버입니다.

시술 예약부터 D-day 기반 회복 기록, AI 피드백, 날씨·일정 기반 오늘의 케어 브리핑, 카카오 알림톡까지 한 곳에서 관리합니다.

- 배포: https://madi.hufsglobalikelion.co.kr
- 프론트엔드: https://madi-eta.vercel.app
- Swagger: https://madi.hufsglobalikelion.co.kr/swagger-ui/index.html

14th LikeLion 5조 해커톤 프로젝트입니다.

## 기술 스택

- **언어/프레임워크**: Java 21, Spring Boot 4.1.0, Gradle
- **DB**: MySQL 8.0, Spring Data JPA (Hibernate `ddl-auto: update`)
- **인증**: Spring Security OAuth2 Client (카카오/구글 소셜 로그인), JWT (jjwt)
- **API 문서**: springdoc-openapi (Swagger UI)
- **외부 연동**: OpenAI API(AI 피드백), OpenWeatherMap API(날씨), Google Calendar API, 카카오톡 메시지 API(나에게 보내기), AWS S3(사진 업로드)
- **배포**: Docker / Docker Compose, GitHub Actions → EC2 (SSH 배포), Caddy 리버스 프록시

## 주요 기능

| 영역 | 설명 |
|---|---|
| 인증 | 카카오/구글 소셜 로그인, JWT 발급/재발급, 로그아웃, 회원탈퇴 |
| 케어카드 | 시술 등록 및 카드별 상세 조회 |
| 회복 기록 | D-day별 기록 작성, 사진/상태 태그 첨부 |
| AI 피드백 | 기록 기반 AI 분석 및 케어 가이드 (1일 3회 제한) |
| 오늘의 케어 | 날씨/일정 기반 체크리스트 및 브리핑 자동 생성 |
| 일정 | 시술 일정 관리, 구글 캘린더 연동 |
| 카카오 알림 | 알림 동의/연결 관리, 정기 알림톡 발송 |
| 마이페이지 | 회원 정보 수정, 위치 갱신 |
| 시술/매장 정보 | 시술 카탈로그 및 매장 정보 제공 |

### 카카오 알림 발송 스케줄 (`Asia/Seoul` 기준)

- **09:00 / 19:00** — 오늘의 케어 브리핑 + 체크리스트
- **21:00** — 회복 기록 리마인더 (D+3, D+7)
- **21:00** — 날씨/일정 기반 위험 경고

## 로컬 실행

### 요구 사항

- JDK 21
- MySQL 8.0

### 설정

`src/main/resources/application.yaml`은 git에 포함되지 않는 로컬 전용 설정 파일입니다. 아래 값들을 채워 직접 생성해야 합니다.

- DB 접속 정보 (`spring.datasource.*`)
- 카카오/구글 OAuth2 클라이언트 ID·시크릿 (`spring.security.oauth2.client.registration.*`)
- JWT 시크릿 및 만료 시간
- OpenAI API 키
- OpenWeatherMap API 키
- (선택) AWS S3 자격 증명 — 미설정 시 `S3AutoConfiguration` 제외 가능

### 실행

```bash
./gradlew bootRun
```

## Docker로 실행 (배포와 동일한 방식)

`application.yaml` 없이도 동작하도록, 컨테이너는 `docker-compose.yml`의 환경 변수만으로 전체 설정을 구성합니다 (Spring relaxed binding — 환경 변수는 언더스코어만 사용).

```bash
docker compose up -d --build
```

필요한 `.env` 값: `DB_PASSWORD`, `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `JWT_SECRET`, `OPENWEATHER_API_KEY`, `OPENAI_API_KEY`, `FRONTEND_BASE_URL`(선택), `FRONTEND_REDIRECT_URIS`(선택)

## 배포

`main` 브랜치에 push되면 GitHub Actions(`.github/workflows/deploy.yml`)가 EC2에 SSH로 접속해 최신 코드를 받고 `docker compose up -d --build`로 재배포합니다. `develop` 브랜치 push는 자동 배포되지 않습니다.

## 주요 도메인

`User`, `SocialAccount`, `CareCard`, `CareCardTreatment`, `CareRecord`, `CareRecordPhoto`, `CareRecordTag`, `AiFeedback`, `CareChecklist`, `CareJudgement`, `BriefingCache`, `ChecklistGenerationContext`, `TodayCareMessage`, `Schedule`, `GoogleCalendarConnection`, `KakaoNotification`, `Treatment`, `AacStore`, `Weather`
