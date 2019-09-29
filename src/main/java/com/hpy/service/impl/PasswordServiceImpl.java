package com.hpy.service.impl;

import com.hpy.vo.PasswordVO;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Author: hpy
 * Date: 2019-09-29 10:45
 * Description: 密码服务
 */
@Component
public class PasswordServiceImpl implements PasswordService {

    @Value("${algorithmName}")
    private String algorithmName;

    @Value("${hashIterations}")
    private Integer hashIterations;

    @Value("${encode}")
    private Boolean encode;

    @Override
    public String encryptPassword(Object plaintextPassword) throws IllegalArgumentException {
        PasswordVO passwordVO = (PasswordVO) plaintextPassword;
        if (encode) {
            return new SimpleHash(algorithmName, passwordVO.getPassword(), passwordVO.getSalt(), hashIterations).toBase64();
        } else {
            return new SimpleHash(algorithmName, passwordVO.getPassword(), passwordVO.getSalt(), hashIterations).toHex();
        }
    }

    @Override
    public boolean passwordsMatch(Object submittedPlaintext, String encrypted) {
        return false;
    }
}
