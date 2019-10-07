package com.hpy.dao;

import com.hpy.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> selectByUserIdAndOrderNo(@Param("userId") Integer userId,
                                             @Param("orderNo") Long orderNo);

    List<OrderItem> selectByOrderNo(Long orderNo);

    void batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);
}