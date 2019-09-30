package com.hpy.controller.backend;

import com.hpy.common.Const;
import com.hpy.common.ResponseCode;
import com.hpy.common.ResponseResult;
import com.hpy.pojo.Category;
import com.hpy.pojo.User;
import com.hpy.service.CategoryService;
import com.hpy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Author: hpy
 * Date: 2019-09-29 16:08
 * Description: <描述>
 */
@RestController
@RequestMapping("/manager/category")
public class CategoryManagerController {

    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping("/add_category")
    public ResponseResult addCategory(HttpSession session, String categoryName,
                                      @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (userService.checkAdmin(user).isSuccess()) {
            // 执行业务
            return categoryService.addCategory(categoryName, parentId);
        } else {
            return ResponseResult.createByError("权限不足，无法执行操作");
        }
    }

    @PostMapping("/set_category_name")
    public ResponseResult setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (userService.checkAdmin(user).isSuccess()) {
            // 执行业务
            return categoryService.updateCategoryName(categoryId, categoryName);
        } else {
            return ResponseResult.createByError("权限不足，无法执行操作");
        }
    }

    @PostMapping("/get_category")
    public ResponseResult<List<Category>> getChildrenParallelCategory(HttpSession session,
                                                                      @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (userService.checkAdmin(user).isSuccess()) {
            // 执行业务
            return categoryService.getChildrenParallelCategory(categoryId);
        } else {
            return ResponseResult.createByError("权限不足，无法执行操作");
        }
    }

    @PostMapping("/get_deep_category")
    public ResponseResult<List<Integer>> getCategoryAndDeepChildrenCategory(HttpSession session,
                                                                            @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (userService.checkAdmin(user).isSuccess()) {
            // 执行业务，获取当前节点和递规获取其所有子节点的ID
            return categoryService.getCategoryAndDeepChildrenCategory(categoryId);
        } else {
            return ResponseResult.createByError("权限不足，无法执行操作");
        }
    }

}
