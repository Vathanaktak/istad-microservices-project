import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { CheckCircle2, CircleAlert } from 'lucide-react';
import AuthLayout from '../../components/AuthLayout';
import { Button, TextField } from '../../components/ui';
import { useI18n } from '../../i18n';
import { ensureCsrf } from '../../auth/csrf';
import { SOCIAL_PROVIDERS } from '../../config';

const PROVIDER_NAMES = { google: 'Google', github: 'GitHub', facebook: 'Facebook' };

function Telegram({ bot }) {
  const ref = useRef(null);
  useEffect(() => {
    if (!bot || !ref.current) return;
    const s = document.createElement('script');
    s.async = true; s.src = 'https://telegram.org/js/telegram-widget.js?22';
    s.setAttribute('data-telegram-login', bot); s.setAttribute('data-size', 'large');
    s.setAttribute('data-auth-url', '/login/telegram'); s.setAttribute('data-request-access', 'write');
    ref.current.appendChild(s);
    return () => { if (ref.current) ref.current.innerHTML = ''; };
  }, [bot]);
  return <div ref={ref} className="telegram" />;
}

/**
 * Sign-in screen of the IAM authorization server. A real HTML form POST to Spring Security's /login keeps the
 * server-side session (needed by /oauth2/authorize) while the UI itself is React.
 */
export default function Login() {
  const { t } = useI18n();
  const [params] = useSearchParams();
  const formRef = useRef(null);
  const [busy, setBusy] = useState(false);
  const [bot, setBot] = useState('');

  useEffect(() => {
    ensureCsrf();
    fetch('/web-config').then((r) => r.json()).then((c) => setBot(c.telegramBot || '')).catch(() => {});
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    const token = await ensureCsrf();
    formRef.current.elements._csrf.value = token;
    formRef.current.submit();
  };

  return (
    <AuthLayout title={t('login.title')} subtitle={t('login.subtitle')}>
      {params.has('error') && <p className="note note-error" role="alert"><CircleAlert size={16} />{t('login.error')}</p>}
      {params.has('logout') && <p className="note note-ok"><CheckCircle2 size={16} />{t('login.loggedOut')}</p>}
      {params.has('registered') && <p className="note note-ok"><CheckCircle2 size={16} />{t('login.registered')}</p>}
      {params.has('reset') && <p className="note note-ok"><CheckCircle2 size={16} />{t('login.resetDone')}</p>}

      <form ref={formRef} method="post" action="/login" onSubmit={submit} className="stack">
        <input type="hidden" name="_csrf" defaultValue="" />
        <TextField label={t('field.usernameOrEmail')} name="username" autoComplete="username" required autoFocus />
        <TextField label={t('field.password')} name="password" type="password" autoComplete="current-password" required />
        <div className="between">
          <span />
          <Link to="/forgot-password" className="link">{t('login.forgot')}</Link>
        </div>
        <Button variant="primary" type="submit" loading={busy} className="block">{t('login.submit')}</Button>
      </form>

      <div className="divider"><span>{t('login.or')}</span></div>
      <div className="social">
        {SOCIAL_PROVIDERS.map((p) => (
          <a key={p} className="btn btn-secondary" href={`/oauth2/authorization/${p}`}>{t('login.continueWith', { provider: PROVIDER_NAMES[p] })}</a>
        ))}
        <Telegram bot={bot} />
      </div>
      <p className="auth-foot">{t('login.noAccount')} <Link to="/register" className="link">{t('register.title')}</Link></p>
    </AuthLayout>
  );
}
