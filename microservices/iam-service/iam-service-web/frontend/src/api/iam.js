import { get, post, patch, put, del } from './http';

// ---- self-service (/api/me) ----------------------------------------------------------------
export const me = {
  get: () => get('/api/me'),
  userinfo: () => get('/userinfo'),
  update: (body) => patch('/api/me', body),
  changePassword: (body) => put('/api/me/password', body),
  identities: () => get('/api/me/identities'),
  unlink: (id) => del(`/api/me/identities/${id}`),
  sessions: () => get('/api/me/sessions'),
  terminateSession: (id) => del(`/api/me/sessions/${id}`),
  terminateAllSessions: () => del('/api/me/sessions'),
  loginHistory: (query) => get('/api/me/login-history', { query }),
  activity: (query) => get('/api/me/activity', { query }),
  deactivation: () => get('/api/me/deactivation-request'),
  requestDeactivation: (body) => post('/api/me/deactivation-request', body),
  cancelDeactivation: () => del('/api/me/deactivation-request'),
};

export const publicApi = {
  register: (body) => post('/api/auth/register', body, { auth: false }),
  forgotPassword: (body) => post('/api/auth/forgot-password', body, { auth: false }),
  resetPassword: (body) => post('/api/auth/reset-password', body, { auth: false }),
};

// ---- administration ------------------------------------------------------------------------
export const users = {
  list: (query) => get('/api/users', { query }),
  get: (id) => get(`/api/users/${id}`),
  create: (body) => post('/api/users', body),
  update: (id, body) => patch(`/api/users/${id}`, body),
  remove: (id) => del(`/api/users/${id}`),
  action: (id, name) => post(`/api/users/${id}/${name}`), // enable | disable | lock | unlock
  resetPassword: (id, body) => put(`/api/users/${id}/password`, body),
  authorities: (id) => get(`/api/users/${id}/authorities`),
  assignRoles: (id, body) => post(`/api/users/${id}/roles`, body),
  revokeRole: (id, roleId, reason) => del(`/api/users/${id}/roles/${roleId}`, { query: { reason } }),
  grantPermissions: (id, body) => post(`/api/users/${id}/permissions`, body),
  revokePermission: (id, permId, reason) => del(`/api/users/${id}/permissions/${permId}`, { query: { reason } }),
  identities: (id) => get(`/api/users/${id}/identities`),
  unlinkIdentity: (id, iid) => del(`/api/users/${id}/identities/${iid}`),
  sessions: (id) => get(`/api/users/${id}/sessions`),
  terminateSession: (id, sid) => del(`/api/users/${id}/sessions/${sid}`),
  terminateAllSessions: (id) => del(`/api/users/${id}/sessions`),
  loginHistory: (id, query) => get(`/api/users/${id}/login-history`, { query }),
  deactivationRequest: (id) => get(`/api/users/${id}/deactivation-request`),
};

export const roles = {
  list: (query) => get('/api/roles', { query }),
  get: (id) => get(`/api/roles/${id}`),
  create: (body) => post('/api/roles', body),
  update: (id, body) => patch(`/api/roles/${id}`, body),
  remove: (id) => del(`/api/roles/${id}`),
  action: (id, name) => post(`/api/roles/${id}/${name}`), // activate | deactivate
  assignPermissions: (id, body) => post(`/api/roles/${id}/permissions`, body),
  revokePermission: (id, permId) => del(`/api/roles/${id}/permissions/${permId}`),
};

export const permissions = {
  list: (query) => get('/api/permissions', { query }),
  create: (body) => post('/api/permissions', body),
  update: (id, body) => patch(`/api/permissions/${id}`, body),
  remove: (id) => del(`/api/permissions/${id}`),
  action: (id, name) => post(`/api/permissions/${id}/${name}`),
};

export const clients = {
  list: (query) => get('/api/clients', { query }),
  create: (body) => post('/api/clients', body),
  update: (id, body) => put(`/api/clients/${id}`, body),
  remove: (id) => del(`/api/clients/${id}`),
  regenerateSecret: (id) => post(`/api/clients/${id}/secret`),
};

export const monitoring = {
  loginHistory: (query) => get('/api/login-history', { query }),
  auditLogs: (query) => get('/api/audit-logs', { query }),
};

export const system = {
  getConfig: () => get('/api/system/config'),
  saveConfig: (body) => put('/api/system/config', body),
};

export const deactivationRequests = {
  list: (query) => get('/api/deactivation-requests', { query }),
  countPending: () => get('/api/deactivation-requests/count-pending'),
  approve: (id, body) => post(`/api/deactivation-requests/${id}/approve`, body),
  reject: (id, body) => post(`/api/deactivation-requests/${id}/reject`, body),
};
