// 백엔드 공통 응답 포맷: ApiResponse(success, code, message, data)
export interface ApiResponse<T> {
  success: boolean
  code: string
  message: string
  data: T
}

// 우리 화면에서 쓰는 정규화된 페이지 응답 모양
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

// 백엔드가 실제로 내려주는 Spring Data Page의 JSON 모양 (필드명이 다름: number/size)
export interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export function fromSpringPage<T>(page: SpringPage<T>): PageResponse<T> {
  return {
    content: page.content,
    totalElements: page.totalElements,
    totalPages: page.totalPages,
    page: page.number,
    size: page.size,
  }
}

export type LoadState = 'idle' | 'loading' | 'success' | 'error'
