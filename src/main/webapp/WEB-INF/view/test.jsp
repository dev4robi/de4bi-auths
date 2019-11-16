<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
	<!-- Meta -->
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
	<!-- Javascript and css library -->
	<!-- jquery 3.4.0 -->
	<script type="text/javascript" src="/common/lib/jquery-3.4.0/jquery-3.4.0.min.js"></script>
	<script type="text/javascript" src="/common/lib/jquery-3.4.0/jquery-cookie-1.4.1.js"></script>
	<!-- bootstrap 4.3.1 -->
	<link rel="stylesheet" href="/common/lib/bootstrap-4.3.1/css/bootstrap.min.css">
	<script type="text/javascript" src="/common/lib/bootstrap-4.3.1/js/bootstrap.min.js"></script>
	<!-- popper 1.14.7 -->
	<script type="text/javascript" src="/common/lib/popper-1.14.7/popper-1.14.7.js"></script>
	<script type="text/javascript" src="/common/lib/popper-1.14.7/tooltip-1.3.2.js"></script>
	<!-- fontawesome 5.8.1 -->
	<link rel="stylesheet" href="/common/lib/fontawesome-5.8.1/css/fontawesome-5.8.1.css">
	<script type="text/javascript" src="/common/lib/fontawesome-5.8.1/js/fontawesome-5.8.1.js"></script>
	<!-- common.js -->
	<script type="text/javascript" src="/common/js/common.js?ver=<%=System.currentTimeMillis()%>"></script>
	<!-- register.js -->
	<script type="text/javascript" src="/view/js/main.js?ver=<%=System.currentTimeMillis()%>"></script>
    <script type="text/javascript">
        function d4r_login_return(userJwt) {
            $('#userJwt').html(userJwt);
        }
    </script>
</head>
<body>
    <div>
        <button id="popup">팝업</button>
    </div>
    <div>
        <iframe src="http://localhost:40000/main?afterIssueParam=iframe" width="50%"></iframe>
    </div>
    <div>
        <p>토큰 : <span id="userJwt">?</span>
    </div>
</body>
<script>
    $('#popup').on('click', function(){
        window.open("http://localhost:40000/main?afterIssueParam=popup", "팝업로그인", "menubar=no", true);
    });
</script>
</html>