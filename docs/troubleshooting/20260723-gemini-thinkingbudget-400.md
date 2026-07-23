# Gemini 사진 인식 400 invalid argument — thinkingBudget(0) 거부

- 발생일: 2026-07-23 (SP1)
- 관련 기능: F-09 (사진 인식, BFF → Gemini)
- 작성자: 송승준(T)
- 소요: 약 1시간 (진단 격리 반복)

## 증상

`POST /api/v1/recognitions`에 테스트 사진(`spike/_handoff_f5/z_04.jpg`)을 업로드하면, 인식이 항상 실패하고 `RecognitionService`의 폴백(unknown)만 반환됐다. 서버 로그에서 확인된 근본 예외는 다음과 같았다:

```
Caused by: com.google.genai.errors.ClientException: 400 . Request contains an invalid argument.
```

- 파이프라인(controller → service → client → Gemini)은 정상 동작했다 — 예외를 잡아 unknown으로 폴백하는 N-01 설계가 의도대로 작동했으므로, "죽은 화면"은 발생하지 않았다.
- Spring AI가 Google 원본 에러 본문을 `400 . Request contains an invalid argument.`로 뭉개서, 상세 사유가 로그에 드러나지 않았다. 이로 인해 진단에 어려움이 생겼다.

## 원인

`gemini-flash-latest` **별칭(alias)이 스파이크 시점(2026-07-21) 이후 Gemini 3.x 계열로 이동**했고, 3.x 모델은 `thinkingBudget = 0`(thinking 끄기)을 거부한다.

- Gemini 2.5 계열: `thinking_budget`(토큰 예산) 사용 → `0`으로 thinking 비활성화 가능.
- Gemini 3.x 계열: `thinking_level`(의미적 레벨) 사용 → `thinking_budget = 0`은 무효(*"Budget 0 is invalid. This model only works in thinking mode."*) → 400.
- 아키텍처 문서 §5의 "thinkingBudget:0 필수" 메모는 이 별칭 이동으로 낡은(stale) 정보가 됐다.

출처:
- https://help.apiyi.com/en/gemini-api-thinking-budget-level-error-fix-en.html
- https://github.com/cline/cline/issues/7735

### 진단 과정 (변수 하나씩 격리)

에러 본문이 뭉개져 원인을 바로 알 수 없었으므로, "요청에서 스파이크(`vision_spike.py`, raw REST로 이미 성공 검증됨)와 다른 부분"을 하나씩 제거·교체하며 400이 사라지는 지점을 찾는 방식으로 접근했다.

**1단계 — 가설 ① 이미지가 아닌 MIME으로 전송돼 거절당했다**

에러 문구를 처음 봤을 때 `invalid argument`에서 가장 먼저 떠올린 원인이다. curl `-F "photo=@file.jpg"`는 기본적으로 Content-Type을 `application/octet-stream`으로 보낸다(jpg를 자동 인식하지 않음). 그러면 `photo.getContentType()`이 이미지가 아닌 MIME을 반환하고, Gemini는 `inline_data`의 mime_type이 이미지가 아닐 때 invalid argument로 거절한다 - 검증을 위해 curl에 `;type=image/jpeg`를 명시해 올바른 MIME을 강제 전송했으나 여전히 400 응답이 수신되었다. MIME이 원인이라면 타입 명시로 해결됐어야 하므로 이 가설을 배제했다. (실제 브라우저는 올바른 MIME을 자동 첨부하므로 코드 자체는 문제없고, 이건 curl 테스트에서만 생기는 인공적 변수였다.)

**2단계 — 가설 ② 구조화 출력(responseSchema)이 요청을 무효화했다**

이미 성공이 검증된 스파이크와 지금 코드를 비교했을 때 가장 눈에 띄는 차이가 `.entity(Result.class)`였다. 스파이크는 프롬프트로 "JSON 줘"라고 요청하고 텍스트를 직접 파싱한 반면, `.entity()`는 Spring AI가 Gemini 요청에 네이티브 `responseSchema`를 덧붙인다 — 이 스키마가 요청을 무효화하는 게 아닐까 의심했다. 검증을 위해 `.entity(Result.class)`를 `.content()`(구조화 출력 없이 원문 텍스트만 수신)로 교체했으나 그대로 400 응답이 돌아왔다. 스키마 문제가 아님을 확인했다. 이로써 남은 차이는 `.options()`(thinkingBudget 등)로 좁혀졌다.

