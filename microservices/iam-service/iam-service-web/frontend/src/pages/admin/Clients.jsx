import { useState } from 'react';
import { Building2, Copy, KeyRound, Pencil, Plus, Trash2 } from 'lucide-react';
import {
  Async, Badge, Button, Chips, DataTable, EmptyState, Field, ListField, Modal, PageHeader, Pagination, Panel, SearchBox, TextField, Toggle, useConfirm, useToast,
} from '../../components/ui';
import { useData, useDebounced } from '../../components/hooks';
import { describeError, useI18n } from '../../i18n';
import { useAuth } from '../../auth/AuthContext';
import { clients } from '../../api/iam';

const GRANTS = ['authorization_code', 'refresh_token', 'client_credentials'];
const STD_SCOPES = ['openid', 'profile', 'email', 'phone', 'address'];
const validUri = (u) => { try { const x = new URL(u); return x.protocol === 'https:' || (x.protocol === 'http:' && ['localhost', '127.0.0.1'].includes(x.hostname)); } catch { return false; } };

const blank = () => ({
  clientId: '', clientName: '', type: 'confidential', grantTypes: ['authorization_code', 'refresh_token'], redirectUris: [], postLogoutRedirectUris: [],
  scopes: ['openid', 'profile'], requireProofKey: true, requireAuthorizationConsent: true, accessTokenMinutes: 15, refreshTokenHours: 24,
});
const fromClient = (c) => ({ ...blank(), ...c, type: (c.authenticationMethods || []).includes('none') ? 'public' : 'confidential' });

function SecretModal({ client, secret, onClose }) {
  const { t } = useI18n();
  const toast = useToast();
  const copy = async (v) => { try { await navigator.clipboard.writeText(v); toast.success(t('common.copied')); } catch { toast.error(t('error.generic')); } };
  return (
    <Modal title={t('clients.credentials')} onClose={onClose} footer={<Button variant="primary" onClick={onClose}>{t('common.done')}</Button>}>
      <div className="stack">
        <p className="callout warn">{t('clients.secretOnce')}</p>
        <Field label={t('clients.clientId')}>{(id) => <div className="copy"><input id={id} readOnly value={client.clientId} /><Button icon={Copy} aria-label={t('common.copy')} onClick={() => copy(client.clientId)} /></div>}</Field>
        <Field label={t('clients.clientSecret')}>{(id) => <div className="copy"><input id={id} readOnly value={secret} className="num" /><Button icon={Copy} aria-label={t('common.copy')} onClick={() => copy(secret)} /></div>}</Field>
      </div>
    </Modal>
  );
}

