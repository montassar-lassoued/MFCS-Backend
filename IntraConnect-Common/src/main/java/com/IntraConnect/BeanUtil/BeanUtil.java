package com.IntraConnect.BeanUtil;

import io.micrometer.common.lang.NonNullApi;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BeanUtil implements ApplicationContextAware {
	
	private static ApplicationContext context;
	
	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}
	
	/**
	 * Holt eine Bean aus dem Spring-Kontext anhand ihrer Klasse.
	 */
	public static <T> T getBean(Class<T> beanClass) {
		if (context == null) {
			throw new IllegalStateException("BeanUtil ist nicht initialisiert. Ist die Klasse eine @Component?");
		}
		return context.getBean(beanClass);
	}
}