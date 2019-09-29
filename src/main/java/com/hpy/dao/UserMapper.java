package com.hpy.dao;

import com.hpy.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByUsername(String username);

    int checkUsername(String username);

    int checkEmail(String email);

    String selectQuestionByUsername(String username);

    int checkAnswer(@Param("username") String username,
                    @Param("question") String question,
                    @Param("answer") String answer);

    int updatePasswordByUsername(@Param("username") String username,
                                 @Param("password") String password);

    int checkPasswordByUserId(@Param("userId") Integer userId,
                              @Param("password") String passwordOld);

    int checkEmailByUserId(@Param("email") String email,
                           @Param("userId") Integer userId);
}