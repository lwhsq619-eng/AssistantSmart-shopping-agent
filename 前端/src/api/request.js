import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const auth = JSON.parse(localStorage.getItem('mall_auth') || 'null')
  if (auth?.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && Object.prototype.hasOwnProperty.call(body, 'success')) {
      if (body.success === true) {
        return body
      }
      const message = body.message || body.reply || 'AI 服务请求失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    if (body.code !== 200) {
      const message = body.message || '接口请求失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return body.data
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络异常，请检查后端服务'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
