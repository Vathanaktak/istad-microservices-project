@import kh.edu.istad.platform.iam.web.WebContext
@import kh.edu.istad.platform.iam.application.result.ConsentInfoResult
@param WebContext ctx
@param ConsentInfoResult consent
@param String state

@template.layout(ctx = ctx, title = ctx.t("consent.title"), content = @`
<section class="card narrow">
  <h1>${ctx.t("consent.title")}</h1>
  <p>${ctx.t("consent.intro", consent.clientName())}</p>
  <ul class="scopes">
    @for(String scope : consent.scopes())
      <li>${ctx.t("scope." + scope)}</li>
    @endfor
  </ul>
  <form method="post" action="/oauth2/authorize" class="row">
    <input type="hidden" name="${ctx.csrfName()}" value="${ctx.csrfToken()}">
    <input type="hidden" name="client_id" value="${consent.clientId()}">
    <input type="hidden" name="state" value="${state}">
    @for(String scope : consent.scopes())
      <input type="hidden" name="scope" value="${scope}">
    @endfor
    <button type="submit" class="primary">${ctx.t("consent.allow")}</button>
  </form>
  <form method="post" action="/oauth2/authorize" class="row">
    <input type="hidden" name="${ctx.csrfName()}" value="${ctx.csrfToken()}">
    <input type="hidden" name="client_id" value="${consent.clientId()}">
    <input type="hidden" name="state" value="${state}">
    <button type="submit">${ctx.t("consent.cancel")}</button>
  </form>
</section>
`)
