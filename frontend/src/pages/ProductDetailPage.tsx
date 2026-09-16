import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { productApi } from '@/api/productApi'
import { useApiRequest } from '@/hooks/useApiRequest'
import { useAuth } from '@/hooks/useAuth'
import { useCart } from '@/hooks/useCart'
import { useToast } from '@/hooks/useToast'
import { ProductImage } from '@/components/common/ProductImage'
import { QuantityStepper } from '@/components/common/QuantityStepper'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { ErrorState } from '@/components/common/ErrorState'
import { formatCurrency } from '@/utils/currency'
import './ProductDetailPage.css'

export function ProductDetailPage() {
  const { id } = useParams<{ id: string }>()
  const productId = Number(id)
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  const cart = useCart()
  const { showToast } = useToast()

  const { data: product, loading, error, refetch } = useApiRequest(
    () => productApi.getProduct(productId),
    [productId],
  )

  const [quantity, setQuantity] = useState(1)
  const [likeCount, setLikeCount] = useState(0)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (product) {
      setQuantity(1)
      setLikeCount(product.likeCount)
    }
  }, [product])

  if (loading) return <LoadingSpinner />
  if (error) return <ErrorState message={error} onRetry={refetch} />
  if (!product) return null

  const soldOut = product.status !== 'SELLING'
  const totalPrice = product.price * quantity

  const requireLogin = () => {
    showToast('로그인이 필요한 서비스입니다.', 'error')
    navigate('/login')
  }

  // 백엔드에 좋아요 등록/취소 API가 아직 없어 화면에서만 증가시킨다.
  const handleLike = () => setLikeCount((c) => c + 1)

  const handleAddToCart = async () => {
    if (!isAuthenticated) return requireLogin()
    setSubmitting(true)
    try {
      await cart.addItem(product.id, quantity)
    } catch {
      // 토스트는 useCart 내부에서 처리
    } finally {
      setSubmitting(false)
    }
  }

  const handleBuyNow = async () => {
    if (!isAuthenticated) return requireLogin()
    setSubmitting(true)
    try {
      const item = await cart.addItem(product.id, quantity)
      navigate('/checkout', { state: { cartItemIds: [item.cartItemId] } })
    } catch {
      // 토스트는 useCart 내부에서 처리
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="container product-detail">
      <div className="product-detail__pic">
        <ProductImage alt={product.name} />
      </div>

      <div className="product-detail__info">
        <span className="badge badge--tag">{product.categoryName}</span>
        <h1 className="product-detail__name">{product.name}</h1>
        {product.description && <p className="product-detail__desc">{product.description}</p>}

        <div className="product-detail__meta">
          <span className="product-detail__price">{formatCurrency(product.price)}</span>
          <button type="button" className="product-detail__like" onClick={handleLike}>
            🧡 좋아요 {likeCount}
          </button>
        </div>

        <p className="product-detail__stock">
          {soldOut ? '현재 품절된 상품이에요.' : `남은 재고 ${product.stock}개`}
        </p>
        <p className="product-detail__shipping">오직 양파만 · 5만원 이상 구매 시 무료배송 (미만 3,000원)</p>

        {!soldOut && (
          <div className="product-detail__qty">
            <span>수량</span>
            <QuantityStepper value={quantity} max={product.stock} onChange={setQuantity} />
          </div>
        )}

        <div className="product-detail__total">
          총 상품 금액 <strong>{formatCurrency(totalPrice)}</strong>
        </div>

        <div className="product-detail__actions">
          <button
            type="button"
            className="btn btn--secondary btn--block"
            disabled={soldOut || submitting}
            onClick={handleAddToCart}
          >
            장바구니 담기
          </button>
          <button
            type="button"
            className="btn btn--primary btn--block"
            disabled={soldOut || submitting}
            onClick={handleBuyNow}
          >
            바로 구매하기
          </button>
        </div>
      </div>
    </div>
  )
}
