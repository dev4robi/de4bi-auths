$(document).ready(function(){
    // 닉네임 중복검사 버튼
    $('#button_nickname_duplicated_check').on('click', function() {
        var userNickname = $('#input_nickname').val();
        
        if (!checkNickname(userNickname)) {
            return;
        }

        var checkDuplicationApiURL = '/users/api/duplicated/' + userNickname;

        AJAX.apiCall('GET', checkDuplicationApiURL, null, null,
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

    if (!password) {
        alert('비밀번호를 입력해주세요.');
        return null;
    }
    else if (password.length < 8 || password.length > 32) {
        alert('비밀번호는 8~32자 사이여야 합니다.\n(현재: ' + password.length + '자)');
        return null;
    }
    else if (password != passwordCheck) {
        alert('비밀번호 확인이 일치하지 않습니다.');
        return null;
    }

    if (!checkNickname(nickname)) {
        return null;
    }

    if (!fullName) {
        alert('이름을 입력해주세요.');
        return null;
    }
    else if (fullName.length < 1 || fullName.length > 64) {
        alert('이름은 1~64자 사이여야 합니다.\n(현재: ' + fullName.length + '자)');
        return null;
    }

    if (!gender) {
        alert('성별을 선택해주세요.');
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
        'password' : password,
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

// 닉네임 조건 검사
function checkNickname(nickname) {
    if (!nickname) {
        alert('닉네임을 입력해주세요.');
        return false;
    }
    else if (nickname.length < 2 || nickname.length > 16) {
        alert('닉네임은 2~16자 사이여야 합니다.\n(현재: ' + nickname.length + '자)');
        return false;
    }

    return true;
}

// 생년월일 문자열을 시간(ms)으로 변환
function convertDateOfBirth(dateOfBirth) {
    if (!dateOfBirth) {
        alert('생년월일을 입력해주세요.');
        return null;
    }

    if (dateOfBirth.length < 8) {
        alert('올바른 생년월일값을 입력해주세요.\n(현재: ' + dateOfBirth + ' / 양식: yyyy.MM.dd)');
        return null;
    }

    // Format : yyyy.MM.dd
    var monthIdx = dateOfBirth.indexOf('.');
    var dateIdx = dateOfBirth.lastIndexOf('.');
    var year = null;
    var month = null;
    var date = null;

    try {
        year = parseInt(dateOfBirth.substring(0, monthIdx));
        month = parseInt(dateOfBirth.substring(monthIdx + 1, dateIdx));
        date = parseInt(dateOfBirth.substring(dateIdx + 1, dateOfBirth.length));
    }
    catch {
        alert('올바른 생년월일값을 입력해주세요.\n(현재: ' + dateOfBirth + ' / 양식: yyyy.MM.dd)');
        return null;
    }

    if (year < 0 || year > 9999) {
        alert('올바른 연도를 입력해주세요.\n(현재: ' + year + ")")
        return null;
    }

    if (month < 0 || month > 12) {
        alert('올바른 월을 입력해주세요.\n(현재: ' + year + ")")
        return null;
    }

    if (date < 0 || date > 31) {
        alert('올바른 일을 입력해주세요.\n(현재: ' + date + ")")
        return null;
    }

    var date = null;
    
    if ((date = new Date(dateOfBirth)) == null) {
        alert('생년월일 변환중 오류가 발생했습니다. (입력값: ' + dateOfBirth + ')');
        return null;
    }

    return date.getTime();
}