import { Link } from 'react-router-dom'
import { useApiRequest } from '@/hooks/useApiRequest'
import { productApi } from '@/api/productApi'
import { ProductGrid } from '@/components/product/ProductGrid'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { ErrorState } from '@/components/common/ErrorState'
import characterImg from '@/assets/onionz-character.png'
import './HomePage.css'

const FEATURES_TOP = ['신선하게', '맛있게', '간편하게']
const FEATURES_BOTTOM = ['전남 무안 산지 직송', '꼼꼼한 선별', '안전 포장']

export function HomePage() {
  const { data, loading, error, refetch } = useApiRequest(
    () => productApi.getProducts({ sort: 'POPULAR', page: 0, size: 4 }),
    [],
  )

  return (
    <div>
      <section className="hero container">
        <div className="hero__copy">
          <h1 className="hero__title">좋은 양파가, 좋은 하루를 만들어요!</h1>
          <p className="hero__subtitle">귀여운 양파 친구들이 산지의 신선함을 식탁까지 즐겁게 전해드려요.</p>
          <Link to="/products" className="btn btn--primary">
            싱싱한 양파 만나보기
          </Link>
        </div>
        <div className="hero__art">
          <img src={characterImg} alt="어니언즈 캐릭터들이 인사하는 모습" />
        </div>
      </section>

      <section className="benefits">
        <div className="container benefits__row">
          {FEATURES_TOP.map((f) => (
            <span key={f} className="benefits__item">
              {f}
            </span>
          ))}
        </div>
        <div className="container benefits__row benefits__row--sub">
          {FEATURES_BOTTOM.map((f) => (
            <span key={f} className="benefits__item benefits__item--sub">
              {f}
            </span>
          ))}
        </div>
      </section>

      <section className="container popular-section">
        <h2 className="popular-section__title">지금 인기 있는 양파예요</h2>
        {loading && <LoadingSpinner />}
        {!loading && error && <ErrorState message={error} onRetry={refetch} />}
        {!loading && !error && data && <ProductGrid products={data.content} />}
      </section>
    </div>
  )
}
