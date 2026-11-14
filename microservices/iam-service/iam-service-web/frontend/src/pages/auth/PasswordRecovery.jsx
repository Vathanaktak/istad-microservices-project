import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { MailCheck } from 'lucide-react';
import AuthLayout from '../../components/AuthLayout';
import { Button, TextField } from '../../components/ui';
import { describeError, useI18n } from '../../i18n';
import { publicApi } from '../../api/iam';
import { PASSWORD_HINT, validatePassword } from '../../components/validation';

export function ForgotPassword() {
  const { t } = useI18n();
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true); setError('');
    try { await publicApi.forgotPassword({ email }); setSent(true); }
    catch (err) { setError(describeError(err, t)); }
    finally { setBusy(false); }
  };

  return (
    <AuthLayout title={t('forgot.title')} subtitle={t('forgot.subtitle')}>
      {sent ? (
        <div className="note note-ok"><MailCheck size={18} />{t('forgot.sent')}</div>
      ) : (
        <form onSubmit={submit} className="stack">
          {error && <p className="note note-error" role="alert">{error}</p>}
          <TextField label={t('field.email')} type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" required autoFocus />
          <Button variant="primary" type="submit" loading={busy} className="block">{t('forgot.submit')}</Button>
        </form>
      )}
      <p className="auth-foot"><Link to="/login" className="link">{t('common.backToSignIn')}</Link></p>
    </AuthLayout>
  );
}

export function ResetPassword() {
  const { t } = useI18n();
  const nav = useNavigate();
  const [params] = useSearchParams();
  const token = params.get('token') || '';
  const [f, setF] = useState({ newPassword: '', confirmPassword: '' });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    const local = {};
    if (!validatePassword(f.newPassword)) local.newPassword = t(PASSWORD_HINT);
    if (f.newPassword !== f.confirmPassword) local.confirmPassword = t('validation.passwordMismatch');
    setErrors(local); setError('');
    if (Object.keys(local).length) return;
    setBusy(true);
    try { await publicApi.resetPassword({ token, ...f }); nav('/login?reset'); }
    catch (err) { setErrors(err.fieldErrors || {}); setError(err.status === 400 || err.status === 422 ? t('reset.invalid') : describeError(err, t)); }
    finally { setBusy(false); }
  };

  return (
    <AuthLayout title={t('reset.title')} subtitle={t('reset.subtitle')}>
      {!token && <p className="note note-error" role="alert">{t('reset.missingToken')}</p>}
      {error && <p className="note note-error" role="alert">{error}</p>}
      <form onSubmit={submit} className="stack" noValidate>
        <TextField label={t('field.newPassword')} type="password" value={f.newPassword} error={errors.newPassword} hint={t(PASSWORD_HINT)}
          onChange={(e) => setF({ ...f, newPassword: e.target.value })} autoComplete="new-password" required autoFocus />
        <TextField label={t('field.confirmPassword')} type="password" value={f.confirmPassword} error={errors.confirmPassword}
          onChange={(e) => setF({ ...f, confirmPassword: e.target.value })} autoComplete="new-password" required />
        <Button variant="primary" type="submit" loading={busy} disabled={!token} className="block">{t('reset.submit')}</Button>
      </form>
      <p className="auth-foot"><Link to="/login" className="link">{t('common.backToSignIn')}</Link></p>
    </AuthLayout>
  );
}
