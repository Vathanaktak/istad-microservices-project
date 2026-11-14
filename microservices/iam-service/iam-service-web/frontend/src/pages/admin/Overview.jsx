import { Link } from 'react-router-dom';
import { Async, Panel, PageHeader } from '../../components/ui';
import { AuditTable } from './AuditLogs';
import { useData } from '../../components/hooks';
import { useI18n } from '../../i18n';
import { useAuth } from '../../auth/AuthContext';
import { clients, monitoring, permissions, roles, users } from '../../api/iam';

const total = (p) => p?.page?.totalElements ?? 0;
const settle = (promise) => promise.then((value) => ({ value }), (error) => ({ error }));

export default function Overview() {
  const { t, num } = useI18n();
  const { can } = useAuth();
  const state = useData(async () => {
    const [allUsers, activeUsers, lockedProbe, allRoles, allPerms, allClients, recent] = await Promise.all([
      can('user:read') ? settle(users.list({ size: 1 })) : { error: true },
      can('user:read') ? settle(users.list({ size: 1, enabled: true })) : { error: true },
      can('user:read') ? settle(users.list({ size: 100, enabled: true })) : { error: true },
      can('role:read') ? settle(roles.list({ size: 1 })) : { error: true },
      can('permission:read') ? settle(permissions.list({ size: 1 })) : { error: true },
      can('client:read') ? settle(clients.list({ size: 1 })) : { error: true },
      can('audit:read') ? settle(monitoring.auditLogs({ size: 8, sort: 'occurredAt,desc' })) : { error: true },
    ]);
    return { allUsers, activeUsers, lockedProbe, allRoles, allPerms, allClients, recent };
  }, []);

  return (
    <>
      <PageHeader title={t('overview.title')} description={t('overview.description')} />
      <Async state={state}>
        {(d) => {
          const val = (r, fmt = total) => (r.error ? '—' : num(fmt(r.value)));
          const locked = d.lockedProbe.error ? '—' : num(d.lockedProbe.value.content.filter((u) => u.locked).length);
          const stats = [
            { label: t('overview.users'), value: val(d.allUsers), to: '/admin/users', sub: t('overview.activeOf', { n: val(d.activeUsers) }) },
            { label: t('overview.locked'), value: locked, to: '/admin/users', sub: t('overview.lockedHint') },
            { label: t('overview.roles'), value: val(d.allRoles), to: '/admin/roles' },
            { label: t('overview.permissions'), value: val(d.allPerms), to: '/admin/permissions' },
            { label: t('overview.clients'), value: val(d.allClients), to: '/admin/clients' },
          ];
          return (
            <div className="stack-lg">
              <div className="figures">
                {stats.map((s) => (
                  <Link key={s.label} to={s.to} className="figure">
                    <span className="figure-value">{s.value}</span>
                    <span className="figure-label">{s.label}</span>
                    {s.sub && <small>{s.sub}</small>}
                  </Link>
                ))}
              </div>
              <Panel title={t('overview.recent')} flush actions={can('audit:read') && <Link className="link" to="/admin/audit-logs">{t('overview.viewAll')}</Link>}>
                {d.recent.error ? <p className="muted pad">{t('overview.recentUnavailable')}</p> : <AuditTable rows={d.recent.value.content} />}
              </Panel>
            </div>
          );
        }}
      </Async>
    </>
  );
}
