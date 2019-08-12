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
	<!-- register.js -->
	<script type="text/javascript" src="/view/js/main.js?ver=<%=System.currentTimeMillis()%>"></script>
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
	<div class="row pt-3"></div>
	<!-- User info form (when logined) -->
	<div id="div_userinfo">
		<div class="form-row justify-content-around align-items-center">
			<div class="form-group col-md-1"></div>
			<div class="form-group col-md-2 align-self-center">
				<label for="input_info_id">User No#</label>
				<input type="email" class="form-control" id="input_info_id" placeholder="회원번호" readonly>
			</div>
			<div class="form-group col-md-4 align-self-center">
				<label for="input_info_email">Email</label>
				<input type="email" class="form-control" id="input_info_email" placeholder="이메일" readonly>
			</div>
			<div class="form-group col-md-2 align-self-center">
				<label for="input_info_access_level">Level</label>
				<input type="text" class="form-control" id="input_access_level" placeholder="접근 레벨" readonly>
			</div>
			<div class="form-group col-md-2 align-self-center">
				<label for="input_info_status">Status</label>
				<input type="text" class="form-control" id="input_info_status" placeholder="상태" readonly>
			</div>
			<div class="form-group col-md-1"></div>
		</div>
		<div class="form-row justify-content-around align-items-center">
			<div class="form-group col-md-1"></div>
			<div class="form-group col-md-4 align-self-center">
				<label for="input_info_password">Origin Password</label>
				<input type="password" class="form-control" id="input_info_origin_password" placeholder="기존 비밀번호">
			</div>
			<div class="form-group col-md-3 align-self-center">
				<label for="input_info_password">New Password</label>
				<input type="password" class="form-control" id="input_info_password" placeholder="변경할 비밀번호">
			</div>
			<div class="form-group col-md-3 align-self-center">
				<label for="input_info_password_check">New Password Check</label>
				<input type="password" class="form-control" id="input_info_password_check" placeholder="변경할 비밀번호 확인">
			</div>
			<div class="form-group col-md-1"></div>
		</div>
		<div class="form-row justify-content-around align-items-center">
			<div class="form-group col-md-1"></div>
			<div class="form-group col-md-3 align-self-center">
				<label for="input_info_nickname">Nickname</label>
				<input type="text" class="form-control" id="input_info_nickname" placeholder="닉네임 (2~16자)" readonly>
			</div>
			<div class="form-group col-md-3 align-self-center">
				<label for="input_info_full_name">Full Name</label>
				<input type="text" class="form-control" id="input_info_full_name" placeholder="이름">
			</div>
			<div class="form-group col-md-3 align-self-center">
				<label for="input_info_date_of_birth">Date of birth</label>
				<input type="text" class="form-control" id="input_info_date_of_birth" placeholder="생년월일">
			</div>
            <div class="form-group col-md-1 align-self-center">
				<label for="input_info_radio_gender">Gender</label><br>
				<div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="inlineRadioOptions" id="input_info_radio_gender_male" value="M">
                    <label class="form-check-label" for="input_info_radio_gender_male">M(남)</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="inlineRadioOptions" id="input_info_radio_gender_female" value="F">
                    <label class="form-check-label" for="input_info_radio_gender_female">F(여)</label>
                </div>
			</div>
			<div class="form-group col-md-1"></div>
		</div>
		<div class="form-row justify-content-around align-items-center">
			<div class="form-group col-md-1"></div>
			<div class="form-group col-md-4 align-self-center">
				<label for="input_info_join_time">Join Date</label>
				<input type="text" class="form-control" id="input_info_join_time" placeholder="가입일자" readonly>
			</div>
			<div class="form-group col-md-3 align-self-center">
				<label for="input_info_last_login_time">Last Login Date</label>
				<input type="text" class="form-control" id="input_info_last_login_time" placeholder="마지막 로그인 일자" readonly>
			</div>
			<div class="form-group col-md-3 align-self-center">
				<label for="input_info_service_accessible_time">Service Accessible Date</label>
				<input type="text" class="form-control" id="input_info_service_accessible_time" placeholder="서비스 접근가능 일자" readonly>
			</div>
			<div class="form-group col-md-1"></div>
		</div>
		<div class="form-row justify-content-around align-items-center pt-3">
			<div class="col-md-1"></div>
			<div class="col-md-8 align-self-center">
				<button type="submit" class="btn btn-success w-100" id="button_info_update">정보 수정</button>
			</div>
			<div class="col-md-2 align-self-center">
				<button type="submit" class="btn btn-secondary w-100" id="button_info_deregister">회원 탈퇴</button>
			</div>
			<div class="col-md-1"></div>
		</div>
		<div class="form-row justify-content-around align-items-center pt-2">
			<div class="col-md-1"></div>
			<div class="col-md-10 align-self-center">
				<button type="submit" class="btn btn-danger w-100" id="button_info_logout">로그아웃</button>
			</div>
			<div class="col-md-1"></div>
		</div>
	</div>
	<!-- Signin form -->
	<div id="div_signin">
		<!-- login -->
		<div class="form-row justify-content-around align-items-center">
			<div class="form-group col-md-10 align-self-center">
				<label for="input_email">Email</label>
				<input type="email" class="form-control" id="input_email" placeholder="이메일">
			</div>
			<div class="form-group col-md-10 align-self-center">
				<label for="input_password">Password</label>
				<input type="password" class="form-control" id="input_password" placeholder="비밀번호">
			</div>
			<div class="col-md-10 align-self-center">
				<button type="submit" class="btn btn-primary w-100" id="button_login">로그인</button>
			</div>
			<div class="form-group col-md-10 align-self-center">
				<div class="form-check">
					<input class="form-check-input" type="checkbox" id="cb_keep_logged_in">
					<label class="form-check-label" for="cb_keep_logged_in">
						15일간 로그인 유지
					</label>
				</div>
			</div>
		</div>
		<!-- sing up with social login -->
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
	</div>
	<!-- Hidden params -->
	<input type="hidden" id="input_clientSalt" value="${clientSalt}">
	<input type="hidden" id="input_audience" value="${audience}">
	<input type="hidden" id="input_duration" value="${duration}">
	<input type="hidden" id="input_afterIssueParam" value="${afterIssueParam}">
</body>
</html>
