import {Link} from "react-router-dom";

export default function NonDiagnosticNotice() {
  return (
    <div className="card">
      <p>
        이 서비스의 AI는 직접 진단하지 않습니다. 상황을 추정해 소방청 등 공식 기관의
        응급처치 안내로 연결할 뿐이며, 판단이 서지 않으면 먼저 119입니다.
      </p>
      <Link to="/notice">이용 안내·처리방침</Link>
    </div>
  );
}