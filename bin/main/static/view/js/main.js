$(document).ready(function(){
    // update 
    $('#div_userinfo').hide();
    $('#div_signin').hide();

    // user info form
    // logout button
    $('#button_info_logout').on('click', function(){
        logout();
    });

    $('#button_info_update').on('click', function(){
        updateUserInfo();
    });

    $('#button_info_deregister').on('click', function(){
        deregister();
    });

    // signin form
    // login button
    $('#input_password').keyup(function(key){
        if (key.keyCode == 13) login(); // enter key
    });
    $('#button_login').on('click', function(){
        login();
    });

    // sign-up with google button
    $('#btnGoogleLogin').on('click', function(){
        window.location.href = $('#googleLoginUrl').val();
    });

    // sign-up with kakao button
    $('#btnKakaoLogin').on('click', function(){
        window.location.href = $('#kakaoLoginUrl').val();
    });

    // sign-up with naver button
    $('#btnNaverLogin').on('click', function(){
        window.location.href = $('#naverLoginUrl').val();
    });

    // check logged in
    var userJwt = $.cookie('userJwt');
    if (!!userJwt) {
        getUserInfoWihtUpdateUI(userJwt);
    }
    else {
        $('#div_signin').show();
    }
});

function getUserInfoWihtUpdateUI(userJwt) {
    AJAX.apiCall('GET', '/users', {'userJwt':userJwt}, null,
        // Always
        function() {
            // ...
        },
        // Success
        function(apiResult) {
            if (!AJAX.checkResultSuccess(apiResult)) {
                alert(AJAX.getResultMsg(apiResult));
                $.removeCookie('userJwt');
                location.replace('/main');
            }

            $('#input_info_id').val(AJAX.getResultData(apiResult, 'id'));
            $('#input_info_email').val(AJAX.getResultData(apiResult, 'email'));
            $('#input_access_level').val(AJAX.getResultData(apiResult, 'accessLevel'));
            $('#input_info_status').val(AJAX.getResultData(apiResult, 'status'));
            $('#input_info_nickname').val(AJAX.getResultData(apiResult, 'nickname'));
            $('#input_info_full_name').val(AJAX.getResultData(apiResult, 'fullName'));
            $('#input_info_date_of_birth').val(new Date(AJAX.getResultData(apiResult, 'dateOfBirth')).format('yyyy.MM.dd'));
            
            if (AJAX.getResultData(apiResult, 'gender') == 'M') {
                $('#input_info_radio_gender_male').click();
            }
            else {
                $('#input_info_radio_gender_femail').click();
            }
            
            $('#input_info_join_time').val(new Date(AJAX.getResultData(apiResult, 'joinTime')).format('yyyy-MM-dd HH:mm:ss'));
            $('#input_info_last_login_time').val(new Date(AJAX.getResultData(apiResult, 'lastLoginTime')).format('yyyy-MM-dd HH:mm:ss'));
            $('#input_info_service_accessible_time').val(new Date(AJAX.getResultData(apiResult, 'accessibleTime')).format('yyyy-MM-dd HH:mm:ss'));

            $('#div_signin').hide();
            $('#div_userinfo').show();
        },
        // Fail
        function() {
            alert('서버와 통신에 실패했습니다.');
            return;
        },
    )
}

