import { createContext, useCallback, useContext, useEffect, useId, useMemo, useRef, useState } from 'react';
import { AlertTriangle, CheckCircle2, ChevronLeft, ChevronRight, Info, Loader2, Search, X } from 'lucide-react';
import { useI18n } from '../i18n';

// ---- primitives -------------------------------------------------------------------------------
export function Button({ variant = 'secondary', size, loading, icon: Icon, children, className = '', ...rest }) {
  return (
    <button className={`btn btn-${variant} ${size === 'sm' ? 'btn-sm' : ''} ${className}`} disabled={loading || rest.disabled} {...rest}>
      {loading ? <Loader2 size={16} className="spin" /> : Icon ? <Icon size={16} /> : null}
      {children}
    </button>
  );
}

export function Badge({ tone = 'neutral', children }) {
  return <span className={`badge badge-${tone}`}>{children}</span>;
}

export function Spinner({ label }) {
  return <div className="state" role="status"><Loader2 className="spin" size={20} />{label && <span>{label}</span>}</div>;
}

export function EmptyState({ icon: Icon = Info, title, children }) {
  return (
    <div className="state empty">
      <Icon size={22} />
      <strong>{title}</strong>
      {children && <p>{children}</p>}
    </div>
  );
}

export function ErrorState({ error, onRetry }) {
  const { t } = useI18n();
  const forbidden = error?.status === 403;
  return (
    <div className="state error" role="alert">
      <AlertTriangle size={22} />
      <strong>{forbidden ? t('error.forbidden') : t('error.loadFailed')}</strong>
      {!forbidden && error?.message && <p>{error.message}</p>}
      {onRetry && !forbidden && <Button size="sm" onClick={onRetry}>{t('common.retry')}</Button>}
    </div>
  );
}

export function PageHeader({ title, description, actions }) {
  return (
    <header className="page-head">
      <div>
        <h1>{title}</h1>
        {description && <p>{description}</p>}
      </div>
      {actions && <div className="page-actions">{actions}</div>}
    </header>
  );
}

export function Panel({ title, description, actions, children, tone, flush }) {
  return (
    <section className={`panel ${tone ? `panel-${tone}` : ''}`}>
      {(title || actions) && (
        <div className="panel-head">
          <div>
            {title && <h2>{title}</h2>}
            {description && <p>{description}</p>}
          </div>
          {actions && <div className="panel-actions">{actions}</div>}
        </div>
      )}
      <div className={flush ? 'panel-body flush' : 'panel-body'}>{children}</div>
    </section>
  );
}

// ---- form fields ------------------------------------------------------------------------------
export function Field({ label, hint, error, children, className = '' }) {
  const id = useId();
  const child = typeof children === 'function' ? children(id) : children;
  return (
    <div className={`field ${error ? 'has-error' : ''} ${className}`}>
      {label && <label htmlFor={id}>{label}</label>}
      {child}
      {hint && !error && <small className="hint">{hint}</small>}
      {error && <small className="err" role="alert">{error}</small>}
    </div>
  );
}

export function TextField({ label, hint, error, className, ...input }) {
  return <Field label={label} hint={hint} error={error} className={className}>{(id) => <input id={id} {...input} />}</Field>;
}

