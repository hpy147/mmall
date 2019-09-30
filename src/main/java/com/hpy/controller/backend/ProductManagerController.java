package com.hpy.controller.backend;

import com.google.common.collect.Maps;
import com.hpy.common.Const;
import com.hpy.common.ResponseResult;
import com.hpy.pojo.Product;
import com.hpy.pojo.User;
import com.hpy.service.FileService;
import com.hpy.service.ProductService;
import com.hpy.service.UserService;
import com.hpy.util.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Author: hpy
 * Date: 2019-09-29
 * Description: <描述>
 */
@RestController
@RequestMapping("/manager/product")
public class ProductManagerController {

    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private FileService fileService;

    @PostMapping("/save")
    public ResponseResult updateOrInsertProduct(HttpSession session, Product product) {
        ResponseResult responseResult = this.checkAdmin(session);
        if (responseResult.isSuccess()) {
            return productService.updateOrInsertProduct(product);
        }
        return responseResult;
    }

    @PostMapping("/set_sale_status")
    public ResponseResult setSaleStatus(HttpSession session, Integer productId, Integer status) {
        ResponseResult responseResult = this.checkAdmin(session);
        if (responseResult.isSuccess()) {
            return productService.setSaleStatus(productId, status);
        }
        return responseResult;
    }

    @PostMapping("/detail")
    public ResponseResult getDetail(HttpSession session, Integer productId) {
        ResponseResult responseResult = this.checkAdmin(session);
        if (responseResult.isSuccess()) {
            return productService.managerProductDetail(productId);
        }
        return responseResult;
    }

    @PostMapping("/list")
    public ResponseResult getList(HttpSession session,
                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                   @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        ResponseResult responseResult = this.checkAdmin(session);
        if (responseResult.isSuccess()) {
            return productService.getProductList(pageNum, pageSize);
        }
        return responseResult;
    }

    @PostMapping("/search")
    public ResponseResult search(HttpSession session, String productName, Integer productId,
                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        ResponseResult responseResult = this.checkAdmin(session);
        if (responseResult.isSuccess()) {
            return productService.searchProduct(productName, productId, pageNum, pageSize);
        }
        return responseResult;
    }

    @PostMapping("/upload")
    public ResponseResult search(HttpSession session,
                                 @RequestParam(value = "upload_file") MultipartFile file) {
        ResponseResult responseResult = this.checkAdmin(session);
        if (responseResult.isSuccess()) {

            String path = session.getServletContext().getRealPath("upload");
            String fileName = fileService.upload(file, path);
            if (StringUtils.isBlank(fileName)) {
                return ResponseResult.createByError("上传失败");
            }
            String url = PropertyUtils.getProperty("ftp.server.http.prefix") + fileName;

            Map<String, String> fileMap = Maps.newHashMap();
            fileMap.put("uri", fileName);
            fileMap.put("url", url);
            return ResponseResult.createBySuccess(fileMap);
        }
        return responseResult;
    }

    @PostMapping("/richtext_img_upload")
    public Map richTextImgUpload(HttpSession session, HttpServletResponse response,
                                 @RequestParam(value = "upload_file") MultipartFile file) {

        Map<String, Object> resultMap = Maps.newHashMap();

        ResponseResult responseResult = this.checkAdmin(session);
        if (responseResult.isSuccess()) {

            //富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
//        {
//            "success": true/false,
//                "msg": "error message", # optional
//            "file_path": "[real file path]"
//        }
            String path = session.getServletContext().getRealPath("upload");
            String fileName = fileService.upload(file, path);
            if (StringUtils.isBlank(fileName)) {
                resultMap.put("success", false);
                resultMap.put("msg", "文件上传失败");
                return resultMap;
            }
            String url = PropertyUtils.getProperty("ftp.server.http.prefix") + fileName;
            resultMap.put("success", true);
            resultMap.put("msg", "文件上传成功");
            resultMap.put("file_path", url);
            //
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }
        String msg = responseResult.getMsg();
        resultMap.put("success", false);
        resultMap.put("msg", msg);
        return resultMap;
    }



    // 校验登陆和权限
    private ResponseResult checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseResult.createByError("用户未登陆");
        }
        return userService.checkAdmin(user);
    }

}
