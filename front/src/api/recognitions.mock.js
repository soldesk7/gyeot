import { ApiError } from "./client";

/**
 * 목으로 화면을 확인할 때 쓰는 파일. 평소에는 어디에서도 import하지 않는다.
 *
 * 확인 방법: PhotoUploader의 recognize import를 이 파일로 잠시 바꾸고 아래 값을 원하는
 * 시나리오로 바꾼다. 확인이 끝나면 둘 다 되돌린다 — 목 import는 커밋하지 않는다.
 */
const MOCK_SCENARIO = "success"; 
export const recognize = (_photo) => {
  switch (MOCK_SCENARIO) {
    /*** 성공 응답(200). 시간 초과·안전 필터 거부도 여기로 오며 lowConfidence가 참이다 ***/

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
        new ApiError(
          "RATE_LIMITED",
          "목업: 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.",
          429,
        ),
      );

    /** 서버 오류 (예: 안전 필터 거부) */
    case "internal-error":
      return Promise.reject(
        new ApiError(
          "INTERNAL_ERROR",
          "목업: 서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
          500,
        ),
      );

    /** 네트워크 오류 */
    case "network":
      return Promise.reject(
        new ApiError(
          "NETWORK",
          "목업: 인터넷에 문제가 발생했습니다. 연결 상태를 확인하고 잠시 후 다시 시도해주세요.",
          0,
        ),
      );

    default:
      return Promise.reject(
        new ApiError(
          "UNKNOWN_MOCK_SCENARIO",
          "목업: 지원하지 않는 목 시나리오입니다.",
          0,
        ),
      );
  }
};
