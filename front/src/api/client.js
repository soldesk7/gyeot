// BFF 공통 호출 래퍼. 모든 API 호출은 이 request()를 거친다 —
// 컴포넌트 안에서 fetch를 직접 쓰지 말 것 (오류 처리가 제각각이 되는 걸 막기 위함).
//
// BFF 오류 계약 (아키텍처 문서 4절): 실패 응답의 몸체는 항상
//   { "error": "<CODE>", "message": "사용자 노출용 문구" }
// 프론트는 반드시 code로 분기한다. message는 그대로 화면에 보여주기만 하고,
// 문자열 내용을 비교해서 로직을 만들지 않는다 (문구는 언제든 바뀔 수 있다).
//
// 사용 예:
//   try {
//     const guide = await fetchGuide('burn')
//   } catch (e) {
//     if (e.code === 'RATE_LIMITED') { /* 수동 선택으로 유도 */ }
//     else { /* e.message를 화면에 표시 */ }
//   }

// Vite는 VITE_ 접두사가 붙은 환경변수만 프론트 코드에 노출한다 (.env 참고).
// ?? 는 "왼쪽이 없으면(null/undefined) 오른쪽을 쓴다"는 뜻 — .env가 없어도 로컬 기본값으로 동작.
const BASE = import.meta.env.VITE_BFF_BASE_URL ?? ''

// Error를 상속해서 code(분기용)·status(HTTP 상태)를 추가로 실어 나른다.
export class ApiError extends Error {
  constructor(code, message, status) {
    super(message) // e.message에 사용자 노출용 문구가 담긴다
    this.code = code
    this.status = status
  }
}

export async function request(path, options) {
  let res
  try {
    res = await fetch(BASE + path, options)
  } catch {
    // fetch 자체가 던지는 경우 = 서버에 닿지도 못함 (BFF 꺼짐, 네트워크 끊김).
    // 이때도 화면이 멈추면 안 되므로 같은 ApiError 모양으로 바꿔서 던진다.
    throw new ApiError('NETWORK', '네트워크 연결을 확인해 주세요.', 0)
  }
  if (!res.ok) {
    // 오류 몸체가 JSON이 아닐 수도 있으므로(프록시 오류 등) 파싱 실패 시 빈 객체로 대체
    const body = await res.json().catch(() => ({}))
    throw new ApiError(body.error ?? 'UNKNOWN', body.message, res.status)
  }
  try {
  return await res.json()
} catch {
  throw new ApiError('INVALID_RESPONSE', '서버 응답을 처리하지 못했어요.', res.status)
}
}
