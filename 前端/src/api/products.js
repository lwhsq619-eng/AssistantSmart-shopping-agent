import request from './request'

export const pageProductsApi = (params) => request.get('/products/page', { params })
export const listProductsApi = (params) => request.get('/products/list', { params })
export const createProductApi = (data) => request.post('/products', data)
export const updateProductApi = (id, data) => request.put(`/products/${id}`, data)
export const deleteProductApi = (id) => request.delete(`/products/${id}`)
export const updateProductStatusApi = (id, status) => request.patch(`/products/${id}/status`, null, { params: { status } })
