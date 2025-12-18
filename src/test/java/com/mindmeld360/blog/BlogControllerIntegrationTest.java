package com.mindmeld360.blog;

import com.mindmeld360.blog.config.BlogConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest
@Import(BlogConfiguration.class)
@TestPropertySource(locations = "classpath:application.properties")
class BlogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void blogIndex_shouldReturnBlogPage() throws Exception {
        mockMvc.perform(get("/blog"))
            .andExpect(status().isOk())
            .andExpect(view().name("blog/index"))
            .andExpect(model().attributeExists("posts"))
            .andExpect(model().attribute("blogTitle", "Test Blog"))
            .andExpect(model().attribute("blogDescription", "A test blog for unit tests"));
    }

    @Test
    void blogIndex_shouldContainPosts() throws Exception {
        mockMvc.perform(get("/blog"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("posts", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void blogPost_shouldReturnPostPage() throws Exception {
        mockMvc.perform(get("/blog/test-post"))
            .andExpect(status().isOk())
            .andExpect(view().name("blog/post"))
            .andExpect(model().attributeExists("post"))
            .andExpect(model().attribute("disqusEnabled", true))
            .andExpect(model().attribute("disqusShortname", "test-disqus"));
    }

    @Test
    void blogPost_shouldReturn404ForNonExistent() throws Exception {
        mockMvc.perform(get("/blog/non-existent-post"))
            .andExpect(status().isNotFound())
            .andExpect(view().name("blog/not-found"))
            .andExpect(model().attribute("slug", "non-existent-post"));
    }

    @Test
    void rssFeed_shouldReturnXml() throws Exception {
        mockMvc.perform(get("/blog/rss.xml"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/xml"))
            .andExpect(content().string(containsString("<rss")))
            .andExpect(content().string(containsString("Test Blog")))
            .andExpect(content().string(containsString("Test Post Title")));
    }

    @Test
    void rssFeed_shouldContainRfc822Date() throws Exception {
        mockMvc.perform(get("/blog/rss.xml"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("<pubDate>")));
    }
}