export function SelectField({ label, hint, error, options, className, ...select }) {
  return (
    <Field label={label} hint={hint} error={error} className={className}>
      {(id) => (
        <select id={id} {...select}>
          {options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      )}
    </Field>
  );
}

export function TextArea({ label, hint, error, className, ...ta }) {
  return <Field label={label} hint={hint} error={error} className={className}>{(id) => <textarea id={id} rows={3} {...ta} />}</Field>;
}

export function Toggle({ label, hint, checked, onChange, disabled }) {
  return (
    <label className="toggle">
      <input type="checkbox" role="switch" checked={!!checked} disabled={disabled} onChange={(e) => onChange(e.target.checked)} />
      <span className="toggle-track" aria-hidden="true"><span /></span>
      <span className="toggle-text">{label}{hint && <small>{hint}</small>}</span>
    </label>
  );
}

export function SearchBox({ value, onChange, placeholder }) {
  return (
    <div className="search">
      <Search size={16} aria-hidden="true" />
      <input type="search" value={value} onChange={(e) => onChange(e.target.value)} placeholder={placeholder} aria-label={placeholder} />
    </div>
  );
}

/** Edits a list of strings (redirect URIs, scopes …) one per line. */
export function ListField({ label, hint, error, values, onChange, placeholder }) {
  return (
    <TextArea label={label} hint={hint} error={error} placeholder={placeholder}
      value={(values || []).join('\n')}
      onChange={(e) => onChange(e.target.value.split('\n').map((s) => s.trim()).filter(Boolean))} />
  );
}

// ---- data table ---------------------------------------------------------------------------------
export function DataTable({ columns, rows, rowKey = 'uuid', empty, onRowClick }) {
  if (!rows?.length) return empty || null;
  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>{columns.map((c) => <th key={c.key} className={c.className} scope="col">{c.header}</th>)}</tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r[rowKey]} className={onRowClick ? 'clickable' : ''} onClick={onRowClick ? () => onRowClick(r) : undefined}>
              {columns.map((c) => <td key={c.key} className={c.className} data-label={typeof c.header === 'string' ? c.header : undefined}>{c.render ? c.render(r) : r[c.key]}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function Pagination({ page, onPage }) {
  const { t, num } = useI18n();
  if (!page || page.totalPages <= 1) {
    return page ? <div className="pager"><span>{t('common.totalItems', { n: num(page.totalElements) })}</span></div> : null;
  }
  return (
    <nav className="pager" aria-label={t('common.pagination')}>
      <span>{t('common.totalItems', { n: num(page.totalElements) })}</span>
      <div>
        <Button size="sm" icon={ChevronLeft} disabled={page.number <= 0} onClick={() => onPage(page.number - 1)} aria-label={t('common.previous')} />
        <span className="pager-pos">{t('common.pageOf', { n: num(page.number + 1), total: num(page.totalPages) })}</span>
        <Button size="sm" icon={ChevronRight} disabled={page.number >= page.totalPages - 1} onClick={() => onPage(page.number + 1)} aria-label={t('common.next')} />
      </div>
    </nav>
  );
}

export function Tabs({ tabs, value, onChange }) {
  return (
    <div className="tabs" role="tablist">
      {tabs.map((tab) => (
        <button key={tab.key} role="tab" aria-selected={value === tab.key} className={value === tab.key ? 'on' : ''} onClick={() => onChange(tab.key)}>
          {tab.label}
        </button>
      ))}
    </div>
  );
}

// ---- modal ----------------------------------------------------------------------------------
export function Modal({ title, onClose, children, footer, wide }) {
  const ref = useRef(null);
  const { t } = useI18n();
  useEffect(() => {
    const prev = document.activeElement;
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    ref.current?.querySelector('input, select, textarea, button.btn-primary, button.btn-danger')?.focus();
    return () => { document.removeEventListener('keydown', onKey); prev?.focus?.(); };
  }, [onClose]);
  return (
    <div className="overlay" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className={`modal ${wide ? 'wide' : ''}`} role="dialog" aria-modal="true" aria-label={title} ref={ref}>
        <div className="modal-head">
          <h2>{title}</h2>
          <button className="icon-btn" onClick={onClose} aria-label={t('common.close')}><X size={18} /></button>
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-foot">{footer}</div>}
      </div>
    </div>
  );
}

// ---- toasts + confirm -----------------------------------------------------------------------
const FeedbackContext = createContext(null);
export const useToast = () => useContext(FeedbackContext).toast;
export const useConfirm = () => useContext(FeedbackContext).confirm;

export function FeedbackProvider({ children }) {
  const { t } = useI18n();
  const [toasts, setToasts] = useState([]);
  const [dialog, setDialog] = useState(null);

  const push = useCallback((tone, message) => {
    const id = Math.random();
    setToasts((l) => [...l, { id, tone, message }]);
    setTimeout(() => setToasts((l) => l.filter((x) => x.id !== id)), tone === 'error' ? 7000 : 3500);
  }, []);
  const toast = useMemo(() => ({ success: (m) => push('success', m), error: (m) => push('error', m), info: (m) => push('info', m) }), [push]);

  const confirm = useCallback((opts) => new Promise((resolve) => setDialog({ ...opts, resolve })), []);
  const close = (v) => { dialog.resolve(v); setDialog(null); };

  return (
    <FeedbackContext.Provider value={useMemo(() => ({ toast, confirm }), [toast, confirm])}>
      {children}
      <div className="toasts" aria-live="polite">
        {toasts.map((x) => (
          <div key={x.id} className={`toast toast-${x.tone}`}>
            {x.tone === 'success' ? <CheckCircle2 size={18} /> : x.tone === 'error' ? <AlertTriangle size={18} /> : <Info size={18} />}
            <span>{x.message}</span>
          </div>
        ))}
      </div>
      {dialog && (
        <Modal title={dialog.title} onClose={() => close(false)}
          footer={<>
            <Button onClick={() => close(false)}>{t('common.cancel')}</Button>
            <Button variant={dialog.danger ? 'danger' : 'primary'} onClick={() => close(true)}>{dialog.confirmLabel || t('common.confirm')}</Button>
          </>}>
          <p className="confirm-text">{dialog.message}</p>
        </Modal>
      )}
    </FeedbackContext.Provider>
  );
}

// ---- async + misc helpers -------------------------------------------------------------------
/** Renders loading / error / content for a useData() state. */
export function Async({ state, children }) {
  if (state.loading && !state.data) return <Spinner />;
  if (state.error) return <ErrorState error={state.error} onRetry={state.reload} />;
  return children(state.data);
}

export function PasswordMeter({ score }) {
  const { t } = useI18n();
  const labels = ['', 'password.weak', 'password.fair', 'password.good', 'password.strong'];
  return (
    <div className="meter" aria-live="polite">
      <div className={`meter-bar s${score}`}>{[1, 2, 3, 4].map((i) => <i key={i} className={i <= score ? 'on' : ''} />)}</div>
      <small>{score ? t(labels[score]) : ''}</small>
    </div>
  );
}

export function Chips({ items, empty }) {
  if (!items?.length) return <span className="muted">{empty || '—'}</span>;
  return <div className="chips">{items.map((i) => <span className="chip" key={i}>{i}</span>)}</div>;
}

export function KeyValue({ rows }) {
  return (
    <dl className="kv">
      {rows.filter(Boolean).map(([k, v]) => (<div key={k}><dt>{k}</dt><dd>{v ?? '—'}</dd></div>))}
    </dl>
  );
}
