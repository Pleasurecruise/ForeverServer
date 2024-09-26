package cn.yiming1234.foreverserver.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yiming1234.linkai")
@Data
public class LinkAiProperties {

    private String app_code;
    private String api_key;

}
