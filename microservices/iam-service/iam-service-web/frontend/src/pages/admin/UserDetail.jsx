import { useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, LockOpen, Lock, LogOut, Power, PowerOff, Repeat, Trash2, Unlink } from 'lucide-react';
import { Avatar } from '../../components/Shell';
import {
  Async, Badge, Button, Chips, DataTable, EmptyState, KeyValue, Modal, PageHeader, Pagination, Panel, SelectField, Tabs, TextField,
  TextArea, useConfirm, useToast,
} from '../../components/ui';
import { Picker, UserStatus, fetchAll } from '../../components/admin';
import { LoginTable, SessionList } from '../../components/shared';
import { useData } from '../../components/hooks';
import { describeError, useI18n } from '../../i18n';
import { useAuth } from '../../auth/AuthContext';
import { permissions as permsApi, roles as rolesApi, users } from '../../api/iam';
import { PASSWORD_HINT, PHONE_RE, validatePassword } from '../../components/validation';

const dateInput = (v) => (v ? new Date(v).toISOString().slice(0, 16) : '');

// ---- profile -----------------------------------------------------------------------------------
function ProfileTab({ user, onSaved }) {
  const { t, date } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const [f, setF] = useState({ givenName: user.givenName || '', familyName: user.familyName || '', phoneNumber: '', locale: user.locale || 'en' });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setF((s) => ({ ...s, [k]: e.target.value }));
  const save = async (e) => {
    e.preventDefault();
    const local = {};
    if (!f.givenName.trim()) local.givenName = t('validation.required');
    if (!f.familyName.trim()) local.familyName = t('validation.required');
    if (f.phoneNumber && !PHONE_RE.test(f.phoneNumber)) local.phoneNumber = t('validation.phone');
    setErrors(local);
    if (Object.keys(local).length) return;
    setBusy(true);
    try {
      await users.update(user.uuid, { givenName: f.givenName.trim(), familyName: f.familyName.trim(), phoneNumber: f.phoneNumber || undefined, locale: f.locale });
      toast.success(t('profile.saved')); onSaved();
    } catch (err) { setErrors(err.fieldErrors || {}); toast.error(describeError(err, t)); } finally { setBusy(false); }
  };
  return (
    <div className="cols">
      <Panel title={t('profile.personal')}>
        <form onSubmit={save} className="stack" noValidate>
          <div className="grid-2">
            <TextField label={t('field.givenName')} value={f.givenName} onChange={set('givenName')} error={errors.givenName} disabled={!can('user:update')} />
            <TextField label={t('field.familyName')} value={f.familyName} onChange={set('familyName')} error={errors.familyName} disabled={!can('user:update')} />
            <TextField label={t('field.phone')} placeholder="+85512345678" value={f.phoneNumber} onChange={set('phoneNumber')} error={errors.phoneNumber} disabled={!can('user:update')} hint={t('users.phoneHint')} />
            <SelectField label={t('field.language')} value={f.locale} onChange={set('locale')} disabled={!can('user:update')} options={[{ value: 'en', label: 'English' }, { value: 'km', label: 'ខ្មែរ' }]} />
          </div>
          {can('user:update') && <div className="form-actions"><Button variant="primary" type="submit" loading={busy}>{t('common.saveChanges')}</Button></div>}
        </form>
      </Panel>
      <Panel title={t('users.account')}>
        <KeyValue rows={[
          [t('field.username'), user.username], [t('field.email'), <>{user.email} {user.emailVerified && <Badge tone="ok">{t('profile.verified')}</Badge>}</>],
          [t('common.status'), <UserStatus user={user} />], [t('common.created'), date(user.createdAt)], [t('common.updated'), date(user.updatedAt)],
          [t('field.uuid'), <span className="num wrap">{user.uuid}</span>],
        ]} />
      </Panel>
    </div>
  );
}

