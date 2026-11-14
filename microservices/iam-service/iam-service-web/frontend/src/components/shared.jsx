import { Laptop, Smartphone, Tablet, ShieldAlert } from 'lucide-react';
import { Badge, Button, DataTable, EmptyState } from './ui';
import { useI18n } from '../i18n';
import { MonitorSmartphone, History } from 'lucide-react';

const deviceIcon = (s) => {
  const d = `${s.device} ${s.os}`.toLowerCase();
  if (/iphone|android|pixel|mobile/.test(d)) return Smartphone;
  if (/ipad|tablet/.test(d)) return Tablet;
  return Laptop;
};

/** Active sessions as rows: device, browser, IP, location, last activity + terminate. Used by account and admin pages. */
export function SessionList({ sessions, onTerminate, busy }) {
  const { t, date, ago } = useI18n();
  if (!sessions.length) return <EmptyState icon={MonitorSmartphone} title={t('sessions.none')}>{t('sessions.noneHint')}</EmptyState>;
  return (
    <ul className="rows">
      {sessions.map((s) => {
        const Icon = deviceIcon(s);
        return (
          <li key={s.id}>
            <span className="row-icon"><Icon size={18} /></span>
            <div className="row-main">
              <strong>{s.device} {s.current && <Badge tone="info">{t('sessions.thisDevice')}</Badge>}</strong>
              <small>{[s.browser, s.os].filter(Boolean).join(' · ')}</small>
              <small>{[s.ipAddress, s.location].filter(Boolean).join(' · ')}</small>
              <small title={date(s.lastActiveAt)}>{t('sessions.lastActive', { when: ago(s.lastActiveAt) })} — {t('sessions.signedIn', { when: date(s.createdAt, { dateStyle: 'medium' }) })}</small>
            </div>
            <Button size="sm" disabled={busy} onClick={() => onTerminate(s)}>{s.current ? t('sessions.signOutHere') : t('sessions.terminate')}</Button>
          </li>
        );
      })}
    </ul>
  );
}

export function StatusBadge({ status }) {
  const { t } = useI18n();
  return status === 'SUCCESS' ? <Badge tone="ok">{t('history.success')}</Badge> : <Badge tone="danger">{t('history.failed')}</Badge>;
}

/** Login attempts table; `showUser` adds the account column (admin monitoring). */
export function LoginTable({ rows, showUser }) {
  const { t, date } = useI18n();
  const columns = [
    { key: 'occurredAt', header: t('history.when'), render: (r) => <span className="nowrap">{date(r.occurredAt)}</span> },
    ...(showUser ? [{ key: 'username', header: t('history.account') }] : []),
    { key: 'status', header: t('history.result'), render: (r) => (
      <span className="stack-xs"><StatusBadge status={r.status} />{r.reason && <small className="muted">{t(`history.reason.${r.reason}`)}</small>}</span>) },
    { key: 'device', header: t('history.device'), render: (r) => <span className="stack-xs"><span>{r.device}</span><small className="muted">{[r.browser, r.os].filter(Boolean).join(' · ')}</small></span> },
    { key: 'ip', header: t('history.ip'), render: (r) => <span className="stack-xs"><span className="num">{r.ipAddress}</span><small className="muted">{r.location}</small></span> },
    ...(showUser ? [{ key: 'suspicious', header: '', render: (r) => (r.suspicious ? <Badge tone="warn"><ShieldAlert size={12} /> {t('history.suspicious')}</Badge> : null) }] : []),
  ];
  return <DataTable rows={rows} columns={columns} rowKey="id"
    empty={<EmptyState icon={History} title={t('history.none')}>{t('history.noneHint')}</EmptyState>} />;
}