**3단계 — 옵션으로 범위 확정**

두 가설이 모두 빗나가면서, 남은 차이인 `.options()`가 유력해졌다. `.options()`에는 `thinkingBudget(0)`과 `maxOutputTokens(4000)` 두 인자가 묶여 있었는데, 이 둘 중 무엇이 문제인지 판단하려면 먼저 "옵션 전체가 원인 범위 안에 있는가"부터 확인해야 했다. 그래서 `.options(...)` 호출을 통째로 제거하고, 응답을 `.entity()` 대신 `.content()`로 받아 콘솔에 원문을 출력하고 진단용 `throw`로 중단시키는 형태로 바꿔 요청을 보냈다. 그 결과 400 없이 raw JSON이 정상 출력됐고(그 뒤 심은 `throw`에 걸려 프로그램 중지), 원인이 `.options()` 안에 있음이 확정됐다. 동시에, 앞서 2단계에서 `.content()`가 `thinkingBudget(0)`과 함께일 때는 실패했던 것과 대비되어 구조화 출력은 무관함이 재확인됐다.

**4단계 — thinkingBudget으로 인자 특정**

옵션 안의 두 인자 중 `maxOutputTokens(4000)`은 스파이크에서 동일하게 사용해 이미 성공한 값이므로, 남은 변수인 `thinkingBudget`을 먼저 확인해봤다. 이 시점에 제미나이 신규 모델이 출시됐다는 뉴스가 생각나 관련 자료를 찾아봤다. cline/cline 저장소의 이슈 #7735에서 우리와 똑같이 thinkingBudget이 0이라 특정 모델에서 거부당하는 사례를 확인했고, apiyi 기술 블로그의 Gemini thinking 오류 해설 글에서 "Gemini 2.5는 thinking_budget을, 3.x는 thinking_level을 쓰며 3.x에서 budget 0은 무효"라는 설명을 확인했다. 별칭이 3.x로 이동했다면 정확히 이 현상과 맞물렸다. 옵션을 되살리되 `thinkingBudget` 값만 `0` → `1`(최소 thinking)로 바꿔 다시 요청하니 성공했다. 이로써 400을 유발한 인자가 **`thinkingBudget(0)`** 임이 확정됐고, `1`로 주면 통과함까지 함께 확인됐다.

## 해결

`GeminiClient.recognize()`의 옵션에서 `thinkingBudget(0)` → `thinkingBudget(1)`(최소 thinking) 으로 변경.

```java
.options(GoogleGenAiChatOptions.builder().thinkingBudget(1).maxOutputTokens(4000))
```

변경 후 실제 응답 정상 확인:

```json
{"category":"unconscious","confidence":0.9,"visibleSigns":"복도 바닥에 한 사람이 쓰러져 누워 있는 모습이 관찰됩니다."}
```

(`thinkingLevel(GoogleGenAiThinkingLevel.LOW)`도 대안이나, Spring AI javadoc상 "Gemini 3 Pro 전용"으로 표기돼 Flash에서의 동작이 불확실하여 채택하지 않음.)

## 재발 방지

1. **모델 별칭 대신 버전 핀 고정 검토** — `-latest` 별칭을 쓰면 우리 코드가 그대로여도 인식 동작·정확도가 외부 서비스 사정에 따라 예고 없이 달라질 수 있고, 코드에 변화가 없어 원인 추적이 늦어진다(이번 사례). 특정 모델 버전으로 핀 고정하면 이 변동을 없앨 수 있다.
2. **정확도 재검증(G1 재측정) 필요** — G1 스파이크의 합격 판정은 이동 전 모델 기준이다. `spike/vision_spike.py`도 `thinkingBudget`을 0→1로 맞춰 재실행하여, 현재 별칭이 가리키는 3.x 모델 기준 정확도를 다시 확인해야 한다.
3. **프로덕션 코드와 스파이크 스크립트의 Gemini 설정 동기화** — 둘의 thinking 설정이 어긋나면 스파이크가 "실제로 배포되는 설정"을 검증하지 못한다.
4. **외부 API 에러 본문 확보 수단 마련(차후)** — Spring AI가 뭉갠 Google 원본 에러 메시지를 볼 수 있으면 이런 진단이 훨씬 빨라진다. (SP3 예외 흐름 정교화 시 함께 검토.)
