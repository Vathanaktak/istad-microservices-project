@import kh.edu.istad.platform.iam.web.WebContext
@import kh.edu.istad.platform.iam.web.Forms.PasswordForm
@import java.util.Map
@param WebContext ctx
@param PasswordForm form
@param Map<String, String> errors
@param boolean saved
@param boolean hasPassword

@template.layout(ctx = ctx, title = ctx.t("nav.password"), content = @`
<section class="card narrow">
  <h1>${ctx.t("nav.password")}</h1>
  @if(saved)<p class="notice">${ctx.t("common.saved")}</p>@endif
  <form method="post" action="/profile/password">
    <input type="hidden" name="${ctx.csrfName()}" value="${ctx.csrfToken()}">
    @if(hasPassword)
      <label>${ctx.t("field.currentPassword")}<input type="password" name="currentPassword" autocomplete="current-password">
        @if(errors.containsKey("currentPassword"))<small class="err">${ctx.t(errors.get("currentPassword"))}</small>@endif</label>
    @endif
    <label>${ctx.t("field.newPassword")}<input type="password" name="newPassword" autocomplete="new-password">
      @if(errors.containsKey("newPassword"))<small class="err">${ctx.t(errors.get("newPassword"))}</small>@endif</label>
    <label>${ctx.t("field.confirmPassword")}<input type="password" name="confirmPassword" autocomplete="new-password">
      @if(errors.containsKey("_global"))<small class="err">${ctx.t(errors.get("_global"))}</small>@endif</label>
    <button type="submit" class="primary">${ctx.t("common.save")}</button>
  </form>
</section>
`)
