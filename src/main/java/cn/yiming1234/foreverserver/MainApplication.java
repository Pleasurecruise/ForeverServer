package cn.yiming1234.foreverserver;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 启动类
 */
/**
 * 项目思路流程
 *
 * 每隔2小时获取一次过期时间，和当前时间进行比较   OKK
 * 当时间小于12小时时，开始执行下面操作   OKK
 *
 * 爬取当前热点文章进行转载（CSDN等）   OKK
 * 调用AI接口生成50字测评内容，并插入测评内容   OKK
 * 发布成功后，储存文章链接并调用工具类进行截图
 * 添加检测类，当OSS中截图达到一定数量时，执行删除操作
 * 在延时网站中填入地址和截图
 * 提交延期申请并发送成功邮件
 *
 * 当申请提交后每隔一小时检查申请状态，  OKK
 * 若成功则发送邮件提醒下次到期时间,   OKK
 * 若失败则发送邮件显示失败原因，提醒人工操作,删除博客
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@Slf4j
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
