import { useState } from 'react';
import { Pencil, Plus, Power, PowerOff, ShieldHalf, Trash2, KeyRound, X } from 'lucide-react';
import {
  Async, Badge, Button, DataTable, EmptyState, Modal, PageHeader, Pagination, Panel, SearchBox, SelectField, TextArea, TextField, useConfirm, useToast,
} from '../../components/ui';
import { Picker, fetchAll } from '../../components/admin';
import { useData, useDebounced } from '../../components/hooks';
import { describeError, useI18n } from '../../i18n';
import { useAuth } from '../../auth/AuthContext';
import { permissions as permsApi, roles } from '../../api/iam';
import { ROLE_NAME_RE } from '../../components/validation';

function RoleFormModal({ role, onClose, onSaved }) {
  const { t } = useI18n();
  const toast = useToast();
  const perms = useData(() => (role ? Promise.resolve([]) : fetchAll(permsApi.list, { active: true })), []);
  const [f, setF] = useState({ name: role?.name || '', description: role?.description || '', permissionUuids: [] });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    const local = {};
    if (!role?.system && !ROLE_NAME_RE.test(f.name)) local.name = t('roles.nameRule');
    setErrors(local);
    if (Object.keys(local).length) return;
    setBusy(true);
    try {
      if (role) await roles.update(role.uuid, { name: role.system ? undefined : f.name, description: f.description });
      else await roles.create({ name: f.name, description: f.description || undefined, permissionUuids: f.permissionUuids.length ? f.permissionUuids : undefined });
      toast.success(t(role ? 'roles.updated' : 'roles.created')); onSaved();
    } catch (err) { setErrors(err.fieldErrors || {}); toast.error(err.status === 409 ? t('roles.exists') : describeError(err, t)); } finally { setBusy(false); }
  };
  return (
    <Modal title={role ? t('roles.edit') : t('roles.create')} onClose={onClose}
      footer={<><Button onClick={onClose}>{t('common.cancel')}</Button><Button variant="primary" type="submit" form="role-form" loading={busy}>{t('common.save')}</Button></>}>
      <form id="role-form" onSubmit={submit} className="stack" noValidate>
        <TextField label={t('roles.name')} value={f.name} onChange={(e) => setF({ ...f, name: e.target.value.toUpperCase() })} error={errors.name} hint={t('roles.nameRule')} disabled={role?.system} required />
        <TextArea label={t('common.description')} value={f.description} maxLength={255} onChange={(e) => setF({ ...f, description: e.target.value })} error={errors.description} />
        {!role && (
          <div className="field">
            <label>{t('roles.initialPermissions')}</label>
            {perms.loading ? <small className="muted">{t('common.loading')}</small> : (
              <Picker items={(perms.data || []).map((p) => ({ value: p.uuid, label: p.name, hint: p.description }))} selected={f.permissionUuids}
                onChange={(permissionUuids) => setF({ ...f, permissionUuids })} placeholder={t('permissions.search')} empty={t('permissions.none')} />
            )}
          </div>
        )}
      </form>
    </Modal>
  );
}

function RolePermissionsModal({ roleId, onClose, onChanged }) {
  const { t } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const state = useData(() => roles.get(roleId), [roleId]);
  const all = useData(() => fetchAll(permsApi.list, { active: true }), []);
  const [selected, setSelected] = useState([]);
  const [busy, setBusy] = useState(false);
  const canAssign = can('role:assign');

  const add = async () => {
    setBusy(true);
    try { await roles.assignPermissions(roleId, { permissionUuids: selected }); toast.success(t('roles.permAdded')); setSelected([]); state.reload(); onChanged(); }
    catch (e) { toast.error(describeError(e, t)); } finally { setBusy(false); }
  };
  const remove = async (p) => {
    try { await roles.revokePermission(roleId, p.uuid); toast.success(t('roles.permRemoved')); state.reload(); onChanged(); }
    catch (e) { toast.error(describeError(e, t)); }
  };
  return (
    <Modal wide title={t('roles.permissionsOf', { name: state.data?.name || '…' })} onClose={onClose} footer={<Button onClick={onClose}>{t('common.close')}</Button>}>
      <Async state={state}>
        {(role) => {
          const have = new Set(role.permissions.map((p) => p.uuid));
          const items = (all.data || []).filter((p) => !have.has(p.uuid)).map((p) => ({ value: p.uuid, label: p.name, hint: p.description }));
          return (
            <div className="cols">
              <div>
                <h3 className="sub">{t('roles.assigned', { n: role.permissions.length })}</h3>
                {role.permissions.length === 0 ? <p className="muted">{t('roles.noPermissions')}</p> : (
                  <ul className="rows compact">
                    {role.permissions.map((p) => (
                      <li key={p.uuid}><div className="row-main"><strong>{p.name}</strong>{p.description && <small>{p.description}</small>}</div>
                        {canAssign && <button className="icon-btn" onClick={() => remove(p)} aria-label={t('roles.removePermission', { name: p.name })}><X size={16} /></button>}</li>))}
                  </ul>
                )}
              </div>
              {canAssign && (
                <div>
                  <h3 className="sub">{t('roles.addPermissions')}</h3>
                  <Picker items={items} selected={selected} onChange={setSelected} placeholder={t('permissions.search')} empty={t('permissions.none')} />
                  <div className="form-actions"><Button variant="primary" onClick={add} loading={busy} disabled={!selected.length}>{t('roles.addSelected', { n: selected.length })}</Button></div>
                </div>
              )}
            </div>
          );
        }}
      </Async>
    </Modal>
  );
}

