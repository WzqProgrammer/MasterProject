package com.wzqcode.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author 14188
 * @date 2021/2/15 12 :43
 * @description
 */
public class InterceptorConfig  extends WebMvcConfigurationSupport {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/resources/")
                .addResourceLocations("classpath:/static/")
                .addResourceLocations("classpath:/admin/")
                .addResourceLocations("classpath:/public/");
        super.addResourceHandlers(registry);
    }
}
