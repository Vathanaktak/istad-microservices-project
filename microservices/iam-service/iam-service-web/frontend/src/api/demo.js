// In-memory stand-in for REST endpoints that iam-service does not expose yet.
// Used ONLY when the real server answers 404/405 for the path (see http.js) and VITE_DEMO_FALLBACK=true.
// The shapes here are the contract documented in docs/API_CONTRACT.md.
const now = Date.now();
const ago = (min) => new Date(now - min * 60000).toISOString();
const uid = () => (crypto.randomUUID ? crypto.randomUUID() : String(Math.random()).slice(2));
const pageOf = (all, query = {}) => {
  const size = Math.max(1, parseInt(query.size || 10, 10));
  const number = Math.max(0, parseInt(query.page || 0, 10));
  return {
    content: all.slice(number * size, number * size + size),
    page: { number, size, totalElements: all.length, totalPages: Math.max(1, Math.ceil(all.length / size)) },
  };
};
const matches = (value, q) => !q || String(value ?? '').toLowerCase().includes(String(q).toLowerCase());

// ---- sessions (per user id; "me" is just another key) -------------------------------------------
const DEVICES = [
  ['MacBook Pro', 'Chrome 131', 'macOS 14', '203.144.80.12', 'Phnom Penh, KH'],
  ['iPhone 15', 'Safari 18', 'iOS 18', '175.100.32.7', 'Phnom Penh, KH'],
  ['Windows PC', 'Edge 130', 'Windows 11', '116.212.140.9', 'Siem Reap, KH'],
  ['Pixel 8', 'Chrome Mobile 131', 'Android 15', '103.5.140.21', 'Battambang, KH'],
];
const sessionStore = {};
const sessionsFor = (key) => (sessionStore[key] ||= DEVICES.slice(0, key === 'me' ? 4 : 2).map(([device, browser, os, ip, loc], i) => ({
  id: uid(), current: key === 'me' && i === 0, device, browser, os, ipAddress: ip, location: loc,
  createdAt: ago(60 * 24 * (i + 1)), lastActiveAt: ago(i === 0 ? 1 : 25 * (i + 1)),
})));

// ---- login history ---------------------------------------------------------------------------
const mkLogins = (n, withUser) => Array.from({ length: n }, (_, i) => {
  const failed = i % 4 === 1 || i % 9 === 0;
  const [device, browser, os, ip, loc] = DEVICES[i % DEVICES.length];
  const foreign = i % 9 === 0;
  return {
    id: uid(), status: failed ? 'FAILED' : 'SUCCESS',
    reason: failed ? (i % 2 ? 'BAD_CREDENTIALS' : 'ACCOUNT_LOCKED') : null,
    ipAddress: foreign ? '185.220.101.4' : ip, location: foreign ? 'Unknown (Tor exit node)' : loc,
    device, browser, os, suspicious: foreign || (failed && i % 3 === 0),
    ...(withUser ? { username: ['admin', 'sokha.chan', 'dara.ly', 'vanna.kim'][i % 4] } : {}),
    occurredAt: ago(35 * (i + 1)),
  };
});
const myLogins = mkLogins(24, false);
const allLogins = mkLogins(60, true);

// ---- activity -----------------------------------------------------------------------------------
const ACT = [
  ['PASSWORD_CHANGED', 'Password changed'], ['PROFILE_UPDATED', 'Profile details updated'],
  ['IDENTITY_LINKED', 'Linked GitHub account'], ['SESSION_TERMINATED', 'Signed out another device'],
  ['EMAIL_VERIFIED', 'Email address verified'], ['LOGIN_SUCCESS', 'Signed in'],
  ['IDENTITY_UNLINKED', 'Unlinked Facebook account'], ['PROFILE_PICTURE_UPDATED', 'Profile picture updated'],
];
const myActivity = Array.from({ length: 18 }, (_, i) => ({
  id: uid(), type: ACT[i % ACT.length][0], description: ACT[i % ACT.length][1],
  ipAddress: DEVICES[i % DEVICES.length][3], occurredAt: ago(90 * (i + 1)),
}));
let deactivation = { status: 'NONE' };

