package com.hpy.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Author: hpy
 * Date: 2019-10-04
 * Description: <描述>
 */
@Getter@Setter
public class OrderItemVO {

    private Long orderNo;
    private Integer productId;
    private String productName;
    private String productImage;
    private BigDecimal currentUnitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String createTime;

}
