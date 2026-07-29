// 화면 D — 응급실 지도 (F-05). 담당: F1
//
// 데이터 경로가 둘로 나뉜다는 점만 기억하면 된다 (아키텍처 문서 1절):
//   - 지도 그리기·마커: 카카오맵 JS SDK를 프론트에서 직접 호출 (키는 VITE_KAKAO_MAP_KEY,
//     도메인 제한으로 보호되는 키라서 유일하게 프론트에 둬도 된다)
//   - 응급실 목록·병상 수: 반드시 BFF 경유 (api/에 hospitals.js를 guides.js 본떠 추가)
//
// 앞으로 이 화면이 지켜야 할 것:
//   - 위치 권한을 거부당하면 주소 검색으로 대체한다 — 화면이 멈추면 안 된다 (N-08)
//   - 병상 수 옆에는 항상 "OO시 OO분 기준"(응답의 asOf)을 함께 표기한다
//   - 데이터 조회 실패 시 E-Gen(정부 공식 응급의료정보 서비스) 링크를 안내한다
import { useEffect, useRef, useState } from 'react'
import { fetchHospitals } from '../api/hospitals'

const KAKAO_KEY = import.meta.env.VITE_KAKAO_MAP_KEY

// autoload=false + kakao.maps.load(): 스크립트 로드와 지도 SDK 초기화 시점을 분리해서
// React 마운트 이후에 안전하게 지도를 그릴 수 있게 한다.
function loadKakaoMaps() {
  return new Promise((resolve, reject) => {
    if (window.kakao?.maps) {
      resolve(window.kakao)
      return
    }
    const script = document.createElement('script')
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_KEY}&autoload=false`
    script.onload = () => window.kakao.maps.load(() => resolve(window.kakao))
    script.onerror = () => reject(new Error('카카오맵 스크립트 로드 실패'))
    document.head.appendChild(script)
  })
}

// 서울시청 — 위치 권한을 거부/실패했을 때 쓰는 기본 좌표
const FALLBACK_CENTER = { lat: 37.566826, lng: 126.9786567 }

// 위치 권한이 거부되거나 브라우저가 지원하지 않아도 reject 대신 fallback 좌표로 resolve한다 —
// 지도 자체는 항상 뜨게 하기 위함(N-08, 화면이 멈추면 안 됨).
function getCurrentPosition() {
  return new Promise((resolve) => {
    if (!navigator.geolocation) {
      resolve({ ...FALLBACK_CENTER, isFallback: true })
      return
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude, isFallback: false }),
      (err) => {
        // code: 1=PERMISSION_DENIED, 2=POSITION_UNAVAILABLE, 3=TIMEOUT
        console.error('geolocation 실패', err.code, err.message)
        resolve({ ...FALLBACK_CENTER, isFallback: true })
      },
      { timeout: 10000, enableHighAccuracy: false },
    )
  })
}

export default function HospitalMapPage() {
  const mapContainerRef = useRef(null)
  const [status, setStatus] = useState('loading') // 'loading' | 'ready' | 'error'
  const [usedFallback, setUsedFallback] = useState(false)

  useEffect(() => {
    let cancelled = false
    Promise.all([loadKakaoMaps(), getCurrentPosition()])
      .then(([kakao, position]) => {
        if (cancelled || !mapContainerRef.current) return
        const center = new kakao.maps.LatLng(position.lat, position.lng)
        const map = new kakao.maps.Map(mapContainerRef.current, { center, level: 3 })
        new kakao.maps.Marker({ position: center, map })
        //--------응급실 마커 찍기 시작--------
        return fetchHospitals(position.lat, position.lng).then((data) => {
          if (cancelled) return
          data.items.forEach((hospital) => {
            new kakao.maps.Marker({
              position: new kakao.maps.LatLng(hospital.lat, hospital.lng),
              map,
            })
          })
          setUsedFallback(position.isFallback)
          setStatus('ready')
        })
        //--------응급실 마커 찍기 끝--------
      })
      .catch(() => {
        if (!cancelled) setStatus('error')
      })
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <main className="page">
      <h1>주변 응급실</h1>
      {status === 'error' && (
        <p>지도를 불러오지 못했습니다. VITE_KAKAO_MAP_KEY와 카카오 개발자 콘솔의 도메인 등록을 확인하세요.</p>
      )}
      {status === 'ready' && usedFallback && (
        <p>현재 위치를 확인할 수 없어 기본 위치를 표시했습니다.</p>
      )}
      <div ref={mapContainerRef} style={{ width: '100%', height: '400px' }} />
    </main>
  )
}
