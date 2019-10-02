package com.hpy.dao;

import com.hpy.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByUserIdAndShippingId(@Param("userId") Integer userId,
                                    @Param("shippingId") Integer shippingId);

    int updateShippingByUserId(Shipping shipping);

    Shipping selectByUserIdAndShippingId(@Param("userId") Integer userId,
                                         @Param("shippingId") Integer shippingId);

    List<Shipping> selectByUserId(Integer userId);
}