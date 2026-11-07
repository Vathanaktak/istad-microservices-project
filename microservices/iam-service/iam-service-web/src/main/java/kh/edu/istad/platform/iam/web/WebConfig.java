package kh.edu.istad.platform.iam.web;

import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/** Locale from ?lang=en|km, remembered in a cookie. Default: English. */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    LocaleResolver localeResolver() {
        CookieLocaleResolver r = new CookieLocaleResolver("iam-lang");
        r.setDefaultLocale(Locale.ENGLISH);
        return r;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor i = new LocaleChangeInterceptor();
        i.setParamName("lang");
        registry.addInterceptor(i);
    }
}