// 로그인 시도
function login() {
    var audience = $('#input_audience').val();
    var email = $('#input_email').val();
    var password = $('#input_password').val();
    var duration = $('input_duration').val();

    if (!audience) {
        alert('페이지에 오류가 발생했습니다.\n(audience: ' + audience + ')');
        return;
    }

    if (!email || email.length == 0) {
        alert('이메일이 비었습니다.');
        return;
    }

    if (!password || password.length == 0) {
        alert('비밀번호가 비었습니다.');
        return;
    }

    if (!duration) {
        duration = 0; // default duration in server
    }

    var reqBody = {
        "audience" : audience,
        "email" : email,
        "password" : SHA256(password + $('#input_clientSalt').val()),
        "duration" : duration
    };

    AJAX.apiCall('POST', '/users/api/jwt/issue', null, reqBody,
        // Always
        function() {
            // alert(JSON.stringify(reqBody));
        },
        // Success
        function(apiResult) {
            if (AJAX.checkResultSuccess(apiResult)) {
                var userJwt = AJAX.getResultData(apiResult, 'userJwt');
                var keepLoggedIn = $('#cb_keep_logged_in').is(':checked');
                afterIssuing(userJwt, keepLoggedIn);
            }
            else {
                alert('로그인에 실패했습니다.\n(' + apiResult.result_message + ')');
            }
        },
        // Fail
        function() {
            alert('서버와 통신에 실패했습니다.');
        }
    );
}

// 로그아웃
function logout() {
    $.removeCookie('userJwt');
    alert('로그아웃 되었습니다.');
    location.replace('/main');
}

// 회원정보 수정
function updateUserInfo() {
    //{"password","nickname","fullName","gender","dateOfBirth"}   

    if (!confirm('정말로 회원 정보를 수정하시겠습니까?')) {
        alert('회원정보 수정을 취소했습니다.');
        return;
    }

    var password;
    var nickname;
    var fullName;
    var gender;
    var dateOfBirth;

    // 회원정보 수정/탈퇴시 패스워드 전달하여 한번 더 검증하게 함.
    // 회원정보 수정 데이터 파싱및 검증부분 만들기. 여기부터 시작 @@
}

// 회원탈퇴
function deregister() {
    if (!confirm('회원탈퇴후 복원할 수 없습니다.\n정말로 탈퇴하시겠습니까?')) {
        alert('회원 탈퇴를 취소했습니다.');
        return;
    }

    AJAX.apiCall('DELETE', '/user', $.cookie('userJwt'), null,
        // Always
        function() {
            // ...
        },
        // Success
        function(apiResult) {
            if (!AJAX.checkResultSuccess(apiResult)) {
                alert('회원 탈퇴에 실패했습니다.\n(' + AJAX.getResultMsg(apiResult) + ')');
                return;
            }

            $.removeCookie('userJwt');
            alert('회원 탈퇴에 성공했습니다.\n메인페이지로 돌아갑니다.');
            location.replace('/main');
        },
        // Fail
        function() {
            alert('서버와 통신에 실패했습니다.');
        }
    );
}

// userJwt 발급후 수행
function afterIssuing(userJwt, keepLoggedIn) {
    var afterIssueParam = $('#input_afterIssueParam').val();

    try {
        if (afterIssueParam == 'popup') {
            if (!top.opener) {
                alert('로그인 토큰을 전달받을 부모창을 찾을 수 없습니다.');
                return;
            }

            top.opener.d4r_login_return(userJwt, keepLoggedIn);
            self.close();
        }
        else if (afterIssueParam == 'iframe') {
            if (!parent) {
                alert('로그인 토큰을 전달받을 부모창을 찾을 수 없습니다.');
                return;
            }

            parent.d4r_login_return(userJwt, keepLoggedIn);
        }
        else { // redirection
            var redirectionUrl = null;

            if (!afterIssueParam) {
                redirectionUrl = '/main?userJwt=' + userJwt;
            }
            if (afterIssueParam.indexOf('?') == -1) { // no extra param
                redirectionUrl = afterIssueParam + '?userJwt=' + userJwt + '&keepLoggedIn=' + keepLoggedIn;
            }
            else { // redirect url contains it's own extra param
                redirectionUrl = afterIssueParam + '&userJwt=' + userJwt + '&keepLoggedIn=' + keepLoggedIn;
            }

            location.replace(redirectionUrl);
        }
    }
    catch (e) {
        alert('토큰 전달중 예외가 발생했습니다.\n' + e);
    }
}