// ---- roles ---------------------------------------------------------------------------------------
function AssignRoleModal({ userId, assigned, replacing, onClose, onDone }) {
  const { t } = useI18n();
  const toast = useToast();
  const roles = useData(() => fetchAll(rolesApi.list, { active: true }), []);
  const [selected, setSelected] = useState([]);
  const [expiresAt, setExpiresAt] = useState('');
  const [reason, setReason] = useState('');
  const [busy, setBusy] = useState(false);
  const items = (roles.data || []).filter((r) => !assigned.includes(r.uuid)).map((r) => ({ value: r.uuid, label: r.name, hint: r.description }));
  const pick = replacing ? (v) => setSelected(v.slice(-1)) : setSelected;

  const submit = async () => {
    setBusy(true);
    try {
      await users.assignRoles(userId, { roleUuids: selected, expiresAt: expiresAt ? new Date(expiresAt).toISOString() : undefined, reason: reason || undefined });
      if (replacing) await users.revokeRole(userId, replacing.uuid, reason || undefined);
      toast.success(t(replacing ? 'users.roleChanged' : 'users.roleAssigned')); onDone();
    } catch (e) { toast.error(describeError(e, t)); } finally { setBusy(false); }
  };
  return (
    <Modal title={replacing ? t('users.changeRole', { name: replacing.name }) : t('users.assignRole')} onClose={onClose}
      footer={<><Button onClick={onClose}>{t('common.cancel')}</Button><Button variant="primary" onClick={submit} loading={busy} disabled={!selected.length}>{replacing ? t('users.changeRoleConfirm') : t('users.assign')}</Button></>}>
      <div className="stack">
        {roles.loading ? <small className="muted">{t('common.loading')}</small> : (
          <Picker items={items} selected={selected} onChange={pick} placeholder={t('roles.search')} empty={t('roles.none')} />
        )}
        <TextField label={t('users.expiresAt')} type="datetime-local" value={expiresAt} onChange={(e) => setExpiresAt(e.target.value)} hint={t('users.expiresHint')} min={dateInput(new Date(Date.now() + 60000))} />
        <TextArea label={t('users.reason')} value={reason} maxLength={255} onChange={(e) => setReason(e.target.value)} />
      </div>
    </Modal>
  );
}

function RolesTab({ userId, auth }) {
  const { t, date } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const confirm = useConfirm();
  const [modal, setModal] = useState(null); // {} | {replacing}
  const canAssign = can('user:assign');

  const revoke = async (r) => {
    if (!(await confirm({ title: t('users.revokeRoleTitle', { name: r.name }), message: t('users.revokeRoleBody'), confirmLabel: t('users.revoke'), danger: true }))) return;
    try { await users.revokeRole(userId, r.uuid); toast.success(t('users.roleRevoked')); auth.reload(); } catch (e) { toast.error(describeError(e, t)); }
  };
  const columns = [
    { key: 'name', header: t('roles.role'), render: (r) => <strong>{r.name}</strong> },
    { key: 'assignedAt', header: t('users.assignedAt'), render: (r) => date(r.assignedAt) },
    { key: 'expiresAt', header: t('users.expiresAt'), render: (r) => (r.expiresAt ? date(r.expiresAt) : <span className="muted">{t('users.never')}</span>) },
    { key: 'reason', header: t('users.reason'), render: (r) => r.reason || <span className="muted">—</span> },
    ...(canAssign ? [{ key: 'a', header: <span className="sr-only">{t('common.actions')}</span>, className: 'actions', render: (r) => (
      <span className="row-actions">
        <Button size="sm" icon={Repeat} onClick={() => setModal({ replacing: r })}>{t('users.change')}</Button>
        <Button size="sm" icon={Unlink} onClick={() => revoke(r)}>{t('users.revoke')}</Button>
      </span>) }] : []),
  ];
  const roles = auth.data.roles;
  return (
    <Panel title={t('users.roles')} description={t('users.rolesHint')} flush
      actions={canAssign && <Button variant="primary" onClick={() => setModal({})}>{t('users.assignRole')}</Button>}>
      <DataTable rows={roles} columns={columns} empty={<EmptyState title={t('users.noRoles')}>{t('users.noRolesHint')}</EmptyState>} />
      {modal && <AssignRoleModal userId={userId} assigned={roles.map((r) => r.uuid)} replacing={modal.replacing} onClose={() => setModal(null)} onDone={() => { setModal(null); auth.reload(); }} />}
    </Panel>
  );
}

