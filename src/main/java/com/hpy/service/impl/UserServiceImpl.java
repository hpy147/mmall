package com.hpy.service.impl;

import com.hpy.common.Const;
import com.hpy.common.ResponseCode;
import com.hpy.common.ResponseResult;
import com.hpy.common.TokenCache;
import com.hpy.dao.UserMapper;
import com.hpy.pojo.User;
import com.hpy.service.UserService;
import com.hpy.vo.PasswordVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Author: hpy
 * Date: 2019-09-29 10:34
 * Description: <描述>
 */
@Service(value = "userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordService passwordService;

    @Override
    public ResponseResult login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            // 密码加密再进行匹配
            PasswordVO passwordVO = new PasswordVO(password, username);
            String encryptPassword = passwordService.encryptPassword(passwordVO);

            if (user.getPassword().equals(encryptPassword)) {
                // 登陆成功，清空密码
                user.setPassword(StringUtils.EMPTY);
                return ResponseResult.createBySuccess("登陆成功", user);
            }
        }
        return ResponseResult.createByError("登陆失败，用户名或密码错误");
    }

    @Override
    @Transactional
    public ResponseResult register(User user) {
        // 检查用户名和邮箱是否已被注册
        ResponseResult responseResult = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!responseResult.isSuccess()) {
            return responseResult;
        }
        responseResult = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!responseResult.isSuccess()) {
            return responseResult;
        }
        // 对密码进行加密
        PasswordVO passwordVO = new PasswordVO(user.getPassword(), user.getUsername());
        String encryptPassword = passwordService.encryptPassword(passwordVO);
        user.setPassword(encryptPassword);
        // 设置角色
        user.setRole(Const.Role.ROLE_CUSTOMER);
        int rowCount = userMapper.insert(user);
        if (rowCount == 0) {
            return ResponseResult.createByError("注册失败");
        }
        return ResponseResult.createBySuccess("注册成功");
    }

    @Override
    public ResponseResult checkValid(String str, String type) {
        if (StringUtils.isBlank(str) || StringUtils.isBlank(type)) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        if (Const.USERNAME.equals(type)) {
            int rowCount = userMapper.checkUsername(str);
            if (rowCount > 0) {
                return ResponseResult.createByError("用户名已存在");
            }
        }
        if (Const.EMAIL.equals(type)) {
            int rowCount = userMapper.checkEmail(str);
            if (rowCount > 0) {
                return ResponseResult.createByError("邮箱已存在");
            }
        }
        return ResponseResult.createBySuccess();
    }

    @Override
    public ResponseResult selectQuestion(String username) {
        ResponseResult responseResult = this.checkValid(username, Const.USERNAME);
        if (responseResult.isSuccess()) {
            return ResponseResult.createByError("该用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isBlank(question)) {
            return ResponseResult.createByError("用户未设置找回密码问题");
        }
        return ResponseResult.createBySuccess(question);
    }

    @Override
    public ResponseResult checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            // 问题和问题答案是该用户的，并且是正确的, 返回给前台 Token
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ResponseResult.createBySuccess(forgetToken);
        }
        return ResponseResult.createByError("问题答案错误");
    }

    @Override
    @Transactional
    public ResponseResult forgetRestPassword(String username, String passwordNew, String token) {
        // Token 不能为空
        if (StringUtils.isBlank(token)) {
            return ResponseResult.createByError("Token不能为空");
        }
        // 从缓存中取出 Token
        String cacheToken = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(cacheToken)) {
            return ResponseResult.createByError("Token已过期，请重新获取Token");
        }
        if (StringUtils.equals(token, cacheToken)) {
            // Token正确，将Token设置为空，防止使用该Token重复修改密码
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, StringUtils.EMPTY);
            // 修改密码
            PasswordVO passwordVO = new PasswordVO(passwordNew, username);
            String encryptPassword = passwordService.encryptPassword(passwordVO);
            int rowCount = userMapper.updatePasswordByUsername(username, encryptPassword);
            if (rowCount > 0) {
                return ResponseResult.createBySuccess("密码修改成功");
            }
            return ResponseResult.createByError("密码修改失败");
        }
        return ResponseResult.createByError("Token不匹配，密码修改失败");
    }

    @Override
    @Transactional
    public ResponseResult resetPassword(User user, String passwordOld, String passwordNew) {
        // 校验旧密码是否正确
        PasswordVO passwordVO = new PasswordVO(passwordOld, user.getUsername());
        String encryptPassword = passwordService.encryptPassword(passwordVO);
        int resultCount = userMapper.checkPasswordByUserId(user.getId(), encryptPassword);
        if (resultCount == 0) {
            // 旧密码错误
            return ResponseResult.createByError("旧密码错误");
        }
        // 修改密码
        passwordVO = new PasswordVO(passwordNew, user.getUsername());
        encryptPassword = passwordService.encryptPassword(passwordVO);
        user.setPassword(encryptPassword);
        int rowCount = userMapper.updateByPrimaryKeySelective(user);
        if (rowCount > 0) {
            user.setPassword(StringUtils.EMPTY);
            return ResponseResult.createBySuccess("密码修改成功");
        }
        return ResponseResult.createBySuccess("密码修改失败");
    }

    @Override
    @Transactional
    public ResponseResult updateInformation(User user) {
        // 校验邮箱
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ResponseResult.createByError("邮箱已存在，请更换邮箱再重试");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            updateUser.setPassword(StringUtils.EMPTY);
            return ResponseResult.createBySuccess("个人信息更新成功", updateUser);
        }
        return ResponseResult.createByError("个人信息更新失败");
    }

    @Override
    public ResponseResult getInformation(Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null) {
            return ResponseResult.createByError("用户信息获取失败");
        }
        user.setPassword(StringUtils.EMPTY);
        return ResponseResult.createBySuccess(user);
    }

    /**
     * 判断是否为管理员
     */
    @Override
    public ResponseResult checkAdmin(User user) {
        if (user != null) {
            if (Const.Role.ROLE_ADMIN == user.getRole()) {
                return ResponseResult.createBySuccess();
            }
        }
        return ResponseResult.createByError("权限不足");
    }


}
