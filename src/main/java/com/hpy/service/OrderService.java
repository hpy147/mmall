package com.hpy.service;

import com.hpy.common.ResponseResult;

import java.util.Map;

/**
 * Author: hpy
 * Date: 2019-10-02
 * Description: <描述>
 */
public interface OrderService {
    ResponseResult pay(Integer userId, Long orderNo, String path);

    ResponseResult checkOrderParameter(Map<String, String> params);

    ResponseResult alipayCallBack(Map<String, String> params);

    ResponseResult queryOrderPayStatus(Integer userId, Long orderNo);

    void tradeRefund(Map<String, String> params, String refundDesc);

    ResponseResult createOrder(Integer userId, Integer shippingId);

    ResponseResult cancel(Integer userId, Long orderNo);

    ResponseResult getOrderCartProduct(Integer userId);

    ResponseResult getDetail(Integer userId, Long orderNo);

    ResponseResult getOrderList(Integer userId, Integer pageNum, Integer pageSize);

    ResponseResult managerList(Integer pageNum, Integer pageSize);

    ResponseResult manageDetail(Long orderNo);

    ResponseResult managerSearch(Long orderNo, Integer pageNum, Integer pageSize);

    ResponseResult manageSendGoods(Long orderNo);
}
