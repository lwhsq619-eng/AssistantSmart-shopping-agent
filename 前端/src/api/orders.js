import request from './request'

export const pageOrdersApi = (params) => request.get('/orders/page', { params })
export const createOrderApi = (data) => request.post('/orders', data)
export const updateOrderApi = (id, data) => request.put(`/orders/${id}`, data)
export const deleteOrderApi = (id) => request.delete(`/orders/${id}`)
export const updateOrderStatusApi = (id, status) => request.patch(`/orders/${id}/status`, null, { params: { status } })
