import { useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import AuthLayout from '../../components/AuthLayout';
import { Button, Spinner } from '../../components/ui';
import { useAuth } from '../../auth/AuthContext';
import { useI18n } from '../../i18n';

/** OAuth2 redirect target: exchanges ?code for tokens, then returns to where the user was heading. */
export default function Callback() {
  const { t } = useI18n();
  const { completeLogin, login } = useAuth();
  const [params] = useSearchParams();
  const nav = useNavigate();
  const [error, setError] = useState('');
  const started = useRef(false);

  useEffect(() => {
    if (started.current) return; // StrictMode runs effects twice; an auth code is single-use
    started.current = true;
    completeLogin(params).then((to) => nav(to, { replace: true })).catch((e) => setError(e.message));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <AuthLayout title={t('auth.signingIn')}>
      {error ? (
        <>
          <p className="note note-error" role="alert">{t('auth.callbackFailed')} <small>({error})</small></p>
          <Button variant="primary" onClick={() => login('/')}>{t('auth.tryAgain')}</Button>
        </>
      ) : <Spinner />}
    </AuthLayout>
  );
}
