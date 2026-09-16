import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react'
import { authApi } from '@/api/authApi'
import { userApi } from '@/api/userApi'
import { clearAccessToken, getAccessToken, setAccessToken } from '@/utils/storage'
import type { User } from '@/types/user'

interface AuthContextValue {
  user: User | null
  isAuthenticated: boolean
  isAdmin: boolean
  initializing: boolean
  login: (email: string, password: string) => Promise<void>
  signup: (name: string, email: string, phoneNumber: string, password: string) => Promise<void>
  refreshUser: () => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [initializing, setInitializing] = useState(true)

  useEffect(() => {
    if (!getAccessToken()) {
      setInitializing(false)
      return
    }
    userApi
      .getMe()
      .then(setUser)
      .catch(() => clearAccessToken())
      .finally(() => setInitializing(false))
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const { accessToken } = await authApi.login({ email, password })
    setAccessToken(accessToken)
    // 로그인 응답에는 이름/이메일이 없어 내 정보를 따로 조회한다.
    const me = await userApi.getMe()
    setUser(me)
  }, [])

  const signup = useCallback(
    async (name: string, email: string, phoneNumber: string, password: string) => {
      // 회원가입 응답은 토큰을 주지 않는다. 가입 후 바로 로그인까지 이어서 처리한다.
      await authApi.signup({ name, email, phoneNumber, password })
      await login(email, password)
    },
    [login],
  )

  const refreshUser = useCallback(async () => {
    const me = await userApi.getMe()
    setUser(me)
  }, [])

  const logout = useCallback(() => {
    clearAccessToken()
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider
      value={{ user, isAuthenticated: !!user, isAdmin: user?.role === 'ADMIN', initializing, login, signup, refreshUser, logout }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth는 AuthProvider 내부에서만 사용할 수 있습니다.')
  return ctx
}
