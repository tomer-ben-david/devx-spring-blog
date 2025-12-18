package com.mindmeld360.blog.service;

import com.mindmeld360.blog.config.BlogProperties;
import com.mindmeld360.blog.exception.BlogNotFoundException;
import com.mindmeld360.blog.model.BlogPost;
import org.commonmark.Extension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class BlogService {

    private static final Logger log = LoggerFactory.getLogger(BlogService.class);

    private final BlogProperties blogProperties;
    private final ResourcePatternResolver resourceResolver;
    private final Parser parser;
    private final HtmlRenderer htmlRenderer;

    public BlogService(BlogProperties blogProperties, ResourcePatternResolver resourceResolver) {
        this.blogProperties = blogProperties;
        this.resourceResolver = resourceResolver;

        List<Extension> extensions = List.of(
            YamlFrontMatterExtension.create(),
            TablesExtension.create()
        );

        this.parser = Parser.builder()
            .extensions(extensions)
            .build();

        this.htmlRenderer = HtmlRenderer.builder()
            .extensions(extensions)
            .sanitizeUrls(true)
            .escapeHtml(false) // Trusted author content
            .build();
    }

    public List<BlogPost> getAllPosts() {
        log.info("Loading blog posts from classpath: {}", blogProperties.getContentPath());

        String pattern = "classpath*:" + blogProperties.getContentPath() + "/*.md";
        List<BlogPost> posts = new ArrayList<>();

        try {
            Resource[] resources = resourceResolver.getResources(pattern);
            log.info("Found {} markdown files", resources.length);

            for (Resource resource : resources) {
                try {
                    BlogPost post = parsePost(resource);
                    if (post != null && !post.draft()) {
                        posts.add(post);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse blog post from {}: {}", resource.getFilename(), e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Failed to load blog posts: {}", e.getMessage());
        }

        posts.sort(Comparator.comparing(BlogPost::pubDate).reversed()
            .thenComparing(BlogPost::slug));

        log.info("Loaded {} published blog posts", posts.size());
        return posts;
    }

    public Optional<BlogPost> getPostBySlug(String slug) {
        return getAllPosts().stream()
            .filter(post -> post.slug().equals(slug))
            .findFirst();
    }

    public BlogPost getPostBySlugOrThrow(String slug) {
        return getPostBySlug(slug)
            .orElseThrow(() -> new BlogNotFoundException(slug));
    }

    private BlogPost parsePost(Resource resource) throws IOException {
        String filename = resource.getFilename();
        if (filename == null) {
            return null;
        }

        String slug = filename.endsWith(".md")
            ? filename.substring(0, filename.length() - 3)
            : filename;

        String content;
        try (var inputStream = resource.getInputStream()) {
            content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        Node document = parser.parse(content);

        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);
        Map<String, List<String>> frontMatter = visitor.getData();

        String title = getFirstValue(frontMatter, "title");
        if (title == null || title.isBlank()) {
            log.warn("Skipping post {}: missing title", filename);
            return null;
        }

        String pubDateStr = getFirstValue(frontMatter, "pubDate");
        LocalDate pubDate;
        try {
            pubDate = LocalDate.parse(pubDateStr);
        } catch (DateTimeParseException | NullPointerException e) {
            log.warn("Skipping post {}: invalid or missing pubDate", filename);
            return null;
        }

        String description = getFirstValue(frontMatter, "description");
        if (description == null) {
            description = "";
        }

        String author = getFirstValue(frontMatter, "author");
        if (author == null || author.isBlank()) {
            author = blogProperties.getDefaultAuthor();
        }

        List<String> tags = frontMatter.getOrDefault("tags", List.of());
        boolean draft = "true".equalsIgnoreCase(getFirstValue(frontMatter, "draft"));

        String htmlContent = htmlRenderer.render(document);

        return new BlogPost(slug, title, description, content, htmlContent, pubDate, author, tags, draft);
    }

    private String getFirstValue(Map<String, List<String>> frontMatter, String key) {
        List<String> values = frontMatter.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
}