function ClientModal({ client, onClose, onSaved }) {
  const { t } = useI18n();
  const toast = useToast();
  const editing = !!client;
  const [f, setF] = useState(client ? fromClient(client) : blank());
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const isPublic = f.type === 'public';
  const set = (patch) => setF((s) => ({ ...s, ...patch }));
  const toggleIn = (key, v) => set({ [key]: f[key].includes(v) ? f[key].filter((x) => x !== v) : [...f[key], v] });
  const custom = f.scopes.filter((s) => !STD_SCOPES.includes(s));

  const submit = async (e) => {
    e.preventDefault();
    const local = {};
    if (!/^[a-z0-9][a-z0-9._-]{2,99}$/.test(f.clientId)) local.clientId = t('clients.idRule');
    if (!f.clientName.trim()) local.clientName = t('validation.required');
    if (!f.grantTypes.length) local.grantTypes = t('clients.grantRequired');
    if (isPublic && f.grantTypes.includes('client_credentials')) local.grantTypes = t('clients.publicNoCc');
    if (f.grantTypes.includes('authorization_code') && !f.redirectUris.length) local.redirectUris = t('clients.redirectRequired');
    else if (f.redirectUris.some((u) => !validUri(u))) local.redirectUris = t('clients.redirectInvalid');
    if (f.postLogoutRedirectUris.some((u) => !validUri(u))) local.postLogoutRedirectUris = t('clients.redirectInvalid');
    setErrors(local);
    if (Object.keys(local).length) return;
    setBusy(true);
    const { type, ...rest } = f;
    const body = { ...rest, authenticationMethods: isPublic ? ['none'] : ['client_secret_basic'], requireProofKey: isPublic ? true : f.requireProofKey,
      accessTokenMinutes: Number(f.accessTokenMinutes), refreshTokenHours: Number(f.refreshTokenHours) };
    try {
      const saved = editing ? await clients.update(client.id, body) : await clients.create(body);
      toast.success(t(editing ? 'clients.updated' : 'clients.created'));
      onSaved(saved, !editing);
    } catch (err) { setErrors(err.fieldErrors || {}); toast.error(err.status === 409 ? t('clients.exists') : describeError(err, t)); } finally { setBusy(false); }
  };

  return (
    <Modal wide title={editing ? t('clients.edit') : t('clients.register')} onClose={onClose}
      footer={<><Button onClick={onClose}>{t('common.cancel')}</Button><Button variant="primary" type="submit" form="client-form" loading={busy}>{editing ? t('common.save') : t('clients.register')}</Button></>}>
      <form id="client-form" onSubmit={submit} className="stack" noValidate>
        <div className="grid-2">
          <TextField label={t('clients.name')} value={f.clientName} onChange={(e) => set({ clientName: e.target.value })} error={errors.clientName} required />
          <TextField label={t('clients.clientId')} value={f.clientId} onChange={(e) => set({ clientId: e.target.value })} error={errors.clientId} hint={t('clients.idHint')} disabled={editing} required />
        </div>
        <fieldset className="choice" disabled={editing}>
          <legend>{t('clients.type')}</legend>
          {['confidential', 'public'].map((k) => (
            <label key={k} className={`option ${f.type === k ? 'on' : ''}`}>
              <input type="radio" name="ctype" checked={f.type === k} onChange={() => set({ type: k, ...(k === 'public' ? { requireProofKey: true, grantTypes: f.grantTypes.filter((g) => g !== 'client_credentials') } : {}) })} />
              <span><strong>{t(`clients.type.${k}`)}</strong><small>{t(`clients.type.${k}Hint`)}</small></span>
            </label>
          ))}
        </fieldset>
        <div className="field">
          <label>{t('clients.grantTypes')}</label>
          <div className="check-row">
            {GRANTS.map((g) => (
              <label key={g} className="check"><input type="checkbox" checked={f.grantTypes.includes(g)} disabled={isPublic && g === 'client_credentials'} onChange={() => toggleIn('grantTypes', g)} /><span>{g}</span></label>
            ))}
          </div>
          {errors.grantTypes && <small className="err">{errors.grantTypes}</small>}
        </div>
        <ListField label={t('clients.redirectUris')} hint={t('clients.oneperline')} values={f.redirectUris} onChange={(v) => set({ redirectUris: v })} error={errors.redirectUris} placeholder="https://app.example.com/callback" />
        <ListField label={t('clients.postLogoutUris')} hint={t('clients.oneperline')} values={f.postLogoutRedirectUris} onChange={(v) => set({ postLogoutRedirectUris: v })} error={errors.postLogoutRedirectUris} />
        <div className="field">
          <label>{t('clients.scopes')}</label>
          <div className="check-row">
            {STD_SCOPES.map((s) => <label key={s} className="check"><input type="checkbox" checked={f.scopes.includes(s)} onChange={() => toggleIn('scopes', s)} /><span>{s}</span></label>)}
          </div>
          <ListField hint={t('clients.customScopes')} values={custom} onChange={(v) => set({ scopes: [...f.scopes.filter((s) => STD_SCOPES.includes(s)), ...v] })} />
        </div>
        <div className="grid-2">
          <TextField label={t('clients.accessTtl')} type="number" min="1" max="1440" value={f.accessTokenMinutes} onChange={(e) => set({ accessTokenMinutes: e.target.value })} />
          <TextField label={t('clients.refreshTtl')} type="number" min="0" max="8760" value={f.refreshTokenHours} onChange={(e) => set({ refreshTokenHours: e.target.value })} hint={t('clients.refreshHint')} />
        </div>
        <Toggle checked={isPublic || f.requireProofKey} disabled={isPublic} onChange={(v) => set({ requireProofKey: v })} label={t('clients.pkce')} hint={t('clients.pkceHint')} />
        <Toggle checked={f.requireAuthorizationConsent} onChange={(v) => set({ requireAuthorizationConsent: v })} label={t('clients.consent')} hint={t('clients.consentHint')} />
      </form>
    </Modal>
  );
}

