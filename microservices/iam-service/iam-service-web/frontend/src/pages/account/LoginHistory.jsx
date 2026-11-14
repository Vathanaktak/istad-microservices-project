import { useState } from 'react';
import { Async, PageHeader, Pagination, Panel, SelectField } from '../../components/ui';
import { LoginTable } from '../../components/shared';
import { useData } from '../../components/hooks';
import { useI18n } from '../../i18n';
import { me } from '../../api/iam';

export default function LoginHistory() {
  const { t } = useI18n();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState('');
  const state = useData(() => me.loginHistory({ page, size: 10, status, sort: 'occurredAt,desc' }), [page, status]);
  return (
    <>
      <PageHeader title={t('history.title')} description={t('history.description')}
        actions={<SelectField value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }} aria-label={t('history.result')}
          options={[{ value: '', label: t('history.all') }, { value: 'SUCCESS', label: t('history.success') }, { value: 'FAILED', label: t('history.failed') }]} />} />
      <Async state={state}>
        {(p) => (<Panel flush><LoginTable rows={p.content} /><Pagination page={p.page} onPage={setPage} /></Panel>)}
      </Async>
    </>
  );
}
