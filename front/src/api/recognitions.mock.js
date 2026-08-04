/** Issue #51: 응답 종류별 목을 둔다 — 정상 / 저확신 / unknown / 422 / 504 / 429
 *
 * recognitions.js와 비슷한 이름과 달리는 관련도 없는 목업 파일이므로,
 * 실제 recognitions.js를 import하지 않고, request만 import한다.
 */

// TODO: 주석을 매번 해제하는 구조보다는 시나리오 이름으로 선택할 수 있어야 합니다. 예를 들면 "success", "low-confidence", "unknown", "blocked", "timeout", "rate-limited"를 switch 또는 응답 맵으로 관리하는 방식입니다.

import { ApiError } from "./client";

const MOCK_SCENARIO = "success";

export const recognize = (photo) => {
  switch (MOCK_SCENARIO) {
    /*** 성공 응답(200) ***/
    /** 정상 */
    case "success":
      return Promise.resolve({
        category: "burn",
        confidence: 0.9,
        lowConfidence: false,
        visibleSigns: "목업: 사진에서 붉은 피부가 관찰됩니다.",
      });

    /** 저확신: confidence < 0.6 */
    case "low-confidence":
      return Promise.resolve({
        category: "bleeding",
        confidence: 0.4,
        lowConfidence: true,
        visibleSigns: "목업: 사진에서 붉은 흔적이 관찰됩니다.",
      });

    /** unknown: category가 unknown 또는 다른 응답인 경우 */
    case "unknown":
      return Promise.resolve({
        category: "unknown",
        confidence: 0,
        lowConfidence: true,
        visibleSigns: "",
      });

    /*** 오류 목은 ApiError(code, message, status)로 reject ***/

    case "blocked":
      return Promise.reject(
        new ApiError(
          "RECOGNITION_BLOCKED",
          "목업: 사진을 인식할 수 없어요. 증상을 직접 선택해 주세요.",
          422,
        ),
      );

    case "timeout":
      return Promise.reject(
        new ApiError(
          "TIMEOUT",
          "목업: 서버가 응답하지 않아요. 잠시 후 다시 시도해 주세요.",
          504,
        ),
      );

    case "rate-limited":
      return Promise.reject(
        new ApiError(
          "RATE_LIMITED",
          "목업: 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.",
          429,
        ),
      );

    default:
      return Promise.reject(
        new ApiError(
          "UNKNOWN_MOCK_SCENARIO",
          "지원하지 않는 목 시나리오입니다.",
          0,
        ),
      );
  }
};
