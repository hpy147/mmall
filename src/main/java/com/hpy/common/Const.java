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

}
