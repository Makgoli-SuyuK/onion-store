import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div className="container state-box">
      <span className="state-box__emoji" aria-hidden="true">
        🧅
      </span>
      <p>페이지를 찾을 수 없어요.</p>
      <Link to="/" className="btn btn--primary btn--sm">
        홈으로 돌아가기
      </Link>
    </div>
  )
}
