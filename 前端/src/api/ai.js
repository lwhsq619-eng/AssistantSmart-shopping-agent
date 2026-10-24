import request from './request'

const aiConfig = { timeout: 60000 }

export function aiChat(data) {
  return request.post('/ai/chat', data, aiConfig)
}

export function aiWorkbenchChat(data) {
  return request.post('/ai/workbench/chat', data, aiConfig)
}

export function shoppingGuideChat(data) {
  return request.post('/ai/guide/chat', data, aiConfig)
}

export function customerServiceChat(data) {
  return request.post('/ai/customer-service/chat', data, aiConfig)
}

export function semanticSearch(data) {
  return request.post('/ai/search/semantic', data, aiConfig)
}

export function retentionGenerate(data) {
  return request.post('/ai/retention/generate', data, aiConfig)
}

export function copywritingGenerate(data) {
  return request.post('/ai/merchant/copywriting/generate', data, aiConfig)
}

export function commentAnalyze(data) {
  return request.post('/ai/merchant/comment/analyze', data, aiConfig)
}

export function marketingGenerate(data) {
  return request.post('/ai/merchant/marketing/generate', data, aiConfig)
}

export function orderAnalysis(data) {
  return request.post('/ai/merchant/order-analysis', data, aiConfig)
}

export function inventoryAnalysis(data) {
  return request.post('/ai/inventory/analysis', data, aiConfig)
}

export function afterSaleClassify(data) {
  return request.post('/ai/after-sale/classify', data, aiConfig)
}

export function ragAsk(data) {
  return request.post('/ai/rag/ask', data, aiConfig)
}

export function personalizedRecommend(data) {
  return request.post('/ai/recommend/personalized', data, aiConfig)
}

export function sceneMatch(data) {
  return request.post('/ai/match/scene', data, aiConfig)
}

export function aiHealth() {
  return request.get('/ai/health', aiConfig)
}

export const aiBaseChat = aiChat
export const aiGuideChat = shoppingGuideChat
export const aiCustomerServiceChat = customerServiceChat
export const aiSemanticSearch = semanticSearch
export const aiCopywritingGenerate = copywritingGenerate
export const aiCommentAnalyze = commentAnalyze
export const aiMarketingGenerate = marketingGenerate
export const aiOrderAnalysis = orderAnalysis
export const aiInventoryAnalysis = inventoryAnalysis
export const aiAfterSaleClassify = afterSaleClassify
export const aiRagAsk = ragAsk
export const aiPersonalizedRecommend = personalizedRecommend
export const aiSceneMatch = sceneMatch
