import { LogOut } from 'lucide-react';
import { Async, Button, PageHeader, Panel, useConfirm, useToast } from '../../components/ui';
import { SessionList } from '../../components/shared';
import { useData } from '../../components/hooks';
import { describeError, useI18n } from '../../i18n';
import { useAuth } from '../../auth/AuthContext';
import { me } from '../../api/iam';

export default function Sessions() {
  const { t } = useI18n();
  const toast = useToast();
  const confirm = useConfirm();
  const { logout } = useAuth();
  const state = useData(() => me.sessions(), []);

  const terminate = async (s) => {
    const ok = await confirm({
      title: s.current ? t('sessions.signOutHereTitle') : t('sessions.terminateTitle'),
      message: s.current ? t('sessions.signOutHereBody') : t('sessions.terminateBody', { device: s.device }),
      confirmLabel: s.current ? t('sessions.signOutHere') : t('sessions.terminate'), danger: true,
    });
    if (!ok) return;
    try {
      await me.terminateSession(s.id);
      if (s.current) return logout();
      toast.success(t('sessions.terminated'));
      state.reload();
    } catch (e) { toast.error(describeError(e, t)); }
  };

  const terminateAll = async () => {
    if (!(await confirm({ title: t('sessions.allTitle'), message: t('sessions.allBody'), confirmLabel: t('sessions.allConfirm'), danger: true }))) return;
    try { await me.terminateAllSessions(); logout(); }
    catch (e) { toast.error(describeError(e, t)); }
  };

  return (
    <>
      <PageHeader title={t('sessions.title')} description={t('sessions.description')} />
      <Async state={state}>
        {(list) => (
          <div className="stack-lg">
            <Panel title={t('sessions.active', { n: list.length })} flush>
              <SessionList sessions={list} onTerminate={terminate} />
            </Panel>
            <Panel tone="danger" title={t('sessions.allTitle')} description={t('sessions.allHint')}
              actions={<Button variant="danger" icon={LogOut} onClick={terminateAll} disabled={!list.length}>{t('sessions.allConfirm')}</Button>} />
          </div>
        )}
      </Async>
    </>
  );
}
