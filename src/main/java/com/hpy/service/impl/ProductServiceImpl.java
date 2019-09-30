package com.hpy.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.hpy.common.Const;
import com.hpy.common.ResponseCode;
import com.hpy.common.ResponseResult;
import com.hpy.dao.CategoryMapper;
import com.hpy.dao.ProductMapper;
import com.hpy.pojo.Category;
import com.hpy.pojo.Product;
import com.hpy.service.CategoryService;
import com.hpy.service.ProductService;
import com.hpy.util.DateTimeUtils;
import com.hpy.util.PropertyUtils;
import com.hpy.vo.ProductDetailVO;
import com.hpy.vo.ProductListVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Author: hpy
 * Date: 2019-09-29
 * Description: <描述>
 */
@Service(value = "productService")
public class ProductServiceImpl implements ProductService {

    private Logger logger = LoggerFactory.getLogger(ProductService.class);


    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CategoryService categoryService;

    @Override
    @Transactional
    public ResponseResult updateOrInsertProduct(Product product) {
        if (product == null) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 将第一张子图设置为主图
        if (StringUtils.isNotBlank(product.getSubImages())) {
            String[] subImageArray = product.getSubImages().split(",");
            product.setMainImage(subImageArray[0]);
        }
        // 判断是新增还是修改
        if (product.getId() == null) {
            // 新增
            int rowCount = productMapper.insert(product);
            if (rowCount > 0) {
                return ResponseResult.createBySuccess("商品新增成功");
            }
            return ResponseResult.createByError("商品新增失败");
        } else {
            // 修改
            int updateCount = productMapper.updateByPrimaryKeySelective(product);
            if (updateCount > 0) {
                return ResponseResult.createBySuccess("商品修改成功");
            }
            return ResponseResult.createByError("商品修改失败");
        }
    }

    @Override
    @Transactional
    public ResponseResult setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int updateCount = productMapper.updateByPrimaryKeySelective(product);
        if (updateCount > 0) {
            return ResponseResult.createBySuccess("商品状态修改成功");
        }
        return ResponseResult.createByError("商品状态修改失败");
    }

    @Override
    public ResponseResult managerProductDetail(Integer productId) {
        if (productId == null) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ResponseResult.createByError("商品已下架或删除");
        }
        ProductDetailVO productDetailVO = this.assembleProductDetailVO(product);
        return ResponseResult.createBySuccess(productDetailVO);
    }

    @Override
    public ResponseResult getProductList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Product> productList = productMapper.selectList();
        List<ProductListVO> productListVOList = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVO productListVO = this.assembleProductListVO(product);
            productListVOList.add(productListVO);
        }

        PageInfo<ProductListVO> pageInfo = new PageInfo<>(productListVOList);
        return ResponseResult.createBySuccess(pageInfo);
    }

    @Override
    public ResponseResult searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize) {
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        PageHelper.startPage(pageNum, pageSize);

        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
        List<ProductListVO> productListVOList = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVO productListVO = this.assembleProductListVO(product);
            productListVOList.add(productListVO);
        }

        PageInfo<ProductListVO> pageInfo = new PageInfo<>(productListVOList);
        return ResponseResult.createBySuccess(pageInfo);
    }

    @Override
    public ResponseResult getProductDetail(Integer productId) {
        if (productId == null) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            // 商品不存在
            return ResponseResult.createByError("商品不存在");
        }
        if (product.getStatus() == Const.ProductStatusEnum.UN_ON_SALE.getCode()) {
            // 商品已下架
            return ResponseResult.createByError("商品已下架");
        }
        if (product.getStatus() == Const.ProductStatusEnum.DELETE.getCode()) {
            // 商品已删除
            return ResponseResult.createByError("商品已删除");
        }
        // 商品正常状态， Product -> ProductDetailVo
        ProductDetailVO productDetailVO = this.assembleProductDetailVO(product);
        return ResponseResult.createBySuccess(productDetailVO);
    }

    @Override
    public ResponseResult getProductByKeywordCategory(String keyword, Integer categoryId, String orderBy,
                                                      Integer pageNum, Integer pageSize) {
        // 要查询的两个条件不能全部为空
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ResponseResult.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        // 设置分页
        PageHelper.startPage(pageNum, pageSize);

        // categoryIdList初始值设置为null，为mybatis查询做准备
        List<Integer> categoryIdList = null;

        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                // 没有查找到商品分类，也没有传入关键字，就返回一个空的PageInfo给前端，不返回error
                List<ProductListVO> productListVOList = Lists.newArrayList();
                PageInfo<ProductListVO> pageInfo = new PageInfo<>(productListVOList);
                return ResponseResult.createBySuccess(pageInfo);
            }
            if (category != null) {
                // 如果分类存在，递规遍历自身和所有子分类
                categoryIdList = (List<Integer>) categoryService.getCategoryAndDeepChildrenCategory(category.getId()).getData();
            }
        }

        if (StringUtils.isBlank(keyword)) {
            // keyword如果没有值就设置为null，为mybatis查询做准备
            keyword = null;
        } else {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        // 处理排序
        // orderBy = price_asc 或者 price_desc
        if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
            String[] orderByArray = orderBy.split("_");
            PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
        }

        List<Product> productList = productMapper.selectByNameAndCategoryIds(keyword, categoryIdList);

        // Product -> ProductListVO
        List<ProductListVO> productListVOList = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVO productListVO = this.assembleProductListVO(product);
            productListVOList.add(productListVO);
        }
        PageInfo<ProductListVO> pageInfo = new PageInfo<>(productListVOList);
        return ResponseResult.createBySuccess(pageInfo);
    }


    // Product -> ProductDetailVO
    private ProductDetailVO assembleProductDetailVO(Product product) {
        ProductDetailVO productDetailVO = new ProductDetailVO();
        // 值拷贝
        BeanUtils.copyProperties(product, productDetailVO);
        // 设置图片前缀
        productDetailVO.setImageHost(PropertyUtils.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        // 设置商品的父分类的ID
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category != null) {
            productDetailVO.setParentCategoryId(category.getParentId());
        }
        // 设置时间
        productDetailVO.setCreateTime(DateTimeUtils.dateToStr(product.getCreateTime()));
        productDetailVO.setUpdateTime(DateTimeUtils.dateToStr(product.getUpdateTime()));

        return productDetailVO;
    }

    // Product -> ProductListVO
    private ProductListVO assembleProductListVO(Product product) {
        ProductListVO productListVO = new ProductListVO();
        BeanUtils.copyProperties(product, productListVO);
        productListVO.setImageHost(PropertyUtils.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        return productListVO;
    }

}
