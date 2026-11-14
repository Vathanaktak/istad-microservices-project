import { useState } from 'react';
import { Download, ScrollText } from 'lucide-react';
import { Async, Badge, Button, DataTable, EmptyState, PageHeader, Pagination, Panel, SearchBox, SelectField, TextField } from '../../components/ui';
import { useData, useDebounced } from '../../components/hooks';
import { useI18n } from '../../i18n';
import { monitoring } from '../../api/iam';

export const CATEGORIES = ['AUTH', 'USER', 'ROLE', 'PERMISSION', 'CLIENT', 'SYSTEM'];
const csv = (rows) => {
  const esc = (v) => `"${String(v ?? '').replace(/"/g, '""')}"`;
  const head = ['occurredAt', 'category', 'action', 'outcome', 'actor', 'target', 'ipAddress', 'detail'];
  return [head.join(','), ...rows.map((r) => head.map((h) => esc(r[h])).join(','))].join('\n');
};

export function AuditTable({ rows }) {
  const { t, date } = useI18n();
  const columns = [
    { key: 'occurredAt', header: t('history.when'), render: (r) => <span className="nowrap">{date(r.occurredAt)}</span> },
    { key: 'category', header: t('audit.category'), render: (r) => <Badge tone="info">{t(`audit.cat.${r.category}`)}</Badge> },
    { key: 'action', header: t('audit.action'), render: (r) => { const k = `audit.act.${r.action}`; const v = t(k); return <span className="stack-xs"><strong>{v === k ? r.action : v}</strong>{r.detail && <small className="muted">{r.detail}</small>}</span>; } },
    { key: 'actor', header: t('audit.actor') },
    { key: 'target', header: t('audit.target') },
    { key: 'outcome', header: t('audit.outcome'), render: (r) => (r.outcome === 'FAILURE' ? <Badge tone="danger">{t('audit.failure')}</Badge> : <Badge tone="ok">{t('audit.success')}</Badge>) },
    { key: 'ipAddress', header: t('history.ip'), render: (r) => <span className="num">{r.ipAddress}</span> },
  ];
  return <DataTable rows={rows} columns={columns} rowKey="id" empty={<EmptyState icon={ScrollText} title={t('audit.none')}>{t('common.noResultsHint')}</EmptyState>} />;
}

export default function AuditLogs() {
  const { t } = useI18n();
  const [q, setQ] = useState('');
  const [category, setCategory] = useState('');
  const [outcome, setOutcome] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [page, setPage] = useState(0);
  const dq = useDebounced(q);
  const state = useData(() => monitoring.auditLogs({ q: dq, category, outcome, from: from || undefined, to: to || undefined, page, size: 15, sort: 'occurredAt,desc' }),
    [dq, category, outcome, from, to, page]);
  const reset = (fn) => (v) => { fn(v); setPage(0); };

  const exportCsv = () => {
    const url = URL.createObjectURL(new Blob([csv(state.data?.content || [])], { type: 'text/csv;charset=utf-8' }));
    const a = Object.assign(document.createElement('a'), { href: url, download: `audit-logs-${new Date().toISOString().slice(0, 10)}.csv` });
    a.click(); URL.revokeObjectURL(url);
  };

  return (
    <>
      <PageHeader title={t('audit.title')} description={t('audit.description')}
        actions={<Button icon={Download} onClick={exportCsv} disabled={!state.data?.content?.length}>{t('audit.export')}</Button>} />
      <div className="toolbar wrap">
        <SearchBox value={q} onChange={reset(setQ)} placeholder={t('audit.search')} />
        <SelectField value={category} onChange={(e) => reset(setCategory)(e.target.value)} aria-label={t('audit.category')}
          options={[{ value: '', label: t('audit.allCategories') }, ...CATEGORIES.map((c) => ({ value: c, label: t(`audit.cat.${c}`) }))]} />
        <SelectField value={outcome} onChange={(e) => reset(setOutcome)(e.target.value)} aria-label={t('audit.outcome')}
          options={[{ value: '', label: t('audit.allOutcomes') }, { value: 'SUCCESS', label: t('audit.success') }, { value: 'FAILURE', label: t('audit.failure') }]} />
        <TextField type="date" value={from} onChange={(e) => reset(setFrom)(e.target.value)} aria-label={t('audit.from')} max={to || undefined} />
        <TextField type="date" value={to} onChange={(e) => reset(setTo)(e.target.value)} aria-label={t('audit.to')} min={from || undefined} />
      </div>
      <Async state={state}>{(p) => <Panel flush><AuditTable rows={p.content} /><Pagination page={p.page} onPage={setPage} /></Panel>}</Async>
    </>
  );
}
