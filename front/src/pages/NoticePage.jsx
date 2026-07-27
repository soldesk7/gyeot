// 화면 E — 이용 안내·처리 고지·처리방침 (N-05). 담당: F4
//
// 서버 API가 필요 없는 정적 화면이다 — 문구를 이 파일(JSX) 안에 직접 쓰면 된다.
// 처리 고지에는 "이용 목적·무저장·즉시 폐기" 세 가지가 반드시 들어가야 하고,
// 업로드 화면(화면 B)의 고지 문구에서 이 페이지로 링크가 연결된다 (F4 SP2 과제).
import { useState } from "react";

export default function NoticePage() {
  // 사진 처리 고지용 토글 설정
  const [photoOpen, setPhotoOpen] = useState(false);
  const [photoDetailOpen, setPhotoDetailOpen] = useState(false);

  // 개인정보 처리방침용 토글 설정
  const [privacyOpen, setPrivacyOpen] = useState(false);
  const [privacyDetailOpen, setPrivacyDetailOpen] = useState(false);

  // AI 이용 안내용 토글 설정
  const [aiOpen, setAiOpen] = useState(false);
  const [aiDetailOpen, setAiDetailOpen] = useState(false);

  // 위치 정보 안내용 토글 설정
  const [locationOpen, setLocationOpen] = useState(false);
  const [locationDetailOpen, setLocationDetailOpen] = useState(false);

  return (
    <main className="page">
      <h1>이용 안내</h1>

      <p>
        본 서비스는 응급 상황에서 참고할 수 있는 안내를 제공하는 웹서비스입니다. 
        <br />
        AI의 분류 결과는 참고용이며, 사용자의 확인 후 공식 기관의 응급처치 안내를 제공합니다.
        응급 상황에서는 119 신고를 가장 먼저 해주시기 바랍니다.
      </p>
      <h3>
        서비스 이용 전 아래 내용을 반드시 확인해 주세요.
      </h3>


      <section className="card">
        
        <h2 onClick={() => setPhotoOpen(!photoOpen)}>
          사진 처리 고지 {photoOpen ? "▲" : "▼"}
        </h2>

        {photoOpen && (
          <>
            <p>
              업로드한 사진은 응급 상황 분류를 위한 AI 인식 목적으로만 이용됩니다.
              <br />
              사진은 분석 완료 후 즉시 폐기되며,
              별도로 저장하거나 다른 목적으로 이용하지 않습니다.
            </p>

            <b onClick={() => setPhotoDetailOpen(!photoDetailOpen)}>
              자세히 보기 {photoDetailOpen ? "▲" : "▼"}
            </b>

            {photoDetailOpen && (
              <>
            <h3>제1조(목적)</h3>
            <p>
              본 서비스는 사용자가 업로드한 사진을 AI 기반 응급 상황 분류 및
              응급처치 안내 제공을 위한 목적으로만 처리합니다.
            </p>

            <h3>제2조(처리 대상)</h3>
            <p>서비스 이용 과정에서 사용자가 직접 업로드한 사진을 처리합니다.</p>

            <h3>제3조(이용 목적)</h3>
            <p>업로드된 사진은 다음의 목적으로만 이용됩니다.</p>
            <ul>
              <li>AI 기반 응급 상황 분류</li>
              <li>응급처치 안내 제공</li>
              <li>응급 상황 관련 안내 제공</li>
            </ul>

            <h3>제4조(저장 및 폐기)</h3>
            <p>
              업로드된 사진은 AI 분석 완료 후 즉시 삭제되며,
              별도로 저장하거나 다른 목적으로 이용하지 않습니다.
            </p>

            <h3>제5조(동의 거부)</h3>
            <p>
              사용자는 사진 처리에 동의하지 않을 수 있으며,
              동의하지 않을 경우 AI 기반 응급 상황 분류 기능 이용이 제한될 수 있습니다.
            </p>
          </>
          )}
        </>
        )}
      </section>


      <section className="card">
        <h2 onClick={() => setPrivacyOpen(!privacyOpen)}>
          개인정보 처리방침 {privacyOpen ? "▲" : "▼"}
        </h2>

        {privacyOpen && (
          <>
            <p>
              본 서비스는 서비스 제공에 필요한 정보만 처리하며, 사용자가 제공한 사진은 분석 완료 후 저장하지 않습니다.
            </p>

            <b onClick={() => setPrivacyDetailOpen(!privacyDetailOpen)}>
              자세히 보기 {privacyDetailOpen ? "▲" : "▼"}
            </b>

            {privacyDetailOpen && (
              <>
                <h3>제1조(개인정보 처리 목적)</h3>
                <p>
                  본 서비스는 AI 기반 응급 상황 분류 및 응급처치 안내 제공을 위해 필요한 정보를 처리합니다.
                </p>

                <h3>제2조(처리 정보)</h3>
                <p>서비스 이용 과정에서 다음 정보가 처리될 수 있습니다.</p>
                <ul>
                  <li>업로드한 사진</li>
                  <li>위치정보(사용자가 위치 권한에 동의한 경우)</li>
                </ul>
                <p>※ 본 서비스는 회원가입 없이 이용 가능하며 별도의 회원 정보를 수집하지 않습니다.</p>

                <h3>제3조(이용 목적)</h3>
                <p>처리되는 정보는 다음 목적으로만 이용됩니다.</p>
                <ul>
                  <li>응급 상황 분류</li>
                  <li>응급처치 정보 제공</li>
                  <li>서비스 제공 및 기능 운영</li>
                </ul>

                <h3>제4조(보관 및 삭제)</h3>
                <p>업로드된 사진은 AI 분석 완료 후 즉시 삭제하며 별도로 저장하지 않습니다.</p>

                <h3>제5조(정보 제공)</h3>
                <p>사용자의 정보는 서비스 제공 목적 외에는 이용하지 않으며, 관련 법령에 따른 경우를 제외하고 제3자에게 제공하지 않습니다.</p>

                <h3>제6조(동의 거부)</h3>
                <p>사용자는 개인정보 및 위치정보 제공에 대한 동의를 거부할 수 있습니다. 다만 동의하지 않을 경우 일부 서비스 이용이 제한될 수 있습니다.</p>
              </>
            )}
          </>
        )}
      </section>

      <section className="card">
        <h2  onClick={() => setAiOpen(!aiOpen)}>
          AI 이용 안내 {aiOpen ? "▲" : "▼"}
        </h2>

        {aiOpen && (
          <>
            <p>
              AI 분석 결과는 참고용 정보이며,
              전문 의료진의 판단을 대신하지 않습니다.
              <br />
              응급 상황에서는 119 신고 및 의료기관의 안내를 우선해 주시기 바랍니다.
            </p>

            <b onClick={() => setAiDetailOpen(!aiDetailOpen)}>
              자세히 보기 {aiDetailOpen ? "▲" : "▼"}
            </b>

          {aiDetailOpen && (
            <>
              <h3>제1조(AI 서비스)</h3>
              <p>
                AI를 이용하여 응급 상황을 분류하고 응급처치 정보를 제공합니다.
              </p>

              <h3>제2조(AI 결과의 한계)</h3>
              <p>
                AI 분석 결과는 참고용이며 전문 의료진의 판단을 대신하지 않습니다.
              </p>

              <h3>제3조(사용자 확인)</h3>
              <p>
                사용자는 AI 분석 결과를 참고하여 상황에 맞는 적절한 대응을 해야 합니다.
              </p>

              <h3>제4조(응급 상황)</h3>
              <p>
                응급 상황에서는 AI 결과보다 119 신고 및 의료기관의 도움을
                우선해야 합니다.
              </p>
              </>
            )}
          </>
        )}
      </section>

      <section className="card">
        <h2  onClick={() => setLocationOpen(!locationOpen)}>
          위치정보 이용 안내 {locationOpen ? "▲" : "▼"}
        </h2>

        {locationOpen && (
          <>
            <p>
              위치 권한에 동의한 경우 현재 위치정보를 이용하여 주변 응급의료기관 및 응급실 정보를 제공합니다.
            </p>

            <b onClick={() => setLocationDetailOpen(!locationDetailOpen)}>
              자세히 보기 {locationDetailOpen ? "▲" : "▼"}
            </b>

            {locationDetailOpen && (
              <>
                <h3>제1조(목적)</h3>
                <p>
                  주변 응급의료기관 및 응급실 안내를 위해 위치정보를 이용합니다.
                </p>

                <h3>제2조(처리하는 정보)</h3>
                <p>현재 위치정보(GPS, Wi-Fi 등)</p>

                <h3>제3조(보관 및 삭제)</h3>
                <p>위치정보는 안내 목적으로만 사용되며 별도로 저장하지 않습니다.</p>

                <h3>제4조 동의 거부</h3>
                <p>
                  사용자는 위치정보 제공에 동의하지 않을 수 있으며,
                  동의하지 않을 경우 주변 응급실 안내 기능 이용이 제한될 수 있습니다.
                </p>
              </>
            )}
          </>
        )}
      </section>
    </main>
  )
}
