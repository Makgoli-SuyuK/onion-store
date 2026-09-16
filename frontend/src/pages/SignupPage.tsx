import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { resolveErrorMessage } from '@/utils/errorMessage'
import './LoginPage.css'

export function SignupPage() {
  const { signup } = useAuth()
  const navigate = useNavigate()

  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  const validate = () => {
    const next: Record<string, string> = {}
    if (!name.trim()) next.name = '이름을 입력해 주세요.'
    if (!email.trim()) next.email = '이메일을 입력해 주세요.'
    if (!phoneNumber.trim()) next.phoneNumber = '전화번호를 입력해 주세요.'
    if (password.length < 4) next.password = '비밀번호는 4자 이상 입력해 주세요.'
    if (password !== passwordConfirm) next.passwordConfirm = '비밀번호가 일치하지 않아요.'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!validate()) return
    setSubmitting(true)
    try {
      await signup(name, email, phoneNumber, password)
      navigate('/', { replace: true })
    } catch (err) {
      setErrors({ form: (err as Error).message || resolveErrorMessage(err) })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="container login-page">
      <div className="card login-page__box">
        <h1 className="login-page__title">회원가입</h1>
        <p className="login-page__hint">이메일에 admin이 들어가면 관리자 계정으로 가입돼요 (데모용).</p>
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="signup-name">이름</label>
            <input id="signup-name" className="input" value={name} onChange={(e) => setName(e.target.value)} />
            {errors.name && <span className="field-error">{errors.name}</span>}
          </div>
          <div className="field">
            <label htmlFor="signup-email">이메일</label>
            <input
              id="signup-email"
              type="email"
              className="input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
            {errors.email && <span className="field-error">{errors.email}</span>}
          </div>
          <div className="field">
            <label htmlFor="signup-phone">전화번호</label>
            <input
              id="signup-phone"
              type="tel"
              className="input"
              placeholder="01000000000"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
            />
            {errors.phoneNumber && <span className="field-error">{errors.phoneNumber}</span>}
          </div>
          <div className="field">
            <label htmlFor="signup-password">비밀번호</label>
            <input
              id="signup-password"
              type="password"
              className="input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            {errors.password && <span className="field-error">{errors.password}</span>}
          </div>
          <div className="field">
            <label htmlFor="signup-password-confirm">비밀번호 확인</label>
            <input
              id="signup-password-confirm"
              type="password"
              className="input"
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
            />
            {errors.passwordConfirm && <span className="field-error">{errors.passwordConfirm}</span>}
          </div>
          {errors.form && <p className="field-error" style={{ marginBottom: 12 }}>{errors.form}</p>}
          <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
            {submitting ? '가입 중...' : '회원가입'}
          </button>
        </form>
        <p className="login-page__footer">
          이미 계정이 있으신가요? <Link to="/login">로그인</Link>
        </p>
      </div>
    </div>
  )
}
