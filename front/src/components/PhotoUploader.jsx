// F-09 — 사진 업로드·대기 화면 (F2 담당)
//
// 이슈 #22: 처리 고지는 F4의 실제 팝업(이슈 #24)이 머지되기 전이라 자리표시자로 대체.
// 인식 API는 F1의 api/recognitions.js가 준비되기 전이라 setTimeout으로 흉내만 낸다.
import { useState } from 'react'
import { Link } from 'react-router-dom'


export default function PhotoUploader() {
const [agreed, setAgreed] = useState(false)
    const [photo, setPhoto] = useState(null)

     const handleSelectPhoto = (e) => {
    const file = e.target.files[0]
    setPhoto(file)
    // TODO(F1): api/recognitions.js 준비되면 여기서 실제 API 호출로 교체
    setTimeout(() => {
      // 지금은 PhotoResult가 비어있어서 완료 후 처리할 로직 없음
    }, 2000)
  }

  if (!agreed) {
    return (
        <>
      <div className="card">
        {/* TODO(F4): 이슈 #24 머지되면 실제 고지 팝업 컴포넌트로 교체 */}
        <p>[자리표시자] 이용 목적: 응급 상황 판단 지원</p>
        <p>[자리표시자] 사진은 저장하지 않고 인식 즉시 폐기됩니다</p>
        <button className="action" onClick={() => setAgreed(true)}>동의</button>
      </div>
       <Link className="action" to="/guide">📋 증상 직접 선택</Link>
      </>
    )
  }

  if (photo) {
    return (
      <>
        <p>인식 중입니다...</p>
        <Link className="action" to="/guide">📋 증상 직접 선택</Link>
      </>
    )
  }

  return (
    <>
      <label className="action" htmlFor="photo-input">📷 사진 선택</label>
      <input
        id="photo-input"
        type="file"
        accept="image/*"
        onChange={handleSelectPhoto}
        style={{ display: 'none' }}
      />
      <Link className="action" to="/guide">📋 증상 직접 선택</Link>
    </>
  )
}

