package com.hpy.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradeRefundRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.model.result.AlipayF2FRefundResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hpy.common.Const;
import com.hpy.common.ResponseCode;
import com.hpy.common.ResponseResult;
import com.hpy.dao.*;
import com.hpy.pojo.*;
import com.hpy.service.OrderService;
import com.hpy.util.BigdecimalUtils;
import com.hpy.util.DateTimeUtils;
import com.hpy.util.FTPUtils;
import com.hpy.util.PropertyUtils;
import com.hpy.vo.OrderItemVO;
import com.hpy.vo.OrderProductVO;
import com.hpy.vo.OrderVO;
import com.hpy.vo.ShippingVO;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Author: hpy
 * Date: 2019-10-02
 * Description: <描述>
 */
@Service(value = "orderService")
public class OrderServiceImpl implements OrderService {

    // 支付宝当面付2.0服务
    private static AlipayTradeService tradeService;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    private Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    @Transactional
    public ResponseResult createOrder(Integer userId, Integer shippingId) {
        // 获取购物车中被选中的商品
        List<Cart> cartList = cartMapper.selectByUserIdAndStatus(userId, Const.ProductStatusEnum.ON_SALE.getCode());
        // 根据购物车中的商品，生成订单明细
        ResponseResult orderItemResponse = this.getCartOrderItem(cartList);
        if (!orderItemResponse.isSuccess()) {
            return orderItemResponse;
        }
        // 计算商品的总价
        List<OrderItem> orderItemList = (List<OrderItem>) orderItemResponse.getData();
        BigDecimal orderPayment = this.getOrderTotalPrice(orderItemList);
        // 生成订单
        Order order = this.assembleOrder(userId, shippingId, orderPayment);
        if (order == null) {
            return ResponseResult.createByError("生成订单错误");
        }
        // 设置OrderItem的订单号
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        // 批量插入OrderItem
        orderItemMapper.batchInsert(orderItemList);

        // 订单生成成功，减少商品库存(在商品支付成功的时候才减库存)

        // 清空购物车
        this.cleanCart(cartList);

        // 组装OrderVO
        OrderVO orderVO = this.assembleOrderVO(order, orderItemList);
        return ResponseResult.createBySuccess(orderVO);
    }

