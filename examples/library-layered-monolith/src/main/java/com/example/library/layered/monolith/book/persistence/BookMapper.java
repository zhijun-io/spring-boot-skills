package com.example.library.layered.monolith.book.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BookMapper extends BaseMapper<BookEntity> {

    @Update("update books set available_copies = available_copies - 1 "
            + "where id = #{bookId} and available_copies > 0")
    int reserve(@Param("bookId") Long bookId);

    @Update("update books set available_copies = available_copies + 1 "
            + "where id = #{bookId} and available_copies < total_copies")
    int release(@Param("bookId") Long bookId);
}
