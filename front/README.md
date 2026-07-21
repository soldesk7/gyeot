# gyeot-front

React(Vite) 웹 MVP. 실행: `.env.example`을 `.env`로 복사 → `npm install` → `npm run dev`.

## 구조

```
src/
├── pages/       화면 1개 = 파일 1개. 파일 상단 주석에 화면 코드·담당자 명시
├── components/  여러 화면이 같이 쓰는 것만 (지금은 Banner119 하나)
├── api/         BFF 호출 래퍼. client.js가 공통, 리소스별 파일은 guides.js를 본떠 추가
└── App.jsx      라우팅 + 119 배너 + 오류 경계
```

## 새 화면 만드는 법 (공용 패턴)

1. `pages/HomePage.jsx`를 복사해서 시작한다 — `<main className="page">` + `.action` 큰 버튼 + `.card` 카드. 이 3개 클래스 밖의 새 스타일이 필요하면 T에게 먼저 물어본다.
2. `App.jsx`의 `<Routes>`에 한 줄 추가한다.
3. BFF 호출은 `api/`의 함수만 쓴다 — 컴포넌트에서 `fetch` 직접 호출 금지. 오류는 `ApiError`의 `code`로 분기한다 (메시지 문자열 비교 금지).

## 지켜야 할 것 (자세한 건 루트 AGENTS.md)

- 119 배너는 App.jsx가 라우팅 바깥에서 항상 그린다 — 페이지에서 가리거나 조건부로 만들지 않는다.
- 어떤 로딩·오류 화면에도 대안 행동(수동 선택 등)을 같은 크기 버튼으로 남긴다. 빈 화면 금지.
- 판정 로직(확신도 기준 등)은 프론트에서 만들지 않는다 — 필요한 값이 응답에 없으면 T에게 이슈로 요청.
- `VITE_` 환경변수는 번들에 노출된다 — 카카오맵 키 외에는 넣지 않는다.
