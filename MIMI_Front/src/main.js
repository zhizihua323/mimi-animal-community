import { createSSRApp } from 'vue'
import pinia from './stores'

import App from './App.vue'
import { setupAuthGuard } from './utils/auth'
export function createApp() {
  const app = createSSRApp(App)
  app.use(pinia)
  setupAuthGuard()
  return {
    app,
  }
}