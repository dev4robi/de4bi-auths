$(document).ready(function(){
    // 닉네임 중복검사 버튼
    $('#button_nickname_duplicated_check').on('click', function() {
        var userNickname = $('#input_nickname').val();
        
        if (!checkNickname(userNickname)) {
            return;
        }

        AJAX.apiCall('GET', ('/users/api/duplicated/' + userNickname), null, null,
            // Always    
            function() {
                // ...
            },
            // Success
            function(apiResult) {
                if (AJAX.checkResultSuccess(apiResult)) {
                    updateLeggalNicknameUI();
                }
                else {
                    updateIllegalNicknameUI();
                }
            },
            // Fail
            function() {
                alert('서버와 통신에 실패했습니다.');
            }
        );
    });

    // 회원가입 버튼
    $('#button_submit_register').on('click', function() {
        var postData = null;

        if ((postData = checkAndupdateFormDataUI()) == null) {
            return;
        }
        
        AJAX.apiCall('POST', '/users', null, postData, 
            // Always
            function() {
                // ...
            },
            // Success
            function(apiResult) {
                if (AJAX.checkResultSuccess(apiResult)) {
                    alert('회원 가입에 성공했습니다.\n로그인 페이지로 이동합니다.');
                    location.replace('/main');
                    return;
                }
                else {
                    alert('회원 가입에 실패했습니다.\n(사유: ' + apiResult.result_message + ')');
                    return;
                }
            },
            // Fail
            function() {
                alert('서버와 통신에 실패했습니다.');
            }
        );
    });
});

// 사용 불가능한 닉네임 UI업데이트
function updateIllegalNicknameUI() {
    alert('사용 불가능한 닉네임입니다.');
}

// 사용가능한 닉네임 UI업데이트
function updateLeggalNicknameUI() {
    alert('사용 가능한 닉네임입니다.');
}

// 회원가입 폼 전체 UI업데이트
function checkAndupdateFormDataUI() {
    var email = $('#input_email').val();
    var password = $('#input_password').val();
    var passwordCheck = $('#input_password_check').val();
    var nickname = $('#input_nickname').val();
    var fullName = $('#input_full_name').val();
    var gender = null;

    if ($('#input_radio_gender_male').is(':checked')) {
        gender = $('#input_radio_gender_male').val();
    }
    else if ($('#input_radio_gender_female').is(':checked')) {
        gender = $('#input_radio_gender_female').val();
    }

    var dateOfBirth = $('#input_date_of_birth').val();
    var termsOfService = $('#input_terms_of_service').is(':checked');

    if (!email) {
        alert('이메일값이 올바르지 않습니다.');
        location.replace('/main');
        return null;
    }

    if (!checkPassword(password, passwordCheck)) {
        return null;
    }

    if (!checkNickname(nickname)) {
        return null;
    }

    if (!checkFullName(fullName)) {
        return null;
    }

    if (!checkGender(gender)) {
        return null;
    }

    if ((dateOfBirth = convertDateOfBirth(dateOfBirth)) == null) {
        return null;
    }

    if (!termsOfService) {
        alert('회원가입 약관 동의가 필요합니다.');
        return null;
    }

    var sign = $('#input_sign').val();
    var nonce = $('#input_nonce').val();

    if (!sign || !nonce) {
        alert('페이지 오류가 발생했습니다. 다시 진행해주세요.');
        location.replace('/main');
        return null;
    }

    var postData = {
        'email' : email,
        'password' : SHA256(password + $('#clientSalt').val()),
        'passwordCheck' : passwordCheck,
        'nickname' : nickname,
        'fullName' : fullName,
        'gender' : gender,
        'dateOfBirth' : dateOfBirth,
        'sign' : sign,
        'nonce' : nonce,
    };

    return postData;
}