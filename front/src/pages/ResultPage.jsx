// 화면 B — 사진 업로드·인식 결과 확인 (F-09). 담당: F1(업로드·API 연동)·F3(확인 화면)
//
// 앞으로 이 화면이 지켜야 할 것:
//   - 업로드 전에 처리 고지(무저장·즉시 폐기)를 보여준다 (N-05, 문구는 F4가 작성)
//   - 인식을 기다리는 동안·실패했을 때·확신도가 낮을 때, 어느 경우든
//     수동 선택(/guide)으로 가는 버튼을 같은 크기로 유지한다 — 빈 화면 금지 (N-08)
//   - "맞나요?" 확인 버튼과 "잘 모르겠어요" 버튼도 같은 크기로 (아키텍처 문서 6절)
//   - 확신도 판정은 BFF가 한다 — 프론트에서 0.6 같은 기준값을 만들지 않는다
import PhotoUploader from '../components/PhotoUploader'
import PhotoResult from '../components/PhotoResult'

export default function ResultPage() {
  return (
    <main className="page">
      <h1>사진으로 상황 확인</h1>
      <PhotoUploader />
      <PhotoResult />
    </main>
  )
}
