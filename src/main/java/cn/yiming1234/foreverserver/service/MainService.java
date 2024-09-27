package cn.yiming1234.foreverserver.service;

import cn.yiming1234.foreverserver.entity.Tieba;
import cn.yiming1234.foreverserver.mapper.TiebaMapper;
import cn.yiming1234.foreverserver.util.ScreenshotUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Service
@Slf4j
public class MainService {

    @Autowired
    private ScreenshotUtil screenshotUtil;

    @Autowired
    private TiebaMapper tiebaMapper;

    /**
     * 获取图片并本地临时储存
     */
    public String getPicture(String url) {
        String result = screenshotUtil.takeScreenshot(url);
        log.info("截图结果: " + result);
        return result;
    }

    /**
     * 将链接储存进数据库
     * 避免重复
     */
    public String storeUrl(String title, String url, String time) {
        Tieba existingTieba = tiebaMapper.getByUrl(url);
        if (existingTieba != null) {
            log.info("URL already exists in the database: {}", url);
            return "URL already exists.";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Timestamp timestamp = new Timestamp(dateFormat.parse(time).getTime());
            Tieba tieba = Tieba.builder()
                    .title(title)
                    .url(url)
                    .time(timestamp.toLocalDateTime())
                    .build();
            tiebaMapper.insert(tieba);
            log.info("URL stored: Title: {}, URL: {}, Time: {}", title, url, timestamp);
            return "URL stored successfully.";
        } catch (ParseException e) {
            log.error("Error parsing time: {}", time, e);
            return "Error storing URL.";
        }
    }
}
