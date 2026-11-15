export const CLIENT_ID = import.meta.env.VITE_CLIENT_ID || 'iam-web';
export const API_BASE = import.meta.env.VITE_API_BASE || '';
export const DEMO_FALLBACK = (import.meta.env.VITE_DEMO_FALLBACK ?? 'false') === 'true';
export const SCOPES = 'openid profile email phone address';
export const redirectUri = () => `${window.location.origin}/auth/callback`;
export const SOCIAL_PROVIDERS = ['google', 'github', 'facebook'];
