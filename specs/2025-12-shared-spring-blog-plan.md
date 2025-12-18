# Plan: Shared Spring Blog Library

## Overview
Extract blog functionality from market-thermometer into a reusable **opinionated** library for Spring Boot projects. This is a personal shared library with sensible defaults for the author's websites.

## Project Info

| Field | Value |
|-------|-------|
| Name | `shared-spring-blog` |
| GroupId | `com.mindmeld360` |
| ArtifactId | `shared-spring-blog` |
| Type | Library (not Starter) - consumers use `@Import` |
| License | MIT (public open source) |
| Min Spring Boot | 3.2+ |
| Java Version | 17+ |

## Dependencies

```xml
<!-- Core -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Markdown Processing -->
<dependency>
    <groupId>org.commonmark</groupId>
    <artifactId>commonmark</artifactId>
    <version>0.21.0</version>
</dependency>
<dependency>
    <groupId>org.commonmark</groupId>
    <artifactId>commonmark-ext-yaml-front-matter</artifactId>
    <version>0.21.0</version>
</dependency>
<dependency>
    <groupId>org.commonmark</groupId>
    <artifactId>commonmark-ext-gfm-tables</artifactId>
    <version>0.21.0</version>
</dependency>

<!-- RSS Feed -->
<dependency>
    <groupId>com.rometools</groupId>
    <artifactId>rome</artifactId>
    <version>2.1.0</version>
</dependency>

<!-- IDE Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

## Key Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Library vs Starter | Library | Explicit `@Import` - one line, no magic, easier to debug |
| Raw HTML in Markdown | Allowed (trusted) | Author-controlled content, not user-generated. Use `escapeHtml(false)` |
| URL Sanitization | CommonMark built-in | Use `sanitizeUrls(true)` - blocks dangerous schemes |
| Disqus/Social | ON by default | Opinionated - author's sites all use Disqus |
| Caching | Spring `@Cacheable` | Simpler than custom ReadWriteLock. Consumer must add `@EnableCaching` |
| Template path | `templates/blog/` | Standard Spring override behavior |
| GroupId | `com.mindmeld360` | Author owns this domain |
| YAML Parser | CommonMark YamlFrontMatter extension | Built-in, no extra dependency needed |
| RSS Library | Rome | Industry standard, handles escaping/encoding |

## Blog Content Specification

**Location:** `src/main/resources/content/blog/*.md` (classpath resources)

**Loading Strategy:** `ResourcePatternResolver` with `classpath*:content/blog/*.md`

**Front Matter Format (YAML):**
```yaml
---
title: "Post Title"
description: "Short description for SEO/RSS"
pubDate: 2025-12-14                # LocalDate format (YYYY-MM-DD)
author: "Author Name"              # Optional, falls back to blog.default-author
tags: ["Tag1", "Tag2"]             # Optional
draft: false                       # Optional, default false
---

Markdown content here...
```

**Date Handling:**
- Input: `pubDate` as `LocalDate` (YYYY-MM-DD)
- Storage: `LocalDate` in BlogPost record
- RSS Output: RFC 822 format with UTC timezone (e.g., `Sat, 14 Dec 2025 00:00:00 GMT`)

**Filename Convention:** `kebab-case-slug.md` → URL becomes `/blog/kebab-case-slug`

**Post Ordering:** By `pubDate` descending (newest first)

**Draft Handling:** Posts with `draft: true` are excluded from listing/RSS

**Malformed YAML Handling:** Skip post with warning log, don't fail startup

## What Gets Extracted

**Java classes:**
- BlogProperties, BlogService, BlogController, RssFeedController, BlogPost
- New: BlogConfiguration (for `@Import`)
- New: BlogNotFoundException (for 404 handling)

**Templates:**
- blog/index.html, blog/post.html, blog/not-found.html
- blog-fragments/disqus-comments.html, social-sharing.html

**CSS:**
- Blog-specific styles in `static/css/shared-blog.css`
- All classes prefixed with `sb-` (shared-blog) to avoid conflicts

## What Stays in Consumer Projects

- Site-specific fragments: `templates/fragments/header.html`, `footer.html`, `head.html`
- Blog content: `src/main/resources/content/blog/*.md`
- Configuration in application.properties

## Consumer Fragment Requirements

Library templates expect these fragments in consumer project:

```html
<!-- templates/fragments/header.html -->
<header th:fragment="header">...</header>

<!-- templates/fragments/footer.html -->
<footer th:fragment="footer">...</footer>

<!-- templates/fragments/head.html - must include blog CSS -->
<head th:fragment="head">
    ...
    <link rel="stylesheet" th:href="@{/css/shared-blog.css}">
</head>
```

Library templates use: `th:replace="~{fragments/header :: header}"`

## Configuration Properties

```properties
# Required
blog.title=
blog.description=
blog.publisher-url=
blog.publisher-name=

# Optional (with defaults)
blog.content-path=content/blog    # Classpath location for markdown files
blog.disqus.shortname=            # Required for comments to work
blog.disqus.enabled=true          # ON by default (opinionated)
blog.social-sharing.enabled=true  # ON by default (opinionated)
blog.medium-url=                  # For social sharing link
blog.default-author=              # Fallback when post has no author
blog.rss.max-items=20
```

**Validation:** Uses `@ConfigurationProperties` with `@Validated`. Missing required properties = clear startup error.

**IDE Support:** Includes `spring-boot-configuration-processor` for autocomplete in application.properties.

## Security

1. **Allow raw HTML** - CommonMark configured with `escapeHtml(false)` (trusted author content)
2. **URL sanitization** - CommonMark `sanitizeUrls(true)` blocks dangerous schemes (`javascript:`, `data:`)
3. **Template escaping:**
   - Front matter fields (title, description, author): escaped with `th:text`
   - Post body HTML: rendered with `th:utext` (safe because URLs sanitized, author-controlled content)

**Note:** This library assumes trusted markdown sources (author-controlled). Not suitable for user-generated content platforms.

## Error Handling

**404 - Post Not Found:**
- `BlogNotFoundException` thrown when slug doesn't match any post
- `@ExceptionHandler` in BlogController catches and returns `blog/not-found.html`
- Returns HTTP 404 status code

**Malformed Content:**
- Invalid YAML front matter: skip post, log warning, continue loading others
- Missing required front matter fields: skip post, log warning

**Missing Configuration:**
- Missing required properties: fail fast at startup with clear error message

## Caching

- Uses Spring `@Cacheable` on post loading methods
- Cache name: `blogPosts`
- **Consumer responsibility:** Add `@EnableCaching` and configure cache provider
- Without `@EnableCaching`, caching is a no-op (posts reload each request)
- Recommended: Caffeine for simple in-memory caching

```java
// Example consumer cache config
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager("blogPosts");
    manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)));
    return manager;
}
```

## Changes from Current Implementation

1. **Fix HTML escaping** - Keep `escapeHtml(false)` (trusted content), ensure `sanitizeUrls(true)`
2. **Extract Disqus config** - Move hardcoded shortname to BlogProperties
3. **Add draft support** - Add `draft` field to BlogPost, filter in service
4. **Create BlogConfiguration** - New class for `@Import`
5. **Add BlogNotFoundException** - Custom exception + handler for 404
6. **Use model attributes** - Replace `${@blogProperties.xxx}` bean lookups in templates
7. **Simplify cache** - Replace custom ReadWriteLock with `@Cacheable`
8. **Externalize hardcoded values** - Add `publisherName`, `defaultAuthor` properties
9. **Standard template path** - Use `templates/blog/` for normal override behavior
10. **CSS namespacing** - Prefix all classes with `sb-`, place in `static/css/shared-blog.css`
11. **Move fragments** - From `fragments/` to `blog-fragments/` for namespace

## Consumer Usage

```java
@EnableCaching  // Required for caching to work
@Import(BlogConfiguration.class)
@SpringBootApplication
public class MyApp { }
```

```properties
blog.title=My Blog
blog.description=My awesome blog
blog.publisher-url=https://example.com
blog.publisher-name=Example Inc
blog.disqus.shortname=my-site
blog.default-author=John Doe
```

Consumer must:
1. Add markdown files to `src/main/resources/content/blog/`
2. Provide `templates/fragments/header.html`, `footer.html`, `head.html`
3. Include `<link rel="stylesheet" th:href="@{/css/shared-blog.css}">` in head fragment

Blog works at `/blog`.

## OSS Requirements

- README with setup instructions and migration guide
- LICENSE (MIT)
- GitHub Actions for publishing to GitHub Packages
- Integration tests with MockMvc
- Unit tests for BlogService parsing logic

## Implementation Steps

1. Create GitHub repo (public, MIT license)
2. Set up Maven project structure with dependencies (see Dependencies section)
3. Write README with API contract and usage examples
4. Extract and refactor Java classes:
   - Create BlogConfiguration for `@Import`
   - Create BlogNotFoundException + handler
   - Add `@ConfigurationProperties` validation
   - Ensure `escapeHtml(false)` + `sanitizeUrls(true)` for security
   - Add draft field and filtering
   - Add logging for post discovery
   - Handle malformed YAML gracefully (skip with warning)
5. Extract and refactor templates (use model attributes)
6. Extract blog CSS to `static/css/shared-blog.css` (namespace with `sb-` prefix)
7. Write unit tests for BlogService (parsing, filtering, sorting)
8. Write integration tests (template rendering, 404 handling, config validation)
9. Set up GitHub Actions publish workflow
10. Create migration guide for market-thermometer
11. Update market-thermometer to use the library
12. Remove extracted code from market-thermometer

## Model Attributes (Template Contract)

Templates receive these model attributes:

| Attribute | Type | Description |
|-----------|------|-------------|
| `posts` | `List<BlogPost>` | All published posts (index page) |
| `post` | `BlogPost` | Current post (post page) |
| `blogTitle` | `String` | From config |
| `blogDescription` | `String` | From config |
| `publisherUrl` | `String` | From config |
| `publisherName` | `String` | From config |
| `disqusEnabled` | `boolean` | From config |
| `disqusShortname` | `String` | From config |
| `socialSharingEnabled` | `boolean` | From config |
| `mediumUrl` | `String` | From config |

## RSS Feed

- URL: `/blog/rss.xml`
- Generated using Rome library
- Uses `blog.rss.max-items` (default 20)
- **pubDate format:** RFC 822 with UTC (e.g., `Sat, 14 Dec 2025 00:00:00 GMT`)
- **description:** Plain text (HTML escaped by Rome)
- **link:** Canonical URL using `publisher-url` + `/blog/` + slug
- **encoding:** UTF-8
- Includes: title, description, pubDate, author, link
