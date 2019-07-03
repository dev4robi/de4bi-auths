$(document).ready(function(){
    $('#btnGoogleLogin').on('click', function(){
        location.href = $('#googleLoginUrl').val();
    });
});