    @Override
    @Transactional
    public ResponseResult cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ResponseResult.createByError("该用户此订单不存在");
        }
        if (Const.OrderStatusEnum.NO_PAY.getCode() != order.getStatus()) {
            return ResponseResult.createByError("已付款,无法取消订单");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int updateRow = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (updateRow > 0) {
            return ResponseResult.createBySuccess();
        }
        return ResponseResult.createByError();
    }

    @Override
    public ResponseResult getOrderCartProduct(Integer userId) {
        // 根据userId获取购物车中选中的商品
        List<Cart> cartList = cartMapper.selectByUserIdAndStatus(userId, Const.Cart.CHECKED);

        OrderProductVO orderProductVO = new OrderProductVO();

        ResponseResult response = this.getCartOrderItem(cartList);
        if (!response.isSuccess()) {
            return response;
        }
        // 获取 OrderItem
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        // OrderItem -> OrderItemVO
        List<OrderItemVO> orderItemVOList = this.assembleOrderItemVO(orderItemList);
        // 商品总价
        BigDecimal orderTotalPrice = this.getOrderTotalPrice(orderItemList);

        orderProductVO.setOrderItemVOList(orderItemVOList);
        orderProductVO.setPayment(orderTotalPrice);
        orderProductVO.setImageHost(PropertyUtils.getProperty("ftp.server.http.prefix"));

        return ResponseResult.createBySuccess(orderProductVO);
    }

    @Override
    public ResponseResult getDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId, orderNo);
            OrderVO orderVO = this.assembleOrderVO(order, orderItemList);
            return ResponseResult.createBySuccess(orderVO);
        }
        return ResponseResult.createByError("该用户未找到该订单");
    }

    @Override
    public ResponseResult getOrderList(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVO> orderVOList = this.assembleOrderVOList(orderList, userId);
        PageInfo<OrderVO> pageInfo = new PageInfo<>(orderVOList);
        return ResponseResult.createBySuccess(pageInfo);
    }

    @Override
    public ResponseResult managerList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAll();
        List<OrderVO> orderVOList = this.assembleOrderVOList(orderList, null);
        PageInfo<OrderVO> pageInfo = new PageInfo<>(orderVOList);
        return ResponseResult.createBySuccess(pageInfo);
    }

    @Override
    public ResponseResult manageDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ResponseResult.createByError("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVO orderVO = this.assembleOrderVO(order, orderItemList);
        return ResponseResult.createBySuccess(orderVO);
    }

    @Override
    public ResponseResult managerSearch(Long orderNo, Integer pageNum, Integer pageSize) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ResponseResult.createByError("订单不存在");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVO orderVO = this.assembleOrderVO(order, orderItemList);
        PageInfo<OrderVO> pageInfo = new PageInfo<>(Lists.<OrderVO>newArrayList(orderVO));
        return ResponseResult.createBySuccess(pageInfo);
    }

    @Override
    public ResponseResult manageSendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ResponseResult.createByError("订单不存在");
        }
        // 如果订单已付款，则可以发货
        if (Const.OrderStatusEnum.PAID.getCode() == order.getStatus()) {
            order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            int updateRow = orderMapper.updateByPrimaryKeySelective(order);
            if (updateRow > 0) {
                return ResponseResult.createBySuccess("订单发货成功");
            }
            return ResponseResult.createByError("订单发货失败");
        }
        return ResponseResult.createByError("订单未付款或已发货");
    }

    private List<OrderVO> assembleOrderVOList(List<Order> orderList, Integer userId) {
        List<OrderVO> orderVOList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId, order.getOrderNo());
            OrderVO orderVO = this.assembleOrderVO(order, orderItemList);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

    // OrderVO
    private OrderVO assembleOrderVO(Order order, List<OrderItem> orderItemList) {
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderNo(order.getOrderNo());
        orderVO.setPayment(order.getPayment());
        orderVO.setPaymentType(order.getPaymentType());
        // 支付类型描述
        orderVO.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getDesc());
        orderVO.setPostage(order.getPostage());
        orderVO.setStatus(order.getStatus());

        // 支付状态描述
        orderVO.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getDesc());

        // 设置时间
        orderVO.setPaymentTime(DateTimeUtils.dateToStr(order.getPaymentTime()));
        orderVO.setSendTime(DateTimeUtils.dateToStr(order.getSendTime()));
        orderVO.setEndTime(DateTimeUtils.dateToStr(order.getEndTime()));
        orderVO.setCloseTime(DateTimeUtils.dateToStr(order.getCloseTime()));
        orderVO.setCreateTime(DateTimeUtils.dateToStr(order.getCreateTime()));

        // 设置订单明细
        List<OrderItemVO> orderItemVOList = this.assembleOrderItemVO(orderItemList);
        orderVO.setOrderItemVOList(orderItemVOList);

        // 设置收货地址
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVO.setReceiverName(shipping.getReceiverName());
            orderVO.setShippingId(shipping.getId());
            orderVO.setShippingVO(this.assembleShippingVO(shipping));
        }

        // 设置图片前缀
        orderVO.setImageHost(PropertyUtils.getProperty("ftp.server.http.prefix"));

        return orderVO;

    }

    private ShippingVO assembleShippingVO(Shipping shipping) {
        ShippingVO shippingVO = new ShippingVO();
        BeanUtils.copyProperties(shipping, shippingVO);
        return shippingVO;
    }

    private List<OrderItemVO> assembleOrderItemVO(List<OrderItem> orderItemList) {
        List<OrderItemVO> orderItemVOList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVO orderItemVO = new OrderItemVO();
            BeanUtils.copyProperties(orderItem, orderItemVO);
            // 设置时间 date -> String
            orderItemVO.setCreateTime(DateTimeUtils.dateToStr(orderItem.getCreateTime()));
            orderItemVOList.add(orderItemVO);
        }
        return orderItemVOList;
    }

    // 清空购物车
    private void cleanCart(List<Cart> cartList) {
        List<Integer> cartIds = Lists.newArrayList();
        for (Cart cart : cartList) {
            cartIds.add(cart.getId());
        }
        cartMapper.deleteByIds(cartIds);
    }

    // 减少商品库存
    //private void reduceProductStock(List<OrderItem> orderItemList) {
    //    for (OrderItem orderItem : orderItemList) {
    //        Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
    //        product.setStock(product.getStock() - orderItem.getQuantity());
    //        productMapper.updateByPrimaryKeySelective(product);
    //    }
    //}

    // 生成订单
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal orderPayment) {
        Order order = new Order();
        // 生成订单号
        Long orderNo = this.generatorOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(orderPayment);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPostage(0);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        int rowCount = orderMapper.insert(order);
        if (rowCount > 0) {
            return order;
        }
        return null;
    }

    private Long generatorOrderNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigdecimalUtils.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    // 根据购物车生成订单明细
    private ResponseResult getCartOrderItem(List<Cart> cartList) {

        if (CollectionUtils.isEmpty(cartList)) {
            return ResponseResult.createByError("购物车为空");
        }

        List<OrderItem> orderItemList = Lists.newArrayList();
        for (Cart cart : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());

            // 判断商品状态
            if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
                return ResponseResult.createByError("商品(" + product.getName() + ")已下架!");
            }
            // 判断库存
            if (product.getStock() < cart.getQuantity()) {
                return ResponseResult.createByError("商品(" + product.getName() + ")库存不足!");
            }

            // 组装OrderItem
            orderItem.setUserId(cart.getUserId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            // 设置总价 = 商品单价 * 购买数量
            orderItem.setTotalPrice(BigdecimalUtils.multiply(product.getPrice().doubleValue(), cart.getQuantity().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ResponseResult.createBySuccess(orderItemList);
    }

















    @Override
    public ResponseResult pay(Integer userId, Long orderNo, String path) {
        if (orderNo == null) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        // 查询订单
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ResponseResult.createByError("用户没有该订单");
        }

        // 返回给前端一个Map:
        //      order: 订单号
        //      qrUrl: 生成的二维码在FTP服务器的路径
        Map<String, String> resultMap = Maps.newHashMap();
        resultMap.put("orderNo", orderNo.toString());

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("happymmall扫码支付,订单号:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder()
                .append("订单 ").append(outTradeNo)
                .append(" 购买商品共: ").append(order.getPayment())
                .append(" 元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "收银员001";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "商户门店编号1001";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<>();
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId, orderNo);
        for (OrderItem orderItem : orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigdecimalUtils.multiply(orderItem.getCurrentUnitPrice().doubleValue(), 100L).longValue(), orderItem.getQuantity());
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertyUtils.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 生成二维码的名称
                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());
                // 二维码生成的路径
                String qrPath = String.format(path + "/" + qrFileName, response.getOutTradeNo());
                // 生成二维码
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                logger.info("qrPath:" + qrPath);

                // 生成的二维码上传到FTP服务器
                File targetFile = new File(path, qrFileName);
                try {
                    FTPUtils.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("二维码上传到FTP服务器异常", e);
                }

                // 上传到FTP服务器的二维码路径
                String qrUrl = PropertyUtils.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl", qrUrl);

                // 删除临时文件
                targetFile.delete();

                return ResponseResult.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ResponseResult.createByError("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ResponseResult.createByError("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ResponseResult.createByError("不支持的交易状态，交易返回异常!!!");
        }
    }

    @Override
    public ResponseResult checkOrderParameter(Map<String, String> params) {
        String orderNo = params.get("out_trade_no");
        String totalAmount = params.get("total_amount");
        String sellerId = params.get("seller_id");

        logger.info("支付宝回调的订单号:{},订单总金额:{},sellerId:{}", orderNo, totalAmount, sellerId);

        // 只有订单号，订单总金额，sellerId 全部正确才算验证通过
        Order order = orderMapper.selectByOrderNo(Long.parseLong(orderNo));
        if (order != null) {
            if (StringUtils.equals(order.getPayment().toString(), totalAmount)) {
                if (StringUtils.equals(sellerId, Configs.getPid())) {
                    return ResponseResult.createBySuccess();
                }
            }
        }
        return ResponseResult.createByError();
    }

    @Override
    @Transactional
    public ResponseResult alipayCallBack(Map<String, String> params) {
        // 订单号
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        // 支付宝交易号
        String tradeNo = params.get("trade_no");
        // 交易状态
        String tradeStatus = params.get("trade_status");

        // 获取订单
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ResponseResult.createByError("订单未找到");
        }

        // 判断支付状态，如果已支付，提示支付宝重复调用
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            // 该订单已支付
            // 因为这里是支付宝正常调用，Controller需要返回success
            // 所以这里需要用success，而不能用error
            return ResponseResult.createBySuccess("支付宝重复调用");
        }

        // 判断支付状态
        if (StringUtils.equals(Const.AlipayCallBack.TRADE_STATUS_TRADE_SUCCESS, tradeStatus)) {
            // 支付成功
            // 修改支付成功的时间
            String paymentTime = params.get("gmt_payment");
            order.setPaymentTime(DateTimeUtils.strToDate(paymentTime));
            // 修改支付状态
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);

            // 更新商品库存数
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
            for (OrderItem orderItem : orderItemList) {
                Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
                // 重新计算商品库存数，并更新商品库存
                product.setStock(product.getStock() - orderItem.getQuantity());
                productMapper.updateByPrimaryKeySelective(product);
            }
        }

        PayInfo payInfoItem = payInfoMapper.selectByOrderNo(orderNo);
        if (payInfoItem == null) {
            // 首次回调，创建PayInfo
            PayInfo payInfo = new PayInfo();
            payInfo.setUserId(order.getUserId());
            payInfo.setOrderNo(order.getOrderNo());
            payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
            payInfo.setPlatformNumber(tradeNo);
            payInfo.setPlatformStatus(tradeStatus);
            payInfoMapper.insert(payInfo);
        } else {
            // 后续回调，更新PayInfo
            payInfoItem.setPlatformStatus(tradeStatus);
            payInfoMapper.updateByPrimaryKeySelective(payInfoItem);
        }

        return ResponseResult.createBySuccess();
    }

    @Override
    public ResponseResult queryOrderPayStatus(Integer userId, Long orderNo) {
        if (orderNo == null) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ResponseResult.createByError("订单不存在");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ResponseResult.createBySuccess(true);
        }
        return ResponseResult.createBySuccess(false);
    }

    @Override
    public void tradeRefund(Map<String, String> params, String refundDesc) {
        // (必填) 外部订单号，需要退款交易的商户外部订单号
        String outTradeNo = params.get("out_trade_no");

        // (必填) 退款金额，该金额必须小于等于订单的支付金额，单位为元
        String refundAmount = params.get("total_amount");

        // (可选，需要支持重复退货时必填) 商户退款请求号，相同支付宝交易号下的不同退款请求号对应同一笔交易的不同退款申请，
        // 对于相同支付宝交易号下多笔相同商户退款请求号的退款交易，支付宝只会进行一次退款
        String outRequestNo = "";

        // (必填) 退款原因，可以说明用户退款原因，方便为商家后台提供统计
        String refundReason = refundDesc;

        // (必填) 商户门店编号，退款情况下可以为商家后台提供退款权限判定和统计等作用，详询支付宝技术支持
        String storeId = "商户门店编号1001";

        // 创建退款请求builder，设置请求参数
        AlipayTradeRefundRequestBuilder builder = new AlipayTradeRefundRequestBuilder()
                .setOutTradeNo(outTradeNo).setRefundAmount(refundAmount).setRefundReason(refundReason)
                .setOutRequestNo(outRequestNo).setStoreId(storeId);

        AlipayF2FRefundResult result = tradeService.tradeRefund(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝退款成功: )");
                break;

            case FAILED:
                logger.error("支付宝退款失败!!!");
                break;

            case UNKNOWN:
                logger.error("系统异常，订单退款状态未知!!!");
                break;

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                break;
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }
}
