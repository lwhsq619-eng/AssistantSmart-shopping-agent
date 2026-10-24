import request from './request'

export const getProductsApi = (params) => request.get('/products', { params })
export const searchProductsApi = (params) => request.get('/products/search', { params })
export const getProductDetailApi = (id) => request.get(`/products/${id}`)
export const getSpecialProductsApi = () => request.get('/products/special')
export const getRecommendProductsApi = (type = 'hot') => request.get('/products/recommend', { params: { type } })
export const getSimilarProductsApi = (id) => request.get(`/products/similar/${id}`)
export const getCategoriesApi = () => request.get('/categories')
export const getBrandsApi = () => request.get('/brands')
export const getBrandDetailApi = (id) => request.get(`/brands/${id}`)

export const getCartApi = () => request.get('/cart')
export const addCartApi = (data) => request.post('/cart', data)
export const updateCartApi = (id, data) => request.put(`/cart/${id}`, data)
export const deleteCartApi = (id) => request.delete(`/cart/${id}`)
export const batchDeleteCartApi = (ids) => request.delete('/cart/batch', { data: ids })

export const getAddressesApi = () => request.get('/addresses')
export const createAddressApi = (data) => request.post('/addresses', data)
export const updateAddressApi = (id, data) => request.put(`/addresses/${id}`, data)
export const deleteAddressApi = (id) => request.delete(`/addresses/${id}`)

export const createOrderApi = (data) => request.post('/orders', data)
export const getMyOrdersApi = (params) => request.get('/orders/my', { params })
export const getOrderDetailApi = (id) => request.get(`/orders/${id}`)
export const cancelOrderApi = (id) => request.put(`/orders/${id}/cancel`)
export const confirmReceiptApi = (id) => request.put(`/orders/${id}/confirm-receipt`)

export const getPaymentApi = (orderNo) => request.get(`/payments/${orderNo}`)
export const payOrderApi = (orderNo, paymentMethod) => request.post(`/payments/${orderNo}`, null, { params: { paymentMethod } })

export const getProductReviewsApi = (productId) => request.get(`/reviews/product/${productId}`)
export const createReviewApi = (data) => request.post('/reviews', data)
export const getMyReviewsApi = () => request.get('/reviews/my')

export const getGiftCardsApi = () => request.get('/gift-cards')
export const purchaseGiftCardApi = (data) => request.post('/gift-cards/purchase', data)
export const getMyGiftCardsApi = () => request.get('/gift-cards/my')
export const redeemGiftCardApi = (code) => request.post('/gift-cards/redeem', null, { params: { code } })
