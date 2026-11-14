import { useState } from 'react';
import { Async, PageHeader, Pagination, Panel, SearchBox, SelectField, Toggle } from '../../components/ui';
import { LoginTable } from '../../components/shared';
import { useData, useDebounced } from '../../components/hooks';
import { useI18n } from '../../i18n';
import { monitoring } from '../../api/iam';

export default function LoginMonitor() {
  const { t } = useI18n();
  const [q, setQ] = useState('');
  const [status, setStatus] = useState('');
  const [suspicious, setSuspicious] = useState(false);
  const [page, setPage] = useState(0);
  const dq = useDebounced(q);
  const state = useData(() => monitoring.loginHistory({ q: dq, status, suspicious: suspicious || undefined, page, size: 15 }), [dq, status, suspicious, page]);
  const reset = (fn) => (v) => { fn(v); setPage(0); };
  return (
    <>
      <PageHeader title={t('monitor.title')} description={t('monitor.description')} />
      <div className="toolbar">
        <SearchBox value={q} onChange={reset(setQ)} placeholder={t('monitor.search')} />
        <SelectField value={status} onChange={(e) => reset(setStatus)(e.target.value)} aria-label={t('history.result')}
          options={[{ value: '', label: t('history.all') }, { value: 'SUCCESS', label: t('history.success') }, { value: 'FAILED', label: t('history.failed') }]} />
        <Toggle checked={suspicious} onChange={reset(setSuspicious)} label={t('monitor.suspiciousOnly')} />
      </div>
      <Async state={state}>{(p) => <Panel flush><LoginTable rows={p.content} showUser /><Pagination page={p.page} onPage={setPage} /></Panel>}</Async>
    </>
  );
}
