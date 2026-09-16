import { http, unwrap, USE_MOCK } from './client'
import { mockGetMe } from '@/mocks/authMock'
import type { User } from '@/types/user'

export const userApi = {
  async getMe(): Promise<User> {
    if (USE_MOCK) return mockGetMe()
    const res = await http.get('/api/users/me')
    return unwrap(res)
  },
}
