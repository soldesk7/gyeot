import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import basicSsl from '@vitejs/plugin-basic-ssl'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), basicSsl()],
  server: {
    // 같은 Wi-Fi의 휴대폰·태블릿에서도 Vite 개발 서버에 접속할 수 있게 허용
    host: '0.0.0.0',
    // 휴대폰 접속 주소를 항상 :5173으로 안내할 수 있도록 포트를 고정한다.
    strictPort: true,
    proxy: {
      // 브라우저에는 같은 출처(/api) 요청으로 보이고, Vite가 PC 내부 BFF로 전달한다.
      // 따라서 휴대폰에서 localhost:8080을 직접 호출하거나 BFF CORS 설정 불필요
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
})
