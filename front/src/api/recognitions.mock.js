/** Issue #51: 응답 종류별 목을 둔다 — 정상 / 저확신 / unknown / 422 / 504 / 429
 *
 * recognitions.js와 비슷한 이름과 달리는 관련도 없는 목업 파일이므로,
 * 실제 recognitions.js를 import하지 않고, request만 import한다.
 */

import { request } from "./client";

export const recognize = (photo) => {
  const formData = new FormData();
  formData.append("photo", photo);

  /** 응답 200 목은 { category, confidence, visibleSigns, lowConfidence } */

  // 정상
  return Promise.resolve({
    category: "burn",
    confidence: 0.9,
    visibleSigns: "목업: 사진에서 관찰된 특징",
    lowConfidence: false,
  });

  // 저확신
  // return Promise.resolve({
  //   category: "bleeding",
  //   confidence: 0.4,
  //   visibleSigns: "목업: 사진에서 관찰된 특징",
  //   lowConfidence: true,
  // });

  /** TODO: unknown이 404인지, 200이지만 category가 unknown인지, BFF와 협의 필요 */
  // unknown
  // return Promise.resolve({
  //   result: "unknown",
  //   message: "목업: 알 수 없는 상황이에요.",
  //   status: 200,
  // });

  /** 오류 목은 (code, message, status) 반환 */
  // 422
  // return Promise.reject({
  //   code: "INVALID_IMAGE",
  //   message: "목업: 이미지 파일이 올바르지 않아요.",
  //   status: 422,
  // });

  // 504
  // return Promise.reject({
  //   code: "TIMEOUT",
  //   message: "목업: 서버가 응답하지 않아요. 잠시 후 다시 시도해 주세요.",
  //   status: 504,
  // });

  // 429
  // return Promise.reject({
  //   code: "RATE_LIMITED",
  //   message: "목업: 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.",
  //   status: 429,
  // });

  return request("/api/v1/recognitions", {
    method: "POST",
    body: formData,
  });
};
