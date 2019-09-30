package com.hpy.service;

import com.hpy.common.ResponseResult;
import com.hpy.pojo.Category;

import java.util.List;

/**
 * Author: hpy
 * Date: 2019-09-29 16:09
 * Description: <描述>
 */
public interface CategoryService {
    ResponseResult addCategory(String categoryName, Integer parentId);

    ResponseResult updateCategoryName(Integer categoryId, String categoryName);

    ResponseResult getChildrenParallelCategory(Integer categoryId);

    ResponseResult getCategoryAndDeepChildrenCategory(Integer categoryId);
}
