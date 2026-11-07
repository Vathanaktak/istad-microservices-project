@import kh.edu.istad.platform.iam.web.WebContext
@import kh.edu.istad.platform.iam.web.Forms.ProfileForm
@import java.util.Map
@param WebContext ctx
@param ProfileForm form
@param Map<String, String> errors
@param boolean saved

@template.layout(ctx = ctx, title = ctx.t("nav.edit"), content = @`
<section class="card narrow">
  <h1>${ctx.t("nav.edit")}</h1>
  @if(saved)<p class="notice">${ctx.t("common.saved")}</p>@endif
  <form method="post" action="/profile/edit">
    <input type="hidden" name="${ctx.csrfName()}" value="${ctx.csrfToken()}">
    <label>${ctx.t("field.givenName")}<input name="givenName" value="${form.givenName()}">
      @if(errors.containsKey("givenName"))<small class="err">${ctx.t(errors.get("givenName"))}</small>@endif</label>
    <label>${ctx.t("field.familyName")}<input name="familyName" value="${form.familyName()}">
      @if(errors.containsKey("familyName"))<small class="err">${ctx.t(errors.get("familyName"))}</small>@endif</label>
    <label>${ctx.t("field.nickname")}<input name="nickname" value="${form.nickname()}"></label>
    <label>${ctx.t("field.phone")}<input name="phoneNumber" value="${form.phoneNumber()}" placeholder="+85512345678">
      @if(errors.containsKey("phoneNumber"))<small class="err">${ctx.t(errors.get("phoneNumber"))}</small>@endif</label>
    <label>${ctx.t("field.language")}
      <select name="locale">
        <option value="en" selected="${form.locale().equals("en")}">English</option>
        <option value="km" selected="${form.locale().equals("km")}">ខ្មែរ</option>
      </select></label>
    <button type="submit" class="primary">${ctx.t("common.save")}</button>
  </form>
</section>
`)
