package com.hpy.controller.backend;

import com.hpy.common.Const;
import com.hpy.common.ResponseResult;
import com.hpy.pojo.User;
import com.hpy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * Author: hpy
 * Date: 2019-09-29 14:40
 * Description: <描述>
 */
@RestController
@RequestMapping("/manager/user")
public class UserManagerController {

    @Autowired
    private UserService userService;

    @RequestMapping("/login")
    public ResponseResult<User> login(String username, String password, HttpSession session) {
        ResponseResult<User> responseResult = userService.login(username, password);
        if (responseResult.isSuccess()) {
            // 用户名密码正确，判断是不是管理员
            User user = responseResult.getData();
            if (user.getRole() != Const.Role.ROLE_ADMIN) {
                return ResponseResult.createByError("不是管理员，登陆失败");
            }
            // 是管理员，将用户信息存入session
            session.setAttribute(Const.CURRENT_USER, user);
        }
        return responseResult;
    }

}
