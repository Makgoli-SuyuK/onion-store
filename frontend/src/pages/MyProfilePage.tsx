import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { userApi } from '@/api/userApi'
import { useAuth } from '@/hooks/useAuth'
import { useToast } from '@/hooks/useToast'
import './MyProfilePage.css'

export function MyProfilePage() {
  const { user, refreshUser, logout } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [withdrawPassword, setWithdrawPassword] = useState('')
  const [profileSubmitting, setProfileSubmitting] = useState(false)
  const [passwordSubmitting, setPasswordSubmitting] = useState(false)
  const [withdrawSubmitting, setWithdrawSubmitting] = useState(false)

  useEffect(() => {
    if (!user) return
    setName(user.name)
    setPhoneNumber(user.phoneNumber)
  }, [user])

  if (!user) return null

  const updateProfile = async (event: FormEvent) => {
    event.preventDefault()
    setProfileSubmitting(true)
    try {
      await userApi.updateProfile({ name: name.trim(), phoneNumber: phoneNumber.trim() })
      await refreshUser()
      showToast('내 정보를 수정했습니다.', 'success')
    } catch (err) {
      showToast((err as Error).message, 'error')
    } finally {
      setProfileSubmitting(false)
    }
  }

  const changePassword = async (event: FormEvent) => {
    event.preventDefault()
    setPasswordSubmitting(true)
    try {
      await userApi.changePassword({ currentPassword, newPassword })
      setCurrentPassword('')
      setNewPassword('')
      showToast('비밀번호를 변경했습니다.', 'success')
    } catch (err) {
      showToast((err as Error).message, 'error')
    } finally {
      setPasswordSubmitting(false)
    }
  }

  const withdraw = async (event: FormEvent) => {
    event.preventDefault()
    if (!window.confirm('회원 탈퇴 시 계정을 다시 사용할 수 없습니다. 계속할까요?')) return

    setWithdrawSubmitting(true)
    try {
      await userApi.withdraw({ password: withdrawPassword })
      logout()
      showToast('회원 탈퇴가 완료되었습니다.', 'success')
      navigate('/', { replace: true })
    } catch (err) {
      showToast((err as Error).message, 'error')
    } finally {
      setWithdrawSubmitting(false)
    }
  }

  return (
    <div className="container profile-page">
      <h1 className="profile-page__title">내 정보 관리</h1>

      <section className="card profile-page__section">
        <h2>기본 정보</h2>
        <form onSubmit={updateProfile}>
          <div className="field">
            <label htmlFor="profile-email">이메일</label>
            <input id="profile-email" className="input" value={user.email} disabled />
          </div>
          <div className="field">
            <label htmlFor="profile-name">이름</label>
            <input id="profile-name" className="input" maxLength={30} value={name} onChange={(event) => setName(event.target.value)} required />
          </div>
          <div className="field">
            <label htmlFor="profile-phone">전화번호</label>
            <input id="profile-phone" className="input" maxLength={20} value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} required />
          </div>
          <button type="submit" className="btn btn--primary" disabled={profileSubmitting}>
            {profileSubmitting ? '저장 중...' : '기본 정보 저장'}
          </button>
        </form>
      </section>

      <section className="card profile-page__section">
        <h2>비밀번호 변경</h2>
        <form onSubmit={changePassword}>
          <div className="field">
            <label htmlFor="current-password">현재 비밀번호</label>
            <input id="current-password" type="password" className="input" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} required />
          </div>
          <div className="field">
            <label htmlFor="new-password">새 비밀번호</label>
            <input id="new-password" type="password" className="input" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} required />
          </div>
          <button type="submit" className="btn btn--outline" disabled={passwordSubmitting}>
            {passwordSubmitting ? '변경 중...' : '비밀번호 변경'}
          </button>
        </form>
      </section>

      <section className="card profile-page__section profile-page__section--withdraw">
        <h2>회원 탈퇴</h2>
        <p>탈퇴하려면 현재 비밀번호를 입력해 주세요.</p>
        <form onSubmit={withdraw}>
          <div className="field">
            <label htmlFor="withdraw-password">현재 비밀번호</label>
            <input id="withdraw-password" type="password" className="input" value={withdrawPassword} onChange={(event) => setWithdrawPassword(event.target.value)} required />
          </div>
          <button type="submit" className="btn btn--ghost profile-page__withdraw-button" disabled={withdrawSubmitting}>
            {withdrawSubmitting ? '탈퇴 처리 중...' : '회원 탈퇴'}
          </button>
        </form>
      </section>
    </div>
  )
}
