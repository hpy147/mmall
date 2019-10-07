package com.hpy.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Author: hpy
 * Date: 2019-09-29 10:37
 * Description: 常量
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_asc", "price_desc");
    }

    public interface Role {
        int ROLE_CUSTOMER = 0;  // 普通用户
        int ROLE_ADMIN = 1;     // 管理员
    }

    public interface Cart {
        Integer CHECKED = 1;    // 购物车商品被选中
        Integer UN_CHECKED = 0; // 购物车商品未选中

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public enum ProductStatusEnum {
        ON_SALE(1, "在售"),
        UN_ON_SALE(2, "下架"),
        DELETE(3, "删除");

        private Integer code;
        private String dedc;

        private ProductStatusEnum(Integer code, String desc) {
            this.code = code;
            this.dedc = desc;
        }
        public Integer getCode() {
            return code;
        }
        public String getDedc() {
            return dedc;
        }
    }

    public interface AlipayCallBack {
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";   // 交易创建
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";     // 支付成功
        String TRADE_STATUS_TRADE_FINISHED = "TRADE_FINISHED";   // 交易完成
        String TRADE_STATUS_TRADE_CLOSED = "TRADE_CLOSED";       // 交易关闭

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum OrderStatusEnum {
        CANCELED(0, "已取消"),
        NO_PAY(10, "未付款"),
        PAID(20, "已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭")
        ;
        private Integer code;
        private String desc;
        private OrderStatusEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static OrderStatusEnum codeOf(Integer code) {
            OrderStatusEnum[] values = OrderStatusEnum.values();
            for (OrderStatusEnum orderStatusEnum : values) {
                if (orderStatusEnum.getCode() == code) {
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到该枚举");
        }

        public Integer getCode() {
            return code;
        }
        public String getDesc() {
            return desc;
        }
    }

    public enum PayPlatformEnum {
        ALIPAY(1, "支付宝"),
        WECHAT(2, "微信")
        ;
        private Integer code;
        private String desc;
        private PayPlatformEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public Integer getCode() {
            return code;
        }
        public String getDesc() {
            return desc;
        }
    }

    public enum PaymentTypeEnum {
        ONLINE_PAY(1, "在线支付"),
        ;
        private Integer code;
        private String desc;
        private PaymentTypeEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static PaymentTypeEnum codeOf(Integer code) {
            PaymentTypeEnum[] values = PaymentTypeEnum.values();
            for (PaymentTypeEnum paymentTypeEnum : values) {
                if (paymentTypeEnum.getCode() == code) {
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到该枚举");
        }

        public Integer getCode() {
            return code;
        }
        public String getDesc() {
            return desc;
        }
    }

}