// ---- audit log -----------------------------------------------------------------------------------
const AUD = [
  ['AUTH', 'LOGIN_SUCCESS'], ['AUTH', 'LOGIN_FAILED'], ['USER', 'USER_CREATED'], ['USER', 'USER_LOCKED'],
  ['USER', 'USER_PASSWORD_RESET'], ['ROLE', 'ROLE_ASSIGNED'], ['ROLE', 'ROLE_REVOKED'],
  ['PERMISSION', 'PERMISSION_GRANTED'], ['PERMISSION', 'PERMISSION_UPDATED'], ['CLIENT', 'CLIENT_REGISTERED'], ['SYSTEM', 'CONFIG_UPDATED'],
];
const auditLogs = Array.from({ length: 80 }, (_, i) => {
  const [category, action] = AUD[i % AUD.length];
  const failed = action === 'LOGIN_FAILED';
  return {
    id: uid(), category, action, outcome: failed ? 'FAILURE' : 'SUCCESS',
    actor: ['admin', 'sokha.chan', 'system', 'dara.ly'][i % 4],
    target: ['sokha.chan', 'ROLE_MANAGER', 'user:read', 'admin-portal', 'vanna.kim'][i % 5],
    ipAddress: DEVICES[i % DEVICES.length][3], detail: failed ? 'Bad credentials' : null, occurredAt: ago(22 * (i + 1)),
  };
});

// ---- OAuth2 clients ----------------------------------------------------------------------------
let clients = [
  { id: uid(), clientId: 'iam-web', clientName: 'IAM Web Console', authenticationMethods: ['none'],
    grantTypes: ['authorization_code', 'refresh_token'], redirectUris: ['http://localhost:9000/auth/callback', 'http://localhost:5173/auth/callback'],
    postLogoutRedirectUris: [], scopes: ['openid', 'profile', 'email', 'phone', 'address'], requireProofKey: true,
    requireAuthorizationConsent: false, accessTokenMinutes: 30, refreshTokenHours: 168, createdAt: ago(60 * 24 * 40) },
  { id: uid(), clientId: 'student-portal', clientName: 'Student Portal', authenticationMethods: ['client_secret_basic'],
    grantTypes: ['authorization_code', 'refresh_token'], redirectUris: ['https://portal.istad.co/callback'], postLogoutRedirectUris: ['https://portal.istad.co'],
    scopes: ['openid', 'profile', 'email'], requireProofKey: true, requireAuthorizationConsent: true, accessTokenMinutes: 15, refreshTokenHours: 24, createdAt: ago(60 * 24 * 12) },
  { id: uid(), clientId: 'billing-service', clientName: 'Billing Service', authenticationMethods: ['client_secret_basic'],
    grantTypes: ['client_credentials'], redirectUris: [], postLogoutRedirectUris: [], scopes: ['client.read'], requireProofKey: false,
    requireAuthorizationConsent: false, accessTokenMinutes: 60, refreshTokenHours: 0, createdAt: ago(60 * 24 * 3) },
];
const publicClient = (c) => { const { clientSecret, ...rest } = c; return rest; };
const secret = () => `${uid().replace(/-/g, '')}${uid().replace(/-/g, '')}`;

// ---- system configuration --------------------------------------------------------------------
let config = {
  authentication: { allowRegistration: true, allowSocialLogin: true, maxFailedAttempts: 5, lockoutMinutes: 15, requireVerifiedEmail: false },
  password: { minLength: 8, requireUppercase: true, requireLowercase: true, requireDigit: true, requireSymbol: true, expireDays: 0, historyCount: 3 },
  session: { idleTimeoutMinutes: 30, absoluteTimeoutHours: 12, maxConcurrentSessions: 5 },
  token: { accessTokenMinutes: 30, refreshTokenHours: 168, idTokenMinutes: 30, authorizationCodeMinutes: 5 },
};

