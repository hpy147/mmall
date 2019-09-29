package com.hpy.service;

import com.hpy.common.ResponseResult;
import com.hpy.pojo.User;

/**
 * Author: hpy
 * Date: 2019-09-29 10:34
 * Description: <描述>
 */
public interface UserService {
    ResponseResult<User> login(String username, String password);

    ResponseResult register(User user);

    ResponseResult checkValid(String str, String type);

    ResponseResult selectQuestion(String username);

    ResponseResult checkAnswer(String username, String password, String answer);

    ResponseResult forgetRestPassword(String username, String passwordNew, String token);

    ResponseResult resetPassword(User user, String passwordOld, String passwordNew);

    ResponseResult<User> updateInformation(User user);

    ResponseResult<User> getInformation(Integer id);
}
