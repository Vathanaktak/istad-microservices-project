import { useEffect, useState } from 'react';
import { RotateCcw, Save } from 'lucide-react';
import { Async, Button, PageHeader, Panel, TextField, Toggle, useToast } from '../../components/ui';
import { useData } from '../../components/hooks';
import { describeError, useI18n } from '../../i18n';
import { useAuth } from '../../auth/AuthContext';
import { system } from '../../api/iam';

const RULES = {
  'authentication.maxFailedAttempts': [1, 20], 'authentication.lockoutMinutes': [1, 1440],
  'password.minLength': [8, 72], 'password.expireDays': [0, 730], 'password.historyCount': [0, 24],
  'session.idleTimeoutMinutes': [1, 1440], 'session.absoluteTimeoutHours': [1, 720], 'session.maxConcurrentSessions': [1, 50],
  'token.accessTokenMinutes': [1, 1440], 'token.refreshTokenHours': [1, 8760], 'token.idTokenMinutes': [1, 1440], 'token.authorizationCodeMinutes': [1, 30],
};

function Form({ initial, onSaved }) {
  const { t } = useI18n();
  const { can } = useAuth();
  const toast = useToast();
  const [cfg, setCfg] = useState(initial);
  const [busy, setBusy] = useState(false);
  const [errors, setErrors] = useState({});
  useEffect(() => setCfg(initial), [initial]);
  const dirty = JSON.stringify(cfg) !== JSON.stringify(initial);
  const writable = can('system:update');
  const set = (group, key, value) => setCfg((c) => ({ ...c, [group]: { ...c[group], [key]: value } }));

  const num = (group, key, hint) => (
    <TextField label={t(`system.${group}.${key}`)} type="number" hint={hint ? t(hint) : undefined} disabled={!writable}
      value={cfg[group][key]} error={errors[`${group}.${key}`]} min={RULES[`${group}.${key}`]?.[0]} max={RULES[`${group}.${key}`]?.[1]}
      onChange={(e) => set(group, key, e.target.value === '' ? '' : Number(e.target.value))} />
  );
  const flag = (group, key, hint) => <Toggle label={t(`system.${group}.${key}`)} hint={hint ? t(hint) : undefined} checked={cfg[group][key]} disabled={!writable} onChange={(v) => set(group, key, v)} />;

  const save = async (e) => {
    e.preventDefault();
    const local = {};
    Object.entries(RULES).forEach(([path, [min, max]]) => {
      const [g, k] = path.split('.');
      const v = cfg[g]?.[k];
      if (typeof v !== 'number' || Number.isNaN(v) || v < min || v > max) local[path] = t('system.range', { min, max });
    });
    if (cfg.token.accessTokenMinutes > cfg.token.refreshTokenHours * 60) local['token.accessTokenMinutes'] = t('system.accessVsRefresh');
    setErrors(local);
    if (Object.keys(local).length) return toast.error(t('system.fixErrors'));
    setBusy(true);
    try { onSaved(await system.saveConfig(cfg)); toast.success(t('system.saved')); }
    catch (err) { toast.error(describeError(err, t)); } finally { setBusy(false); }
  };

  return (
    <form onSubmit={save} className="stack-lg" noValidate>
      <Panel title={t('system.authentication')} description={t('system.authenticationHint')}>
        <div className="grid-2">{num('authentication', 'maxFailedAttempts', 'system.maxFailedHint')}{num('authentication', 'lockoutMinutes')}</div>
        <div className="stack">{flag('authentication', 'allowRegistration', 'system.allowRegistrationHint')}{flag('authentication', 'allowSocialLogin')}{flag('authentication', 'requireVerifiedEmail')}</div>
      </Panel>
      <Panel title={t('system.passwordPolicy')} description={t('system.passwordHint')}>
        <div className="grid-3">{num('password', 'minLength')}{num('password', 'expireDays', 'system.zeroOff')}{num('password', 'historyCount', 'system.zeroOff')}</div>
        <div className="grid-2 toggles">{flag('password', 'requireUppercase')}{flag('password', 'requireLowercase')}{flag('password', 'requireDigit')}{flag('password', 'requireSymbol')}</div>
      </Panel>
      <Panel title={t('system.sessionPolicy')} description={t('system.sessionHint')}>
        <div className="grid-3">{num('session', 'idleTimeoutMinutes')}{num('session', 'absoluteTimeoutHours')}{num('session', 'maxConcurrentSessions')}</div>
      </Panel>
      <Panel title={t('system.tokenPolicy')} description={t('system.tokenHint')}>
        <div className="grid-2">{num('token', 'accessTokenMinutes')}{num('token', 'refreshTokenHours')}{num('token', 'idTokenMinutes')}{num('token', 'authorizationCodeMinutes')}</div>
      </Panel>
      {writable && (
        <div className="savebar" role="region" aria-label={t('system.title')}>
          <span>{dirty ? t('system.unsaved') : t('system.upToDate')}</span>
          <Button icon={RotateCcw} type="button" disabled={!dirty || busy} onClick={() => { setCfg(initial); setErrors({}); }}>{t('common.reset')}</Button>
          <Button variant="primary" icon={Save} type="submit" loading={busy} disabled={!dirty}>{t('common.saveChanges')}</Button>
        </div>
      )}
    </form>
  );
}

export default function SystemConfig() {
  const { t } = useI18n();
  const state = useData(() => system.getConfig(), []);
  return (
    <>
      <PageHeader title={t('system.title')} description={t('system.description')} />
      <Async state={state}>{(cfg) => <Form initial={cfg} onSaved={state.setData} />}</Async>
    </>
  );
}
