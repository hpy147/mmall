<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>index</title>
</head>
<body>

<h3>springMVC文件上传</h3>
<form action="${pageContext.request.contextPath}/manager/product/upload" method="post" enctype="multipart/form-data">
    <input type="file" name="update_file"><br/>
    <input type="submit" value="上传">
</form>

<h3>富文本文件上传</h3>
<form action="${pageContext.request.contextPath}/manager/product/richtext_img_upload" method="post" enctype="multipart/form-data">
    <input type="file" name="update_file"><br/>
    <input type="submit" value="上传">
</form>

</body>
</html>
