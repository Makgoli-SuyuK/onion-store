import './ProductImage.css'

// 백엔드 상품 응답에 이미지 필드가 없어 src는 항상 비어있다.
// 나중에 백엔드가 이미지 URL을 내려주기 시작하면 호출부에서 src만 넘기면 된다.
export function ProductImage({
  src = null,
  alt,
  className,
}: {
  src?: string | null
  alt: string
  className?: string
}) {
  if (src) {
    return <img src={src} alt={alt} className={className ? `product-image ${className}` : 'product-image'} />
  }
  return (
    <div className={className ? `product-image product-image--placeholder ${className}` : 'product-image product-image--placeholder'}>
      <span aria-hidden="true">🧅</span>
      <p>상품 사진 준비 중</p>
    </div>
  )
}
