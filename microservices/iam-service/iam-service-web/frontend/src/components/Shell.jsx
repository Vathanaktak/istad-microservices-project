import { useEffect, useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import {
  Activity, Building2, FlaskConical, History, KeyRound, LayoutDashboard, Link2, LockKeyhole, LogOut, Menu,
  MonitorSmartphone, ScrollText, Settings2, ShieldCheck, ShieldHalf, UserRound, Users, X, Ban,
} from 'lucide-react';
import { useAuth } from '../auth/AuthContext';
import { useI18n } from '../i18n';
import { onDemoUsed } from '../api/http';
import { EmptyState } from './ui';

export function Brand({ light }) {
  return (
    <span className={`brand ${light ? 'brand-light' : ''}`}>
      <svg width="28" height="28" viewBox="0 0 32 32" aria-hidden="true">
        <rect width="32" height="32" rx="7" fill={light ? '#fff' : '#14204f'} />
        <path d="M16 6l8 3v6c0 5.2-3.4 9.2-8 11-4.6-1.8-8-5.8-8-11V9z" fill="none" stroke={light ? '#14204f' : '#fff'} strokeWidth="2" strokeLinejoin="round" />
        <circle cx="16" cy="14" r="2.2" fill="#d9232d" /><path d="M16 16.5v4" stroke="#d9232d" strokeWidth="2" strokeLinecap="round" />
      </svg>
      <span>ISTAD <b>IAM</b></span>
    </span>
  );
}

export function LangSwitch({ light }) {
  const { lang, setLang } = useI18n();
  return (
    <div className={`lang ${light ? 'lang-light' : ''}`} role="group" aria-label="Language">
      <button className={lang === 'en' ? 'on' : ''} onClick={() => setLang('en')} lang="en">EN</button>
      <button className={lang === 'km' ? 'on' : ''} onClick={() => setLang('km')} lang="km">ខ្មែរ</button>
    </div>
  );
}

export function Can({ any, children }) {
  const { canAny } = useAuth();
  const { t } = useI18n();
  if (!canAny(any)) {
    return <EmptyState icon={Ban} title={t('error.forbidden')}>{t('error.forbiddenHint')}</EmptyState>;
  }
  return children;
}

export function Avatar({ src, name, size = 36 }) {
  const [broken, setBroken] = useState(false);
  useEffect(() => setBroken(false), [src]);
  const text = (name || '?').split(/\s+/).map((w) => w[0]).slice(0, 2).join('').toUpperCase();
  return src && !broken
    ? <img className="avatar" style={{ width: size, height: size }} src={src} alt="" onError={() => setBroken(true)} referrerPolicy="no-referrer" />
    : <span className="avatar avatar-fallback" style={{ width: size, height: size, fontSize: size * 0.38 }} aria-hidden="true">{text}</span>;
}

export default function Shell({ children }) {
  const { t } = useI18n();
  const { profile, canAny, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const [demo, setDemo] = useState(false);
  const loc = useLocation();

  useEffect(() => { setOpen(false); setDemo(false); window.scrollTo(0, 0); }, [loc.pathname]);
  useEffect(() => onDemoUsed(() => setDemo(true)), []);

  const groups = [
    { title: t('nav.account'), items: [
      { to: '/account/profile', icon: UserRound, label: t('nav.profile') },
      { to: '/account/security', icon: KeyRound, label: t('nav.security') },
      { to: '/account/linked', icon: Link2, label: t('nav.linked') },
      { to: '/account/sessions', icon: MonitorSmartphone, label: t('nav.sessions') },
      { to: '/account/login-history', icon: History, label: t('nav.loginHistory') },
      { to: '/account/activity', icon: Activity, label: t('nav.activity') },
      { to: '/account/deactivation', icon: Ban, label: t('nav.deactivation') },
    ] },
    { title: t('nav.management'), items: [
      { to: '/admin', end: true, icon: LayoutDashboard, label: t('nav.overview'), any: ['user:read', 'role:read', 'permission:read'] },
      { to: '/admin/users', icon: Users, label: t('nav.users'), any: ['user:read'] },
      { to: '/admin/roles', icon: ShieldHalf, label: t('nav.roles'), any: ['role:read'] },
      { to: '/admin/permissions', icon: LockKeyhole, label: t('nav.permissions'), any: ['permission:read'] },
      { to: '/admin/clients', icon: Building2, label: t('nav.clients'), any: ['client:read'] },
      { to: '/admin/login-history', icon: ShieldCheck, label: t('nav.loginMonitor'), any: ['audit:read', 'user:read'] },
      { to: '/admin/audit-logs', icon: ScrollText, label: t('nav.auditLogs'), any: ['audit:read'] },
      { to: '/admin/system', icon: Settings2, label: t('nav.system'), any: ['system:read'] },
    ].filter((i) => canAny(i.any)) },
  ].filter((g) => g.items.length);

  return (
    <div className="shell">
      <aside className={`rail ${open ? 'open' : ''}`}>
        <div className="rail-top">
          <Brand light />
          <button className="icon-btn rail-close" onClick={() => setOpen(false)} aria-label={t('common.close')}><X size={18} /></button>
        </div>
        <nav aria-label={t('nav.main')}>
          {groups.map((g) => (
            <div className="rail-group" key={g.title}>
              <h3>{g.title}</h3>
              {g.items.map((i) => (
                <NavLink key={i.to} to={i.to} end={i.end} className="rail-link">
                  <i.icon size={17} aria-hidden="true" /><span>{i.label}</span>
                </NavLink>
              ))}
            </div>
          ))}
        </nav>
        <div className="rail-foot">
          <NavLink to="/account/profile" className="who">
            <Avatar src={profile?.picture} name={profile?.name || profile?.preferred_username} />
            <span><b>{profile?.name || profile?.preferred_username || '…'}</b><small>{profile?.email}</small></span>
          </NavLink>
          <div className="rail-foot-actions">
            <LangSwitch light />
            <button className="rail-signout" onClick={logout}><LogOut size={16} />{t('common.signOut')}</button>
          </div>
        </div>
      </aside>
      {open && <div className="scrim" onClick={() => setOpen(false)} />}

      <div className="main">
        <div className="topbar">
          <button className="icon-btn" onClick={() => setOpen(true)} aria-label={t('nav.openMenu')}><Menu size={20} /></button>
          <Brand />
        </div>
        {demo && (
          <div className="demo-banner" role="status">
            <FlaskConical size={16} />
            <span><b>{t('demo.title')}</b> {t('demo.body')}</span>
          </div>
        )}
        <main className="content">{children}</main>
      </div>
    </div>
  );
}
