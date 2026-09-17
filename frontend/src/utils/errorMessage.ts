import axios from 'axios'
import type { ApiResponse } from '@/types/common'

// 화면에 노출해도 되는 기본 안내 문구 (백엔드 코드/영문 오류는 절대 그대로 노출하지 않음)
export const DEFAULT_MESSAGES = {
  NETWORK: '현재 서비스에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.',
  TIMEOUT: '요청 처리 시간이 초과되었습니다. 다시 시도해 주세요.',
  UNKNOWN: '요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.',
  UNAUTHORIZED: '로그인이 필요한 서비스입니다.',
  FORBIDDEN: '해당 요청을 처리할 권한이 없습니다.',
  INSUFFICIENT_STOCK: '선택한 수량보다 재고가 부족합니다.',
  SOLD_OUT: '현재 품절된 상품입니다.',
  CART_ITEM_NOT_FOUND: '장바구니에서 해당 상품을 찾을 수 없습니다.',
} as const

// 백엔드 ErrorCode(onion-store repo, global/exception/ErrorCode.java)의 코드 -> 한글 메시지.
// 백엔드가 이미 message 필드에 같은 한글 문구를 내려주므로 이 표는 message가 비어있을 때의 안전장치용.
const CODE_MESSAGE_MAP: Record<string, string> = {
  COMMON_001: '요청 형식이 올바르지 않습니다.',
  COMMON_002: '입력값을 확인해 주세요.',
  COMMON_003: '요청 형식이 올바르지 않습니다.',
  COMMON_004: '필수 입력값이 빠졌습니다.',
  COMMON_005: DEFAULT_MESSAGES.UNKNOWN,
  COMMON_006: DEFAULT_MESSAGES.UNKNOWN,
  COMMON_007: DEFAULT_MESSAGES.UNKNOWN,
  COMMON_008: DEFAULT_MESSAGES.UNKNOWN,

  AUTH_001: DEFAULT_MESSAGES.UNAUTHORIZED,
  AUTH_002: DEFAULT_MESSAGES.UNAUTHORIZED,
  AUTH_003: DEFAULT_MESSAGES.UNAUTHORIZED,
  AUTH_004: DEFAULT_MESSAGES.FORBIDDEN,

  USER_001: '이미 사용 중인 이메일입니다.',
  USER_002: '회원 정보를 찾을 수 없습니다.',
  USER_003: '이메일 또는 비밀번호가 올바르지 않습니다.',

  CATEGORY_001: '카테고리를 찾을 수 없습니다.',
  CATEGORY_002: '이미 존재하는 카테고리입니다.',
  CATEGORY_003: '상품이 있는 카테고리는 삭제할 수 없습니다.',
  CATEGORY_004: '이미 삭제된 카테고리입니다.',

  PRODUCT_001: '요청하신 상품 정보를 찾을 수 없습니다.',
  PRODUCT_002: DEFAULT_MESSAGES.SOLD_OUT,
  PRODUCT_003: '가격은 0원보다 커야 합니다.',
  PRODUCT_004: '재고는 0 이상이어야 합니다.',
  PRODUCT_005: DEFAULT_MESSAGES.INSUFFICIENT_STOCK,

  LIKE_001: '이미 찜한 상품이에요.',
  LIKE_002: '찜 내역을 찾을 수 없어요.',

  CART_001: '장바구니를 찾을 수 없습니다.',
  CART_002: '장바구니가 비어있습니다.',
  CART_003: DEFAULT_MESSAGES.CART_ITEM_NOT_FOUND,
  CART_004: '수량은 1개 이상이어야 합니다.',
  CART_005: DEFAULT_MESSAGES.FORBIDDEN,

  ORDER_001: '요청하신 주문 정보를 찾을 수 없습니다.',
  ORDER_002: '현재 결제할 수 없는 주문입니다.',
  ORDER_003: '처리할 수 없는 주문 상태예요.',
  ORDER_004: '이미 취소된 주문입니다.',
  ORDER_005: '현재 상태에서는 주문을 취소할 수 없습니다.',
  ORDER_006: DEFAULT_MESSAGES.FORBIDDEN,

  PAYMENT_001: '결제 정보를 찾을 수 없습니다.',
  PAYMENT_002: '결제 금액이 주문 금액과 일치하지 않습니다.',
  PAYMENT_003: '처리할 수 없는 결제 상태예요.',
  PAYMENT_004: '이미 처리된 결제입니다.',
  PAYMENT_005: '결제 처리에 실패했습니다.',
  PAYMENT_006: '결제사 조회에 실패했습니다. 잠시 후 다시 시도해 주세요.',
  PAYMENT_007: '결제가 아직 완료되지 않았습니다.',
  PAYMENT_008: '결제 취소에 실패했습니다.',
  PAYMENT_009: '이미 취소된 결제입니다.',
}

/**
 * axios 에러를 화면에 그대로 보여줘도 되는 한글 메시지로 변환.
 * 우선순위: 백엔드 message -> 코드 매핑표 -> HTTP 상태 기본 문구 -> 통신 실패/시간초과/알수없음
 */
export function resolveErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (error.response) {
      const data = error.response.data as Partial<ApiResponse<unknown>> | undefined
      if (data?.message) return data.message
      if (data?.code && CODE_MESSAGE_MAP[data.code]) return CODE_MESSAGE_MAP[data.code]
      if (error.response.status === 401) return DEFAULT_MESSAGES.UNAUTHORIZED
      if (error.response.status === 403) return DEFAULT_MESSAGES.FORBIDDEN
      return DEFAULT_MESSAGES.UNKNOWN
    }
    if (error.code === 'ECONNABORTED') return DEFAULT_MESSAGES.TIMEOUT
    return DEFAULT_MESSAGES.NETWORK
  }
  return DEFAULT_MESSAGES.UNKNOWN
}
