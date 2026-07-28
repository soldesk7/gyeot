// 처리 고지 팝업 컴포넌트
// - 사진 업로드 전 사용자에게 사진 처리 내용 안내
// - 사용자가 동의하면 부모 컴포넌트에서 전달받은 onAgree 함수 실행
// - 처리 방침 페이지 이동 링크 제공

import { Link } from "react-router-dom";
import "./UploadNotice.css";

export default function UploadNotice({ onAgree }) {
    return  (
        <section className="upload-notice">
            <div className="upload-notice-box">

                <h2>사진 처리 고지</h2>
                <p>
                    업로드한 사진은 응급 상황 분류를 위한 AI 인식 목적으로만 사용됩니다.
                    <br />
                    사진은 분석 완료 후 즉시 폐기되며,
                    별도로 저장하거나 다른 목적으로 이용하지 않습니다.
                </p>

                {/* 처리방침 페이지 이동 링크 (추후 라우팅 설정할 예정)
                    Link to는 a href와 같이 페이지 이동 처리하는 컴포넌트 */}
                <Link to="/notice" className="upload-notice-link">
                    처리방침 전체보기
                </Link>


                <div className="upload-notice-buttons">

                    {/* 부모 컴포넌트에서 전달받은 등의 처리 함수 실행
                        onAgree 함수 존재할 경우에만 실행 */}
                    <button type="button"
                    onClick={() => onAgree?.()}>
                        동의하고 계속
                    </button>

                    <button type="button">
                        취소
                    </button>
                </div>

            </div>
        </section>
    );
}