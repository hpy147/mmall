package com.hpy.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hpy.common.ResponseCode;
import com.hpy.common.ResponseResult;
import com.hpy.dao.CategoryMapper;
import com.hpy.pojo.Category;
import com.hpy.service.CategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Author: hpy
 * Date: 2019-09-29 16:09
 * Description: <描述>
 */
@Service(value = "categoryService")
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @Transactional
    public ResponseResult addCategory(String categoryName, Integer parentId) {
        if (StringUtils.isBlank(categoryName) || parentId == null) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int rowCount = categoryMapper.insertSelective(category);
        if (rowCount > 0) {
            return ResponseResult.createBySuccess("添加分类成功");
        }
        return ResponseResult.createByError("添加分类失败");
    }

    @Override
    @Transactional
    public ResponseResult updateCategoryName(Integer categoryId, String categoryName) {
        if (StringUtils.isBlank(categoryName) || categoryId == null) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Category updateCategory = new Category();
        updateCategory.setId(categoryId);
        updateCategory.setName(categoryName);
        int updateCount = categoryMapper.updateByPrimaryKeySelective(updateCategory);
        if (updateCount > 0) {
            return ResponseResult.createBySuccess("分类名称修改成功");
        }
        return ResponseResult.createByError("分类名称修改失败");
    }

    @Override
    public ResponseResult getChildrenParallelCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectByParentId(categoryId);
        return ResponseResult.createBySuccess(categoryList);
    }

    @Override
    public ResponseResult getCategoryAndDeepChildrenCategory(Integer categoryId) {
        // 算出所有子节点，放入set集合中
        Set<Integer> categoryIdSet = Sets.newHashSet();
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categoryIdSet.add(categoryId);
        }
        this.findChildCategory(categoryIdSet, categoryId);

        // set集合转成list集合
        List<Integer> categoryIdList = Lists.newArrayList();
        categoryIdList.addAll(categoryIdSet);
        Collections.sort(categoryIdList);

        return ResponseResult.createBySuccess(categoryIdList);
    }

    /**
     * 递规查询出所有子节点的ID
     */
    private void findChildCategory(Set<Integer> sets, Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectByParentId(categoryId);
        for (Category category : categoryList) {
            sets.add(category.getId());
            this.findChildCategory(sets, category.getId());
        }
    }
}
