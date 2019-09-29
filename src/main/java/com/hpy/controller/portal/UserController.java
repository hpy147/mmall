package com.hpy.controller.portal;

import com.hpy.common.Const;
import com.hpy.common.ResponseCode;
import com.hpy.common.ResponseResult;
import com.hpy.dao.UserMapper;
import com.hpy.pojo.User;
import com.hpy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * Author: hpy
 * Date: 2019-09-29 10:34
 * Description: <描述>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseResult<User> login(String username, String password, HttpSession session) {
        ResponseResult<User> responseResult = userService.login(username, password);
        if (responseResult.isSuccess()) {
            // 登陆成功
            session.setAttribute(Const.CURRENT_USER, responseResult.getData());
        }
        return responseResult;
    }

    @GetMapping("/logout")
    public ResponseResult logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ResponseResult.createBySuccess();
    }

    @PostMapping("/register")
    public ResponseResult register(User user) {
        return userService.register(user);
    }

    @PostMapping("/check_valid")
    public ResponseResult checkValid(String str, String type) {
        return userService.checkValid(str, type);
    }

    @PostMapping("/get_user_info")
    public ResponseResult<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return ResponseResult.createBySuccess(user);
        }
        return ResponseResult.createByError("用户未登陆，无法获取用户信息");
    }

    @PostMapping("/forget_get_question")
    public ResponseResult forgetGetQuestion(String username) {
        return userService.selectQuestion(username);
    }

    @PostMapping("/forget_check_answer")
    public ResponseResult forgetCheckAnswer(String username, String question, String answer) {
        return userService.checkAnswer(username, question, answer);
    }

    @PostMapping("/forget_reset_password")
    public ResponseResult forgetResetPassword(String username, String passwordNew, String token) {
        return userService.forgetRestPassword(username, passwordNew, token);
    }

    @PostMapping("/reset_password")
    public ResponseResult resetPassword(HttpSession session, String passwordOld, String passwordNew) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return userService.resetPassword(user, passwordOld, passwordNew);
    }

    @PostMapping("/update_information")
    public ResponseResult<User> updateInformation(HttpSession session, User user) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        // ID不允许更改
        user.setId(currentUser.getId());

        ResponseResult<User> responseResult = userService.updateInformation(user);
        if (responseResult.isSuccess()) {
            // 设置用户名，并将更新后的用户信息放入session
            responseResult.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER, responseResult.getData());
        }
        return responseResult;
    }

    @PostMapping("/get_information")
    public ResponseResult<User> getInformation(HttpSession session) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return userService.getInformation(currentUser.getId());
    }

}
