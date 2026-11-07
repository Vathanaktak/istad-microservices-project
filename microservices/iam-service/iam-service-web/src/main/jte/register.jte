@import kh.edu.istad.platform.iam.web.WebContext
@import kh.edu.istad.platform.iam.web.Forms.RegisterForm
@import java.util.Map
@param WebContext ctx
@param RegisterForm form
@param Map<String, String> errors

@template.layout(ctx = ctx, title = ctx.t("register.title"), content = @`
<section class="card narrow">
  <h1>${ctx.t("register.title")}</h1>
  @if(errors.containsKey("_global"))<p class="alert">${ctx.t(errors.get("_global"))}</p>@endif
  <form method="post" action="/register">
    <input type="hidden" name="${ctx.csrfName()}" value="${ctx.csrfToken()}">
    <label>${ctx.t("field.givenName")}<input name="givenName" value="${form.givenName()}">
      @if(errors.containsKey("givenName"))<small class="err">${ctx.t(errors.get("givenName"))}</small>@endif</label>
    <label>${ctx.t("field.familyName")}<input name="familyName" value="${form.familyName()}">
      @if(errors.containsKey("familyName"))<small class="err">${ctx.t(errors.get("familyName"))}</small>@endif</label>
    <label>${ctx.t("field.username")}<input name="username" value="${form.username()}" autocomplete="username">
      @if(errors.containsKey("username"))<small class="err">${ctx.t(errors.get("username"))}</small>@endif</label>
    <label>${ctx.t("field.email")}<input type="email" name="email" value="${form.email()}" autocomplete="email">
      @if(errors.containsKey("email"))<small class="err">${ctx.t(errors.get("email"))}</small>@endif</label>
    <label>${ctx.t("field.password")}<input type="password" name="password" autocomplete="new-password">
      @if(errors.containsKey("password"))<small class="err">${ctx.t(errors.get("password"))}</small>@endif</label>
    <label>${ctx.t("field.confirmPassword")}<input type="password" name="confirmPassword" autocomplete="new-password">
      @if(errors.containsKey("_global"))<small class="err">${ctx.t(errors.get("_global"))}</small>@endif</label>
    <button type="submit" class="primary">${ctx.t("register.submit")}</button>
  </form>
  <p class="muted">${ctx.t("register.haveAccount")} <a href="/login">${ctx.t("login.title")}</a></p>
</section>
`)
