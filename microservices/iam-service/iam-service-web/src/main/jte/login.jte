@import kh.edu.istad.platform.iam.web.WebContext
@param WebContext ctx
@param String telegramBot
@param boolean hasError
@param boolean loggedOut

@template.layout(ctx = ctx, title = ctx.t("login.title"), content = @`
<section class="card narrow">
  <h1>${ctx.t("login.title")}</h1>
  @if(hasError)<p class="alert">${ctx.t("login.error")}</p>@endif
  @if(loggedOut)<p class="notice">${ctx.t("login.loggedOut")}</p>@endif
  <form method="post" action="/login">
    <input type="hidden" name="${ctx.csrfName()}" value="${ctx.csrfToken()}">
    <label>${ctx.t("field.usernameOrEmail")}<input name="username" autocomplete="username" required autofocus></label>
    <label>${ctx.t("field.password")}<input type="password" name="password" autocomplete="current-password" required></label>
    <button type="submit" class="primary">${ctx.t("login.submit")}</button>
  </form>
  <div class="divider"><span>${ctx.t("login.or")}</span></div>
  <div class="social">
    <a class="btn" href="/oauth2/authorization/google">${ctx.t("login.continueWith", "Google")}</a>
    <a class="btn" href="/oauth2/authorization/github">${ctx.t("login.continueWith", "GitHub")}</a>
    <a class="btn" href="/oauth2/authorization/facebook">${ctx.t("login.continueWith", "Facebook")}</a>
    @if(!telegramBot.isBlank())
      <script async src="https://telegram.org/js/telegram-widget.js?22" data-telegram-login="${telegramBot}"
              data-size="large" data-auth-url="/login/telegram" data-request-access="write"></script>
    @endif
  </div>
  <p class="muted">${ctx.t("login.noAccount")} <a href="/register">${ctx.t("register.title")}</a></p>
</section>
`)
