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
    var duration = 0;

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

    var keepLoggedIn = $('#cb_keep_logged_in').is(':checked');

    if (keepLoggedIn) {
        duration = 15 * 1440; // 1day : 1440min
    }
    else {
        duration = 60; // 1hour : 60min
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
                afterIssuing(userJwt, keepLoggedIn);
                return;
            }
            else {
                alert('로그인에 실패했습니다.\n(' + apiResult.result_msg + ')');
                return;
            }
        },
        // Fail
        function(textStatus) {
            alert('서버와 통신에 실패했습니다.\n(Code: ' + textStatus + ')');
            return;
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
    var originPassword      = $('#input_info_origin_password').val();
    var newPassword         = $('#input_info_password').val();
    var newPasswordCheck    = $('#input_info_password_check').val();
    var fullName            = $('#input_info_full_name').val();
    var gender;

    if (!checkPassword(originPassword, null)) {
        return;
    }

    if ((!!newPassword || !!newPasswordCheck)) {
        if (!checkPassword(newPassword, newPasswordCheck)) {
            return;
        }
    }

    if (!newPassword) {
        newPassword = null;
    }

    if ($('#input_info_radio_gender_male').is(':checked')) {
        gender = $('#input_info_radio_gender_male').val();
    }
    else if ($('#input_info_radio_gender_female').is(':checked')) {
        gender = $('#input_info_radio_gender_female').val();
    }

    if (!checkGender(gender)) {
        return;
    }

    if (!checkFullName(fullName)) {
        return;
    }

    var dateOfBirth = convertDateOfBirth($('#input_info_date_of_birth').val());
    
    if (!dateOfBirth) {
        return;
    }

    if (!confirm('정말로 회원 정보를 수정하시겠습니까?')) {
        alert('회원정보 수정을 취소했습니다.');
        return;
    }

    var reqBody = {
        'password': SHA256(originPassword + $('#input_clientSalt').val()),
        'newPassword': (!!newPassword ? SHA256(newPassword + $('#input_clientSalt').val()) : null),
        'fullName' : fullName,
        'gender' : gender,
        'dateOfBirth' : dateOfBirth
    };

    AJAX.apiCall('PUT', '/users', {'userJwt':$.cookie('userJwt')}, reqBody,
        // Always
        function() {
            // alert(JSON.stringify(reqBody));
        },
        // Success
        function(apiResult) {
            if (!AJAX.checkResultSuccess(apiResult)) {
                alert(AJAX.getResultMsg(apiResult));
                return;
            }

            alert('회원 정보가 수정되었습니다.');
            location.reload();
            return;
        },
        // Fail
        function(textStatus) {
            alert('서버와 통신에 실패했습니다.\n(Code: ' + textStatus + ')');
            return;
        }
    );
}

// 회원탈퇴
function deregister() {
    var originPassword = $('#input_info_origin_password').val();

    if (!checkPassword(originPassword, null)) {
        return;
    }

    if (!confirm('회원탈퇴후 복원할 수 없습니다.\n정말로 탈퇴하시겠습니까?')) {
        alert('회원 탈퇴를 취소했습니다.');
        return;
    }

    var reqBody = {
        'password' : SHA256(originPassword + $('#input_clientSalt').val())
    };

    AJAX.apiCall('DELETE', '/users', {'userJwt':$.cookie('userJwt')}, reqBody,
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
            return;
        },
        // Fail
        function(textStatus) {
            alert('서버와 통신에 실패했습니다.\n(Code: ' + textStatus + ')');
            return;
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