// ---- direct permissions ---------------------------------------------------------------------
function GrantModal({ userId, granted, onClose, onDone }) {
  const { t } = useI18n();
  const toast = useToast();
  const perms = useData(() => fetchAll(permsApi.list, { active: true }), []);
  const [selected, setSelected] = useState([]);
  const [effect, setEffect] = useState('GRANT');
  const [expiresAt, setExpiresAt] = useState('');
  const [reason, setReason] = useState('');
  const [busy, setBusy] = useState(false);
  const items = (perms.data || []).filter((p) => !granted.includes(p.uuid)).map((p) => ({ value: p.uuid, label: p.name, hint: p.description }));
  const submit = async () => {
    setBusy(true);
    try {
      await users.grantPermissions(userId, { permissionUuids: selected, effect, expiresAt: expiresAt ? new Date(expiresAt).toISOString() : undefined, reason: reason || undefined });
      toast.success(t('users.permGranted')); onDone();
    } catch (e) { toast.error(describeError(e, t)); } finally { setBusy(false); }
  };
  return (
    <Modal title={t('users.grantPermission')} onClose={onClose}
      footer={<><Button onClick={onClose}>{t('common.cancel')}</Button><Button variant="primary" onClick={submit} loading={busy} disabled={!selected.length}>{t('users.grant')}</Button></>}>
      <div className="stack">
        <SelectField label={t('users.effect')} value={effect} onChange={(e) => setEffect(e.target.value)} hint={t(effect === 'DENY' ? 'users.denyHint' : 'users.grantHint')}
          options={[{ value: 'GRANT', label: t('users.effectGrant') }, { value: 'DENY', label: t('users.effectDeny') }]} />
        {perms.loading ? <small className="muted">{t('common.loading')}</small> : (
          <Picker items={items} selected={selected} onChange={setSelected} placeholder={t('permissions.search')} empty={t('permissions.none')} />
        )}
        <TextField label={t('users.expiresAt')} type="datetime-local" value={expiresAt} onChange={(e) => setExpiresAt(e.target.value)} hint={t('users.expiresHint')} />
        <TextArea label={t('users.reason')} value={reason} maxLength={255} onChange={(e) => setReason(e.target.value)} />
      </div>
    </Modal>
  );
}

function PermissionsTab({ userId, auth }) {
  const { t, date } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const confirm = useConfirm();
  const [open, setOpen] = useState(false);
  const canAssign = can('user:assign');
  const direct = auth.data.directPermissions;
  const revoke = async (p) => {
    if (!(await confirm({ title: t('users.revokePermTitle', { name: p.name }), message: t('users.revokePermBody'), confirmLabel: t('users.revoke'), danger: true }))) return;
    try { await users.revokePermission(userId, p.uuid); toast.success(t('users.permRevoked')); auth.reload(); } catch (e) { toast.error(describeError(e, t)); }
  };
  const columns = [
    { key: 'name', header: t('permissions.permission'), render: (p) => <strong>{p.name}</strong> },
    { key: 'effect', header: t('users.effect'), render: (p) => (p.effect === 'DENY' ? <Badge tone="danger">{t('users.effectDeny')}</Badge> : <Badge tone="ok">{t('users.effectGrant')}</Badge>) },
    { key: 'expiresAt', header: t('users.expiresAt'), render: (p) => (p.expiresAt ? date(p.expiresAt) : <span className="muted">{t('users.never')}</span>) },
    ...(canAssign ? [{ key: 'a', header: <span className="sr-only">{t('common.actions')}</span>, className: 'actions', render: (p) => <Button size="sm" icon={Unlink} onClick={() => revoke(p)}>{t('users.revoke')}</Button> }] : []),
  ];
  return (
    <div className="stack-lg">
      <Panel title={t('users.directPermissions')} description={t('users.directHint')} flush
        actions={canAssign && <Button variant="primary" onClick={() => setOpen(true)}>{t('users.grantPermission')}</Button>}>
        <DataTable rows={direct} columns={columns} rowKey="uuid" empty={<EmptyState title={t('users.noDirect')}>{t('users.noDirectHint')}</EmptyState>} />
      </Panel>
      <Panel title={t('users.effectivePermissions')} description={t('users.effectiveHint')}>
        <Chips items={[...(auth.data.effectivePermissions || [])].sort()} empty={t('users.noEffective')} />
      </Panel>
      {open && <GrantModal userId={userId} granted={direct.map((p) => p.uuid)} onClose={() => setOpen(false)} onDone={() => { setOpen(false); auth.reload(); }} />}
    </div>
  );
}

