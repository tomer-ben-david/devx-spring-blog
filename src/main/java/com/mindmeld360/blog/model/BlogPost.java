package com.mindmeld360.blog.model;

import java.time.LocalDate;
import java.util.List;

public record BlogPost(
    String slug,
    String title,
    String description,
    String content,
    String htmlContent,
    LocalDate pubDate,
    LocalDate updatedDate,
    String heroImage,
    String author,
    List<String> tags,
    boolean draft
) {
    public BlogPost {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("slug cannot be null or blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title cannot be null or blank");
        }
        if (pubDate == null) {
            throw new IllegalArgumentException("pubDate cannot be null");
        }
        tags = tags != null ? List.copyOf(tags) : List.of();
    }

    public String getUrl() {
        return "/blog/" + slug;
    }

    /**
     * Returns the effective modification date (updatedDate if set, otherwise pubDate).
     */
    public LocalDate getEffectiveDate() {
        return updatedDate != null ? updatedDate : pubDate;
    }
}
