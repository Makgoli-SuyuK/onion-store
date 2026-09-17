import { http, unwrap } from './client'
import type { LoginRequest, LoginResponse, SignupRequest } from '@/types/user'

export const authApi = {
  // 회원가입은 로그인 토큰을 돌려주지 않는다. 가입 후 login()을 따로 호출해야 한다.
  async signup(req: SignupRequest): Promise<void> {
    await http.post('/api/auth/signup', req)
  },

  async login(req: LoginRequest): Promise<LoginResponse> {
    const res = await http.post('/api/auth/login', req)
    return unwrap(res)
  },
}
