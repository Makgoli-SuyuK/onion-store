import { http, unwrap } from './client'
import type { User } from '@/types/user'

export const userApi = {
  async getMe(): Promise<User> {
    const res = await http.get('/api/users/me')
    return unwrap(res)
  },
}
