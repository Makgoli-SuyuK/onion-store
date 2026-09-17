import axios, { type AxiosResponse } from 'axios'
import type { ApiResponse, PageResponse, SpringPage } from '@/types/common'
import { fromSpringPage } from '@/types/common'
import { getAccessToken } from '@/utils/storage'
import { DEFAULT_MESSAGES, resolveErrorMessage } from '@/utils/errorMessage'

// 백엔드 주소는 여기 한 곳에서만 관리. 실제 API 명세 확정 전까지는 .env의
// VITE_API_BASE_URL 값만 바꾸면 됨 (컴포넌트에는 주소를 직접 적지 않는다).
export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 화면에서 사용할 정규화된 에러. message는 이미 한글로 변환되어 있어
// catch(err) => toast(err.message) 형태로 바로 써도 됨.
export class AppError extends Error {}

http.interceptors.response.use(
  (response) => response,
  (error) => {
    return Promise.reject(new AppError(resolveErrorMessage(error)))
  },
)

// 응답 바디는 항상 ApiResponse(success, code, message, data) 형태.
export function unwrap<T>(response: AxiosResponse<ApiResponse<T>>): T {
  const body = response.data
  // 정상 HTTP 상태(2xx)인데 success:false인 경우를 대비한 방어 처리
  if (!body.success) {
    throw new AppError(body.message || DEFAULT_MESSAGES.UNKNOWN)
  }
  return body.data
}

// 백엔드는 페이지 응답을 Spring Data Page 그대로 내려준다 (필드명 number/size).
export function unwrapPage<T>(response: AxiosResponse<ApiResponse<SpringPage<T>>>): PageResponse<T> {
  return fromSpringPage(unwrap(response))
}
