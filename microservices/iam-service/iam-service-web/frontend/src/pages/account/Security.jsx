import { useState } from 'react';
import { MailCheck } from 'lucide-react';
import { Button, PageHeader, Panel, PasswordMeter, TextField, useToast } from '../../components/ui';
import { describeError, useI18n } from '../../i18n';
import { useAuth } from '../../auth/AuthContext';
import { me, publicApi } from '../../api/iam';
import { PASSWORD_HINT, passwordScore, validatePassword } from '../../components/validation';

function ChangePassword() {
  const { t } = useI18n();
  const toast = useToast();
  const empty = { currentPassword: '', newPassword: '', confirmPassword: '' };
  const [f, setF] = useState(empty);
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setF((s) => ({ ...s, [k]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    const local = {};
    if (!f.currentPassword) local.currentPassword = t('validation.required');
    if (!validatePassword(f.newPassword)) local.newPassword = t(PASSWORD_HINT);
    else if (f.newPassword === f.currentPassword) local.newPassword = t('security.sameAsOld');
    if (f.newPassword !== f.confirmPassword) local.confirmPassword = t('validation.passwordMismatch');
    setErrors(local);
    if (Object.keys(local).length) return;
    setBusy(true);
    try {
      await me.changePassword(f);
      toast.success(t('security.changed'));
      setF(empty);
    } catch (err) {
      if (err.status === 422) setErrors({ currentPassword: t('error.wrongPassword') });
      else { setErrors(err.fieldErrors || {}); toast.error(describeError(err, t)); }
    } finally { setBusy(false); }
  };

  return (
    <form onSubmit={submit} className="stack" noValidate>
      <TextField label={t('field.currentPassword')} type="password" autoComplete="current-password" value={f.currentPassword} onChange={set('currentPassword')} error={errors.currentPassword} />
      <TextField label={t('field.newPassword')} type="password" autoComplete="new-password" value={f.newPassword} onChange={set('newPassword')} error={errors.newPassword} hint={t(PASSWORD_HINT)} />
      <PasswordMeter score={passwordScore(f.newPassword)} />
      <TextField label={t('field.confirmPassword')} type="password" autoComplete="new-password" value={f.confirmPassword} onChange={set('confirmPassword')} error={errors.confirmPassword} />
      <div className="form-actions"><Button variant="primary" type="submit" loading={busy}>{t('security.change')}</Button></div>
    </form>
  );
}

function ResetByEmail() {
  const { t } = useI18n();
  const toast = useToast();
  const { profile } = useAuth();
  const [busy, setBusy] = useState(false);
  const [sent, setSent] = useState(false);
  const send = async () => {
    setBusy(true);
    try { await publicApi.forgotPassword({ email: profile?.email }); setSent(true); }
    catch (err) { toast.error(describeError(err, t)); }
    finally { setBusy(false); }
  };
  return sent
    ? <p className="note note-ok"><MailCheck size={18} />{t('forgot.sent')}</p>
    : (
      <div className="stack">
        <p>{t('security.resetBody', { email: profile?.email || '' })}</p>
        <div><Button onClick={send} loading={busy} disabled={!profile?.email}>{t('security.sendReset')}</Button></div>
      </div>
    );
}

export default function Security() {
  const { t } = useI18n();
  return (
    <>
      <PageHeader title={t('security.title')} description={t('security.description')} />
      <div className="cols">
        <Panel title={t('security.changeTitle')}><ChangePassword /></Panel>
        <Panel title={t('security.resetTitle')} description={t('security.resetHint')}><ResetByEmail /></Panel>
      </div>
    </>
  );
}
