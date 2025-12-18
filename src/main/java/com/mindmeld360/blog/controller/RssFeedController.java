package com.mindmeld360.blog.controller;

import com.mindmeld360.blog.config.BlogProperties;
import com.mindmeld360.blog.model.BlogPost;
import com.mindmeld360.blog.service.BlogService;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@RestController
public class RssFeedController {

    private final BlogService blogService;
    private final BlogProperties blogProperties;

    public RssFeedController(BlogService blogService, BlogProperties blogProperties) {
        this.blogService = blogService;
        this.blogProperties = blogProperties;
    }

    @GetMapping(value = "/blog/rss.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String rssFeed() throws FeedException {
        Channel channel = new Channel();
        channel.setFeedType("rss_2.0");
        channel.setTitle(blogProperties.getTitle());
        channel.setDescription(blogProperties.getDescription());
        channel.setLink(blogProperties.getPublisherUrl() + "/blog");
        channel.setEncoding("UTF-8");
        channel.setLanguage("en");

        List<BlogPost> posts = blogService.getAllPosts();
        int maxItems = blogProperties.getRss().getMaxItems();

        List<Item> items = posts.stream()
            .limit(maxItems)
            .map(this::createRssItem)
            .toList();

        channel.setItems(items);

        WireFeedOutput output = new WireFeedOutput();
        return output.outputString(channel);
    }

    private Item createRssItem(BlogPost post) {
        Item item = new Item();
        item.setTitle(post.title());
        item.setLink(blogProperties.getPublisherUrl() + post.getUrl());
        item.setAuthor(post.author());

        // Convert LocalDate to Date with UTC timezone (RFC 822)
        Date pubDate = Date.from(post.pubDate().atStartOfDay(ZoneOffset.UTC).toInstant());
        item.setPubDate(pubDate);

        Description description = new Description();
        description.setType("text/plain");
        description.setValue(post.description());
        item.setDescription(description);

        return item;
    }
}
