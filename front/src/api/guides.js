// 콘텐츠 API 래퍼 (화면 C에서 사용). 담당: F2
//
// ★ 리소스별 래퍼의 본보기 — recognitions.js(F-09)·hospitals.js(F-05)도
//   이 파일을 본떠 만든다. 엔드포인트당 함수 하나, 몸체는 request() 호출 한 줄이 전부다.
//   오류 처리는 client.js의 request()가 다 해 주므로 여기서는 신경 쓸 게 없다.
import { request } from './client'

// GET /api/v1/guides — 가이드 3종 목록 [{ category, title, summary }, ...]
export const fetchGuides = () => request('/api/v1/guides')

// GET /api/v1/guides/{category} — 개별 가이드 { category, title, sections }
export const fetchGuide = (category) => request(`/api/v1/guides/${category}`)