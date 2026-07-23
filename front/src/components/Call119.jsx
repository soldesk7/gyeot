import { useId, useState } from 'react'

/** User-Agent가 모바일 기기인지 확인 */
const isMobile =
  /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)

export default function Call119({
  className,
  mobileLabel,
  pcLabel = mobileLabel,
  pcButtonStyle,
}) {
  const [isReportGuideOpen, setIsReportGuideOpen] = useState(false)
  const guideTitleId = useId()

  /** 모바일이면 전화 앱을 열고, PC이면 119 신고 방법을 안내한다. */
  if (isMobile) {
    return (
      <a className={className} href="tel:119">
        {mobileLabel}
      </a>
    )
  }

  return (
    <>
      <button
        type="button"
        className={className}
        onClick={() => setIsReportGuideOpen(true)}
        style={pcButtonStyle}
      >
        {pcLabel}
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
            aria-labelledby={guideTitleId}
            onClick={(event) => event.stopPropagation()}
            style={{
              width: 'min(100%, 420px)',
              padding: 24,
              borderRadius: 8,
              background: '#fff',
              textAlign: 'left',
            }}
          >
            <p style={{ marginTop: 0 }}>직접 전화 앱에서 <a href="tel:119">119</a>로 연결해주세요.</p>
            <h2 id={guideTitleId}>119 신고 순서</h2>
            <ol style={{ paddingLeft: 24 }}>
              <li>현재 위치를 먼저 알려주세요.</li>
              <li>무슨 일이 있었는지와 환자 상태를 설명해주세요.</li>
              <li>119 상황실의 질문에 답하고 안내에 따라주세요.</li>
            </ol>
            <button
              type="button"
              className="action"
              onClick={() => setIsReportGuideOpen(false)}
              style={{
                width: 'fit-content',
                margin: '0 auto', 
                padding: '16px 32px'
              }}
            >
              확인
            </button>
          </section>
        </div>
      )}
    </>
  )
}
