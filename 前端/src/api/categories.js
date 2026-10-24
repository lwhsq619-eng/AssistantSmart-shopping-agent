import request from './request'

export const pageCategoriesApi = (params) => request.get('/categories/page', { params })
export const listCategoriesApi = (params) => request.get('/categories/list', { params })
export const createCategoryApi = (data) => request.post('/categories', data)
export const updateCategoryApi = (id, data) => request.put(`/categories/${id}`, data)
export const deleteCategoryApi = (id) => request.delete(`/categories/${id}`)
export const updateCategoryStatusApi = (id, status) => request.patch(`/categories/${id}/status`, null, { params: { status } })
