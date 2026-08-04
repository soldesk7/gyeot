// 앱의 뼈대. 하는 일은 세 가지:
//   1. 119 배너를 모든 화면 위에 항상 띄운다
//   2. 주소(URL)별로 어떤 페이지를 보여줄지 정한다 (라우팅)
//   3. 페이지에서 오류가 나도 앱 전체가 죽지 않게 막는다 (오류 경계)
//
// 새 화면을 추가할 때 이 파일에서 할 일은 <Routes> 안에 <Route> 한 줄 추가뿐이다.
import { Component } from 'react'
import { Routes, Route } from 'react-router-dom'
import Banner119 from './components/Banner119.jsx'
import { Call119Provider } from './components/Call119.jsx'
import HomePage from './pages/HomePage.jsx'
import ResultPage from './pages/ResultPage.jsx'
import GuidePage from './pages/GuidePage.jsx'
import HospitalMapPage from './pages/HospitalMapPage.jsx'
import NoticePage from './pages/NoticePage.jsx'
import CategoryPicker from './components/CategoryPicker.jsx'

// 오류 경계(Error Boundary): 자식 컴포넌트가 렌더링 중 오류를 던지면
// 앱 전체가 하얀 화면이 되는 대신, 여기서 잡아서 안내 화면을 대신 보여준다.
// React가 클래스 문법만 지원하는 유일한 기능이라 이 파일만 class로 작성돼 있다 — 따라 만들 일은 없다.
//
// 중요: 이 경계는 <Routes>(페이지 영역)만 감싼다. 119 배너는 이 바깥에 있으므로
// 어떤 페이지가 죽어도 배너는 계속 떠 있다 (N-01 — 119 경로 무차단).
class PageErrorBoundary extends Component {
  state = { hasError: false }
  static getDerivedStateFromError() {
    return { hasError: true }
  }
  render() {
    if (this.state.hasError) {
      return (
        <main className="page">
          <p>화면을 불러오지 못했어요. 위의 119 배너는 계속 사용할 수 있습니다.</p>
          {/* <a href>로 이동하면 페이지가 새로고침되면서 오류 상태도 초기화된다 (일부러 Link를 안 씀) */}
          <a className="action" href="/">처음으로 돌아가기</a>
        </main>
      )
    }
    return this.props.children
  }
}

export default function App() {
  return (
    <Call119Provider>
      {/* 배너는 라우팅 바깥 = 어느 주소로 들어와도, 페이지가 오류여도 항상 그려진다 */}
      <Banner119 />
      <div className="app-content">
        <PageErrorBoundary>
          {/* 현재 주소와 path가 일치하는 <Route>의 element 하나만 화면에 그려진다 */}
          <Routes>
            <Route path="/" element={<HomePage />} />                      {/* 화면 A — 홈 */}
            <Route path="/result" element={<ResultPage />} />              {/* 화면 B — 사진 인식 */}
            <Route path="/guide" element={<CategoryPicker />} />                {/* 화면 C — 증상 선택 */}
            <Route path="/guide/:category" element={<GuidePage />} />      {/* 화면 C — 개별 가이드 (:category는 변수) */}
            <Route path="/map" element={<HospitalMapPage />} />            {/* 화면 D — 응급실 지도 */}
            <Route path="/notice" element={<NoticePage />} />              {/* 화면 E — 이용 안내 */}
          </Routes>
        </PageErrorBoundary>
      </div>
    </Call119Provider>
  )
}
