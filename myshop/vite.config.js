import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
// vite.config.js
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    port: 3000,
    proxy: {
      // '/api': {
        
      //   // target: 'http://8.137.168.204:8080',
      //   target: 'http://localhost:8080',
      //   // target: 'http://192.168.122.66:8080',
      //   changeOrigin: true
      // },
      '/tinymall': {
        // target: 'http://8.137.168.204:8080',
        target: 'http://localhost:8080',
        // target: 'http://192.168.122.66:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
})