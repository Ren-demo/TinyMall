import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './assets/styles.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
// 等待路由就绪后输出当前路由，便于调试初始重定向问题
router.isReady().then(() => {
	console.debug('[router] initial route after isReady():', router.currentRoute.value.fullPath)
	app.mount('#app')
})