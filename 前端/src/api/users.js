import request from './request'

export const pageUsersApi = (params) => request.get('/users/page', { params })
export const listUsersApi = (params) => request.get('/users/list', { params })
export const createUserApi = (data) => request.post('/users', data)
export const updateUserApi = (id, data) => request.put(`/users/${id}`, data)
export const deleteUserApi = (id) => request.delete(`/users/${id}`)
export const updateUserStatusApi = (id, status) => request.patch(`/users/${id}/status`, null, { params: { status } })
