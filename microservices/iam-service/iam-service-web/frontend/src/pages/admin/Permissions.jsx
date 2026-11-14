import { useMemo, useState } from 'react';
import { Lock, Pencil, Plus, Power, PowerOff, Trash2 } from 'lucide-react';
import {
  Async, Badge, Button, DataTable, EmptyState, Modal, PageHeader, Pagination, Panel, SearchBox, SelectField, TextArea, TextField, useConfirm, useToast,
} from '../../components/ui';
import { useData, useDebounced } from '../../components/hooks';
import { describeError, useI18n } from '../../i18n';
import { useAuth } from '../../auth/AuthContext';
import { permissions } from '../../api/iam';
import { fetchAll } from '../../components/admin';

const ACTIONS = ['create', 'read', 'update', 'delete', 'assign'];
const IDENT = /^[A-Za-z][A-Za-z0-9_-]*$/;

function PermissionModal({ permission, resources, onClose, onSaved }) {
  const { t } = useI18n();
  const toast = useToast();
  const [f, setF] = useState({ resource: permission?.resource || '', action: permission?.action || '', description: permission?.description || '' });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const submit = async (e) => {
    e.preventDefault();
    const local = {};
    if (!permission) {
      if (!IDENT.test(f.resource)) local.resource = t('permissions.identRule');
      if (!IDENT.test(f.action)) local.action = t('permissions.identRule');
    }
    setErrors(local);
    if (Object.keys(local).length) return;
    setBusy(true);
    try {
      if (permission) await permissions.update(permission.uuid, { description: f.description });
      else await permissions.create({ resource: f.resource, action: f.action, description: f.description || undefined });
      toast.success(t(permission ? 'permissions.updated' : 'permissions.created')); onSaved();
    } catch (err) { setErrors(err.fieldErrors || {}); toast.error(err.status === 409 ? t('permissions.exists') : describeError(err, t)); } finally { setBusy(false); }
  };
  return (
    <Modal title={permission ? t('permissions.edit') : t('permissions.create')} onClose={onClose}
      footer={<><Button onClick={onClose}>{t('common.cancel')}</Button><Button variant="primary" type="submit" form="perm-form" loading={busy}>{t('common.save')}</Button></>}>
      <form id="perm-form" onSubmit={submit} className="stack" noValidate>
        <div className="grid-2">
          <div className="field">
            <label htmlFor="perm-resource">{t('permissions.resource')}</label>
            <input id="perm-resource" list="resource-options" value={f.resource} disabled={!!permission} onChange={(e) => setF({ ...f, resource: e.target.value.toLowerCase() })} placeholder="user" />
            <datalist id="resource-options">{resources.map((r) => <option key={r} value={r} />)}</datalist>
            {errors.resource ? <small className="err">{errors.resource}</small> : <small className="hint">{t('permissions.resourceHint')}</small>}
          </div>
          <div className="field">
            <label htmlFor="perm-action">{t('permissions.action')}</label>
            <input id="perm-action" list="action-options" value={f.action} disabled={!!permission} onChange={(e) => setF({ ...f, action: e.target.value.toLowerCase() })} placeholder="read" />
            <datalist id="action-options">{ACTIONS.map((a) => <option key={a} value={a} />)}</datalist>
            {errors.action ? <small className="err">{errors.action}</small> : <small className="hint">{t('permissions.actionHint')}</small>}
          </div>
        </div>
        <p className="preview">{t('permissions.preview')} <code>{(f.resource || 'resource')}:{(f.action || 'action')}</code></p>
        <TextArea label={t('common.description')} value={f.description} maxLength={255} onChange={(e) => setF({ ...f, description: e.target.value })} error={errors.description} />
      </form>
    </Modal>
  );
}