const ok = (result) => ({ result });
const ID = '([^/]+)';
const routes = [
  // account -----------------------------------------------------------------------------------
  ['GET', '^/api/me/sessions$', () => ok(sessionsFor('me'))],
  ['DELETE', '^/api/me/sessions$', () => { sessionStore.me = []; return ok(null); }],
  ['DELETE', `^/api/me/sessions/${ID}$`, (m) => { sessionStore.me = sessionsFor('me').filter((s) => s.id !== m[1]); return ok(null); }],
  ['GET', '^/api/me/login-history$', (m, { query }) =>
    ok(pageOf(myLogins.filter((l) => !query.status || l.status === query.status), query))],
  ['GET', '^/api/me/activity$', (m, { query }) => ok(pageOf(myActivity, query))],
  ['GET', '^/api/me/deactivation-request$', () => ok(deactivation)],
  ['POST', '^/api/me/deactivation-request$', () => { deactivation = { status: 'PENDING', requestedAt: new Date().toISOString() }; return ok(deactivation); }],
  ['DELETE', '^/api/me/deactivation-request$', () => { deactivation = { status: 'NONE' }; return ok(null); }],
  ['POST', '^/api/auth/forgot-password$', () => ok(null)],
  ['POST', '^/api/auth/reset-password$', () => ok(null)],
  // admin: per user -----------------------------------------------------------------------------
  ['GET', `^/api/users/${ID}/sessions$`, (m) => ok(sessionsFor(m[1]))],
  ['DELETE', `^/api/users/${ID}/sessions$`, (m) => { sessionStore[m[1]] = []; return ok(null); }],
  ['DELETE', `^/api/users/${ID}/sessions/${ID}$`, (m) => { sessionStore[m[1]] = sessionsFor(m[1]).filter((s) => s.id !== m[2]); return ok(null); }],
  ['GET', `^/api/users/${ID}/login-history$`, (m, { query }) =>
    ok(pageOf(mkLogins(15, false).filter((l) => !query.status || l.status === query.status), query))],
  // admin: monitoring -------------------------------------------------------------------------
  ['GET', '^/api/login-history$', (m, { query }) => ok(pageOf(allLogins.filter((l) =>
    (!query.status || l.status === query.status) && (query.suspicious !== 'true' || l.suspicious) &&
    (matches(l.username, query.q) || matches(l.ipAddress, query.q))), query))],
  ['GET', '^/api/audit-logs$', (m, { query }) => ok(pageOf(auditLogs.filter((a) =>
    (!query.category || a.category === query.category) && (!query.outcome || a.outcome === query.outcome) &&
    (!query.from || a.occurredAt >= query.from) && (!query.to || a.occurredAt <= `${query.to}T23:59:59.999Z`) &&
    (matches(a.actor, query.q) || matches(a.target, query.q) || matches(a.action, query.q))), query))],
  // clients -------------------------------------------------------------------------------------
  ['GET', '^/api/clients$', (m, { query }) => ok(pageOf(clients.filter((c) => matches(c.clientId, query.q) || matches(c.clientName, query.q)).map(publicClient), query))],
  ['POST', '^/api/clients$', (m, { body }) => {
    const c = { ...body, id: uid(), createdAt: new Date().toISOString() };
    const plain = body.authenticationMethods?.includes('none') ? null : secret();
    clients = [c, ...clients];
    return ok({ ...publicClient(c), ...(plain ? { clientSecret: plain } : {}) });
  }],
  ['GET', `^/api/clients/${ID}$`, (m) => ok(publicClient(clients.find((c) => c.id === m[1])))],
  ['PUT', `^/api/clients/${ID}$`, (m, { body }) => { clients = clients.map((c) => (c.id === m[1] ? { ...c, ...body, id: c.id } : c)); return ok(publicClient(clients.find((c) => c.id === m[1]))); }],
  ['DELETE', `^/api/clients/${ID}$`, (m) => { clients = clients.filter((c) => c.id !== m[1]); return ok(null); }],
  ['POST', `^/api/clients/${ID}/secret$`, () => ok({ clientSecret: secret() })],
  // system configuration ------------------------------------------------------------------------
  ['GET', '^/api/system/config$', () => ok(config)],
  ['PUT', '^/api/system/config$', (m, { body }) => { config = body; return ok(config); }],
];

export function matchDemo(method, path, ctx) {
  for (const [verb, pattern, handler] of routes) {
    if (verb !== method) continue;
    const m = path.match(new RegExp(pattern));
    if (m) return handler(m, { body: ctx.body, query: ctx.query || {} });
  }
  return null;
}
