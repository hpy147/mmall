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
public class ProductListVO {

    private Integer id;
    private Integer categoryId;
    private String name;
    private String subtitle;
    private String mainImage;
    private BigDecimal price;
    private Integer status;

    private String imageHost;

}