export default function Permissions() {
  const { t } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const confirm = useConfirm();
  const [q, setQ] = useState('');
  const [resource, setResource] = useState('');
  const [active, setActive] = useState('');
  const [page, setPage] = useState(0);
  const [form, setForm] = useState(null);
  const dq = useDebounced(q);
  const state = useData(() => permissions.list({ q: dq, resource, active, page, size: 10 }), [dq, resource, active, page]);
  const every = useData(() => fetchAll(permissions.list), []);
  const resources = useMemo(() => [...new Set((every.data || []).map((p) => p.resource))].sort(), [every.data]);
  const reload = () => { state.reload(); every.reload(); };

  const toggle = async (p) => {
    try { await permissions.action(p.uuid, p.active ? 'deactivate' : 'activate'); toast.success(t(p.active ? 'permissions.deactivated' : 'permissions.activated', { name: p.name })); state.reload(); }
    catch (e) { toast.error(describeError(e, t)); }
  };
  const remove = async (p) => {
    if (!(await confirm({ title: t('permissions.deleteTitle', { name: p.name }), message: t('permissions.deleteBody'), confirmLabel: t('common.delete'), danger: true }))) return;
    try { await permissions.remove(p.uuid); toast.success(t('permissions.deleted')); reload(); } catch (e) { toast.error(e.status === 422 ? t('permissions.inUse') : describeError(e, t)); }
  };

  const columns = [
    { key: 'name', header: t('permissions.permission'), render: (p) => <span className="stack-xs"><code className="perm">{p.name}</code>{p.description && <small className="muted">{p.description}</small>}</span> },
    { key: 'resource', header: t('permissions.resource'), render: (p) => <Badge tone="info">{p.resource}</Badge> },
    { key: 'action', header: t('permissions.action') },
    { key: 'system', header: t('common.type'), render: (p) => (p.system ? <Badge tone="info"><Lock size={11} /> {t('common.system')}</Badge> : <Badge>{t('common.custom')}</Badge>) },
    { key: 'active', header: t('common.status'), render: (p) => (p.active ? <Badge tone="ok">{t('status.active')}</Badge> : <Badge>{t('status.inactive')}</Badge>) },
    { key: 'a', header: <span className="sr-only">{t('common.actions')}</span>, className: 'actions', render: (p) => (
      <span className="row-actions">
        {can('permission:update') && <Button size="sm" icon={Pencil} title={t('common.edit')} aria-label={t('common.edit')} onClick={() => setForm(p)} />}
        {can('permission:update') && <Button size="sm" icon={p.active ? PowerOff : Power} title={t(p.active ? 'roles.deactivate' : 'roles.activate')} aria-label={t(p.active ? 'roles.deactivate' : 'roles.activate')} onClick={() => toggle(p)} />}
        {can('permission:delete') && !p.system && <Button size="sm" icon={Trash2} title={t('common.delete')} aria-label={t('common.delete')} onClick={() => remove(p)} />}
      </span>) },
  ];

  return (
    <>
      <PageHeader title={t('permissions.title')} description={t('permissions.description')}
        actions={can('permission:create') && <Button variant="primary" icon={Plus} onClick={() => setForm({})}>{t('permissions.create')}</Button>} />
      <div className="toolbar">
        <SearchBox value={q} onChange={(v) => { setQ(v); setPage(0); }} placeholder={t('permissions.search')} />
        <SelectField value={resource} onChange={(e) => { setResource(e.target.value); setPage(0); }} aria-label={t('permissions.resource')}
          options={[{ value: '', label: t('permissions.allResources') }, ...resources.map((r) => ({ value: r, label: r }))]} />
        <SelectField value={active} onChange={(e) => { setActive(e.target.value); setPage(0); }} aria-label={t('common.status')}
          options={[{ value: '', label: t('common.all') }, { value: 'true', label: t('status.active') }, { value: 'false', label: t('status.inactive') }]} />
      </div>
      <Async state={state}>
        {(p) => (<Panel flush>
          <DataTable rows={p.content} columns={columns} empty={<EmptyState title={t('permissions.none')}>{t('common.noResultsHint')}</EmptyState>} />
          <Pagination page={p.page} onPage={setPage} /></Panel>)}
      </Async>
      {form && <PermissionModal permission={form.uuid ? form : null} resources={resources} onClose={() => setForm(null)} onSaved={() => { setForm(null); reload(); }} />}
    </>
  );
}
