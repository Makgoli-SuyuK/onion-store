// GET /api/carts 응답과 동일한 모양. 이미지/재고/판매상태 필드는 백엔드에 없다.
export interface CartItem {
  cartItemId: number
  productId: number
  productName: string
  price: number
  quantity: number
  subtotal: number
}

export interface Cart {
  cartId: number
  items: CartItem[]
  totalPrice: number
}
