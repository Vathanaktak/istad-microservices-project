@import kh.edu.istad.platform.iam.web.WebContext
@import kh.edu.istad.platform.iam.application.result.ConnectedAppResult
@import java.util.List
@param WebContext ctx
@param List<ConnectedAppResult> apps

@template.layout(ctx = ctx, title = ctx.t("nav.apps"), content = @`
<section class="card">
  <h1>${ctx.t("nav.apps")}</h1>
  @if(apps.isEmpty())<p class="muted">${ctx.t("apps.none")}</p>@endif
  <ul class="list">
    @for(ConnectedAppResult a : apps)
      <li>
        <strong>${a.clientName()}</strong>
        <span>@for(String s : a.scopes())<span class="chip">${s}</span>@endfor</span>
        <form method="post" action="/profile/apps/${a.clientId()}/revoke" class="inline">
          <input type="hidden" name="${ctx.csrfName()}" value="${ctx.csrfToken()}">
          <button type="submit">${ctx.t("apps.revoke")}</button>
        </form>
      </li>
    @endfor
  </ul>
</section>
`)
