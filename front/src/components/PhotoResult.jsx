// 화면 B — 인식 결과 확인 부분 (F-09). 담당: F3
//
// TODO(F3): 인식 결과(카테고리·확신도) 확인 화면을 여기에 구현하세요.
// 지금은 F2가 골격만 잡아둔 빈 컴포넌트입니다.

import { Link } from "react-router-dom";

export default function PhotoResult({ result }) {
  const { category, visibleSigns } = result;

  return (
    <>
      <p>{visibleSigns}</p>
      <p>이 상황이 맞나요?</p>

      <Link className="action" to={`/guide/${category}`}>
        이 상황이 맞아요
      </Link>

      <Link className="action" to="/guide">
        잘 모르겠어요 / 다른 상황
      </Link>
    </>
  );
}
