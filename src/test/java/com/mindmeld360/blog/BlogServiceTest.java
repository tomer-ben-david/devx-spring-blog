package com.mindmeld360.blog;

import com.mindmeld360.blog.config.BlogProperties;
import com.mindmeld360.blog.exception.BlogNotFoundException;
import com.mindmeld360.blog.model.BlogPost;
import com.mindmeld360.blog.service.BlogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BlogServiceTest {

    private BlogService blogService;
    private BlogProperties blogProperties;

    @BeforeEach
    void setUp() {
        blogProperties = new BlogProperties();
        blogProperties.setTitle("Test Blog");
        blogProperties.setDescription("Test Description");
        blogProperties.setPublisherUrl("https://test.example.com");
        blogProperties.setPublisherName("Test Publisher");
        blogProperties.setDefaultAuthor("Default Author");
        blogProperties.setContentPath("content/blog");

        blogService = new BlogService(blogProperties, new PathMatchingResourcePatternResolver());
    }

    @Test
    void getAllPosts_shouldLoadPostsFromClasspath() {
        List<BlogPost> posts = blogService.getAllPosts();

        assertNotNull(posts);
        assertFalse(posts.isEmpty(), "Should load at least one post");
    }

    @Test
    void getAllPosts_shouldFilterDraftPosts() {
        List<BlogPost> posts = blogService.getAllPosts();

        boolean hasDraft = posts.stream().anyMatch(p -> p.slug().equals("draft-post"));
        assertFalse(hasDraft, "Draft posts should be filtered out");
    }

    @Test
    void getAllPosts_shouldParseYamlFrontMatter() {
        List<BlogPost> posts = blogService.getAllPosts();

        Optional<BlogPost> testPost = posts.stream()
            .filter(p -> p.slug().equals("test-post"))
            .findFirst();

        assertTrue(testPost.isPresent(), "Test post should exist");

        BlogPost post = testPost.get();
        assertEquals("Test Post Title", post.title());
        assertEquals("This is a test post description", post.description());
        assertEquals(LocalDate.of(2025, 12, 14), post.pubDate());
        assertEquals("Test Author", post.author());
        assertEquals(List.of("Test", "Example"), post.tags());
        assertFalse(post.draft());
    }

    @Test
    void getAllPosts_shouldRenderMarkdownToHtml() {
        List<BlogPost> posts = blogService.getAllPosts();

        Optional<BlogPost> testPost = posts.stream()
            .filter(p -> p.slug().equals("test-post"))
            .findFirst();

        assertTrue(testPost.isPresent());

        String html = testPost.get().htmlContent();
        assertNotNull(html);
        assertTrue(html.contains("<h2>"), "Should contain H2 heading");
        assertTrue(html.contains("<strong>test content</strong>"), "Should render bold text");
        assertTrue(html.contains("<li>"), "Should render list items");
        assertTrue(html.contains("<code>"), "Should render inline code");
        assertTrue(html.contains("<a href="), "Should render links");
    }

    @Test
    void getAllPosts_shouldSortByDateDescending() {
        List<BlogPost> posts = blogService.getAllPosts();

        if (posts.size() > 1) {
            for (int i = 0; i < posts.size() - 1; i++) {
                LocalDate current = posts.get(i).pubDate();
                LocalDate next = posts.get(i + 1).pubDate();
                assertTrue(current.isAfter(next) || current.isEqual(next),
                    "Posts should be sorted by date descending");
            }
        }
    }

    @Test
    void getPostBySlug_shouldReturnPostWhenExists() {
        Optional<BlogPost> post = blogService.getPostBySlug("test-post");

        assertTrue(post.isPresent());
        assertEquals("test-post", post.get().slug());
    }

    @Test
    void getPostBySlug_shouldReturnEmptyWhenNotExists() {
        Optional<BlogPost> post = blogService.getPostBySlug("non-existent-post");

        assertTrue(post.isEmpty());
    }

    @Test
    void getPostBySlugOrThrow_shouldThrowWhenNotExists() {
        assertThrows(BlogNotFoundException.class, () -> {
            blogService.getPostBySlugOrThrow("non-existent-post");
        });
    }

    @Test
    void getPostBySlugOrThrow_shouldReturnPostWhenExists() {
        BlogPost post = blogService.getPostBySlugOrThrow("test-post");

        assertNotNull(post);
        assertEquals("test-post", post.slug());
    }

    @Test
    void blogPost_shouldGenerateCorrectUrl() {
        BlogPost post = blogService.getPostBySlugOrThrow("test-post");

        assertEquals("/blog/test-post", post.getUrl());
    }
}
