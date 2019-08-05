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
                //updateLoginFieldUI();
                alert('userJwt : ' + AJAX.getResultData(apiResult, 'userJwt'));
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