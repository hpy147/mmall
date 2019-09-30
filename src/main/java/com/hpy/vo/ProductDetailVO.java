package com.hpy.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Author: hpy
 * Date: 2019-09-29
 * Description: <描述>
 */
@Getter@Setter
public class ProductDetailVO {

    private Integer id;
    private Integer categoryId;
    private String name;
    private String subtitle;
    private String mainImage;
    private String subImages;
    private String detail;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private String createTime;
    private String updateTime;

    private String imageHost;
    private Integer parentCategoryId;

}
