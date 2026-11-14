// Mirrors Common.PASSWORD_REGEX in the REST API
export const PASSWORD_RE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/;
export const validatePassword = (p) => PASSWORD_RE.test(p || '');
export const PASSWORD_HINT = 'validation.password';
export const PHONE_RE = /^\+[1-9]\d{7,14}$/;
export const ROLE_NAME_RE = /^[A-Z][A-Z0-9_]*$/;

/** 0–4 strength score for the meter */
export function passwordScore(p = '') {
  let s = 0;
  if (p.length >= 8) s++;
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) s++;
  if (/\d/.test(p)) s++;
  if (/[^A-Za-z0-9]/.test(p)) s++;
  return s;
}
