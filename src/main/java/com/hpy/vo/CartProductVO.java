package com.hpy.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Author: hpy
 * Date: 2019-10-01
 * Description: <描述>
 */
@Getter@Setter
public class CartProductVO {

    // 商品ID
    private Integer productId;
    // 商品购买的数量
    private Integer quantity;

    // 商品名称
    private String productName;
    // 商品标题
    private String productSubtitle;
    // 商品主图
    private String productMainImage;
    // 商品单价
    private BigDecimal productPrice;
    // 商品总价
    private BigDecimal productTotalPrice;
    // 商品是否被勾选
    private Integer productChecked;

    // 限制商品数量的返回结果
    private String limitQuantity;

}