// ---- sessions + history ----------------------------------------------------------------------
function SessionsTab({ userId }) {
  const { t } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const confirm = useConfirm();
  const state = useData(() => users.sessions(userId), [userId]);
  const writable = can('user:update');
  const one = async (s) => {
    if (!(await confirm({ title: t('sessions.terminateTitle'), message: t('sessions.terminateBody', { device: s.device }), confirmLabel: t('sessions.terminate'), danger: true }))) return;
    try { await users.terminateSession(userId, s.id); toast.success(t('sessions.terminated')); state.reload(); } catch (e) { toast.error(describeError(e, t)); }
  };
  const all = async () => {
    if (!(await confirm({ title: t('users.killAllTitle'), message: t('users.killAllBody'), confirmLabel: t('sessions.allConfirm'), danger: true }))) return;
    try { await users.terminateAllSessions(userId); toast.success(t('users.killedAll')); state.reload(); } catch (e) { toast.error(describeError(e, t)); }
  };
  return (
    <Async state={state}>
      {(list) => (
        <Panel title={t('sessions.active', { n: list.length })} flush
          actions={writable && <Button variant="danger" icon={LogOut} disabled={!list.length} onClick={all}>{t('sessions.allConfirm')}</Button>}>
          <SessionList sessions={list.map((s) => ({ ...s, current: false }))} onTerminate={one} />
        </Panel>
      )}
    </Async>
  );
}

function HistoryTab({ userId }) {
  const { t } = useI18n();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState('');
  const state = useData(() => users.loginHistory(userId, { page, size: 10, status }), [userId, page, status]);
  return (
    <>
      <div className="toolbar">
        <SelectField value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }} aria-label={t('history.result')}
          options={[{ value: '', label: t('history.all') }, { value: 'SUCCESS', label: t('history.success') }, { value: 'FAILED', label: t('history.failed') }]} />
      </div>
      <Async state={state}>{(p) => <Panel flush><LoginTable rows={p.content} /><Pagination page={p.page} onPage={setPage} /></Panel>}</Async>
    </>
  );
}

// ---- account security -----------------------------------------------------------------------------
function SecurityTab({ user, onChanged }) {
  const { t } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const confirm = useConfirm();
  const ids = useData(() => users.identities(user.uuid), [user.uuid]);
  const [f, setF] = useState({ newPassword: '', confirmPassword: '' });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const writable = can('user:update');

  const reset = async (e) => {
    e.preventDefault();
    const local = {};
    if (!validatePassword(f.newPassword)) local.newPassword = t(PASSWORD_HINT);
    if (f.newPassword !== f.confirmPassword) local.confirmPassword = t('validation.passwordMismatch');
    setErrors(local);
    if (Object.keys(local).length) return;
    setBusy(true);
    try { await users.resetPassword(user.uuid, f); toast.success(t('users.passwordReset')); setF({ newPassword: '', confirmPassword: '' }); }
    catch (err) { setErrors(err.fieldErrors || {}); toast.error(describeError(err, t)); } finally { setBusy(false); }
  };
  const toggle = async (name, key) => {
    try { await users.action(user.uuid, name); toast.success(t(key, { name: user.username })); onChanged(); } catch (e) { toast.error(describeError(e, t)); }
  };
  const unlink = async (i) => {
    if (!(await confirm({ title: t('linked.unlinkTitle', { provider: i.provider }), message: t('users.unlinkBody'), confirmLabel: t('linked.unlink'), danger: true }))) return;
    try { await users.unlinkIdentity(user.uuid, i.uuid); toast.success(t('linked.unlinked')); ids.reload(); }
    catch (e) { toast.error(e.status === 422 ? t('linked.lastMethod') : describeError(e, t)); }
  };

  return (
    <div className="cols">
      <Panel title={t('users.resetPassword')} description={t('users.resetPasswordHint')}>
        <form onSubmit={reset} className="stack" noValidate>
          <TextField label={t('field.newPassword')} type="password" autoComplete="new-password" value={f.newPassword} onChange={(e) => setF({ ...f, newPassword: e.target.value })} error={errors.newPassword} hint={t(PASSWORD_HINT)} disabled={!writable} />
          <TextField label={t('field.confirmPassword')} type="password" autoComplete="new-password" value={f.confirmPassword} onChange={(e) => setF({ ...f, confirmPassword: e.target.value })} error={errors.confirmPassword} disabled={!writable} />
          <div className="form-actions"><Button variant="primary" type="submit" loading={busy} disabled={!writable}>{t('users.resetPassword')}</Button></div>
        </form>
      </Panel>
      <div className="stack-lg">
        <Panel title={t('users.accountState')} description={t('users.accountStateHint')}>
          <div className="state-rows">
            <div><span><strong>{t('users.lockState')}</strong><small>{user.locked ? t('users.lockedNow') : t('users.notLocked')}</small></span>
              {user.locked
                ? <Button icon={LockOpen} disabled={!writable} onClick={() => toggle('unlock', 'users.unlocked')}>{t('users.unlock')}</Button>
                : <Button icon={Lock} disabled={!writable} onClick={() => toggle('lock', 'users.locked')}>{t('users.lock')}</Button>}</div>
            <div><span><strong>{t('users.activeState')}</strong><small>{user.enabled ? t('users.enabledNow') : t('users.disabledNow')}</small></span>
              {user.enabled
                ? <Button icon={PowerOff} disabled={!writable} onClick={() => toggle('disable', 'users.deactivated')}>{t('users.deactivate')}</Button>
                : <Button icon={Power} disabled={!writable} onClick={() => toggle('enable', 'users.activated')}>{t('users.activate')}</Button>}</div>
          </div>
        </Panel>
        <Panel title={t('linked.connected')} flush>
          <Async state={ids}>
            {(list) => list.length === 0 ? <EmptyState title={t('linked.none')} /> : (
              <ul className="rows">{list.map((i) => (
                <li key={i.uuid}><div className="row-main"><strong>{i.provider}</strong><small>{[i.displayName, i.email].filter(Boolean).join(' · ')}</small></div>
                  {writable && <Button size="sm" icon={Unlink} onClick={() => unlink(i)}>{t('linked.unlink')}</Button>}</li>))}</ul>)}
          </Async>
        </Panel>
      </div>
    </div>
  );
}

