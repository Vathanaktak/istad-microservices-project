import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import en from './en';
import km from './km';

const dictionaries = { en, km };
const I18nContext = createContext(null);
export const useI18n = () => useContext(I18nContext);

const initialLang = () => {
  const saved = localStorage.getItem('iam.lang');
  if (saved === 'en' || saved === 'km') return saved;
  return navigator.language?.toLowerCase().startsWith('km') ? 'km' : 'en';
};

export function I18nProvider({ children }) {
  const [lang, setLangState] = useState(initialLang);
  const setLang = useCallback((l) => { localStorage.setItem('iam.lang', l); setLangState(l); }, []);
  useEffect(() => { document.documentElement.lang = lang; }, [lang]);

  const value = useMemo(() => {
    const t = (key, vars) => {
      const raw = dictionaries[lang][key] ?? en[key] ?? key;
      return vars ? raw.replace(/\{(\w+)\}/g, (_, k) => (vars[k] ?? `{${k}}`)) : raw;
    };
    const locale = lang === 'km' ? 'km-KH' : 'en-GB';
    const date = (v, opts = { dateStyle: 'medium', timeStyle: 'short' }) => (v ? new Intl.DateTimeFormat(locale, opts).format(new Date(v)) : '—');
    const ago = (v) => {
      if (!v) return '—';
      const diff = (new Date(v).getTime() - Date.now()) / 1000;
      const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' });
      const units = [['day', 86400], ['hour', 3600], ['minute', 60]];
      for (const [u, s] of units) if (Math.abs(diff) >= s) return rtf.format(Math.round(diff / s), u);
      return rtf.format(0, 'second');
    };
    const num = (n) => new Intl.NumberFormat(locale).format(n ?? 0);
    return { lang, setLang, t, date, ago, num };
  }, [lang, setLang]);

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

/** Turns a server validation / problem error into a readable string. */
export function describeError(err, t) {
  if (!err) return '';
  if (err.status === 403) return t('error.forbidden');
  if (err.status === 401) return t('error.unauthorized');
  if (err.status === 409) return err.problem?.detail || t('error.conflict');
  if (err.fieldErrors && Object.keys(err.fieldErrors).length) return Object.values(err.fieldErrors).join(' · ');
  return err.problem?.detail || err.message || t('error.generic');
}
