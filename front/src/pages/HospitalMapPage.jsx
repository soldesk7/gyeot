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
    // 카카오맵 SDK는 주소검색 같은 부가 기능을 쓰려면 libraries라는 옵션을 따로 불러와야함
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_KEY}&autoload=false&libraries=services`

    script.onload = () => window.kakao.maps.load(() => resolve(window.kakao))
    script.onerror = () => reject(new Error('카카오맵 스크립트 로드 실패'))
    document.head.appendChild(script)
  })
}

// 서울시청 — 위치 권한을 거부/실패했을 때 쓰는 기본 좌표
const FALLBACK_CENTER = { lat: 37.566826, lng: 126.9786567 }

//----------------------병상 표시 코드------------------------

function bedStatusText(availableBeds) {
  if (availableBeds === null) {
    return null
  } else if (availableBeds > 0) {
    return `여유 병상 ${availableBeds}개`
  } else if (availableBeds === 0) {
    return '만실'
  } else 
    return `정원 초과 ${Math.abs(availableBeds)}명`
}

//----------------------병상 표시 코드 끝---------------------

//----------------------기준 시각 표기------------------------

function formatAsOf(asOf) {
  const date = new Date(asOf)
  return `${date.getHours()}시 ${date.getMinutes()}분 기준`
}

//----------------------기준 시각 표기 끝---------------------

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

// 좌표 하나를 기준으로 지도 중심을 옮기고 그 주변 응급실 마커를 채워 넣는다.
// 내 위치로 시작할 때도, 주소 검색으로 다시 그릴 때도 이 함수 하나를 재사용한다.
function showHospitalsAround(kakao, map, lat, lng, centerOverlayRef, hospitalMarkersRef) {
//-----------기존 마커 제거----------------
  if (centerOverlayRef.current) {
  centerOverlayRef.current.setMap(null)
  }
  hospitalMarkersRef.current.forEach(({ marker, infowindow }) => {
  marker.setMap(null)
  infowindow.close()
})
//-----------기존 마커 제거 끝----------------
hospitalMarkersRef.current = []

  const center = new kakao.maps.LatLng(lat, lng)
  map.setCenter(center) // ← 새 줄 ①: 지도를 새로 만들지 않고, 기존 지도의 중심만 옮김
  
//-----------현위치 마커 overlay변수에 저장----------------
  const overlay = new kakao.maps.CustomOverlay({
  position: center,
  map,
  content: '<div style="width:20px;height:20px;border-radius:50%;background:#e53935;border:2px solid white;box-shadow:0 0 2px rgba(0,0,0,0.5);"></div>',
})
//-----------현위치 저장한 overlay를 ref에 저장----------------
centerOverlayRef.current = overlay

  return fetchHospitals(lat, lng).then((data) => { // ← 새 줄 ②: 여기서 return
    data.items.forEach((hospital) => {
      const hospitalPosition = new kakao.maps.LatLng(hospital.lat, hospital.lng)
      const marker = new kakao.maps.Marker({ position: hospitalPosition, map })

      const bedText = bedStatusText(hospital.availableBeds)
      const content = `
        <div style="padding:8px 10px; font-size:13px; width:200px; white-space:normal;">
          <strong>${hospital.name}</strong><br/>
          ${bedText !== null ? `${bedText} (${formatAsOf(data.asOf)})` : ''}
        </div>
      `
      const infowindow = new kakao.maps.InfoWindow({ content })

      kakao.maps.event.addListener(marker, 'click', () => {
        if (infowindow.getMap()) {
          infowindow.close()
        } else {
          infowindow.open(map, marker)
        }
      })
//----------------------마커 + 인포윈도우 배열에 저장------------------------
      hospitalMarkersRef.current.push({ marker, infowindow })
    })
    // setUsedFallback, setStatus는 여기 안 넣습니다 (다음 단계에서 다르게 처리할 거예요)
  })
}

export default function HospitalMapPage() {
  const mapContainerRef = useRef(null)
  const addressInputRef = useRef(null)
  const kakaoRef = useRef(null)   // ← 새로 추가
  const mapRef = useRef(null)     // ← 새로 추가
  const centerOverlayRef = useRef(null)   // 중심점 빨간 원 (한 개만 존재)
const hospitalMarkersRef = useRef([])   // 병원 마커 + 인포윈도우 목록
  const [status, setStatus] = useState('loading') // 'loading' | 'ready' | 'error'
  const [usedFallback, setUsedFallback] = useState(false)

  useEffect(() => {
    let cancelled = false
    Promise.all([loadKakaoMaps(), getCurrentPosition()])
      .then(([kakao, position]) => {
        if (cancelled || !mapContainerRef.current) return
        const center = new kakao.maps.LatLng(position.lat, position.lng)
        const map = new kakao.maps.Map(mapContainerRef.current, { center, level: 3 })
        kakaoRef.current = kakao   // ← 새 줄: 나중에 검색 버튼에서 쓰려고 저장
        mapRef.current = map       // ← 새 줄: 나중에 검색 버튼에서 쓰려고 저장

        return showHospitalsAround(kakao, map, position.lat, position.lng, centerOverlayRef, hospitalMarkersRef).then(() => {
          if (cancelled) return
          setUsedFallback(position.isFallback)
          setStatus('ready')
        })
      })
      .catch(() => {
        if (!cancelled) setStatus('error')
      })

    return () => {
      cancelled = true
    }
  }, [])
  
  function handleSearch() {
    const address = addressInputRef.current.value.trim()
    if (!address) return

    const kakao = kakaoRef.current
  const geocoder = new kakao.maps.services.Geocoder()

  geocoder.addressSearch(address, (results, status) => {
  if (status !== kakao.maps.services.Status.OK) {
    alert('주소를 찾을 수 없습니다.')
    return
  }
  
  const lat = Number(results[0].y)
  const lng = Number(results[0].x)

  showHospitalsAround(kakao, mapRef.current, lat, lng, centerOverlayRef, hospitalMarkersRef)
  setUsedFallback(false)
})
  }

  return (
    <main className="page">
      <h1>주변 응급실</h1>
      <div style={{ display: 'flex', gap: '8px' }}>
  <input ref={addressInputRef} type="text" placeholder="주소를 입력하세요" />
  <button onClick={handleSearch}>검색</button>
</div>
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
