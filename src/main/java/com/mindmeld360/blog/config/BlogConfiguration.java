package com.mindmeld360.blog.config;

import com.mindmeld360.blog.controller.BlogController;
import com.mindmeld360.blog.controller.RssFeedController;
import com.mindmeld360.blog.service.BlogService;
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
    public BlogController blogController(BlogService blogService, BlogProperties blogProperties) {
        return new BlogController(blogService, blogProperties);
    }

    @Bean
    public RssFeedController rssFeedController(BlogService blogService, BlogProperties blogProperties) {
        return new RssFeedController(blogService, blogProperties);
    }
}
