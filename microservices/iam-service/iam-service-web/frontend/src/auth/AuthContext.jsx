import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { CLIENT_ID, SCOPES, redirectUri } from '../config';
import { challengeFor, decodeJwt, randomString } from './pkce';
import { setAuthBridge } from '../api/http';
import { me } from '../api/iam';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

const TOKENS = 'iam.tokens';
const FLOW = 'iam.flow';
const readTokens = () => { try { return JSON.parse(sessionStorage.getItem(TOKENS)); } catch { return null; } };
const readCookie = (name) => document.cookie.split('; ').find((c) => c.startsWith(`${name}=`))?.split('=')[1];

async function tokenRequest(params) {
  const res = await fetch('/oauth2/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded', Accept: 'application/json' },
    body: new URLSearchParams({ client_id: CLIENT_ID, ...params }),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error_description || data.error || 'token_error');
  return { ...data, expires_at: Date.now() + (data.expires_in || 300) * 1000 };
}

export function AuthProvider({ children }) {
  const [tokens, setTokens] = useState(readTokens);
  const [profile, setProfile] = useState(null); // OIDC claims from /userinfo
  const tokensRef = useRef(tokens);
  const refreshing = useRef(null);

  const store = useCallback((t) => {
    tokensRef.current = t;
    setTokens(t);
    if (t) sessionStorage.setItem(TOKENS, JSON.stringify(t)); else sessionStorage.removeItem(TOKENS);
  }, []);

  /** Starts Authorization Code + PKCE. The IAM server shows our /login page (it owns the session cookie). */
  const login = useCallback(async (returnTo = '/') => {
    const verifier = randomString(48);
    const state = randomString(16);
    sessionStorage.setItem(FLOW, JSON.stringify({ verifier, state, returnTo }));
    const q = new URLSearchParams({
      response_type: 'code', client_id: CLIENT_ID, redirect_uri: redirectUri(), scope: SCOPES,
      state, code_challenge: await challengeFor(verifier), code_challenge_method: 'S256',
    });
    window.location.assign(`/oauth2/authorize?${q}`);
  }, []);

  const completeLogin = useCallback(async (params) => {
    const flow = JSON.parse(sessionStorage.getItem(FLOW) || 'null');
    sessionStorage.removeItem(FLOW);
    if (params.get('error')) throw new Error(params.get('error_description') || params.get('error'));
    if (!flow || flow.state !== params.get('state')) throw new Error('state_mismatch');
    const t = await tokenRequest({
      grant_type: 'authorization_code', code: params.get('code'), redirect_uri: redirectUri(), code_verifier: flow.verifier,
    });
    store(t);
    return flow.returnTo || '/';
  }, [store]);

  const refresh = useCallback(async () => {
    const current = tokensRef.current;
    if (!current?.refresh_token) throw new Error('no_refresh_token');
    refreshing.current ||= tokenRequest({ grant_type: 'refresh_token', refresh_token: current.refresh_token })
      .then((t) => { const merged = { refresh_token: current.refresh_token, ...t }; store(merged); return merged; })
      .finally(() => { refreshing.current = null; });
    return refreshing.current;
  }, [store]);

  const getToken = useCallback(async () => {
    let t = tokensRef.current;
    if (!t) return null;
    if (t.expires_at - Date.now() < 30000) {
      try { t = await refresh(); } catch { store(null); return null; }
    }
    return t.access_token;
  }, [refresh, store]);

  /** Local sign-out + end the IAM browser session. */
  const logout = useCallback(() => {
    sessionStorage.removeItem(TOKENS);
    sessionStorage.removeItem(FLOW);
    const form = document.createElement('form');
    form.method = 'POST'; form.action = '/logout';
    const csrf = readCookie('XSRF-TOKEN');
    if (csrf) {
      const input = document.createElement('input');
      input.type = 'hidden'; input.name = '_csrf'; input.value = decodeURIComponent(csrf);
      form.appendChild(input);
    }
    document.body.appendChild(form);
    form.submit();
  }, []);

  useEffect(() => {
    setAuthBridge({ getToken, onUnauthorized: () => { store(null); } });
  }, [getToken, store]);

  // Load OIDC claims (phone, picture, nickname… are not part of /api/me)
  useEffect(() => {
    if (!tokens) { setProfile(null); return; }
    let live = true;
    me.userinfo().then((p) => live && setProfile(p)).catch(() => live && setProfile({}));
    return () => { live = false; };
  }, [tokens?.access_token]); // eslint-disable-line react-hooks/exhaustive-deps

  const value = useMemo(() => {
    const claims = tokens ? decodeJwt(tokens.access_token) : {};
    const roles = claims.roles || [];
    const perms = new Set(claims.permissions || []);
    const isAdmin = roles.includes('ADMIN');
    return {
      ready: true,
      authenticated: !!tokens,
      userId: claims.sub,
      roles, permissions: [...perms], isAdmin,
      can: (a) => isAdmin || perms.has(a),
      canAny: (list) => isAdmin || list.some((a) => perms.has(a)),
      profile, setProfile,
      login, logout, completeLogin,
    };
  }, [tokens, profile, login, logout, completeLogin]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
