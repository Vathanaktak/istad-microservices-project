import { useEffect } from 'react';
import { Navigate, Outlet, Route, Routes, useLocation } from 'react-router-dom';
import { useAuth } from './auth/AuthContext';
import { useI18n } from './i18n';
import { Spinner } from './components/ui';
import Shell, { Can } from './components/Shell';

import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import { ForgotPassword, ResetPassword } from './pages/auth/PasswordRecovery';
import Consent from './pages/auth/Consent';
import Callback from './pages/auth/Callback';

import Profile from './pages/account/Profile';
import Security from './pages/account/Security';
import LinkedAccounts from './pages/account/LinkedAccounts';
import Sessions from './pages/account/Sessions';
import LoginHistory from './pages/account/LoginHistory';
import Activity from './pages/account/Activity';
import Deactivation from './pages/account/Deactivation';

import Overview from './pages/admin/Overview';
import Users from './pages/admin/Users';
import UserDetail from './pages/admin/UserDetail';
import Roles from './pages/admin/Roles';
import Permissions from './pages/admin/Permissions';
import Clients from './pages/admin/Clients';
import LoginMonitor from './pages/admin/LoginMonitor';
import AuditLogs from './pages/admin/AuditLogs';
import SystemConfig from './pages/admin/SystemConfig';

/** Sends anonymous visitors through the OAuth2 Authorization Code + PKCE flow, then renders the console. */
function Protected() {
  const { authenticated, login } = useAuth();
  const { t } = useI18n();
  const loc = useLocation();
  useEffect(() => {
    if (!authenticated) login(loc.pathname + loc.search);
  }, [authenticated]); // eslint-disable-line react-hooks/exhaustive-deps
  if (!authenticated) return <div className="boot"><Spinner label={t('auth.redirecting')} /></div>;
  return <Shell><Outlet /></Shell>;
}

/** After linking a social account the IAM server lands on "/"; go back to where the user started. */
function Home() {
  const to = sessionStorage.getItem('iam.returnTo');
  sessionStorage.removeItem('iam.returnTo');
  return <Navigate to={to || '/account/profile'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/oauth2/consent" element={<Consent />} />
      <Route path="/auth/callback" element={<Callback />} />

      <Route element={<Protected />}>
        <Route index element={<Home />} />
        <Route path="/account" element={<Navigate to="/account/profile" replace />} />
        <Route path="/account/profile" element={<Profile />} />
        <Route path="/account/security" element={<Security />} />
        <Route path="/account/linked" element={<LinkedAccounts />} />
        <Route path="/account/sessions" element={<Sessions />} />
        <Route path="/account/login-history" element={<LoginHistory />} />
        <Route path="/account/activity" element={<Activity />} />
        <Route path="/account/deactivation" element={<Deactivation />} />

        <Route path="/admin" element={<Can any={['user:read', 'role:read', 'permission:read']}><Overview /></Can>} />
        <Route path="/admin/users" element={<Can any={['user:read']}><Users /></Can>} />
        <Route path="/admin/users/:id" element={<Can any={['user:read']}><UserDetail /></Can>} />
        <Route path="/admin/roles" element={<Can any={['role:read']}><Roles /></Can>} />
        <Route path="/admin/permissions" element={<Can any={['permission:read']}><Permissions /></Can>} />
        <Route path="/admin/clients" element={<Can any={['client:read']}><Clients /></Can>} />
        <Route path="/admin/login-history" element={<Can any={['audit:read', 'user:read']}><LoginMonitor /></Can>} />
        <Route path="/admin/audit-logs" element={<Can any={['audit:read']}><AuditLogs /></Can>} />
        <Route path="/admin/system" element={<Can any={['system:read']}><SystemConfig /></Can>} />
        <Route path="*" element={<Navigate to="/account/profile" replace />} />
      </Route>
    </Routes>
  );
}
