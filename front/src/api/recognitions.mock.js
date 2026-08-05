// TODO: 하드코딩으로 MOCK_SCENARIO의 임의의 변수 지정보다 시나리오 이름으로 선택할 수 있어야 합니다. 예를 들면 "success", "low-confidence", "unknown", "rate-limited"를 switch 또는 응답 맵으로 관리하는 방식입니다.

import { ApiError } from "./client";

const MOCK_SCENARIO = "success";

export const recognize = (_photo) => {
  switch (MOCK_SCENARIO) {
    /*** 성공 응답(200) 처리, 시간 초과는 및 안전 필터는 lowConfidence: true ***/

    /** 정상: 200 + lowConfidence: false */
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

    /** 범주 미선택: category가 unknown 또는 다른 응답인 경우 */
    case "unknown":
      return Promise.resolve({
        category: "unknown",
        confidence: 0,
        lowConfidence: true,
        visibleSigns: "",
      });

    /*** 오류 목은 ApiError(code, message, status)로 reject 처리 ***/

    /** 요청 제한 */
    case "rate-limited":
      return Promise.reject(
        new ApiError("RATE_LIMITED", "목업: 요청 제한", 429),
      );

    /** 서버 오류 (예: 안전 필터 거부) */
    case "internal-error":
      return Promise.reject(
        new ApiError(
          "INTERNAL_ERROR",
          "서버에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.",
          500,
        ),
      );

    /** 네트워크 오류 */
    case "network":
      return Promise.reject(new ApiError("NETWORK", "목업: 호출 실패", 0));

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
