$(document).ready(function(){
    $('#btnGoogleLogin').on('click', function(){
        location.href = $('#googleLoginUrl').val();
    });

    var alert_msg = $('#alert_msg').val();
    alert(alert_msg);
    
});