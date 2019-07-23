$(document).ready(function(){
    // 닉네임 중복검사 버튼
    $('#button_nickname_duplicated_check').on('click', function() {
        var userNickname = $('#input_nickname').val();
        AJAX.apiCall('GET', '/users/nickname/' + userNickname + '/duplicated', null, null,
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
        if (checkAndupdateFormDataUI()) {
            var email = null;
            var password = null;
            var nickname = $('#input_nickname').val();
            var fullName = null;
            var gender = null;
            var dateOfBirth = null;
            var sign = null;
            var postData = { 
                'email' : email,
                'password' : password,
                'nickname' : nickname,
                'fullName' : fullName,
                'gender' : gender,
                'dateOfBirth' : dateOfBirth,
                'sign' : sign,
                'nonce' : nonce
            };

            AJAX.apiCall('POST', '/users', null, postData, 
                // Always
                function() {
                    // ...
                },
                // Success
                function(apiResult) {
                    if (AJAX.checkResultSuccess(apiResult)) {
                        
                    }
                    else {
                        
                    }
                },
                // Fail
                function() {
                    alert('서버와 통신에 실패했습니다.');
                }
            );
        }
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
    var gender = $('#input_radio_gender_male').val();
    var dateOfBirth = $('#input_date_of_birth').val();
    var termsOfService = $('#input_terms_of_service').is(':checked')(); // 여기부터 시작@@ 체크박스 체크시 가져와야 함.

    alert(  'email:' + email +
            'password:' + password +
            'passwordCheck:' + passwordCheck +
            'nickname:' + nickname +
            'fullName:' + fullName +
            'gender:' + gender +
            'dateOfBirth:' + dateOfBirth +
            'termsOfService:' + termsOfService);
}