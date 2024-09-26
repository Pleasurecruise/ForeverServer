package cn.yiming1234.foreverserver.service;

import cn.yiming1234.foreverserver.properties.LinkAiProperties;
import cn.yiming1234.foreverserver.util.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
public class PostArticleService {

    @Autowired
    private LinkAiProperties linkAiProperties;

    @Autowired
    private AliOssUtil aliOssUtil;

    private static final String API_URL = "https://api.link-ai.tech/v1/chat/completions";

    /**
     * 调用AI生成文字
     */
    public String getText(String userInput) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("app_code", linkAiProperties.getApp_code());
            requestBody.put("top_p", 1);
            requestBody.put("frequency_penalty", 2);
            requestBody.put("presence_penalty", 2);
            JSONArray messagesArray = new JSONArray();
            messagesArray.put(new JSONObject().put("role", "user").put("content", userInput));
            requestBody.put("messages", messagesArray);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + linkAiProperties.getApi_key())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray choices = jsonResponse.getJSONArray("choices");

                if (choices.length() > 0) {
                    JSONObject assistantMessage = choices.getJSONObject(0).getJSONObject("message");
                    return assistantMessage.getString("content");
                } else {
                    return "没有收到回复内容。";
                }
            } else {
                throw new RuntimeException("API 请求失败，状态码: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "请求失败: " + e.getMessage();
        }
    }

    /**
     * 发布论坛或文章
     *
     * @return
     */
    public String postArticle() {
        // 爬取文章
        // 调用AI生成文字
        // 发布文章
        // 返回url地址
        return null;
    }

    /**
     * 根据url进行截图,上传OSS
     */
    public void takeScreenshot() {
        String url = postArticle();

    }
}
