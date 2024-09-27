package cn.yiming1234.foreverserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tieba implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String url;

    private LocalDateTime time;

}
