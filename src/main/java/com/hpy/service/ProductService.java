package com.hpy.service;

import com.hpy.common.ResponseResult;
import com.hpy.pojo.Product;
import org.springframework.web.multipart.MultipartFile;

/**
 * Author: hpy
 * Date: 2019-09-29
 * Description: <描述>
 */
public interface ProductService {
    ResponseResult updateOrInsertProduct(Product product);

    ResponseResult setSaleStatus(Integer productId, Integer status);

    ResponseResult managerProductDetail(Integer productId);

    ResponseResult getProductList(Integer pageNum, Integer pageSize);

    ResponseResult searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize);

    ResponseResult getProductDetail(Integer productId);

    ResponseResult getProductByKeywordCategory(String keyword, Integer categoryId, String orderBy, Integer pageNum, Integer pageSize);
}
