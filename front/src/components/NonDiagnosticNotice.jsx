import {Link} from "react-router-dom";

export default function NonDiagnosticNotice() {
  return (
    <div className="card">
      <p>
        이 서비스의 AI는 직접 진단하지 않습니다. 사진을 통해 상황을 추정하고,
        소방청 등 공식 기관의 응급처치 안내로 연결합니다.
        정확한 판단이 어렵다면 먼저 119에 신고해주시길 바랍니다.
      </p>
      <Link to="/notice">이용 안내·처리방침</Link>
    </div>
  );
}