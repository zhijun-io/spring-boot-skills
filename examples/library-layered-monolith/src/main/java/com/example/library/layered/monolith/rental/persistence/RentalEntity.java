package com.example.library.layered.monolith.rental.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.library.layered.monolith.rental.domain.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("rentals")
public class RentalEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookId;
    private String userId;
    private RentalStatus status;
}
