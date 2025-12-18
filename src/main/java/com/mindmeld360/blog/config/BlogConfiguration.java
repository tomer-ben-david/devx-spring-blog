package com.mindmeld360.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmeld360.blog.controller.BlogController;
import com.mindmeld360.blog.controller.RssFeedController;
import com.mindmeld360.blog.service.BlogService;
import com.mindmeld360.blog.util.UrlBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
@EnableConfigurationProperties(BlogProperties.class)
public class BlogConfiguration {

    @Bean
    public BlogService blogService(BlogProperties blogProperties, ResourcePatternResolver resourceResolver) {
        return new BlogService(blogProperties, resourceResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public UrlBuilder urlBuilder() {
        return new UrlBuilder();
    }

    @Bean
    public BlogController blogController(BlogService blogService, BlogProperties blogProperties,
                                         ObjectMapper objectMapper, UrlBuilder urlBuilder) {
        return new BlogController(blogService, blogProperties, objectMapper, urlBuilder);
    }

    @Bean
    public RssFeedController rssFeedController(BlogService blogService, BlogProperties blogProperties) {
        return new RssFeedController(blogService, blogProperties);
    }
}
