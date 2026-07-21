# 곁 (Gyeot) — 골든타임 응급 도우미

응급 순간에 일반인이 골든타임을 놓치지 않도록 돕는, 회원가입 없는 웹서비스. 사진을 업로드하면 응급조치 안내를 보여주고, 모든 화면에 119 우선 배너를 띄우며, 가까운 응급실을 지도로 보여준다.

이 레포는 설치·실행법만 다룬다. 기능 명세·팀 운영·스프린트 계획 같은 문서는 팀 노션 워크스페이스에 보존.

## 구조

```
front/   React(Vite) 프론트엔드
back/    Spring Boot BFF
docs/    스프린트 회고·트러블슈팅·ADR (개발 중 누적 기록)
```

## 백엔드 실행 (`back/`)

요구 사항: JDK 25

1. `back/src/main/resources/application-local.properties.example`을 같은 폴더에 `application-local.properties`로 복사한다.
2. 복사한 파일에 실제 Gemini·공공데이터 API 키를 채운다(이 파일은 커밋되지 않는다).
3. 실행:

```
cd back
./mvnw spring-boot:run
```

`http://localhost:8080`에서 실행.

## 프론트엔드 실행 (`front/`)

요구 사항: Node 24

1. `front/.env.example`을 같은 폴더에 `.env`로 복사한다.
2. 복사한 파일에 카카오맵 JS 키를 채운다(이 파일은 커밋되지 않는다).
3. 실행:

```
cd front
npm install
npm run dev
```

`http://localhost:5173`에서 뜬다.

## 배포

이번 범위에서는 별도의 공개 서버 배포를 하지 않는다 — 개발·시연 모두 로컬 실행을 기준으로 한다.
