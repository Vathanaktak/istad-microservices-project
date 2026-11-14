import { useState } from 'react';
import { TriangleAlert } from 'lucide-react';
import { Async, Badge, Button, PageHeader, Panel, SelectField, TextArea, TextField, Toggle, useConfirm, useToast } from '../../components/ui';
import { useData } from '../../components/hooks';
import { describeError, useI18n } from '../../i18n';
import { me } from '../../api/iam';

const REASONS = ['not_using', 'privacy', 'duplicate', 'security', 'other'];

export default function Deactivation() {
  const { t, date } = useI18n();
  const toast = useToast();
  const confirm = useConfirm();
  const state = useData(() => me.deactivation(), []);
  const [reason, setReason] = useState('not_using');
  const [details, setDetails] = useState('');
  const [ack, setAck] = useState(false);
  const [typed, setTyped] = useState('');
  const [busy, setBusy] = useState(false);
  const word = t('deactivation.word');

  const request = async (e) => {
    e.preventDefault();
    if (!ack || typed.trim().toUpperCase() !== word.toUpperCase()) return;
    if (!(await confirm({ title: t('deactivation.confirmTitle'), message: t('deactivation.confirmBody'), confirmLabel: t('deactivation.submit'), danger: true }))) return;
    setBusy(true);
    try { await me.requestDeactivation({ reason, details: details.trim() || undefined }); toast.success(t('deactivation.requested')); state.reload(); }
    catch (err) { toast.error(describeError(err, t)); }
    finally { setBusy(false); }
  };
  const cancel = async () => {
    try { await me.cancelDeactivation(); toast.success(t('deactivation.cancelled')); state.reload(); }
    catch (err) { toast.error(describeError(err, t)); }
  };

  return (
    <>
      <PageHeader title={t('deactivation.title')} description={t('deactivation.description')} />
      <Async state={state}>
        {(s) => s?.status === 'PENDING' ? (
          <Panel tone="warn" title={t('deactivation.pendingTitle')} description={t('deactivation.pendingBody', { date: date(s.requestedAt) })}
            actions={<Badge tone="warn">{t('deactivation.pending')}</Badge>}>
            <Button onClick={cancel}>{t('deactivation.cancel')}</Button>
          </Panel>
        ) : (
          <Panel tone="danger" title={t('deactivation.formTitle')}>
            <div className="callout"><TriangleAlert size={18} /><p>{t('deactivation.warning')}</p></div>
            <form onSubmit={request} className="stack">
              <SelectField label={t('deactivation.reason')} value={reason} onChange={(e) => setReason(e.target.value)}
                options={REASONS.map((r) => ({ value: r, label: t(`deactivation.reason.${r}`) }))} />
              <TextArea label={t('deactivation.details')} value={details} onChange={(e) => setDetails(e.target.value)} maxLength={500} />
              <Toggle checked={ack} onChange={setAck} label={t('deactivation.ack')} />
              <TextField label={t('deactivation.typeToConfirm', { word })} value={typed} onChange={(e) => setTyped(e.target.value)} autoComplete="off" />
              <div className="form-actions">
                <Button variant="danger" type="submit" loading={busy} disabled={!ack || typed.trim().toUpperCase() !== word.toUpperCase()}>{t('deactivation.submit')}</Button>
              </div>
            </form>
          </Panel>
        )}
      </Async>
    </>
  );
}
