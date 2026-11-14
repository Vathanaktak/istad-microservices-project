import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Lock, LockOpen, Plus, Power, PowerOff, Trash2, Users as UsersIcon } from 'lucide-react';
import { Avatar } from '../../components/Shell';
import { Async, Button, DataTable, EmptyState, Modal, PageHeader, Pagination, Panel, SearchBox, SelectField, TextField, useConfirm, useToast } from '../../components/ui';
import { Picker, UserStatus, fetchAll } from '../../components/admin';
import { useData, useDebounced } from '../../components/hooks';
import { describeError, useI18n } from '../../i18n';
import { useAuth } from '../../auth/AuthContext';
import { roles as rolesApi, users } from '../../api/iam';
import { PASSWORD_HINT, PHONE_RE, validatePassword } from '../../components/validation';

export function CreateUserModal({ onClose, onCreated }) {
  const { t, lang } = useI18n();
  const toast = useToast();
  const roles = useData(() => fetchAll(rolesApi.list, { active: true }), []);
  const [f, setF] = useState({ username: '', email: '', password: '', givenName: '', familyName: '', gender: '', phoneNumber: '', locale: lang, roleUuids: [] });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setF((s) => ({ ...s, [k]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    const local = {};
    if (!/^[A-Za-z0-9._-]{3,50}$/.test(f.username)) local.username = t('validation.username');
    if (!/^\S+@\S+\.\S+$/.test(f.email)) local.email = t('validation.email');
    if (!validatePassword(f.password)) local.password = t(PASSWORD_HINT);
    if (!f.givenName.trim()) local.givenName = t('validation.required');
    if (!f.familyName.trim()) local.familyName = t('validation.required');
    if (f.phoneNumber && !PHONE_RE.test(f.phoneNumber)) local.phoneNumber = t('validation.phone');
    setErrors(local);
    if (Object.keys(local).length) return;
    setBusy(true);
    try {
      const body = { ...f, gender: f.gender || undefined, phoneNumber: f.phoneNumber || undefined, roleUuids: f.roleUuids.length ? f.roleUuids : undefined };
      const created = await users.create(body);
      toast.success(t('users.created'));
      onCreated(created);
    } catch (err) {
      setErrors(err.fieldErrors || {});
      toast.error(err.status === 409 ? t('users.exists') : describeError(err, t));
    } finally { setBusy(false); }
  };

  return (
    <Modal wide title={t('users.create')} onClose={onClose}
      footer={<><Button onClick={onClose}>{t('common.cancel')}</Button><Button variant="primary" type="submit" form="create-user" loading={busy}>{t('users.create')}</Button></>}>
      <form id="create-user" onSubmit={submit} className="stack" noValidate>
        <div className="grid-2">
          <TextField label={t('field.givenName')} value={f.givenName} onChange={set('givenName')} error={errors.givenName} required />
          <TextField label={t('field.familyName')} value={f.familyName} onChange={set('familyName')} error={errors.familyName} required />
          <TextField label={t('field.username')} value={f.username} onChange={set('username')} error={errors.username} required />
          <TextField label={t('field.email')} type="email" value={f.email} onChange={set('email')} error={errors.email} required />
          <TextField label={t('field.password')} type="password" autoComplete="new-password" value={f.password} onChange={set('password')} error={errors.password} hint={t(PASSWORD_HINT)} required />
          <TextField label={t('field.phone')} placeholder="+85512345678" value={f.phoneNumber} onChange={set('phoneNumber')} error={errors.phoneNumber} />
          <SelectField label={t('field.gender')} value={f.gender} onChange={set('gender')}
            options={[{ value: '', label: '—' }, { value: 'female', label: t('gender.female') }, { value: 'male', label: t('gender.male') }, { value: 'other', label: t('gender.other') }]} />
          <SelectField label={t('field.language')} value={f.locale} onChange={set('locale')} options={[{ value: 'en', label: 'English' }, { value: 'km', label: 'ខ្មែរ' }]} />
        </div>
        <div className="field">
          <label>{t('users.initialRoles')}</label>
          {roles.loading ? <small className="muted">{t('common.loading')}</small> : (
            <Picker items={(roles.data || []).map((r) => ({ value: r.uuid, label: r.name, hint: r.description }))}
              selected={f.roleUuids} onChange={(roleUuids) => setF({ ...f, roleUuids })} placeholder={t('roles.search')} empty={t('roles.none')} />
          )}
        </div>
      </form>
    </Modal>
  );
}

export default function Users() {
  const { t } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const confirm = useConfirm();
  const nav = useNavigate();
  const [q, setQ] = useState('');
  const [enabled, setEnabled] = useState('');
  const [page, setPage] = useState(0);
  const [creating, setCreating] = useState(false);
  const dq = useDebounced(q);
  const state = useData(() => users.list({ q: dq, enabled, page, size: 10 }), [dq, enabled, page]);

  const act = async (u, name, key) => {
    try { await users.action(u.uuid, name); toast.success(t(key, { name: u.username })); state.reload(); }
    catch (e) { toast.error(describeError(e, t)); }
  };
  const remove = async (u) => {
    if (!(await confirm({ title: t('users.deleteTitle', { name: u.username }), message: t('users.deleteBody'), confirmLabel: t('common.delete'), danger: true }))) return;
    try { await users.remove(u.uuid); toast.success(t('users.deleted')); state.reload(); } catch (e) { toast.error(describeError(e, t)); }
  };
  const stop = (fn) => (e) => { e.stopPropagation(); fn(); };

  const columns = [
    { key: 'user', header: t('users.user'), render: (u) => (
      <span className="person"><Avatar name={u.name || u.username} size={32} /><span className="stack-xs"><strong>{u.name || u.username}</strong><small className="muted">@{u.username}</small></span></span>) },
    { key: 'email', header: t('field.email') },
    { key: 'status', header: t('common.status'), render: (u) => <UserStatus user={u} /> },
    ...(can('user:update') || can('user:delete') ? [{ key: 'actions', header: <span className="sr-only">{t('common.actions')}</span>, className: 'actions', render: (u) => (
      <span className="row-actions">
        {can('user:update') && (u.enabled
          ? <Button size="sm" icon={PowerOff} title={t('users.deactivate')} aria-label={t('users.deactivate')} onClick={stop(() => act(u, 'disable', 'users.deactivated'))} />
          : <Button size="sm" icon={Power} title={t('users.activate')} aria-label={t('users.activate')} onClick={stop(() => act(u, 'enable', 'users.activated'))} />)}
        {can('user:update') && (u.locked
          ? <Button size="sm" icon={LockOpen} title={t('users.unlock')} aria-label={t('users.unlock')} onClick={stop(() => act(u, 'unlock', 'users.unlocked'))} />
          : <Button size="sm" icon={Lock} title={t('users.lock')} aria-label={t('users.lock')} onClick={stop(() => act(u, 'lock', 'users.locked'))} />)}
        {can('user:delete') && <Button size="sm" icon={Trash2} title={t('common.delete')} aria-label={t('common.delete')} onClick={stop(() => remove(u))} />}
      </span>) }] : []),
  ];

  return (
    <>
      <PageHeader title={t('users.title')} description={t('users.description')}
        actions={can('user:create') && <Button variant="primary" icon={Plus} onClick={() => setCreating(true)}>{t('users.create')}</Button>} />
      <div className="toolbar">
        <SearchBox value={q} onChange={(v) => { setQ(v); setPage(0); }} placeholder={t('users.search')} />
        <SelectField value={enabled} onChange={(e) => { setEnabled(e.target.value); setPage(0); }} aria-label={t('common.status')}
          options={[{ value: '', label: t('common.all') }, { value: 'true', label: t('status.active') }, { value: 'false', label: t('status.disabled') }]} />
      </div>
      <Async state={state}>
        {(p) => (
          <Panel flush>
            <DataTable rows={p.content} columns={columns} onRowClick={(u) => nav(`/admin/users/${u.uuid}`)}
              empty={<EmptyState icon={UsersIcon} title={t('users.none')}>{t('common.noResultsHint')}</EmptyState>} />
            <Pagination page={p.page} onPage={setPage} />
          </Panel>
        )}
      </Async>
      {creating && <CreateUserModal onClose={() => setCreating(false)} onCreated={(u) => { setCreating(false); nav(`/admin/users/${u.uuid}`); }} />}
    </>
  );
}
