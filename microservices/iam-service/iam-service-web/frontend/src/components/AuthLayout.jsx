import { Brand, LangSwitch } from './Shell';
import { useI18n } from '../i18n';

// Decorative role × permission matrix: the whole product in one picture (who can do what).
const COLS = ['user:read', 'user:update', 'role:assign', 'client:create', 'audit:read'];
const ROWS = [
  ['ADMIN', [1, 1, 1, 1, 1]],
  ['MANAGER', [1, 1, 1, 0, 1]],
  ['TEACHER', [1, 0, 0, 0, 0]],
  ['STUDENT', [1, 0, 0, 0, 0]],
  ['GUEST', [0, 0, 0, 0, 0]],
];

function Matrix() {
  return (
    <div className="matrix" aria-hidden="true">
      <div className="matrix-row matrix-head"><span />{COLS.map((c) => <span key={c}>{c}</span>)}</div>
      {ROWS.map(([role, cells]) => (
        <div className="matrix-row" key={role}>
          <span className="matrix-role">{role}</span>
          {cells.map((on, i) => <span key={i} className={`cell ${on ? 'on' : ''}`} />)}
        </div>
      ))}
    </div>
  );
}

export default function AuthLayout({ title, subtitle, children, wide }) {
  const { t } = useI18n();
  return (
    <div className="auth">
      <aside className="auth-aside">
        <Brand light />
        <div>
          <h2>{t('auth.tagline')}</h2>
          <p>{t('auth.taglineSub')}</p>
          <Matrix />
        </div>
        <small>© ISTAD · Institute of Science and Technology Advanced Development</small>
      </aside>
      <main className="auth-main">
        <div className="auth-top"><LangSwitch /></div>
        <div className={`auth-card ${wide ? 'wide' : ''}`}>
          <div className="auth-mobile-brand"><Brand /></div>
          <h1>{title}</h1>
          {subtitle && <p className="auth-sub">{subtitle}</p>}
          {children}
        </div>
      </main>
    </div>
  );
}
