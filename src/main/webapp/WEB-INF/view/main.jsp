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
	<!-- register.js -->
	<script type="text/javascript" src="/view/js/main.js?ver<%=System.currentTimeMillis()%>"></script>
</head>
<body class="container-fluid">
	<!-- Title -->
	<div class="row pt-3"></div>
	<hr>
	<div class="row justify-content-around align-items-center">
		<div class="col-md-12 align-self-center text-center">
			<h1>dev4robi</h1>
		</div>
	</div>
	<hr>
	<!-- Signin form -->
	<div class="row pt-3"></div>
	<form>
		<div class="form-row justify-content-around align-items-center">
			<div class="form-group col-md-10 align-self-center">
				<label for="inputEmail4">Email</label>
				<input type="email" class="form-control" id="inputEmail4" placeholder="이메일">
			</div>
			<div class="form-group col-md-10 align-self-center">
				<label for="inputPassword4">Password</label>
				<input type="password" class="form-control" id="inputPassword4" placeholder="비밀번호">
			</div>
			<div class="form-group col-md-10 align-self-center">
				<button type="submit" class="btn btn-primary w-100">로그인</button>
			</div>
			<div class="form-group col-md-10 align-self-center">
				<div class="form-check">
					<input class="form-check-input" type="checkbox" id="gridCheck">
					<label class="form-check-label" for="gridCheck">
						로그인 유지
					</label>
				</div>
			</div>
		</div>
	</form>
	<!-- Social login -->
	<div class="row justify-content-around align-items-center">
		<div class="col-md-10 text-center align-self-center">
			<h5>또는 소셜 아이디로 가입</h5>
		</div>
		<div class="col-md-10 text-center align-self-center">
			<!-- Reference - https://developers.google.com/identity/protocols/OAuth2WebServer -->
			<input type="hidden" id="googleLoginUrl" value="${googleLoginUrl}">
			<c:choose>
				<c:when test="${not empty googleLoginUrl}">
					<button type="button" class="btn btn-danger w-100" id="btnGoogleLogin" >Google</button>
				</c:when>
				<c:otherwise>
					<button type="button" class="btn btn-danger w-100" id="btnGoogleLogin" disabled>Google (Comming soon)</button>
				</c:otherwise>
			</c:choose>
		</div>
		<div class="col-md-10 text-center align-self-center">
			<input type="hidden" id="kakaoLoginUrl" value="">
			<c:choose>
				<c:when test="${not empty kakaoLoginUrl}">
					<button type="button" class="btn btn-warning w-100" id="btnKakaoLogin">Kakao</button>
				</c:when>
				<c:otherwise>
					<button type="button" class="btn btn-warning w-100" id="btnKakaoLogin" disabled>Kakao (Comming soon)</button>
				</c:otherwise>
			</c:choose>
		</div>
		<div class="col-md-10 text-center align-self-center">
			<input type="hidden" id="naverLoginUrl" value="">
			<c:choose>
				<c:when test="${not empty naverLoginUrl}">
					<button type="button" class="btn btn-success w-100" id="btnNaverLogin">Naver</button>
				</c:when>
				<c:otherwise>
					<button type="button" class="btn btn-success w-100" id="btnNaverLogin" disabled>Naver (Comming soon)</button>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
</body>
</html>
