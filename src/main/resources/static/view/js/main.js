$(document).ready(function(){
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
    $('#input_userJwt').val(); //@@ 로그인후 토큰을 어떻게 메인페이지로 전달할지 고민... + 로그인후 유저정보및 탈퇴수정 페이지 구현
});

function login() {
    var audience = $('#input_audience').val();
    var email = $('#input_email').val();
    var password = $('#input_password').val();
    var duration = $('input_duration').val();

    if (!audience) {
        audience = 'auth-server';
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
                redirectionUrl = afterIssueParam + '?userJwt=' + userJwt + 'keepLoggedIn=' + keepLoggedIn;
            }
            else { // redirect url contains it's own extra param
                redirectionUrl = afterIssueParam + '&userJwt=' + userJwt + 'keepLoggedIn=' + keepLoggedIn;
            }

            location.replace(redirectionUrl);
        }
    }
    catch (e) {
        alert('토큰 전달중 예외가 발생했습니다.\n' + e);
    }
}