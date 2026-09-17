import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { resolveErrorMessage } from '@/utils/errorMessage'
import './LoginPage.css'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from ?? '/'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(email, password)
      navigate(from, { replace: true })
    } catch (err) {
      setError((err as Error).message || resolveErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="container login-page">
      <div className="card login-page__box">
        <h1 className="login-page__title">로그인</h1>
        <p className="login-page__hint">테스트 관리자: refund-admin@onion.store / refund1234</p>
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="login-email">이메일</label>
            <input
              id="login-email"
              type="email"
              className="input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="field">
            <label htmlFor="login-password">비밀번호</label>
            <input
              id="login-password"
              type="password"
              className="input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          {error && <p className="field-error" style={{ marginBottom: 12 }}>{error}</p>}
          <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
            {submitting ? '로그인 중...' : '로그인'}
          </button>
        </form>
        <p className="login-page__footer">
          아직 계정이 없으신가요? <Link to="/signup">회원가입</Link>
        </p>
      </div>
    </div>
  )
}
