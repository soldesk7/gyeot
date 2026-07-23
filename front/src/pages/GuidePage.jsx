// 화면 C — 증상 수동 선택 + 가이드 뷰어 (F-04). 담당: F2
//
// 주소 두 개가 이 한 파일로 연결돼 있다 (App.jsx 참고):
//   /guide           → 카테고리 선택 목록 (category가 undefined)
//   /guide/화상      → 해당 카테고리의 가이드 (category === "화상")
// useParams()는 주소의 :category 자리에 실제로 들어온 값을 꺼내 주는 함수다.
//
// 앞으로 이 화면이 지켜야 할 것:
//   - 콘텐츠는 BFF 콘텐츠 API(api/guides.js)로만 받는다 — 화면에 글을 직접 써넣지 않는다 (불변 원칙 2)
//   - 모든 콘텐츠에 "출처: 소방청" 같은 실제 출처를 표기한다. 출처 없는 콘텐츠는 보여주지 않는다
//   - API가 실패하면 오류 안내 + 119 배너 강조로 대응한다 (N-08)
//   - 화면 B(인식 결과)를 거쳐 오든 홈에서 바로 오든 같은 화면을 재사용한다
import { Link, useParams } from 'react-router-dom'
import './GuidePage.css'

const CATEGORY_TITLES = {
  burn: '화상',
  bleeding: '출혈·외상',
  unconscious: '의식저하',
}

const CATEGORY_STEPS = {
  burn: [{ text: '흐르는 찬물에 15~20분간 식힌다', videoUrl: '/videos/burn.mp4', imageUrl: '/images/burn.jpg' }, { text: '병원으로 간다.', imageUrl: '/images/burn.jpg'}],
  bleeding: [{},{},{},{}],
  unconscious: [],
}

export default function GuidePage() {
  const { category } = useParams()
  return (
    <main className="page">
      <div className="guide-header">
      <h1>{category ? `${CATEGORY_TITLES[category]} 처치 가이드` : '증상 직접 선택'}</h1>
      {category && <Link className="guide-back" to="/guide">다른 증상</Link>}
      </div>
      {category ? (
  <ol>
  {CATEGORY_STEPS[category].map((step, i) => (
    <li key={i} className="guide-step">
  <span className="guide-step__number">{i + 1}단계</span>
  {step.videoUrl && <video src={step.videoUrl} controls />}
  {step.imageUrl && <img src={step.imageUrl} />}
  {step.text && <p>{step.text}</p>}
  {!step.text && !step.imageUrl && !step.videoUrl && '(내용 준비 중)'}
</li>



  ))}
</ol>
) : (
  <>
    <Link className="action" to="/guide/burn">화상</Link>
    <Link className="action" to="/guide/bleeding">출혈·외상</Link>
    <Link className="action" to="/guide/unconscious">의식저하</Link>
    <br></br>
    <Link className="action" to="/">홈으로</Link>
  </>
)}


    </main>
  )
}
