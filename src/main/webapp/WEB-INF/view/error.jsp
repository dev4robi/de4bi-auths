<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
	<!-- Meta -->
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
	<!-- Javascript and css library -->
	<!-- jquery 3.4.0 -->
	<script type="text/javascript" src="/common/lib/jquery-3.4.0/jquery-3.4.0.min.js"></script>
	<script type="text/javascript" src="/common/lib/jquery-3.4.0/jquery-cookie-1.4.1.js"></script>
	<!-- bootstrap 4.3.1 -->
	<link rel="stylesheet" href="/common/lib/bootstrap-4.3.1/css/bootstrap.min.css">
	<script type="text/javascript" src="/common/lib/bootstrap-4.3.1/js/bootstrap.min.js"></script>
	<!-- popper 1.14.7 -->
	<script type="text/javascript" src="/common/lib/popper-1.14.7/popper-1.14.7.js"></script>
	<script type="text/javascript" src="/common/lib/popper-1.14.7/tooltip-1.3.2.js"></script>
	<!-- fontawesome 5.8.1 -->
	<link rel="stylesheet" href="/common/lib/fontawesome-5.8.1/css/fontawesome-5.8.1.css">
	<script type="text/javascript" src="/common/lib/fontawesome-5.8.1/js/fontawesome-5.8.1.js"></script>
	<!-- common.js -->
	<script type="text/javascript" src="/common/js/common.js?ver=<%=System.currentTimeMillis()%>"></script>
	<!-- error.js -->
	<script type="text/javascript" src="/view/js/error.js?ver=<%=System.currentTimeMillis()%>"></script>
</head>
<body class="container-fluid">
	<!-- Title -->
	<div class="row pt-3"></div>
	<hr>
	<div class="row justify-content-around align-items-center">
		<div class="col-md-12 align-self-center text-center">
			<h1>ERROR!</h1>
		</div>
	</div>
	<hr>
	<!-- Signin form -->
	<div class="row justify-content-around align-items-center">
		<div class="col-md-12 align-self-center pt-2">
			<c:choose>
				<c:when test="${not empty errorMsg}">
					<p>${errorMsg}</p>
				</c:when>
				<c:otherwise>
					<p>서버 오류가 발생했습니다.</p>
				</c:otherwise>
			</c:choose>
		</div>
		<div class="col-md-12 align-self-center text-center"><hr></div>
		<div class="col-md-12 align-self-center text-center">
			<button type="button" class="btn btn-info w-100" id="btnGotoMain">메인으로 돌아가기</button>
		</div>
	</div>
	<!-- Hidden data -->
	<input type="hidden" id="alertMsg" value="${alertMsg}">
</body>
</html>
