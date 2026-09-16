import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { useCart } from '@/hooks/useCart'
import { useCategories } from '@/hooks/useCategories'
import './Header.css'

export function Header() {
  const navigate = useNavigate()
  const { isAuthenticated, isAdmin, user, logout } = useAuth()
  const { totalCount } = useCart()
  const { categories } = useCategories()
  const [keyword, setKeyword] = useState('')

  const handleSearch = (e: FormEvent) => {
    e.preventDefault()
    const trimmed = keyword.trim()
    navigate(trimmed ? `/products?name=${encodeURIComponent(trimmed)}` : '/products')
  }

  return (
    <header className="site-header">
      <div className="notice-bar">오직 양파만 · 5만원 이상 무료배송</div>

      <div className="container header-row">
        <Link to="/" className="logo" aria-label="어니언즈 홈으로 이동">
          <span className="logo__kr">어니언즈</span>
          <span className="logo__en">ONIONZ</span>
        </Link>

        <form className="search-bar" role="search" onSubmit={handleSearch}>
          <input
            type="search"
            className="input"
            placeholder="어떤 양파를 찾으세요?"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            aria-label="상품 검색"
          />
          <button type="submit" className="search-bar__btn" aria-label="검색">
            🔍
          </button>
        </form>

        <nav className="header-actions" aria-label="사용자 메뉴">
          {isAdmin && (
            <>
              <Link to="/admin/products" className="header-actions__link">상품 관리</Link>
              <Link to="/admin/refunds" className="header-actions__link">환불 관리</Link>
            </>
          )}
          {isAuthenticated ? (
            <>
              <Link to="/chat" className="header-actions__link">
                {isAdmin ? '문의 관리' : '1:1 문의'}
              </Link>
              <Link to="/mypage/profile" className="header-actions__link">내 정보</Link>
              <Link to="/mypage/orders" className="header-actions__link">주문 내역</Link>
              {!isAdmin && <Link to="/mypage/refunds" className="header-actions__link">환불 내역</Link>}
              <button type="button" className="header-actions__link header-actions__link--btn" onClick={logout}>
                {user?.name}님 로그아웃
              </button>
            </>
          ) : (
            <>
              <Link to="/mypage/orders" className="header-actions__link">
                주문 내역
              </Link>
              <Link to="/login" className="header-actions__link">
                로그인
              </Link>
              <Link to="/signup" className="header-actions__link">
                회원가입
              </Link>
            </>
          )}
          <Link to="/cart" className="header-actions__cart" aria-label={`장바구니, 담긴 상품 ${totalCount}개`}>
            🧺
            {totalCount > 0 && <span className="header-actions__cart-badge">{totalCount}</span>}
          </Link>
        </nav>
      </div>

      <nav className="category-nav" aria-label="상품 카테고리">
        <div className="container category-nav__list">
          <Link to="/products">전체 상품</Link>
          {categories.map((c) => (
            <Link key={c.id} to={`/products?category=${encodeURIComponent(c.name)}`}>
              {c.name}
            </Link>
          ))}
        </div>
      </nav>
    </header>
  )
}
