// 화면 A — 홈. 담당: F3
//
// ★ 이 파일이 공용 패턴의 원본이다. 새 화면은 이 파일을 복사해서 시작한다.
//   패턴 = <main className="page"> 레이아웃 + .action 큰 버튼 + .card 고대비 카드.
//   이 세 가지 밖의 새 스타일이 필요하면 만들기 전에 T에게 먼저 물어본다.
//
// 홈의 핵심 규칙 (N-04): 119 신고·사진 업로드·증상 선택, 세 행동이
// 똑같은 크기로, 첫 화면에서 스크롤 없이 한 번의 탭으로 눌려야 한다.

import { Link } from 'react-router-dom'
import Call119 from '../components/Call119.jsx'
import NonDiagnosticNotice from '../components/NonDiagnosticNotice.jsx'

export default function HomePage() {
  return (
    <main className="page">
      <h1>곁 - 골든타임 응급 도우미</h1>
      <p>골든타임을 지키는 응급 도우미. 세 가지 행동은 언제나 여기서 시작합니다.</p>

      {/* 전화는 <a href="tel:">, 앱 안의 화면 이동은 <Link to="">를 쓴다.
          <Link>는 새로고침 없이 화면만 바꿔 줘서 빠르다 — 내부 이동에 <a>를 쓰지 말 것 */}
      <Call119
        className="action action--danger"
        mobileLabel="📞 119 신고"
        pcButtonStyle={{ cursor: 'pointer' }}
      />
      <Link className="action" to="/result">📷 사진으로 상황 확인</Link>
      <Link className="action" to="/guide">📋 증상 직접 선택</Link>
      <Link className="action" to="/map">🏥 응급실 지도</Link>

      {/* 비진단 고지 — AI가 진단하는 서비스로 보이면 안 된다 (불변 원칙 1) */}
      <NonDiagnosticNotice />
    </main>
  )
}
