import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// In dev every backend path is proxied so the browser sees ONE origin (localhost:5173):
// the IAM session cookie, CSRF cookie and OAuth2 redirects behave exactly as in production.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const target = env.IAM_BACKEND_URL || 'http://localhost:9000';
  const proxied = ['/api', '/oauth2', '/connect', '/userinfo', '/.well-known', '/logout', '/consent-info', '/login/oauth2', '/login/telegram'];
  const proxy = Object.fromEntries(proxied.map((p) => [p, { target, changeOrigin: false, xfwd: true }]));
  // POST /login (form login) is proxied, GET /login stays with the SPA
  proxy['/login'] = {
    target, changeOrigin: false, xfwd: true,
    bypass: (req) => (req.method === 'GET' || req.method === 'HEAD' ? '/index.html' : undefined),
  };
  return { plugins: [react()], server: { port: 5173, proxy }, build: { outDir: 'dist', sourcemap: false } };
});