// ---- page ---------------------------------------------------------------------------------------
export default function UserDetail() {
  const { id } = useParams();
  const { t } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const confirm = useConfirm();
  const nav = useNavigate();
  const [tab, setTab] = useState('profile');
  const user = useData(() => users.get(id), [id]);
  const summary = useData(() => users.list({ q: user.data?.username, size: 50 }).then((p) => p.content.find((u) => u.uuid === id) || null), [user.data?.username]);
  const authz = useData(() => users.authorities(id), [id]);
  const locked = summary.data?.locked ?? false;
  const full = useMemo(() => (user.data ? { ...user.data, locked } : null), [user.data, locked]);
  const reloadAll = () => { user.reload(); summary.reload(); };

  const remove = async () => {
    if (!(await confirm({ title: t('users.deleteTitle', { name: full.username }), message: t('users.deleteBody'), confirmLabel: t('common.delete'), danger: true }))) return;
    try { await users.remove(id); toast.success(t('users.deleted')); nav('/admin/users'); } catch (e) { toast.error(describeError(e, t)); }
  };
  const toggleEnabled = async () => {
    try { await users.action(id, full.enabled ? 'disable' : 'enable'); toast.success(t(full.enabled ? 'users.deactivated' : 'users.activated', { name: full.username })); reloadAll(); }
    catch (e) { toast.error(describeError(e, t)); }
  };

  return (
    <Async state={user}>
      {() => full && (
        <>
          <Link to="/admin/users" className="back"><ArrowLeft size={15} />{t('users.title')}</Link>
          <PageHeader
            title={<span className="person big"><Avatar name={full.name || full.username} size={48} />{full.name || full.username}</span>}
            description={<span className="header-meta">@{full.username} · {full.email} <UserStatus user={full} /></span>}
            actions={<>
              {can('user:update') && <Button icon={full.enabled ? PowerOff : Power} onClick={toggleEnabled}>{full.enabled ? t('users.deactivate') : t('users.activate')}</Button>}
              {can('user:delete') && <Button variant="danger" icon={Trash2} onClick={remove}>{t('common.delete')}</Button>}
            </>} />
          <Tabs value={tab} onChange={setTab} tabs={[
            { key: 'profile', label: t('users.tab.profile') }, { key: 'roles', label: t('users.tab.roles') }, { key: 'permissions', label: t('users.tab.permissions') },
            { key: 'sessions', label: t('users.tab.sessions') }, { key: 'history', label: t('users.tab.history') }, { key: 'security', label: t('users.tab.security') },
          ]} />
          <div className="tab-body">
            {tab === 'profile' && <ProfileTab user={full} onSaved={reloadAll} />}
            {tab === 'roles' && <Async state={authz}>{() => <RolesTab userId={id} auth={authz} />}</Async>}
            {tab === 'permissions' && <Async state={authz}>{() => <PermissionsTab userId={id} auth={authz} />}</Async>}
            {tab === 'sessions' && <SessionsTab userId={id} />}
            {tab === 'history' && <HistoryTab userId={id} />}
            {tab === 'security' && <SecurityTab user={full} onChanged={reloadAll} />}
          </div>
        </>
      )}
    </Async>
  );
}
