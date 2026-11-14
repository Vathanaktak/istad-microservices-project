import { API_BASE, DEMO_FALLBACK } from '../config';
import { matchDemo } from './demo';

let bridge = { getToken: async () => null, onUnauthorized: () => {} };
export const setAuthBridge = (b) => { bridge = b; };

const demoListeners = new Set();
export const onDemoUsed = (fn) => { demoListeners.add(fn); return () => demoListeners.delete(fn); };

export class ApiError extends Error {
  constructor(status, problem = {}) {
    super(problem.detail || problem.title || `HTTP ${status}`);
    this.status = status;
    this.problem = problem;
    this.fieldErrors = Object.fromEntries((problem.errors || []).map((e) => [e.field, e.message]));
    this.code = (problem.type || '').split('/').pop();
  }
}

const qs = (query) => {
  const p = new URLSearchParams();
  Object.entries(query || {}).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') p.append(k, v); });
  const s = p.toString();
  return s ? `?${s}` : '';
};

// A 404/405 that did not come from our domain code means "this endpoint is not implemented on the server yet".
const isMissingEndpoint = (status, problem) =>
  (status === 404 || status === 405 || status === 501) && (!problem?.type || problem.type === 'about:blank');

export async function api(method, path, { body, query, auth = true } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (auth) {
    const token = await bridge.getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }
  const res = await fetch(`${API_BASE}${path}${qs(query)}`, {
    method, headers, body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (res.status === 204) return null;
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = null; }
  if (res.ok) return data;

  if (DEMO_FALLBACK && isMissingEndpoint(res.status, data)) {
    const demo = matchDemo(method, path, { body, query });
    if (demo) {
      demoListeners.forEach((fn) => fn(path));
      return demo.result;
    }
  }
  if (res.status === 401 && auth) bridge.onUnauthorized();
  throw new ApiError(res.status, data || {});
}

export const get = (p, o) => api('GET', p, o);
export const post = (p, body, o) => api('POST', p, { ...o, body: body ?? {} });
export const patch = (p, body, o) => api('PATCH', p, { ...o, body });
export const put = (p, body, o) => api('PUT', p, { ...o, body });
export const del = (p, o) => api('DELETE', p, o);
