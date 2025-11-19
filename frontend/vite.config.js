import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0', // Docker 외부 접속 허용
    port: 3000,      // 포트 명시 (선택 사항)
    proxy: {
      // '/api'로 시작하는 요청이 오면 백엔드로 전달
      '/api': {
        target: 'http://backend:8080', // Docker Compose 서비스 이름 사용
        changeOrigin: true,
        secure: false
      },
    },
  },
})