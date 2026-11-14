import { useEffect, useState } from 'react';
import { BadgeCheck, Lock } from 'lucide-react';
import { Async, Button, Chips, Panel, PageHeader, SelectField, TextField, useToast } from '../../components/ui';
import { Avatar } from '../../components/Shell';
import { useData } from '../../components/hooks';
import { describeError, useI18n } from '../../i18n';
import { useAuth } from '../../auth/AuthContext';
import { me } from '../../api/iam';
import { PHONE_RE } from '../../components/validation';

function ProfileForm({ user, claims, onSaved }) {
  const { t } = useI18n();
  const toast = useToast();
  const initial = () => ({
    givenName: user.givenName || claims?.given_name || '',
    familyName: user.familyName || claims?.family_name || '',
    nickname: claims?.nickname || '',
    phoneNumber: claims?.phone_number || '',
    pictureUrl: claims?.picture || '',
    locale: user.locale || claims?.locale || 'en',
  });
  const [f, setF] = useState(initial);
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  useEffect(() => setF(initial()), [user, claims]); // eslint-disable-line react-hooks/exhaustive-deps
  const set = (k) => (e) => setF((s) => ({ ...s, [k]: e.target.value }));
  const dirty = JSON.stringify(f) !== JSON.stringify(initial());

  const save = async (e) => {
    e.preventDefault();
    const local = {};
    if (!f.givenName.trim()) local.givenName = t('validation.required');
    if (!f.familyName.trim()) local.familyName = t('validation.required');
    if (f.phoneNumber && !PHONE_RE.test(f.phoneNumber)) local.phoneNumber = t('validation.phone');
    if (f.pictureUrl && !/^https?:\/\/\S+$/i.test(f.pictureUrl)) local.pictureUrl = t('validation.url');
    setErrors(local);
    if (Object.keys(local).length) return;
    setBusy(true);
    try {
      const blank = (v) => (v?.trim() ? v.trim() : undefined);
      await me.update({ givenName: f.givenName.trim(), familyName: f.familyName.trim(), nickname: blank(f.nickname),
        phoneNumber: blank(f.phoneNumber), pictureUrl: blank(f.pictureUrl), locale: f.locale });
      toast.success(t('profile.saved'));
      onSaved();
    } catch (err) {
      setErrors(err.fieldErrors || {});
      toast.error(describeError(err, t));
    } finally { setBusy(false); }
  };

  return (
    <form onSubmit={save} noValidate className="stack">
      <div className="profile-pic">
        <Avatar src={f.pictureUrl} name={`${f.givenName} ${f.familyName}`} size={72} />
        <TextField label={t('field.picture')} hint={t('profile.pictureHint')} type="url" placeholder="https://…" value={f.pictureUrl} onChange={set('pictureUrl')} error={errors.pictureUrl} />
      </div>
      <div className="grid-2">
        <TextField label={t('field.givenName')} value={f.givenName} onChange={set('givenName')} error={errors.givenName} required />
        <TextField label={t('field.familyName')} value={f.familyName} onChange={set('familyName')} error={errors.familyName} required />
      </div>
      <div className="grid-2">
        <TextField label={t('field.nickname')} value={f.nickname} onChange={set('nickname')} error={errors.nickname} />
        <TextField label={t('field.phone')} type="tel" placeholder="+85512345678" value={f.phoneNumber} onChange={set('phoneNumber')} error={errors.phoneNumber} />
      </div>
      <div className="grid-2">
        <TextField label={<span className="lock-label"><Lock size={13} />{t('field.username')}</span>} value={user.username} readOnly className="readonly" />
        <TextField label={<span className="lock-label"><Lock size={13} />{t('field.email')}</span>} value={user.email} readOnly className="readonly"
          hint={user.emailVerified ? t('profile.emailVerified') : t('profile.emailUnverified')} />
      </div>
      <SelectField label={t('field.language')} value={f.locale} onChange={set('locale')} options={[{ value: 'en', label: 'English' }, { value: 'km', label: 'ខ្មែរ' }]} />
      <div className="form-actions">
        <Button variant="primary" type="submit" loading={busy} disabled={!dirty}>{t('common.saveChanges')}</Button>
        <Button type="button" disabled={!dirty || busy} onClick={() => { setF(initial()); setErrors({}); }}>{t('common.reset')}</Button>
      </div>
    </form>
  );
}

export default function Profile() {
  const { t } = useI18n();
  const { profile, setProfile, roles, permissions } = useAuth();
  const state = useData(() => me.get(), []);
  const refreshClaims = async () => { state.reload(); setProfile(await me.userinfo().catch(() => profile)); };

  return (
    <>
      <PageHeader title={t('profile.title')} description={t('profile.description')} />
      <Async state={state}>
        {(user) => (
          <div className="cols">
            <Panel title={t('profile.personal')}>
              <ProfileForm user={user} claims={profile} onSaved={refreshClaims} />
            </Panel>
            <div className="stack-lg">
              <Panel title={t('profile.access')} description={t('profile.accessHint')}>
                <p className="label">{t('profile.roles')}</p>
                <Chips items={roles} empty={t('profile.noRoles')} />
                <p className="label">{t('profile.permissions')}</p>
                <Chips items={permissions} empty={t('profile.noPermissions')} />
              </Panel>
              {user.emailVerified && <p className="verified"><BadgeCheck size={16} />{t('profile.emailVerified')}</p>}
            </div>
          </div>
        )}
      </Async>
    </>
  );
}
