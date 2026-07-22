import { useState } from 'react'

// F-17 — 119 우선 배너. 담당: F3
//
// 규칙 (N-01 — 어기면 안 됨):
//   - 모든 페이지 최상단에 항상 떠 있다. App.jsx가 라우팅 바깥에서 그려 주므로
//     페이지 쪽에서는 아무것도 안 해도 된다 — 가리거나 조건부로 숨기지만 않으면 된다.
//   - 서버·API 상태와 무관하게 즉시 떠야 하므로, 이 컴포넌트에는
//     fetch·로딩 상태 같은 걸 절대 넣지 않는다. 정적인 링크 하나만으로 처리한다.
//
// tel:119 — 휴대폰에서 누르면 바로 전화 앱이 열린다.
// PC에서는 동작하지 않으므로 "PC면 문구로 안내" 처리가 필요하다 (F3의 SP1 과제, 가이드 FE 참고).

/**User-Agent가 모바일 기기인지 확인*/
const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);

export default function Banner119() {
  const [isReportGuideOpen, setIsReportGuideOpen] = useState(false)

  /** 유저 에이전트가 모바일이면 119 바로가기 출력, 아니면 문구 안내 처리 */
  if (isMobile) {
    return (
      <a className="banner119" href="tel:119">
        🚨 응급 상황이라면 지금 119에 신고하세요
      </a>
    )
  } else {
    return (
      <div className="banner119">
        <button
          type="button"
          onClick={() => setIsReportGuideOpen(true)}
          style={{
            border: 0,
            padding: 0,
            color: 'inherit',
            background: 'transparent',
            font: 'inherit',
            fontWeight: 'inherit',
            cursor: 'pointer',
          }}
        >
          🚨 응급 상황이라면 직접 119에 신고하세요
        </button>

        {isReportGuideOpen && (
          <div
            role="presentation"
            onClick={() => setIsReportGuideOpen(false)}
            style={{
              position: 'fixed',
              inset: 0,
              zIndex: 10,
              display: 'grid',
              placeItems: 'center',
              padding: 16,
              background: 'rgba(0, 0, 0, 0.55)',
              color: '#1a1a1a',
            }}
          >
            <section
              role="dialog"
              aria-modal="true"
              aria-labelledby="report-guide-title"
              onClick={(event) => event.stopPropagation()}
              style={{
                width: 'min(100%, 420px)',
                padding: 24,
                borderRadius: 8,
                background: '#fff',
                textAlign: 'left',
              }}
            >
              {/* TODO: 직관적인 일러스트 등의 내용 보충 */}
              <h2 id="report-guide-title" style={{ marginTop: 0 }}>119로 직접 전화 앱에서 걸기</h2>
              <ol style={{ paddingLeft: 24 }}>
                <li>현재 위치를 먼저 알려주세요.</li>
                <li>무슨 일이 있었는지와 환자 상태를 설명해주세요.</li>
                <li>119 상황실의 질문에 답하고 안내에 따라주세요.</li>
              </ol>
              <p>이 기기에서는 바로 전화할 수 없으니 전화 앱에서 119로 연결해주세요.</p>
              <button type="button" className="action" onClick={() => setIsReportGuideOpen(false)}>
                확인
              </button>
            </section>
          </div>
        )}
      </div>
    )
  }
}
