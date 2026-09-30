package com.example.library.layered.monolith.rental.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RentalMapper extends BaseMapper<RentalEntity> {

    @Update("update rentals set status = 'RETURNED' "
            + "where id = #{rentalId} and status = 'RENTED'")
    int markReturned(@Param("rentalId") Long rentalId);
}
