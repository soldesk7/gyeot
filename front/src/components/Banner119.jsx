import Call119 from './Call119.jsx'

// F-17 — 119 우선 배너. 담당: F3
//
// 규칙 (N-01 — 어기면 안 됨):
//   - 모든 페이지 최상단에 항상 떠 있다. App.jsx가 라우팅 바깥에서 그려 주므로
//     페이지 쪽에서는 아무것도 안 해도 된다 — 가리거나 조건부로 숨기지만 않으면 된다.
//   - 서버·API 상태와 무관하게 즉시 떠야 하므로, 이 컴포넌트에는
//     fetch·로딩 상태 같은 걸 절대 넣지 않는다. 정적인 링크 하나만으로 처리한다.
//
// 모바일·PC 분기와 119 연결은 Call119가 담당한다.

export default function Banner119() {
  return (
    <Call119
      className="banner119"
      mobileLabel="🚨 응급 상황이라면 지금 119에 신고하세요"
      pcLabel="🚨 응급 상황이라면 직접 119에 신고하세요"
      pcButtonStyle={{
        width: '100%',
        border: 0,
        fontFamily: 'inherit',
        fontSize: 'inherit',
        cursor: 'pointer',
      }}
    />
  )
}
