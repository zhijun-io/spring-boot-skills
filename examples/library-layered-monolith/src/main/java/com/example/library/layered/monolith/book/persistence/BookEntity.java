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
@TableName("books")
public class BookEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String author;
    private String description;
    private Integer totalCopies;
    private Integer availableCopies;
}
