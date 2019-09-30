package com.hpy.service;

import com.hpy.common.ResponseResult;
import com.hpy.pojo.User;

/**
 * Author: hpy
 * Date: 2019-09-29 10:34
 * Description: <描述>
 */
public interface UserService {
    ResponseResult login(String username, String password);

    ResponseResult register(User user);

    ResponseResult checkValid(String str, String type);

    ResponseResult selectQuestion(String username);

    ResponseResult checkAnswer(String username, String password, String answer);

    ResponseResult forgetRestPassword(String username, String passwordNew, String token);

    ResponseResult resetPassword(User user, String passwordOld, String passwordNew);

    ResponseResult updateInformation(User user);

    ResponseResult getInformation(Integer id);

    ResponseResult checkAdmin(User user);
}
