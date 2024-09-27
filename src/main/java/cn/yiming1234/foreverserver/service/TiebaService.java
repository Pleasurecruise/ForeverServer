package cn.yiming1234.foreverserver.service;

import cn.yiming1234.foreverserver.dto.TiebaDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class TiebaService {

    @Autowired
    private MainService mainService;

    public TiebaDTO getPosts() throws IOException {
        String keyword = "三丰云";
        log.info("Fetching posts for keyword: {}", keyword);
        String url = "https://tieba.baidu.com/f/search/res?ie=utf-8&qw=" + keyword;
        Document document;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("Error connecting to URL: {}", url, e);
            throw e;
        }

        Elements posts = document.select("div.s_post");
        List<TiebaDTO> postList = new ArrayList<>();
        int limit = Math.min(posts.size(), 5);

        for (int i = 0; i < limit; i++) {
            Element post = posts.get(i);
            Element titleElement = post.selectFirst("span.p_title a");
            String title = titleElement.text();
            String postUrl = "https://tieba.baidu.com" + titleElement.attr("href");
            String publishTime = post.selectFirst("font.p_green.p_date").text();

            TiebaDTO postData = new TiebaDTO(title, postUrl, publishTime);
            postList.add(postData);

            log.info("Post {}: Title: {}, URL: {}, Publish Time: {}", i + 1, title, postUrl, publishTime);
        }

        if (postList.isEmpty()) {
            log.info("No posts found for keyword: {}", keyword);
            return null;
        }

        Random random = new Random();
        TiebaDTO randomPost = postList.get(random.nextInt(postList.size()));
        log.info("Randomly selected post: Title: {}, URL: {}, Publish Time: {}", randomPost.getTitle(), randomPost.getUrl(), randomPost.getPublishTime());

        return randomPost;
    }
}