package com.hpy.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Author: hpy
 * Date: 2019-10-06
 * Description: <描述>
 */
@Getter@Setter
public class OrderProductVO {

    private List<OrderItemVO> orderItemVOList;
    private BigDecimal payment;
    private String imageHost;

}
