package cn.yiming1234.foreverserver.service;

import cn.yiming1234.foreverserver.dto.TiebaDTO;
import cn.yiming1234.foreverserver.properties.MailProperties;
import cn.yiming1234.foreverserver.properties.ServerProperties;
import cn.yiming1234.foreverserver.util.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ServerService {

    @Autowired
    private MainService mainService;

    @Autowired
    private TiebaService tiebaService;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private MailProperties mailProperties;

    private static final String LOGIN_URL = "https://api.sanfengyun.com/www/login.php";
    private static final String VPS_URL = "https://api.sanfengyun.com/www/vps.php";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36";
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";

    /**
     * 获取Session ID
     */
    public String getSessionId() throws Exception {
        String sessionId = null;
        HttpClient client = HttpClient.newHttpClient();

        String requestBody = String.format(
                "cmd=login&id_mobile=%s&password=%s",
                serverProperties.getUsername(),
                serverProperties.getPassword()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_URL))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", CONTENT_TYPE)
                .header("Origin", "https://www.sanfengyun.com")
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        if (statusCode == 200) {
            String setCookie = response.headers().firstValue("Set-Cookie").orElse("");
            if (!setCookie.isEmpty()) {
                try {
                    sessionId = setCookie.split(";")[0].split("=")[1];
                    log.info("获取到的响应：{}", response.body());
                    log.info("获取到的Session ID: {}", sessionId);
                } catch (Exception e) {
                    log.error("从Set-Cookie头中解析Session ID失败", e);
                    throw new RuntimeException("解析Session ID失败");
                }
            } else {log.error("响应中未找到Set-Cookie头");
                throw new RuntimeException("未找到Set-Cookie头");
            }
        } else {
            log.error("登录失败，状态码: {}", statusCode);
            throw new RuntimeException("登录失败，状态码: " + statusCode);
        }
        return sessionId;
    }

    /**
     * 解析时间字符串为小时数
     */
    private int parseTimeToHours(String timeStr) {
        int days = 0;
        int hours = 0;

        Pattern dayPattern = Pattern.compile("(\\d+)天");
        Matcher dayMatcher = dayPattern.matcher(timeStr);
        if (dayMatcher.find()) {
            days = Integer.parseInt(dayMatcher.group(1));
        }

        Pattern hourPattern = Pattern.compile("(\\d+)小时");
        Matcher hourMatcher = hourPattern.matcher(timeStr);
        if (hourMatcher.find()) {
            hours = Integer.parseInt(hourMatcher.group(1));
        }

        return days * 24 + hours;
    }

    /**
     * 获取服务器到期时间
     */
    @Scheduled(fixedRate = 10800000)
    public void getTime() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String sessionId = getSessionId();

            String requestBody = "cmd=vps_list&vps_type=free";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VPS_URL))
                    .header("User-Agent", USER_AGENT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cookie", "session_id=" + sessionId)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
                String responseBody = response.body();
                log.info("VPS接口返回数据: {}", responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject msgObject = jsonResponse.getJSONObject("msg");
                JSONArray contentArray = msgObject.getJSONArray("content");

                if (contentArray.length() > 0) {
                    JSONObject vpsInfo = contentArray.getJSONObject(0);
                    String leftTime = vpsInfo.getString("left_time");
                    log.info("VPS 剩余时间: {}", leftTime);
                    int totalHours = parseTimeToHours(leftTime);
                    log.info("剩余时间(小时): {}", totalHours);

                    /*如果小于12小时开始执行操作*/
                    if (totalHours < 12) {
                        mainAction();
                        whenfail();
                    } else {
                        log.info("VPS剩余时间超过12小时");
                    }
                } else {
                    log.warn("VPS信息为空");
                }
            } else {
                log.error("获取VPS信息失败，状态码: {}", statusCode);
                throw new RuntimeException("获取VPS信息失败，状态码: " + statusCode);
            }
        } catch (Exception e) {
            log.error("获取VPS信息时发生错误", e);
        }
    }

    /**
     * 获取审核状态
     */
    public String getStatus() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String sessionId = getSessionId();

            String requestBody = "cmd=free_delay_list&ptype=vps&count=10&page=1";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.sanfengyun.com/www/renew.php"))
                    .header("User-Agent", USER_AGENT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cookie", "session_id=" + sessionId)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
                String responseBody = response.body();
                log.info("审核状态接口返回数据: {}", responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject msgObject = jsonResponse.getJSONObject("msg");
                JSONArray contentArray = msgObject.getJSONArray("content");

                if (contentArray.length() > 0) {
                    JSONObject latestEntry = contentArray.getJSONObject(0);
                    String auditState = latestEntry.getString("State");
                    log.info("最新审核状态: {}", auditState);
                    return auditState;
                } else {
                    log.warn("审核记录为空");
                    return "无审核记录";
                }
            } else {
                log.error("获取审核状态失败，状态码: {}", statusCode);
                throw new RuntimeException("获取审核状态失败，状态码: " + statusCode);
            }
        } catch (Exception e) {
            log.error("获取审核状态时发生错误", e);
            return "failure";
        }
    }

    /**
     * 上传审核
     */
    public void postAudit() {

    }

    /**
     * 业务逻辑
     */
    public void mainAction() {
        try {
            // 执行搜索操作获取url
            TiebaDTO post = tiebaService.getPosts();
            // 获取链接和截图
            String url = post.getUrl();
            log.info("获取到的链接: {}", url);

            if (post != null) {
                // 获取截图
                mainService.getPicture(url);
                // 储存进数据库
                String result = mainService.storeUrl(post.getTitle(), post.getUrl(), post.getPublishTime());
                log.info("材料获取结果: {}", result);

                // TODO 上传审核

            } else {
                log.warn("No post found to audit.");
            }
        } catch (IOException e) {
            log.error("Error fetching posts or taking screenshot", e);
        }
    }

    /**
     * 还剩12小时时
     * 执行检索操作
     */
    @Scheduled(fixedRate = 3600000)
    public void whenfail() {
        String status = getStatus();
        if ("审核通过".equals(status)) {
            return;
        }
        MailUtil.sendMail(mailProperties.getTo(), mailProperties.getSubject(), status);
    }
}
