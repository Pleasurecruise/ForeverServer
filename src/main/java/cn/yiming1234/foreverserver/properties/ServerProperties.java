package cn.yiming1234.foreverserver.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yiming1234.sanfengyun")
@Data
@Configuration
public class ServerProperties {

    private String username;
    private String password;
    private String text;

}
