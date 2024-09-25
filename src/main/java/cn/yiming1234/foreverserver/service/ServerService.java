package cn.yiming1234.foreverserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ServerService {

    /**
     * 通过接口获取服务器到期时间
     */
    public String getTime() {
        return "2021-12-31";
    }

    /**
     * 项目思路流程
     *
     * 每隔2小时获取一次过期时间，和当前时间进行比较
     * 当时间小于12小时时，发送邮件并执行下面操作
     *
     * 爬取当前热点文章进行转载（CSDN等）
     * 调用AI接口生成50字测评内容，并插入测评内容
     * 发布成功后，储存文章链接并调用工具类进行截图
     * 添加检测类，当OSS中截图达到一定数量时，执行删除操作
     * 在延时网站中填入地址和截图
     * 提交延期申请并发送成功邮件
     *
     * 当申请提交后每隔一小时检查申请状态，
     * 若成功则发送邮件提醒下次到期时间
     * 若失败则发送邮件显示失败原因，提醒人工操作
     *
     */

}
