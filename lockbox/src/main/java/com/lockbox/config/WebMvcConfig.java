package com.lockbox.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CsrfInterceptor csrfInterceptor;

    @Autowired
    public WebMvcConfig(CsrfInterceptor csrfInterceptor) {
        this.csrfInterceptor = csrfInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(csrfInterceptor);
    }
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(false)
            .ignoreAcceptHeader(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("js", MediaType.valueOf("application/javascript"));
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(org.springframework.http.CacheControl.noCache())
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
                
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCacheControl(org.springframework.http.CacheControl.noCache())
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
                
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCacheControl(org.springframework.http.CacheControl.noCache())
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }
} 