<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="javax.servlet.jsp.PageContext" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE>
<html>
<head>
  <link rel="shortcut icon" href='${pageContext.request.contextPath}/rs/img/favicon.ico' type="image/x-icon">
  <link rel="icon" href='${pageContext.request.contextPath}/rs/img/favicon.ico' type="image/x-icon">
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

  <title>BUGANG error</title>

  <!-- CSS -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/rs/css/layout.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/rs/css/common.css">
  
<%--   <link rel="stylesheet" href="${pageContext.request.contextPath}/rs/css/login.css" type="text/css" /> --%>
<%--   <link rel="stylesheet" href="${pageContext.request.contextPath}/rs/css/font.css"> --%> 
<!--   <link href="https://fonts.googleapis.com/css?family=Noto+Sans+KR" rel="stylesheet"> -->

 <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100;300;400;500;700;900&display=swap" rel="stylesheet">

  <!-- JS -->
  <script src="${pageContext.request.contextPath}/rs/lib/jquery-3.2.0.min.js"></script>
 <script src="${pageContext.request.contextPath}/rs/js/commonUtils.js"></script>

</head>


<style>
div.loginBg > .error {
    position: fixed;
    left: 50%;
    top: 50%;
    width: 500px;
    height: 472px;
    padding: 70px 50px;    
    border-radius: 10px;
    transform: translate(-50%,-50%);
}
</style>


<body>
	<div class="loginBg">
        <h1><img src="${pageContext.request.contextPath}/rs/imgs/bugang_logo.png" alt="부강로고"></h1>
        
        <div class="error" style="text-align: center;">        
        	<p style="font-size:100px;color:#fff;font-weight: bold; line-height: 110px">404</p>
        	<p style=" color:#fff; font-weight: bold;font-size: 20px">PAGE NOT FOUND</p>
        	<p style=" color:#f2f2f2; padding-top:40px">페이지가 존재하지 않습니다.</p>
		</div>
	</div>
        
</body>
</html>