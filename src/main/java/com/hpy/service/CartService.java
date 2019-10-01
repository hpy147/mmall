package com.hpy.service;

import com.hpy.common.ResponseResult;

/**
 * Author: hpy
 * Date: 2019-10-01
 * Description: <描述>
 */
public interface CartService {
    ResponseResult add(Integer userId, Integer productId, Integer count);

    ResponseResult list(Integer userId);

    ResponseResult selectOrUnSelect(Integer userId, Integer checked, Integer productId);

    ResponseResult getCartProductCount(Integer userId);

    ResponseResult update(Integer userId, Integer productId, Integer count);

    ResponseResult deleteProduct(Integer userId, Integer... productIds);
}
