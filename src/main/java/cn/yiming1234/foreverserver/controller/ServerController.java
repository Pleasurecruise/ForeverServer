package cn.yiming1234.foreverserver.controller;

import cn.yiming1234.foreverserver.properties.ServerProperties;
import cn.yiming1234.foreverserver.service.MainService;
import cn.yiming1234.foreverserver.service.PostArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class ServerController {

    @Autowired
    private PostArticleService postArticleService;

    @Autowired
    private ServerProperties serverProperties;
    @Autowired
    private MainService mainService;

    /**
     * AI接口
     */
    @PostMapping("/ai")
    @ResponseBody
    public String postArticle() {
        String response = postArticleService.getText(serverProperties.getText());
        log.info("AI接口返回的内容为：" + response);
        return response;
    }

    /**
     * 截图测试接口
     */
    @GetMapping("/screenshot")
    public String screenshot(@RequestParam String url) {
        mainService.getPicture(url);
        log.info("截图成功");
        return "success";
    }
}