export default function Roles() {
  const { t } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const confirm = useConfirm();
  const [q, setQ] = useState('');
  const [active, setActive] = useState('');
  const [page, setPage] = useState(0);
  const [form, setForm] = useState(null);      // {} create | role edit
  const [permsFor, setPermsFor] = useState(null);
  const dq = useDebounced(q);
  const state = useData(() => roles.list({ q: dq, active, page, size: 10 }), [dq, active, page]);

  const toggle = async (r) => {
    try { await roles.action(r.uuid, r.active ? 'deactivate' : 'activate'); toast.success(t(r.active ? 'roles.deactivated' : 'roles.activated', { name: r.name })); state.reload(); }
    catch (e) { toast.error(describeError(e, t)); }
  };
  const remove = async (r) => {
    if (!(await confirm({ title: t('roles.deleteTitle', { name: r.name }), message: t('roles.deleteBody'), confirmLabel: t('common.delete'), danger: true }))) return;
    try { await roles.remove(r.uuid); toast.success(t('roles.deleted')); state.reload(); } catch (e) { toast.error(e.status === 422 ? t('roles.inUse') : describeError(e, t)); }
  };

  const columns = [
    { key: 'name', header: t('roles.role'), render: (r) => <span className="stack-xs"><strong>{r.name}</strong>{r.description && <small className="muted">{r.description}</small>}</span> },
    { key: 'system', header: t('common.type'), render: (r) => (r.system ? <Badge tone="info">{t('common.system')}</Badge> : <Badge>{t('common.custom')}</Badge>) },
    { key: 'active', header: t('common.status'), render: (r) => (r.active ? <Badge tone="ok">{t('status.active')}</Badge> : <Badge>{t('status.inactive')}</Badge>) },
    { key: 'a', header: <span className="sr-only">{t('common.actions')}</span>, className: 'actions', render: (r) => (
      <span className="row-actions">
        <Button size="sm" icon={KeyRound} onClick={() => setPermsFor(r.uuid)}>{t('roles.permissions')}</Button>
        {can('role:update') && <Button size="sm" icon={Pencil} title={t('common.edit')} aria-label={t('common.edit')} onClick={() => setForm(r)} />}
        {can('role:update') && <Button size="sm" icon={r.active ? PowerOff : Power} title={t(r.active ? 'roles.deactivate' : 'roles.activate')} aria-label={t(r.active ? 'roles.deactivate' : 'roles.activate')} onClick={() => toggle(r)} />}
        {can('role:delete') && !r.system && <Button size="sm" icon={Trash2} title={t('common.delete')} aria-label={t('common.delete')} onClick={() => remove(r)} />}
      </span>) },
  ];

  return (
    <>
      <PageHeader title={t('roles.title')} description={t('roles.description')}
        actions={can('role:create') && <Button variant="primary" icon={Plus} onClick={() => setForm({})}>{t('roles.create')}</Button>} />
      <div className="toolbar">
        <SearchBox value={q} onChange={(v) => { setQ(v); setPage(0); }} placeholder={t('roles.search')} />
        <SelectField value={active} onChange={(e) => { setActive(e.target.value); setPage(0); }} aria-label={t('common.status')}
          options={[{ value: '', label: t('common.all') }, { value: 'true', label: t('status.active') }, { value: 'false', label: t('status.inactive') }]} />
      </div>
      <Async state={state}>
        {(p) => (<Panel flush>
          <DataTable rows={p.content} columns={columns} empty={<EmptyState icon={ShieldHalf} title={t('roles.none')}>{t('common.noResultsHint')}</EmptyState>} />
          <Pagination page={p.page} onPage={setPage} /></Panel>)}
      </Async>
      {form && <RoleFormModal role={form.uuid ? form : null} onClose={() => setForm(null)} onSaved={() => { setForm(null); state.reload(); }} />}
      {permsFor && <RolePermissionsModal roleId={permsFor} onClose={() => setPermsFor(null)} onChanged={state.reload} />}
    </>
  );
}
