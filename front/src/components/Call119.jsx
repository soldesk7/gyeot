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
        <p>직접 전화 앱에서 <a href="tel:119">119</a>로 연결해주세요.</p>
        <h2 id={guideTitleId} style={{ marginTop: 0 }}>
          119 구급신고 이렇게 하세요
        </h2>
        <ol style={{
          listStyle: 'none',
          paddingLeft: 0,
          margin: 0,
        }}>
          <li>1. 먼저 환자가 있다는 것을 알려주세요.</li>
          <li>2. 환자의 위치 알려주기 - 주소를 정확히 알려주세요.</li>
          <li>3. 환자가 아픈 곳을 말하기 - 누가 어떤 이유로 어디가 아픈지, 또 의식과 호흡이 있는지 알려주세요.</li>
          <li>4. 환자의 나이, 지병을 말하기 - 환자의 나이를 말하고, 평소에 앓고 있는 중요한 지병과 먹고 있는 약을 말해주세요.</li>
          <li>5. 신고자의 이름과 예비 연락처 말하기 - 신고 장소가 정확하지 않거나 의료지도 필요시 등 연락하는 경우가 있으니, 예비 연락처를 알려주세요.</li>
          <li>6. 의료지도 받고 응급처치하기 - 구급차는 환자 있는 곳으로 가고 있으니, 전화를 끊지 말고 의료 지도를 받고 침착하게 응급처치를 하면서 구급차를 기다립니다.</li>
        </ol>
        <p style={{fontSize:'12px', color:'#666'}}>출처: <a href="https://119.gg.go.kr/main/cont.do?id=55&menuId=main_006_005" target="_blank" rel="noopener noreferrer">경기도 소방재난본부 「119 신고요령」</a>&nbsp;(공공누리 제1유형)</p>
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
