package com.mindmeld360.blog.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "blog")
@Validated
public class BlogProperties {

    @NotBlank(message = "blog.title is required")
    private String title;

    @NotBlank(message = "blog.description is required")
    private String description;

    @NotBlank(message = "blog.publisher-url is required")
    private String publisherUrl;

    private String publisherName = "";

    private String contentPath = "content/blog";
    private String defaultAuthor = "";
    private String mediumUrl = "";

    @Valid
    private Disqus disqus = new Disqus();
    @Valid
    private SocialSharing socialSharing = new SocialSharing();
    @Valid
    private Rss rss = new Rss();

    public static class Disqus {
        private boolean enabled = true;
        private String shortname = "";

        public boolean isEnabled() {
            return enabled && shortname != null && !shortname.isBlank();
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getShortname() {
            return shortname;
        }

        public void setShortname(String shortname) {
            this.shortname = shortname;
        }
    }

    public static class SocialSharing {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Rss {
        @Min(value = 1, message = "blog.rss.max-items must be at least 1")
        private int maxItems = 20;

        public int getMaxItems() {
            return maxItems;
        }

        public void setMaxItems(int maxItems) {
            this.maxItems = maxItems;
        }
    }

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisherUrl() {
        // Strip trailing slash to prevent double slashes in URL concatenation
        if (publisherUrl != null && publisherUrl.endsWith("/")) {
            return publisherUrl.substring(0, publisherUrl.length() - 1);
        }
        return publisherUrl;
    }

    public void setPublisherUrl(String publisherUrl) {
        this.publisherUrl = publisherUrl;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getDefaultAuthor() {
        return defaultAuthor;
    }

    public void setDefaultAuthor(String defaultAuthor) {
        this.defaultAuthor = defaultAuthor;
    }

    public String getMediumUrl() {
        return mediumUrl;
    }

    public void setMediumUrl(String mediumUrl) {
        this.mediumUrl = mediumUrl;
    }

    public Disqus getDisqus() {
        return disqus;
    }

    public void setDisqus(Disqus disqus) {
        this.disqus = disqus != null ? disqus : new Disqus();
    }

    public SocialSharing getSocialSharing() {
        return socialSharing;
    }

    public void setSocialSharing(SocialSharing socialSharing) {
        this.socialSharing = socialSharing != null ? socialSharing : new SocialSharing();
    }

    public Rss getRss() {
        return rss;
    }

    public void setRss(Rss rss) {
        this.rss = rss != null ? rss : new Rss();
    }
}
