import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useId,
  useRef,
  useState,
} from 'react'

/** User-Agent가 모바일 기기인지 확인 */
const isMobile =
  /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)

const OpenReportGuideContext = createContext(null)

function ReportGuide({ dialogRef, onClose }) {
  const guideTitleId = useId()

  useEffect(() => {
    dialogRef.current?.focus()
  }, [dialogRef])

  const handleKeyDown = (event) => {
    if (event.key === 'Escape') {
      event.preventDefault()
      onClose()
      return
    }

    if (event.key !== 'Tab') return

    const focusableElements = dialogRef.current?.querySelectorAll(
      'a[href], button:not([disabled]), [tabindex]:not([tabindex="-1"])',
    )
    if (!focusableElements?.length) {
      event.preventDefault()
      return
    }

    const firstElement = focusableElements[0]
    const lastElement = focusableElements[focusableElements.length - 1]

    if (
      event.shiftKey
      && (event.target === firstElement || event.target === dialogRef.current)
    ) {
      event.preventDefault()
      lastElement.focus()
    } else if (!event.shiftKey && event.target === lastElement) {
      event.preventDefault()
      firstElement.focus()
    }
  }

  return (
    <div
      className="call119-report-guide"
      role="presentation"
      onClick={onClose}
    >
      <section
        ref={dialogRef}
        className="call119-report-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby={guideTitleId}
        tabIndex="-1"
        onKeyDown={handleKeyDown}
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
          onClick={onClose}
          style={{
            width: 'fit-content',
            margin: '0 auto',
            padding: '16px 32px',
          }}
        >
          확인
        </button>
      </section>
    </div>
  )
}

export function Call119Provider({ children }) {
  const [isReportGuideOpen, setIsReportGuideOpen] = useState(false)
  const isReportGuideOpenRef = useRef(false)
  const dialogRef = useRef(null)
  const triggerRef = useRef(null)

  const openReportGuide = useCallback((trigger) => {
    if (isReportGuideOpenRef.current) {
      dialogRef.current?.focus()
      return
    }

    triggerRef.current = trigger
    isReportGuideOpenRef.current = true
    setIsReportGuideOpen(true)
  }, [])

  const closeReportGuide = useCallback(() => {
    isReportGuideOpenRef.current = false
    setIsReportGuideOpen(false)
  }, [])

  useEffect(() => {
    if (isReportGuideOpen || !triggerRef.current) return

    triggerRef.current.focus()
    triggerRef.current = null
  }, [isReportGuideOpen])

  return (
    <OpenReportGuideContext.Provider value={openReportGuide}>
      <div className="app-shell">
        {children}
        {isReportGuideOpen && (
          <ReportGuide dialogRef={dialogRef} onClose={closeReportGuide} />
        )}
      </div>
    </OpenReportGuideContext.Provider>
  )
}

export default function Call119({
  className,
  mobileLabel,
  pcLabel = mobileLabel,
  pcButtonStyle,
}) {
  const openReportGuide = useContext(OpenReportGuideContext)

  /** 모바일이면 전화 앱을 열고, PC이면 119 신고 방법을 안내한다. */
  if (isMobile) {
    return (
      <a className={className} href="tel:119">
        {mobileLabel}
      </a>
    )
  }

  if (!openReportGuide) {
    throw new Error('Call119 must be used within Call119Provider')
  }

  return (
    <button
      type="button"
      className={className}
      onClick={(event) => openReportGuide(event.currentTarget)}
      style={pcButtonStyle}
    >
      {pcLabel}
    </button>
  )
}
