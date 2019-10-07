package com.hpy.controller.backend;

import com.hpy.common.Const;
import com.hpy.common.ResponseResult;
import com.hpy.pojo.User;
import com.hpy.service.OrderService;
import com.hpy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * Author: hpy
 * Date: 2019-10-06
 * Description: <描述>
 */
@RestController
@RequestMapping("/manager/order")
public class OrderManagerController {

    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;

    @PostMapping("/list")
    public ResponseResult orderList(HttpSession session,
                                    @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        ResponseResult responseResult = this.checkAdmin(session);
        if (responseResult.isSuccess()) {
            return orderService.managerList(pageNum, pageSize);
        }
        return responseResult;
    }

    @PostMapping("/detail")
    public ResponseResult orderDetail(HttpSession session, Long orderNo) {
        ResponseResult responseResult = this.checkAdmin(session);
        if (responseResult.isSuccess()) {
            return orderService.manageDetail(orderNo);
        }
        return responseResult;
    }

    @PostMapping("/search")
    public ResponseResult orderSearch(HttpSession session, Long orderNo,
                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        ResponseResult responseResult = this.checkAdmin(session);
        if (responseResult.isSuccess()) {
            return orderService.managerSearch(orderNo, pageNum, pageSize);
        }
        return responseResult;
    }

    @PostMapping("/send_goods")
    public ResponseResult orderSendGoods(HttpSession session, Long orderNo) {
        ResponseResult responseResult = this.checkAdmin(session);
        if (responseResult.isSuccess()) {
            return orderService.manageSendGoods(orderNo);
        }
        return responseResult;
    }


    // 校验登陆和权限
    private ResponseResult checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError("用户未登陆");
        }
        return userService.checkAdmin(user);
    }

}
