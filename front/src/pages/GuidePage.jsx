// 화면 C — 응급처치 가이드 뷰어 (F-04). 담당: F2
//
// /guide/:category 하나만 이 파일이 맡는다 (App.jsx 참고).
// /guide(카테고리 없음) 목록 화면은 F4가 별도 이슈에서 만든다 — 여기서는
// category가 없을 때 빈 화면만 나지 않게 최소 안내만 둔다.
//
// 앞으로 이 화면이 지켜야 할 것:
//   - 콘텐츠는 BFF 콘텐츠 API(api/guides.js)로만 받는다 — 화면에 글을 직접 써넣지 않는다 (불변 원칙 2)
//   - 모든 섹션에 실제 출처를 표기한다. 출처 없는 섹션은 보여주지 않는다
//   - API가 실패하면 오류 안내 + 119 배너 강조로 대응한다 (N-08)
//   - 화면 B(인식 결과)를 거쳐 오든 홈에서 바로 오든 같은 화면을 재사용한다
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchGuide } from '../api/guides'
import './GuidePage.css'
import NonDiagnosticNotice from '../components/NonDiagnosticNotice.jsx'

// <video src>는 mp4 같은 파일만 재생할 수 있다 — 유튜브 시청 페이지 URL은
// 재생이 안 되고 검은 화면만 뜨므로, embed 주소로 바꿔서 <iframe>으로 그린다.
function toYoutubeEmbedUrl(url) {
  try {
    const u = new URL(url)
    if (u.hostname === 'youtu.be') {
      return `https://www.youtube.com/embed${u.pathname}`
    }
    if (u.hostname.endsWith('youtube.com') && u.searchParams.get('v')) {
      return `https://www.youtube.com/embed/${u.searchParams.get('v')}`
    }
  } catch {
    // url이 아니면 무시하고 아래에서 일반 video로 처리
  }
  return null
}

export default function GuidePage() {
  const { category } = useParams()
  const [status, setStatus] = useState('loading') // 'loading' | 'ready' | 'error'
  const [guide, setGuide] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!category) return
    let cancelled = false
    setStatus('loading')
    fetchGuide(category)
      .then((data) => {
        if (cancelled) return
        setGuide(data)
        setStatus('ready')
      })
      .catch((e) => {
        if (cancelled) return
        setError(e)
        setStatus('error')
      })
    return () => {
      cancelled = true
    }
  }, [category])

  if (!category) {
    return (
      <main className="page">
        <p>증상을 선택하면 처치 가이드를 볼 수 있어요.</p>
        <Link className="action" to="/">홈으로</Link>
      </main>
    )
  }

  const visibleSections = status === 'ready' ? guide.sections.filter((s) => s.source) : []

  return (
    <main className="page">
      <div className="guide-header">
        <h1>{status === 'ready' ? guide.title : '처치 가이드'}</h1>
        <Link className="guide-back" to="/guide">다른 증상</Link>
      </div>
      
      <NonDiagnosticNotice />

      {status === 'loading' && <p className="card">불러오는 중이에요…</p>}

      {status === 'error' && (
        <div className="card">
          <p>{error?.message || '가이드를 불러오지 못했어요.'}</p>
          <p>급한 상황이면 위 119 배너로 바로 신고할 수 있어요.</p>
        </div>
      )}

      {status === 'ready' && visibleSections.length === 0 && (
        <p className="card">표시할 수 있는 처치 정보가 아직 없어요. 급한 상황이면 위 119 배너로 신고해 주세요.</p>
      )}

      {status === 'ready' && visibleSections.length > 0 && (
        <ol className="guide-sections">
          {visibleSections.map((section, i) => (
            <li key={i} className="guide-section card">
              <h2>{section.title}</h2>
              <ol className="guide-steps">
                {section.steps.map((step, j) => (
                  <li key={j} className="guide-step">
                    <span className="guide-step__number">{j + 1}단계</span>
                    <p>{step}</p>
                  </li>
                ))}
              </ol>
              {section.media?.map((m, k) => {
                if (m.type !== 'video') {
                  return <img key={k} className="guide-img" src={m.url} alt={m.alt ?? ''} />
                }
                const embedUrl = toYoutubeEmbedUrl(m.url)
                return embedUrl ? (
                  <iframe
                    key={k}
                    className="guide-video"
                    src={embedUrl}
                    title={m.alt ?? section.title}
                    allowFullScreen
                  />
                ) : (
                  <video key={k} className="guide-video" src={m.url} controls aria-label={m.alt} />
                )
              })}
              <p className="guide-source">
                출처:{' '}
                <a href={section.source.url} target="_blank" rel="noreferrer">
                  {section.source.name}
                </a>
                {section.source.license && ` (${section.source.license})`}
              </p>
            </li>
          ))}
        </ol>
      )}
    </main>
  )
}
