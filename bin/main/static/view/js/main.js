$(document).ready(function(){
    $('#btnGoogleLogin').on('click', function(){
        alert($('#googleLoginUrl').val());
        location.href = $('#googleLoginUrl').val();
    });
});