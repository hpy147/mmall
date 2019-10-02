package com.hpy.service;

import com.hpy.common.ResponseResult;
import com.hpy.pojo.Shipping;

/**
 * Author: hpy
 * Date: 2019-10-02
 * Description: <描述>
 */
public interface ShippingService {
    ResponseResult add(Integer userId, Shipping shipping);

    ResponseResult del(Integer userId, Integer shippingId);

    ResponseResult update(Integer userId, Shipping shipping);

    ResponseResult select(Integer userId, Integer shippingId);

    ResponseResult list(Integer userId, Integer pageNum, Integer pageSize);
}
