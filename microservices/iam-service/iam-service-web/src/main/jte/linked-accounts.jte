@import kh.edu.istad.platform.iam.web.WebContext
@import kh.edu.istad.platform.iam.application.result.IdentityResult
@import java.util.List
@param WebContext ctx
@param List<IdentityResult> identities
@param List<String> available
@param String telegramBot
@param boolean hasError

@template.layout(ctx = ctx, title = ctx.t("profile.linkedAccounts"), content = @`
<section class="card">
  <h1>${ctx.t("profile.linkedAccounts")}</h1>
  @if(hasError)<p class="alert">${ctx.t("accounts.lastMethod")}</p>@endif
  @if(identities.isEmpty())<p class="muted">${ctx.t("accounts.none")}</p>@endif
  <ul class="list">
    @for(IdentityResult i : identities)
      <li>
        <strong>${i.provider()}</strong> <span class="muted">${i.displayName() != null ? i.displayName() : ""}</span>
        <form method="post" action="/profile/accounts/${i.uuid().toString()}/unlink" class="inline">
          <input type="hidden" name="${ctx.csrfName()}" value="${ctx.csrfToken()}">
          <button type="submit">${ctx.t("accounts.unlink")}</button>
        </form>
      </li>
    @endfor
  </ul>
  <h2>${ctx.t("accounts.link")}</h2>
  <div class="social">
    @for(String p : available)
      <a class="btn" href="/oauth2/authorization/${p}">${ctx.t("accounts.linkWith", p)}</a>
    @endfor
    @if(!telegramBot.isBlank())
      <script async src="https://telegram.org/js/telegram-widget.js?22" data-telegram-login="${telegramBot}"
              data-size="medium" data-auth-url="/login/telegram" data-request-access="write"></script>
    @endif
  </div>
</section>
`)
