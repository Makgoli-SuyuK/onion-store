import { useCallback, useEffect, useState } from 'react'
import { resolveErrorMessage } from '@/utils/errorMessage'

interface UseApiRequestResult<T> {
  data: T | null
  loading: boolean
  error: string | null
  refetch: () => void
}

// 목록/상세 조회처럼 "로딩 -> 성공/빈 결과/에러"로 끝나는 GET성 요청을 위한 공통 훅
export function useApiRequest<T>(fetcher: () => Promise<T>, deps: unknown[]): UseApiRequestResult<T> {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  const run = useCallback(() => {
    let cancelled = false
    setLoading(true)
    setError(null)
    fetcher()
      .then((result) => {
        if (!cancelled) setData(result)
      })
      .catch((err) => {
        if (!cancelled) setError((err as Error).message || resolveErrorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, reloadKey])

  useEffect(() => run(), [run])

  const refetch = useCallback(() => setReloadKey((k) => k + 1), [])

  return { data, loading, error, refetch }
}
