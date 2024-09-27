package cn.yiming1234.foreverserver.dto;

import lombok.Data;

@Data
public class TiebaDTO {
    private String title;
    private String url;
    private String publishTime;

    // 构造函数
    public TiebaDTO(String title, String url, String publishTime) {
        this.title = title;
        this.url = url;
        this.publishTime = publishTime;
    }
}
