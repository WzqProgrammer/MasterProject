import Vue from 'vue'
import App from './App.vue'

// 导入 Element_ui
import ElementUI from 'element-ui'
// 导入 Element-ui 样式
import 'element-ui/lib/theme-chalk/index.css'
// 插件 Element-ui
Vue.use(ElementUI)

import less from 'less'
Vue.use(less)
// 导入全局初始化样式
// import './assets/css/index.css'
import './assets/css/global.css'
// 导入路由设置
import router from './route'

import storage from './utils/storage'

import axios from 'axios'

// 为axios的请求头添加token
// axios.interceptors.request.use(config => {
//   config.headers.Authorization = localStorage.getItem('token')
//   return config
// })


//每个Vue组件可通过this.$http使用axios
Vue.prototype.$http = axios

Vue.prototype.$storage = storage

Vue.config.productionTip = false


new Vue({
  render: h => h(App),
  // 挂载到Vue示例上
  router
}).$mount('#app')
