import { useCallback, useEffect, useRef, useState } from 'react'

type RequestState<T> = {
  requestKey?: string
  data?: T
  error?: string
}

export function useRequest<T>(fetcher: () => Promise<T>, key: string) {
  const fetcherRef = useRef(fetcher)
  const [version, setVersion] = useState(0)
  const [state, setState] = useState<RequestState<T>>({})
  const requestKey = `${key}#${version}`

  useEffect(() => {
    fetcherRef.current = fetcher
  })

  useEffect(() => {
    let active = true

    fetcherRef
      .current()
      .then((data) => active && setState({ requestKey, data }))
      .catch((err: Error) => active && setState({ requestKey, error: err.message }))

    return () => {
      active = false
    }
  }, [requestKey])

  const reload = useCallback(() => setVersion((v) => v + 1), [])
  const setData = useCallback((data: T) => setState({ requestKey, data }), [requestKey])

  const sameKey = state.requestKey?.startsWith(`${key}#`) ?? false
  const current = state.requestKey === requestKey

  return {
    data: sameKey ? state.data : undefined,
    error: current ? state.error : undefined,
    loading: !current,
    reload,
    setData,
  }
}
