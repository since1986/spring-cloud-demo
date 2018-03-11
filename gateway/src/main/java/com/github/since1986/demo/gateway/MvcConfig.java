package com.github.since1986.demo.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.util.UrlPathHelper;

import java.nio.charset.Charset;
import java.util.List;

@ComponentScan
@Configuration
public class MvcConfig extends DelegatingWebMvcConfiguration {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Autowired
    public MvcConfig(ObjectMapper objectMapper, AppProperties appProperties) {
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setRemoveSemicolonContent(false); //相当于命名空间设置中的enable-matrix-variables
        urlPathHelper.setDefaultEncoding(appProperties.getDefaultCharacterEncoding());
        configurer.setUrlPathHelper(urlPathHelper);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new BufferedImageHttpMessageConverter());
        converters.add(new StringHttpMessageConverter(Charset.forName(appProperties.getDefaultCharacterEncoding())));
    }

    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        //注意前端的ajax请求要开启CORS并且要把cookies传过来（也就是withCredentials=true） jQuery ajax的例子：
        //jQuery.ajaxSetup({
        //     xhrFields: {
        //          withCredentials: true
        //      },
        //      crossDomain: true
        //});
        registry
                .addMapping(appProperties.getCors().getMapping())
                .allowedOrigins(appProperties.getCors().getAllowedOrigins().toArray(new String[]{}))
                .allowCredentials(appProperties.getCors().isAllowCredentials()) // setAllowCredentials(true) is important, otherwise: The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
                .allowedMethods(appProperties.getCors().getAllowedMethods().toArray(new String[]{}))
                .allowedHeaders(appProperties.getCors().getAllowedHeaders().toArray(new String[]{})); // setAllowedHeaders is important! Without it, OPTIONS preflight request will fail with 403 Invalid CORS request
    }
}