// F-09 — 사진 업로드·대기 화면 (F2 담당)
//
// 이슈 #22: 처리 고지는 F4의 실제 팝업(이슈 #24)이 머지되기 전이라 자리표시자로 대체.
// 인식 API는 F1의 api/recognitions.js가 준비되기 전이라 setTimeout으로 흉내만 낸다.
import { useState } from 'react'
import { Link } from 'react-router-dom'
import UploadNotice from './UploadNotice'
import { recognize } from '../api/recognitions'

export default function PhotoUploader({ onResult }) {
  const [agreed, setAgreed] = useState(false)
  const [photo, setPhoto] = useState(null)

  const handleSelectPhoto = (e) => {
    const file = e.target.files[0]
    setPhoto(file)
    recognize(file)
      .then((result) => {
        onResult?.(result)
      })
      .catch((err) => {
        console.error('인식 실패:', err)
      })
  }



  if (!agreed) {
    return (
        <>
      <UploadNotice onAgree={() => setAgreed(true)} />

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
