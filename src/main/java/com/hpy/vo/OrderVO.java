package com.hpy.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Author: hpy
 * Date: 2019-10-04
 * Description: <描述>
 */
@Getter@Setter
public class OrderVO {

    private Long orderNo;
    private BigDecimal payment;
    private Integer paymentType;
    // 订单支付类型描述
    private String paymentTypeDesc;
    private Integer postage;
    private Integer status;
    // 订单状态描述
    private String statusDesc;
    private String paymentTime;
    private String sendTime;
    private String endTime;
    private String closeTime;
    private String createTime;

    private List<OrderItemVO> orderItemVOList;
    private ShippingVO shippingVO;

    private Integer shippingId;
    private String receiverName;

    private String imageHost;


}
