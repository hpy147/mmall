package com.hpy.controller.portal;

import com.hpy.common.ResponseResult;
import com.hpy.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: hpy
 * Date: 2019-09-30
 * Description: <描述>
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @RequestMapping("/detail")
    public ResponseResult getDetail(Integer productId) {
        return productService.getProductDetail(productId);
    }

    @RequestMapping("/list")
    public ResponseResult list(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "categoryId", required = false) Integer categoryId,
                               @RequestParam(value = "orderBy", defaultValue = "") String orderBy,
                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
                               ) {
        return productService.getProductByKeywordCategory(keyword, categoryId, orderBy, pageNum, pageSize);
    }

}
