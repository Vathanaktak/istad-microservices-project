import { useEffect, useRef, useState } from 'react';
import { Github, Facebook, Link2, Send, Unlink, Chrome } from 'lucide-react';
import { Async, Badge, Button, EmptyState, PageHeader, Panel, useConfirm, useToast } from '../../components/ui';
import { useData } from '../../components/hooks';
import { describeError, useI18n } from '../../i18n';
import { me } from '../../api/iam';
import { SOCIAL_PROVIDERS } from '../../config';

const ICONS = { google: Chrome, github: Github, facebook: Facebook, telegram: Send };
const NAMES = { google: 'Google', github: 'GitHub', facebook: 'Facebook', telegram: 'Telegram' };

function TelegramLink({ bot }) {
  const ref = useRef(null);
  useEffect(() => {
    if (!bot || !ref.current) return;
    const s = document.createElement('script');
    s.async = true; s.src = 'https://telegram.org/js/telegram-widget.js?22';
    s.setAttribute('data-telegram-login', bot); s.setAttribute('data-size', 'medium');
    s.setAttribute('data-auth-url', '/login/telegram'); s.setAttribute('data-request-access', 'write');
    ref.current.appendChild(s);
  }, [bot]);
  return <div ref={ref} />;
}

export default function LinkedAccounts() {
  const { t, date } = useI18n();
  const toast = useToast();
  const confirm = useConfirm();
  const state = useData(() => me.identities(), []);
  const [bot, setBot] = useState('');
  useEffect(() => { fetch('/web-config').then((r) => r.json()).then((c) => setBot(c.telegramBot || '')).catch(() => {}); }, []);

  const unlink = async (i) => {
    const ok = await confirm({ title: t('linked.unlinkTitle', { provider: NAMES[i.provider.toLowerCase()] || i.provider }), message: t('linked.unlinkBody'), confirmLabel: t('linked.unlink'), danger: true });
    if (!ok) return;
    try { await me.unlink(i.uuid); toast.success(t('linked.unlinked')); state.reload(); }
    catch (e) { toast.error(e.status === 422 ? t('linked.lastMethod') : describeError(e, t)); }
  };
  // the OAuth2 login flow comes back to "/"; remember to send the user to this page afterwards
  const link = () => sessionStorage.setItem('iam.returnTo', '/account/linked');

  return (
    <>
      <PageHeader title={t('linked.title')} description={t('linked.description')} />
      <Async state={state}>
        {(list) => {
          const have = new Set(list.map((i) => i.provider.toLowerCase()));
          const available = SOCIAL_PROVIDERS.filter((p) => !have.has(p));
          return (
            <div className="cols">
              <Panel title={t('linked.connected')} flush>
                {list.length === 0 ? <EmptyState icon={Link2} title={t('linked.none')}>{t('linked.noneHint')}</EmptyState> : (
                  <ul className="rows">
                    {list.map((i) => {
                      const key = i.provider.toLowerCase(); const Icon = ICONS[key] || Link2;
                      return (
                        <li key={i.uuid}>
                          <span className="row-icon"><Icon size={18} /></span>
                          <div className="row-main">
                            <strong>{NAMES[key] || i.provider}</strong>
                            <small>{[i.displayName, i.email].filter(Boolean).join(' · ')}</small>
                            <small>{t('linked.since', { date: date(i.linkedAt, { dateStyle: 'medium' }) })}</small>
                          </div>
                          <Button size="sm" icon={Unlink} onClick={() => unlink(i)}>{t('linked.unlink')}</Button>
                        </li>
                      );
                    })}
                  </ul>
                )}
              </Panel>
              <Panel title={t('linked.link')} description={t('linked.linkHint')}>
                {available.length === 0 && !(bot && !have.has('telegram')) ? <p className="muted">{t('linked.allLinked')}</p> : (
                  <div className="social">
                    {available.map((p) => {
                      const Icon = ICONS[p];
                      return <a key={p} className="btn btn-secondary" href={`/oauth2/authorization/${p}`} onClick={link}><Icon size={16} />{t('linked.linkWith', { provider: NAMES[p] })}</a>;
                    })}
                    {bot && !have.has('telegram') && <TelegramLink bot={bot} />}
                  </div>
                )}
                <Badge tone="info">{t('linked.requiresSignIn')}</Badge>
              </Panel>
            </div>
          );
        }}
      </Async>
    </>
  );
}
