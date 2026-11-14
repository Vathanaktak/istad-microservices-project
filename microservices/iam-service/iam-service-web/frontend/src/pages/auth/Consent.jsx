import { useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Check } from 'lucide-react';
import AuthLayout from '../../components/AuthLayout';
import { Button, Spinner } from '../../components/ui';
import { useI18n } from '../../i18n';
import { ensureCsrf } from '../../auth/csrf';

export default function Consent() {
  const { t } = useI18n();
  const [params] = useSearchParams();
  const clientId = params.get('client_id') || '';
  const scope = params.get('scope') || '';
  const state = params.get('state') || '';
  const [info, setInfo] = useState(null);
  const [failed, setFailed] = useState(false);
  const formRef = useRef(null);
  const [busy, setBusy] = useState('');

  useEffect(() => {
    ensureCsrf();
    fetch(`/consent-info?${new URLSearchParams({ client_id: clientId, scope })}`, { headers: { Accept: 'application/json' } })
      .then((r) => (r.ok ? r.json() : Promise.reject()))
      .then(setInfo)
      .catch(() => setFailed(true));
  }, [clientId, scope]);

  const send = (allow) => async () => {
    setBusy(allow ? 'allow' : 'deny');
    formRef.current.elements._csrf.value = await ensureCsrf();
    formRef.current.querySelectorAll('input[name=scope]').forEach((i) => { i.disabled = !allow; });
    formRef.current.submit();
  };

  const scopes = info?.scopes || scope.split(' ').filter(Boolean);
  const label = (s) => { const k = `scope.${s}`; const v = t(k); return v === k ? s : v; };

  return (
    <AuthLayout title={t('consent.title')} subtitle={info ? t('consent.intro', { client: info.clientName || clientId }) : undefined}>
      {failed && <p className="note note-error" role="alert">{t('consent.failed')}</p>}
      {!info && !failed && <Spinner />}
      {info && (
        <>
          <ul className="scope-list">
            {scopes.map((s) => <li key={s}><Check size={16} />{label(s)}</li>)}
          </ul>
          <form ref={formRef} method="post" action="/oauth2/authorize">
            <input type="hidden" name="_csrf" defaultValue="" />
            <input type="hidden" name="client_id" value={clientId} />
            <input type="hidden" name="state" value={state} />
            {scopes.map((s) => <input key={s} type="hidden" name="scope" value={s} />)}
          </form>
          <div className="between consent-actions">
            <Button onClick={send(false)} loading={busy === 'deny'}>{t('consent.cancel')}</Button>
            <Button variant="primary" onClick={send(true)} loading={busy === 'allow'}>{t('consent.allow')}</Button>
          </div>
        </>
      )}
    </AuthLayout>
  );
}
