package com.mindmeld360.blog.exception;

public class BlogNotFoundException extends RuntimeException {

    private final String slug;

    public BlogNotFoundException(String slug) {
        super("Blog post not found: " + slug);
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }
}
