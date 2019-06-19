package com.transformedge.apps.appconfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class InternationalizationMessagesSourceConfig extends AcceptHeaderLocaleResolver implements WebMvcConfigurer{

//	@Bean
//	public LocaleResolver localeResolver() {
//		SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
//		sessionLocaleResolver.setDefaultLocale(Locale.US);
//		return sessionLocaleResolver;
//	}
//
//	@Bean
//	public LocaleChangeInterceptor localeChangeInterceptor() {
//		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
//		localeChangeInterceptor.setParamName("lang");
//		localeChangeInterceptor.setIgnoreInvalidLocale(true);
//		return localeChangeInterceptor;
//	}
//
//	public void addInterceptors(InterceptorRegistry interceptorRegistry) {
//		interceptorRegistry.addInterceptor(localeChangeInterceptor());
//	}

	List<Locale> LOCALES = Arrays.asList(new Locale("fr"),new Locale("ge"));

	public Locale resolveLocale(HttpServletRequest request) {
		String headerLang = request.getHeader("Accept-Language");
		return headerLang == null || headerLang.isEmpty()
				? Locale.getDefault()
						: Locale.lookup(Locale.LanguageRange.parse(headerLang), LOCALES);
	}

	@Bean
	public ResourceBundleMessageSource localeMessageSource() {
		ResourceBundleMessageSource rs = new ResourceBundleMessageSource();
		rs.setBasename("messages");
		rs.setDefaultEncoding("UTF-8");
		rs.setUseCodeAsDefaultMessage(true);
		return rs;
	}

}
