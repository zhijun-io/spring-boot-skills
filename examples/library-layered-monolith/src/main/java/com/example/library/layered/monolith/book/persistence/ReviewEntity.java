package com.example.library.layered.monolith.book.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("reviews")
public class ReviewEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookId;
    private String userId;
    private String comment;
    private Short rating;
}
