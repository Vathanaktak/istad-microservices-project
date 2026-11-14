const read = () => {
  const c = document.cookie.split('; ').find((x) => x.startsWith('XSRF-TOKEN='));
  return c ? decodeURIComponent(c.split('=')[1]) : '';
};

/** Spring issues the XSRF-TOKEN cookie on any request it handles; ask for it if the page itself was served by Vite. */
export async function ensureCsrf() {
  let token = read();
  if (!token) {
    await fetch('/web-config', { credentials: 'same-origin' }).catch(() => {});
    token = read();
  }
  return token;
}
