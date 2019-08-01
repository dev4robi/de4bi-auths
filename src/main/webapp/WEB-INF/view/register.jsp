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
	<script type="text/javascript" src="/common/js/common.js?ver<%=System.currentTimeMillis()%>"></script>
	<!-- register.js -->
	<script type="text/javascript" src="/view/js/register.js?ver<%=System.currentTimeMillis()%>"></script>
</head>
<body class="container-fluid">
	<!-- Title -->
	<div class="row pt-3"></div>
	<hr>
	<div class="row justify-content-around align-items-center">
		<div class="col-sm-12 align-self-center text-center">
			<h1>회원 가입</h1>
		</div>
	</div>
	<hr>
	<!-- Signin form -->
	<div class="row pt-3"></div>
	<form>
		<div class="form-row justify-content-around align-items-center">
			<div class="form-group col-md-7 align-self-center">
				<label for="input_email"><b>Email</b></label>
				<input type="email" class="form-control" id="input_email" value="${email}" readonly>
			</div>
			<div class="form-group col-md-7 align-self-center">
				<label for="input_password"><b>Password</b></label>
				<input type="password" class="form-control" id="input_password" placeholder="비밀번호(8~32자)">
			</div>
            <div class="form-group col-md-7 align-self-center">
				<label for="input_password_check"><b>Password Check</b></label>
				<input type="password" class="form-control" id="input_password_check" placeholder="비밀번호 확인">
			</div>
            <div class="form-group col-md-7 align-self-center">
                <label for="inputNickname4"><b>Nickname</b></label>
                <div class="input-group">
                    <input type="text" class="form-control" placeholder="닉네임(2~16자)" aria-label="닉네임" aria-describedby="button_nickname_duplicated_check" id="input_nickname">
                    <div class="input-group-append">
                        <button class="btn btn-outline-success" type="button" id="button_nickname_duplicated_check">중복 확인</button>
                    </div>
                </div>
			</div>
            <div class="form-group col-md-7 align-self-center">
				<label for="input_full_name"><b>Full Name</b></label>
				<input type="text" class="form-control" id="input_full_name" placeholder="이름">
			</div>
            <div class="form-group col-md-7 align-self-center">
				<label for="input_radio_gender"><b>Gender</b></label><br>
				<div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="inlineRadioOptions" id="input_radio_gender_male" value="M">
                    <label class="form-check-label" for="input_radio_gender_male">M(남)</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="inlineRadioOptions" id="input_radio_gender_female" value="F">
                    <label class="form-check-label" for="input_radio_gender_female">F(여)</label>
                </div>
			</div>
            <div class="form-group col-md-7 align-self-center">
				<label for="input_date_of_birth"><b>Date of birth</b></label>
				<input type="text" class="form-control" id="input_date_of_birth" placeholder="생년월일(2019.02.21)">
			</div>
			<div class="form-group col-md-7 align-self-center">
                <label for="inputGender4"><b>Member</b></label><br>
                <div class="accordion" id="accordionExample">
                    <div class="card">
                        <div class="card-header" id="headingOne">
                            <h2 class="mb-0"><button class="btn btn-link" type="button" data-toggle="collapse" data-target="#collapseOne" aria-expanded="true" aria-controls="collapseOne">
                                회원가입 약관 (눌러서 보기)
                            </button></h2>
                        </div>
                        <div id="collapseOne" class="collapse" aria-labelledby="headingOne" data-parent="#accordionExample">
                            <div class="card-body">
                                <span id=span_terms_of_service>${termsOfService}</span>
                            </div>
                        </div>
                    </div>
                </div><br>
				<div class="form-check">
					<input class="form-check-input" type="checkbox" id="input_terms_of_service">
					<label class="form-check-label" for="input_terms_of_service">
						상기 회원가입 약관에 동의합니다.
					</label>
				</div>
			</div>
            <div class="form-group col-md-7 align-self-center pt-2"></div>
            <div class="form-group col-md-7 align-self-center">
				<button type="button" class="btn btn-primary w-100" id="button_submit_register">가입</button>
			</div>
		</div>
		<!-- Sign and nonce-->
    	<input class="form-check-input" type="hidden" name="sign" value="${sign}" id="input_sign">
		<input class="form-check-input" type="hidden" name="nonce" value="${nonce}" id="input_nonce">
	</form>
	<!-- Hidden datas -->
	<input type="hidden" value="${clientSalt}" id="clientSalt">
</body>
</html>
