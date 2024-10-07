package cn.yiming1234.foreverserver.service;

import cn.yiming1234.foreverserver.dto.TiebaDTO;
import cn.yiming1234.foreverserver.mapper.TiebaMapper;
import cn.yiming1234.foreverserver.properties.MailProperties;
import cn.yiming1234.foreverserver.properties.ServerProperties;
import cn.yiming1234.foreverserver.util.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
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
    private TiebaMapper tiebaMapper;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private MailProperties mailProperties;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String LOGIN_URL = "https://api.sanfengyun.com/www/login.php";
    private static final String VPS_URL = "https://api.sanfengyun.com/www/vps.php";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36";
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";
    @Autowired
    private MailUtil mailUtil;

    /**
     * 获取Session ID
     */
    @Async
    public CompletableFuture<String> getSessionId() throws Exception {
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
                    // Store sessionId in Redis
                    redisTemplate.opsForValue().set("session_id", sessionId);

                    log.info("获取到的响应：{}", response.body());
                    log.info("获取到的Session ID: {}", sessionId);
                    return CompletableFuture.completedFuture(sessionId);
                } catch (Exception e) {
                    log.error("从Set-Cookie头中解析Session ID失败", e);
                    throw new RuntimeException("解析Session ID失败");
                }
            } else {
                log.error("响应中未找到Set-Cookie头");
                throw new RuntimeException("未找到Set-Cookie头");
            }
        } else {
            log.error("登录失败，状态码: {}", statusCode);
            throw new RuntimeException("登录失败，状态码: " + statusCode);
        }
    }

    /**
     * 检查session_id是否过期
     */
    private boolean isSessionIdValid(String sessionId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VPS_URL))
                    .header("User-Agent", USER_AGENT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cookie", "session_id=" + sessionId)
                    .POST(HttpRequest.BodyPublishers.ofString("cmd=vps_list&vps_type=free"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("检查Session ID响应状态码: {}", response.statusCode());

            return response.statusCode() == 200;
        } catch (Exception e) {
            log.error("检查Session ID时发生错误", e);
            return false;
        }
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
            String sessionId = getSessionId().get();
            log.info("Session ID: {}", sessionId);
            HttpClient client = HttpClient.newHttpClient();

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
                    //int totalHours = 11; //用于测试
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
            String sessionId = getSessionId().get();
            HttpClient client = HttpClient.newHttpClient();

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
    @Async
    public CompletableFuture<Void> postAudit(String url) {
        try {
            String sessionId = (String) redisTemplate.opsForValue().get("session_id");
            if (sessionId == null || !isSessionIdValid(sessionId)) {
                sessionId = getSessionId().get();
            }
            HttpClient client = HttpClient.newHttpClient();

            Path filePath = Paths.get(System.getProperty("user.dir"), "temp", "screenshot.png");
            byte[] fileBytes = Files.readAllBytes(filePath);

            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            String requestBody = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"cmd\"\r\n\r\n" +
                    "free_delay_add\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"ptype\"\r\n\r\n" +
                    "vps\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"url\"\r\n\r\n" +
                    url + "\r\n" +
                    "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"yanqi_img\"; filename=\"screenshot.png\"\r\n" +
                    "Content-Type: image/png\r\n\r\n" +
                    new String(fileBytes) + "\r\n" +
                    "--" + boundary + "--";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.sanfengyun.com/www/renew.php"))
                    .header("User-Agent", USER_AGENT)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Cookie", "session_id=" + sessionId)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                String msg = jsonResponse.getString("msg");
                if ("提交失败".equals(msg) || msg.contains("上传失败") || msg.contains("您上传的文件不是有效的图片文件")) {
                    log.error("审核上传失败，错误信息: {}", msg);
                    mailUtil.sendMail(mailProperties.getTo(), mailProperties.getSubject(), msg);
                    throw new RuntimeException("审核上传失败，错误信息: " + msg);
                } else {
                    mailUtil.sendMail(mailProperties.getTo(), mailProperties.getSubject(), "审核上传成功");
                    log.info("审核上传成功: {}", response.body());
                }
            } else {
                log.error("审核上传失败，状态码: {}", statusCode);
                throw new RuntimeException("审核上传失败，状态码: " + statusCode);
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("上传审核时发生错误", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 业务逻辑
     */
    private TiebaDTO post;
    @Async
    public CompletableFuture<Void> mainAction() {
        try {
            // 获取当前审核状态
            String status = getStatus();
            if ("待审核".equals(status)) {
                log.info("当前状态为待审核，不进行后续操作");
                mailUtil.sendMail(mailProperties.getTo(), mailProperties.getSubject(), status);
                return CompletableFuture.completedFuture(null);
            }
            // 执行搜索操作获取url
            TiebaDTO post = tiebaService.getPosts();
            String url = post.getUrl();
            log.info("获取到的链接: {}", url);

            while (tiebaMapper.getByUrl(url) != null) {
                log.info("URL already exists in the database: {}", url);
                post = tiebaService.getPosts();
                url = post.getUrl();
                log.info("获取到的新链接: {}", url);
            }
            // 截图操作
            mainService.getPicture(url);
            // 上传审核
            postAudit(url);
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            log.error("Error fetching posts or taking screenshot", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 还剩12小时时
     * 执行检索操作
     */
    @Scheduled(fixedRate = 3600000)
    public void whenfail() {
        mailUtil.sendMail(mailProperties.getTo(), mailProperties.getSubject(), "服务器剩余时间不到12小时");
        String status = getStatus();
        if ("审核通过".equals(status)) {
            String result = mainService.storeUrl(post.getTitle(), post.getUrl(), post.getPublishTime());
            log.info("材料获取结果: {}", result);
            return;
        }
        mailUtil.sendMail(mailProperties.getTo(), mailProperties.getSubject(), status);
    }
}