export default function Clients() {
  const { t, date } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const confirm = useConfirm();
  const [q, setQ] = useState('');
  const [page, setPage] = useState(0);
  const [form, setForm] = useState(null);
  const [secret, setSecret] = useState(null);
  const dq = useDebounced(q);
  const state = useData(() => clients.list({ q: dq, page, size: 10 }), [dq, page]);

  const remove = async (c) => {
    if (!(await confirm({ title: t('clients.deleteTitle', { name: c.clientName }), message: t('clients.deleteBody'), confirmLabel: t('common.delete'), danger: true }))) return;
    try { await clients.remove(c.id); toast.success(t('clients.deleted')); state.reload(); } catch (e) { toast.error(describeError(e, t)); }
  };
  const regenerate = async (c) => {
    if (!(await confirm({ title: t('clients.regenTitle', { name: c.clientName }), message: t('clients.regenBody'), confirmLabel: t('clients.regen'), danger: true }))) return;
    try { const r = await clients.regenerateSecret(c.id); setSecret({ client: c, secret: r.clientSecret }); } catch (e) { toast.error(describeError(e, t)); }
  };

  const columns = [
    { key: 'name', header: t('clients.name'), render: (c) => <span className="stack-xs"><strong>{c.clientName}</strong><code className="perm">{c.clientId}</code></span> },
    { key: 'type', header: t('clients.type'), render: (c) => ((c.authenticationMethods || []).includes('none') ? <Badge tone="info">{t('clients.type.public')}</Badge> : <Badge>{t('clients.type.confidential')}</Badge>) },
    { key: 'grants', header: t('clients.grantTypes'), render: (c) => <Chips items={c.grantTypes} /> },
    { key: 'scopes', header: t('clients.scopes'), render: (c) => <span className="muted">{(c.scopes || []).join(' ')}</span> },
    { key: 'createdAt', header: t('common.created'), render: (c) => <span className="nowrap">{date(c.createdAt, { dateStyle: 'medium' })}</span> },
    { key: 'a', header: <span className="sr-only">{t('common.actions')}</span>, className: 'actions', render: (c) => (
      <span className="row-actions">
        {can('client:update') && <Button size="sm" icon={Pencil} title={t('common.edit')} aria-label={t('common.edit')} onClick={() => setForm(c)} />}
        {can('client:update') && !(c.authenticationMethods || []).includes('none') && <Button size="sm" icon={KeyRound} title={t('clients.regen')} aria-label={t('clients.regen')} onClick={() => regenerate(c)} />}
        {can('client:delete') && <Button size="sm" icon={Trash2} title={t('common.delete')} aria-label={t('common.delete')} onClick={() => remove(c)} />}
      </span>) },
  ];

  return (
    <>
      <PageHeader title={t('clients.title')} description={t('clients.description')}
        actions={can('client:create') && <Button variant="primary" icon={Plus} onClick={() => setForm({ new: true })}>{t('clients.register')}</Button>} />
      <div className="toolbar"><SearchBox value={q} onChange={(v) => { setQ(v); setPage(0); }} placeholder={t('clients.search')} /></div>
      <Async state={state}>
        {(p) => (<Panel flush>
          <DataTable rows={p.content} rowKey="id" columns={columns} empty={<EmptyState icon={Building2} title={t('clients.none')}>{t('clients.noneHint')}</EmptyState>} />
          <Pagination page={p.page} onPage={setPage} /></Panel>)}
      </Async>
      {form && <ClientModal client={form.new ? null : form} onClose={() => setForm(null)}
        onSaved={(saved, created) => { setForm(null); state.reload(); if (created && saved.clientSecret) setSecret({ client: saved, secret: saved.clientSecret }); }} />}
      {secret && <SecretModal client={secret.client} secret={secret.secret} onClose={() => setSecret(null)} />}
    </>
  );
}
