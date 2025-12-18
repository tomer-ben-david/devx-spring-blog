package com.mindmeld360.blog.controller;

import com.mindmeld360.blog.config.BlogProperties;
import com.mindmeld360.blog.exception.BlogNotFoundException;
import com.mindmeld360.blog.model.BlogPost;
import com.mindmeld360.blog.service.BlogService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Controller
@RequestMapping("/blog")
public class BlogController {

    private final BlogService blogService;
    private final BlogProperties blogProperties;

    public BlogController(BlogService blogService, BlogProperties blogProperties) {
        this.blogService = blogService;
        this.blogProperties = blogProperties;
    }

    @GetMapping
    public String index(Model model) {
        List<BlogPost> posts = blogService.getAllPosts();

        addCommonAttributes(model);
        model.addAttribute("posts", posts);

        return "blog/index";
    }

    @GetMapping("/{slug}")
    public String post(@PathVariable String slug, Model model) {
        BlogPost post = blogService.getPostBySlugOrThrow(slug);

        addCommonAttributes(model);
        model.addAttribute("post", post);

        return "blog/post";
    }

    @ExceptionHandler(BlogNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(BlogNotFoundException ex, Model model) {
        addCommonAttributes(model);
        model.addAttribute("slug", ex.getSlug());
        return "blog/not-found";
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("blogTitle", blogProperties.getTitle());
        model.addAttribute("blogDescription", blogProperties.getDescription());
        model.addAttribute("publisherUrl", blogProperties.getPublisherUrl());
        model.addAttribute("publisherName", blogProperties.getPublisherName());
        model.addAttribute("disqusEnabled", blogProperties.getDisqus().isEnabled());
        model.addAttribute("disqusShortname", blogProperties.getDisqus().getShortname());
        model.addAttribute("socialSharingEnabled", blogProperties.getSocialSharing().isEnabled());
        model.addAttribute("mediumUrl", blogProperties.getMediumUrl());
    }
}
