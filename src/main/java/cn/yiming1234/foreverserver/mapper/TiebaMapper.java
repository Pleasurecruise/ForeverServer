package cn.yiming1234.foreverserver.mapper;

import cn.yiming1234.foreverserver.entity.Tieba;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TiebaMapper {

    @Insert("insert into article1(title, url, time) values(#{title}, #{url}, #{time})")
    void insert(Tieba tieba);

    @Select("select * from article1 where url=#{url}")
    Tieba getByUrl(String url);
}
