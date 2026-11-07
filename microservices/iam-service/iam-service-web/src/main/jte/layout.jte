@import gg.jte.Content
@import kh.edu.istad.platform.iam.web.WebContext
@param WebContext ctx
@param String title
@param Content content

<!DOCTYPE html>
<html lang="${ctx.lang()}">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${title} · ISTAD IAM</title>
  <link rel="stylesheet" href="/css/app.css">
</head>
<body>
<header class="topbar">
  <a class="brand" href="/">ISTAD IAM</a>
  <nav>
    @if(ctx.authenticated())
      <a href="/">${ctx.t("nav.profile")}</a>
      <a href="/profile/edit">${ctx.t("nav.edit")}</a>
      <a href="/profile/password">${ctx.t("nav.password")}</a>
      <a href="/profile/accounts">${ctx.t("profile.linkedAccounts")}</a>
      <a href="/profile/apps">${ctx.t("nav.apps")}</a>
    @endif
  </nav>
  <div class="right">
    <a href="?lang=en" class="${ctx.lang().equals("en") ? "active" : ""}">EN</a> |
    <a href="?lang=km" class="${ctx.lang().equals("km") ? "active" : ""}">ខ្មែរ</a>
    @if(ctx.authenticated())
      <form method="post" action="/logout" class="inline">
        <input type="hidden" name="${ctx.csrfName()}" value="${ctx.csrfToken()}">
        <button class="link" type="submit">${ctx.t("common.logout")}</button>
      </form>
    @endif
  </div>
</header>
<main class="container">${content}</main>
</body>
</html>
