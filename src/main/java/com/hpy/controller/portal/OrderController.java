package com.hpy.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.hpy.common.Const;
import com.hpy.common.ResponseCode;
import com.hpy.common.ResponseResult;
import com.hpy.pojo.User;
import com.hpy.service.OrderService;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * Author: hpy
 * Date: 2019-10-02
 * Description: <描述>
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    private Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseResult createOrder(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.createOrder(user.getId(), shippingId);
    }

    @PostMapping("/cancel")
    public ResponseResult cancel(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.cancel(user.getId(), orderNo);
    }

    @PostMapping("/get_order_cart_product")
    public ResponseResult getOrderCartProduct(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.getOrderCartProduct(user.getId());
    }

    @PostMapping("/detail")
    public ResponseResult getDetail(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.getDetail(user.getId(), orderNo);
    }

    @PostMapping("/list")
    public ResponseResult list(HttpSession session,
                                    @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.getOrderList(user.getId(), pageNum, pageSize);
    }











    @RequestMapping("/pay")
    public ResponseResult pay(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = session.getServletContext().getRealPath("upload");
        return orderService.pay(user.getId(), orderNo, path);
    }

    @RequestMapping("/alipay_callback")
    public String alipayCallBack(HttpServletRequest request) throws Exception {

        System.out.println("OrderController.alipayCallBack");

        Map<String, String> params = Maps.newHashMap();
        // 将支付宝回调待验签的数组参数组装成字符串

        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Iterator it = parameterMap.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            String[] values = parameterMap.get(key);
            StringBuilder valueStr = new StringBuilder();
            for (int i=0; i<values.length; i++) {
                if (i == values.length - 1) {
                    // 最后一个元素，结尾不加逗号
                    valueStr.append(values[i]);
                } else {
                    valueStr.append(values[i]).append(",");
                }
            }
            params.put(key, valueStr.toString());
        }
        logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",params.get("sign"), params.get("trade_status"), params.toString());

        //非常重要,验证回调的正确性,是不是支付宝发的.并且呢还要避免重复通知.
        //在通知返回参数列表中，除去sign、sign_type两个参数外，凡是通知返回回来的参数皆是待验签的参数。
        params.remove("sign_type");

        try {
            boolean alipayCheckeV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if (!alipayCheckeV2) {
                logger.info("非法请求，验证不通过");
                return Const.AlipayCallBack.RESPONSE_FAILED;
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常");
        }

        // 如果验证通过，需要判断 out_trade_no 、total_amount 、seller_id 是否正确
        ResponseResult responseResult = orderService.checkOrderParameter(params);
        if (!responseResult.isSuccess()) {
            logger.info("支付宝回调参数与订单参数不匹配");
            if (params.get("trade_status").equals(Const.AlipayCallBack.TRADE_STATUS_TRADE_SUCCESS)) {
                // 到这里即支付成功，但是系统出错，例如订单号不存在，订单总金额不正确，pid不正确等...
                // 执行退款
                orderService.tradeRefund(params, "退款描述信息..");
            }
            // 已经执行了退款，返回 success
            return Const.AlipayCallBack.RESPONSE_SUCCESS;
        }

        // 所有验证通过，开始处理业务
        ResponseResult response = orderService.alipayCallBack(params);
        if (response.isSuccess()) {
            logger.info("支付宝回调成功，数据修改成功!");
            return Const.AlipayCallBack.RESPONSE_SUCCESS;
        }
        logger.info("支付宝回调成功，数据修改失败!");
        return Const.AlipayCallBack.RESPONSE_FAILED;
    }

    @RequestMapping("/query_order_pay_status")
    public ResponseResult queryOrderPayStatus(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.queryOrderPayStatus(user.getId(), orderNo);
    }

}
