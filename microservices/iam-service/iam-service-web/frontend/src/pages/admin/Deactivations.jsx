import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Check, X, UserX } from 'lucide-react';
import { Avatar } from '../../components/Shell';
import { Async, Badge, Button, DataTable, EmptyState, Modal, PageHeader, Pagination, Panel, SelectField, TextArea, useConfirm, useToast } from '../../components/ui';
import { useData } from '../../components/hooks';
import { describeError, useI18n } from '../../i18n';
import { useAuth } from '../../auth/AuthContext';
import { deactivationRequests } from '../../api/iam';

export default function Deactivations() {
  const { t, date } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const confirm = useConfirm();
  const [status, setStatus] = useState('PENDING');
  const [page, setPage] = useState(0);
  const [modal, setModal] = useState(null); // { type: 'approve'|'reject', req }
  const [reviewNotes, setReviewNotes] = useState('');
  const [busy, setBusy] = useState(false);

  const state = useData(() => deactivationRequests.list({ status, page, size: 10 }), [status, page]);

  const submitAction = async () => {
    if (!modal) return;
    setBusy(true);
    try {
      if (modal.type === 'approve') {
        await deactivationRequests.approve(modal.req.uuid, { reviewNotes: reviewNotes.trim() || undefined });
        toast.success(t('deactivation.approved'));
      } else {
        await deactivationRequests.reject(modal.req.uuid, { reviewNotes: reviewNotes.trim() || undefined });
        toast.success(t('deactivation.rejected'));
      }
      setModal(null);
      setReviewNotes('');
      state.reload();
    } catch (err) {
      toast.error(describeError(err, t));
    } finally {
      setBusy(false);
    }
  };

  const columns = [
    {
      key: 'user',
      header: t('users.user'),
      render: (r) => (
        <Link to={`/admin/users/${r.userUuid}`} className="person">
          <Avatar name={r.name || r.username} size={32} />
          <span className="stack-xs">
            <strong>{r.name || r.username}</strong>
            <small className="muted">@{r.username} · {r.email}</small>
          </span>
        </Link>
      ),
    },
    {
      key: 'reason',
      header: t('deactivation.reason'),
      render: (r) => (
        <span className="stack-xs">
          <strong>{t(`deactivation.reason.${r.reason}`, r.reason)}</strong>
          {r.details && <small className="muted">{r.details}</small>}
        </span>
      ),
    },
    {
      key: 'requestedAt',
      header: t('deactivation.requestedOn'),
      render: (r) => date(r.requestedAt),
    },
    {
      key: 'status',
      header: t('common.status'),
      render: (r) => {
        const tone = r.status === 'PENDING' ? 'warn' : r.status === 'PROCESSED' ? 'neutral' : 'danger';
        return <Badge tone={tone}>{r.status}</Badge>;
      },
    },
    ...(can('user:update') ? [{
      key: 'actions',
      header: <span className="sr-only">{t('common.actions')}</span>,
      className: 'actions',
      render: (r) => r.status === 'PENDING' ? (
        <span className="row-actions">
          <Button size="sm" variant="danger" icon={Check} onClick={() => { setModal({ type: 'approve', req: r }); setReviewNotes(''); }}>
            {t('deactivation.approve')}
          </Button>
          <Button size="sm" icon={X} onClick={() => { setModal({ type: 'reject', req: r }); setReviewNotes(''); }}>
            {t('deactivation.reject')}
          </Button>
        </span>
      ) : (
        r.reviewedBy && <small className="muted">{r.reviewedBy} · {date(r.processedAt)}</small>
      ),
    }] : []),
  ];

  return (
    <>
      <PageHeader
        title={t('deactivation.proposals')}
        description={t('deactivation.description')}
        actions={
          <SelectField
            value={status}
            onChange={(e) => { setStatus(e.target.value); setPage(0); }}
            options={[
              { value: 'PENDING', label: t('deactivation.pending') },
              { value: 'PROCESSED', label: 'PROCESSED' },
              { value: 'REJECTED', label: 'REJECTED' },
              { value: 'ALL', label: 'All' },
            ]}
          />
        }
      />

      <Panel flush>
        <Async state={state}>
          {(p) => (
            <>
              {p.content.length === 0 ? (
                <EmptyState icon={UserX} title={t('deactivation.noProposals')} />
              ) : (
                <DataTable columns={columns} rows={p.content} />
              )}
              <Pagination page={p.page.number} totalPages={p.page.totalPages} onPageChange={setPage} />
            </>
          )}
        </Async>
      </Panel>

      {modal && (
        <Modal
          title={modal.type === 'approve'
            ? t('deactivation.approveConfirmTitle', { name: modal.req.name || modal.req.username })
            : t('deactivation.rejectConfirmTitle', { name: modal.req.name || modal.req.username })}
          onClose={() => setModal(null)}
          footer={
            <>
              <Button onClick={() => setModal(null)}>{t('common.cancel')}</Button>
              <Button
                variant={modal.type === 'approve' ? 'danger' : 'primary'}
                loading={busy}
                onClick={submitAction}
              >
                {modal.type === 'approve' ? t('deactivation.approve') : t('deactivation.reject')}
              </Button>
            </>
          }
        >
          <div className="stack">
            <p className="muted">
              {modal.type === 'approve'
                ? t('deactivation.approveConfirmBody')
                : t('deactivation.rejectConfirmBody')}
            </p>
            <TextArea
              label={t('deactivation.reviewNotes')}
              value={reviewNotes}
              onChange={(e) => setReviewNotes(e.target.value)}
              placeholder="..."
              maxLength={500}
            />
          </div>
        </Modal>
      )}
    </>
  );
}
