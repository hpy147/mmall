package com.hpy.dao;

import com.hpy.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectByUserIdAndProductId(@Param("userId") Integer userId,
                                    @Param("productId") Integer productId);

    List<Cart> selectbyUserId(Integer userId);

    int selectCartProductCheckStatusByUserId(Integer userId);

    void updateCheckedOrUnCheckedProduct(@Param("userId") Integer userId,
                                         @Param("checked")Integer checked,
                                         @Param("productId") Integer productId);

    int selectCartProductCountByUserId(Integer userId);

    void deleteByUserIdAndProductIds(@Param("userId") Integer userId,
                                     @Param("productIdsList") List<Integer> productIdsList);
}