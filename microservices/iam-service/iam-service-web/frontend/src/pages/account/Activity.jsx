import { useState } from 'react';
import { Activity as ActivityIcon, KeyRound, Link2, LogIn, MailCheck, MonitorOff, UserPen, Image, Unlink, UserX, UserCheck } from 'lucide-react';
import { Async, EmptyState, PageHeader, Pagination, Panel } from '../../components/ui';
import { useData } from '../../components/hooks';
import { useI18n } from '../../i18n';
import { me } from '../../api/iam';

const ICON = {
  PASSWORD_CHANGED: KeyRound, PROFILE_UPDATED: UserPen, IDENTITY_LINKED: Link2, IDENTITY_UNLINKED: Unlink,
  SESSION_TERMINATED: MonitorOff, EMAIL_VERIFIED: MailCheck, LOGIN_SUCCESS: LogIn, PROFILE_PICTURE_UPDATED: Image,
  DEACTIVATION_REQUESTED: UserX, DEACTIVATION_CANCELLED: UserCheck, DEACTIVATION_APPROVED: UserX, DEACTIVATION_REJECTED: UserCheck,
};

export default function Activity() {
  const { t, date, ago } = useI18n();
  const [page, setPage] = useState(0);
  const state = useData(() => me.activity({ page, size: 10, sort: 'occurredAt,desc' }), [page]);
  return (
    <>
      <PageHeader title={t('activity.title')} description={t('activity.description')} />
      <Async state={state}>
        {(p) => (
          <Panel flush>
            {p.content.length === 0 ? <EmptyState icon={ActivityIcon} title={t('activity.none')} /> : (
              <ul className="timeline">
                {p.content.map((a) => {
                  const Icon = ICON[a.type] || ActivityIcon;
                  const key = `activity.type.${a.type}`; const label = t(key);
                  return (
                    <li key={a.id}>
                      <span className="row-icon"><Icon size={16} /></span>
                      <div className="row-main">
                        <strong>{label === key ? a.description : label}</strong>
                        <small>{[a.ipAddress, date(a.occurredAt)].filter(Boolean).join(' · ')}</small>
                      </div>
                      <small className="muted nowrap">{ago(a.occurredAt)}</small>
                    </li>
                  );
                })}
              </ul>
            )}
            <Pagination page={p.page} onPage={setPage} />
          </Panel>
        )}
      </Async>
    </>
  );
}
