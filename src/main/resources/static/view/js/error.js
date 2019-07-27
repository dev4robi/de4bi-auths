$(document).ready(function(){
    // 메인으로 돌아가기 버튼
    $('#btnGotoMain').on('click', function(){
        location.replace('/main');
    });

    // 오류메시지 얼럿
    var alertMsg = $('#alertMsg').val();
    if (!!alertMsg && alertMsg.length > 0) {
        alert(alertMsg);
    }
});