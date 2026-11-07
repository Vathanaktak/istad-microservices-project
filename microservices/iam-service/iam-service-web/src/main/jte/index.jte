@import kh.edu.istad.platform.iam.web.WebContext
@import kh.edu.istad.platform.iam.application.result.AuthUserResult
@param WebContext ctx
@param AuthUserResult auth

@template.layout(ctx = ctx, title = ctx.t("profile.title"), content = @`
<section class="card">
  <h1>${ctx.t("profile.title")}</h1>
  <dl class="claims">
    <dt>sub</dt><dd>${auth.user().uuid().toString()}</dd>
    <dt>preferred_username</dt><dd>${auth.user().username()}</dd>
    <dt>name</dt><dd>${auth.user().name()}</dd>
    <dt>email</dt><dd>${auth.user().email()} @if(auth.user().emailVerified())✔@endif</dd>
    <dt>locale</dt><dd>${auth.user().locale()}</dd>
    @if(auth.user().phoneNumber() != null)<dt>phone_number</dt><dd>${auth.user().phoneNumber()}</dd>@endif
    @if(auth.user().zoneinfo() != null)<dt>zoneinfo</dt><dd>${auth.user().zoneinfo()}</dd>@endif
  </dl>
  <h2>${ctx.t("profile.roles")}</h2>
  <p>@for(String r : auth.roles())<span class="chip">${r}</span>@endfor</p>
  <h2>${ctx.t("profile.permissions")}</h2>
  <p>@for(String p : auth.permissions())<span class="chip">${p}</span>@endfor</p>
</section>
`)
