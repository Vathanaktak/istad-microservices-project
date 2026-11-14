import { useCallback, useEffect, useRef, useState } from 'react';

/** Loads data on mount / when deps change. `reload()` refetches without clearing what is on screen. */
export function useData(fn, deps = []) {
  const [state, setState] = useState({ data: null, loading: true, error: null });
  const fnRef = useRef(fn);
  fnRef.current = fn;
  const seq = useRef(0);

  const run = useCallback(async (silent) => {
    const id = ++seq.current;
    if (!silent) setState((s) => ({ ...s, loading: true, error: null }));
    try {
      const data = await fnRef.current();
      if (id === seq.current) setState({ data, loading: false, error: null });
    } catch (error) {
      if (id === seq.current) setState((s) => ({ data: silent ? s.data : null, loading: false, error }));
    }
  }, []);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { run(false); }, deps);
  return { ...state, reload: () => run(true), setData: (data) => setState((s) => ({ ...s, data })) };
}

export function useDebounced(value, ms = 350) {
  const [v, setV] = useState(value);
  useEffect(() => { const h = setTimeout(() => setV(value), ms); return () => clearTimeout(h); }, [value, ms]);
  return v;
}

/** Wraps a mutation: tracks `busy`, shows toast on success/error. */
export function useAction(toast, t, describeError) {
  const [busy, setBusy] = useState(false);
  const run = useCallback(async (fn, successKey) => {
    setBusy(true);
    try {
      const out = await fn();
      if (successKey) toast.success(t(successKey));
      return { ok: true, data: out };
    } catch (e) {
      toast.error(describeError(e, t));
      return { ok: false, error: e };
    } finally { setBusy(false); }
  }, [toast, t, describeError]);
  return [run, busy];
}
