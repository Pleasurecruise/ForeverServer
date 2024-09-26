package cn.yiming1234.foreverserver.controller;

import cn.yiming1234.foreverserver.service.ServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Slf4j
public class ServerController {

    @Autowired
    private ServerService serverService;


}
