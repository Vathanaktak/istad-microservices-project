import { useMemo, useState } from 'react';
import { Badge, SearchBox } from './ui';
import { useI18n } from '../i18n';

export function UserStatus({ user }) {
  const { t } = useI18n();
  return (
    <span className="badges">
      {user.enabled ? <Badge tone="ok">{t('status.active')}</Badge> : <Badge tone="neutral">{t('status.disabled')}</Badge>}
      {user.locked && <Badge tone="danger">{t('status.locked')}</Badge>}
    </span>
  );
}

/** Searchable multi-select list of checkboxes. items: [{ value, label, hint }] */
export function Picker({ items, selected, onChange, placeholder, empty }) {
  const [q, setQ] = useState('');
  const shown = useMemo(() => items.filter((i) => `${i.label} ${i.hint || ''}`.toLowerCase().includes(q.toLowerCase())), [items, q]);
  const toggle = (v) => onChange(selected.includes(v) ? selected.filter((x) => x !== v) : [...selected, v]);
  return (
    <div className="picker">
      <SearchBox value={q} onChange={setQ} placeholder={placeholder} />
      <div className="picker-list" role="group">
        {shown.length === 0 && <p className="muted pad">{empty}</p>}
        {shown.map((i) => (
          <label key={i.value} className="check">
            <input type="checkbox" checked={selected.includes(i.value)} onChange={() => toggle(i.value)} />
            <span>{i.label}{i.hint && <small>{i.hint}</small>}</span>
          </label>
        ))}
      </div>
    </div>
  );
}

/** Fetches every page of a list endpoint (for pickers). */
export async function fetchAll(listFn, query = {}) {
  const first = await listFn({ ...query, page: 0, size: 100 });
  let all = first.content;
  for (let p = 1; p < Math.min(first.page.totalPages, 10); p++) all = all.concat((await listFn({ ...query, page: p, size: 100 })).content);
  return all;
}
