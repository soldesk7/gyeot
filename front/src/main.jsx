// 앱의 진입점. index.html의 <div id="root">에 React 앱을 그려 넣는다.
// 이 파일은 건드릴 일이 거의 없다 — 화면 추가는 App.jsx에서 한다.
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css' // 전역 스타일 — 모든 화면에 적용된다
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  // StrictMode: 개발 중에만 잘못된 패턴을 경고해 주는 안전장치 (배포 빌드에는 영향 없음)
  <StrictMode>
    {/* BrowserRouter: 주소(URL)에 따라 다른 화면을 보여주는 기능의 뿌리. App 전체를 감싼다 */}
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
