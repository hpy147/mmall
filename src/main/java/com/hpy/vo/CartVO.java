package com.hpy.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Author: hpy
 * Date: 2019-10-01
 * Description: <描述>
 */
@Getter@Setter
public class CartVO {

    // 购物车商品明细
    private List<CartProductVO> cartProductVoList;
    // 购物车被勾选的商品总价
    private BigDecimal cartTotalPrice;
    // 购物车所有商品是否被全选中
    private Boolean allChecked;
    // 图片地址前缀
    private String imageHost;

}
