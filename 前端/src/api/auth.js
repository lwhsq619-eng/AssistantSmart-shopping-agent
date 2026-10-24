import request from './request'

export const adminLoginApi = (data) => request.post('/auth/admin/login', data)
export const userLoginApi = (data) => request.post('/auth/user/login', data)
export const registerApi = (data) => request.post('/auth/register', data)
export const loginApi = adminLoginApi
