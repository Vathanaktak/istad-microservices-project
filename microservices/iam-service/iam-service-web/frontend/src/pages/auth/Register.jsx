import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../../components/AuthLayout';
import { Button, SelectField, TextField } from '../../components/ui';
import { describeError, useI18n } from '../../i18n';
import { publicApi } from '../../api/iam';
import { PASSWORD_HINT, validatePassword } from '../../components/validation';

export default function Register() {
  const { t, lang } = useI18n();
  const nav = useNavigate();
  const [f, setF] = useState({ givenName: '', familyName: '', username: '', email: '', password: '', confirmPassword: '', locale: lang });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const [formError, setFormError] = useState('');
  const set = (k) => (e) => setF((s) => ({ ...s, [k]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    const local = {};
    if (!/^[A-Za-z0-9._-]{3,50}$/.test(f.username)) local.username = t('validation.username');
    if (!/^\S+@\S+\.\S+$/.test(f.email)) local.email = t('validation.email');
    if (!validatePassword(f.password)) local.password = t(PASSWORD_HINT);
    if (f.password !== f.confirmPassword) local.confirmPassword = t('validation.passwordMismatch');
    setErrors(local); setFormError('');
    if (Object.keys(local).length) return;
    setBusy(true);
    try {
      await publicApi.register(f);
      nav('/login?registered');
    } catch (err) {
      setErrors(err.fieldErrors || {});
      setFormError(err.status === 409 ? t('register.exists') : (Object.keys(err.fieldErrors || {}).length ? '' : describeError(err, t)));
    } finally { setBusy(false); }
  };

  return (
    <AuthLayout title={t('register.title')} subtitle={t('register.subtitle')} wide>
      {formError && <p className="note note-error" role="alert">{formError}</p>}
      <form onSubmit={submit} className="stack" noValidate>
        <div className="grid-2">
          <TextField label={t('field.givenName')} value={f.givenName} onChange={set('givenName')} error={errors.givenName} required autoFocus />
          <TextField label={t('field.familyName')} value={f.familyName} onChange={set('familyName')} error={errors.familyName} required />
        </div>
        <div className="grid-2">
          <TextField label={t('field.username')} value={f.username} onChange={set('username')} error={errors.username} autoComplete="username" required />
          <TextField label={t('field.email')} type="email" value={f.email} onChange={set('email')} error={errors.email} autoComplete="email" required />
        </div>
        <div className="grid-2">
          <TextField label={t('field.password')} type="password" value={f.password} onChange={set('password')} error={errors.password} hint={t(PASSWORD_HINT)} autoComplete="new-password" required />
          <TextField label={t('field.confirmPassword')} type="password" value={f.confirmPassword} onChange={set('confirmPassword')} error={errors.confirmPassword} autoComplete="new-password" required />
        </div>
        <SelectField label={t('field.language')} value={f.locale} onChange={set('locale')} options={[{ value: 'en', label: 'English' }, { value: 'km', label: 'ខ្មែរ' }]} />
        <Button variant="primary" type="submit" loading={busy} className="block">{t('register.submit')}</Button>
      </form>
      <p className="auth-foot">{t('register.haveAccount')} <Link to="/login" className="link">{t('login.title')}</Link></p>
    </AuthLayout>
  );
}
