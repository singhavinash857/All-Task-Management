<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Log in with your account</title>

<link href="${contextPath}/resources/css/bootstrap.min.css"
	rel="stylesheet">

</head>

<body>

	<div class="container">
		<form method="POST" action="${contextPath}/login" class="form-signin">
			 <table>
            <tr>
               <td>User:</td>
               <td><input type='text' name='username' value=''></td>
            </tr>
            <tr>
               <td>Password:</td>
               <td><input type='password' name='password' /></td>
            </tr>
            <tr>
               <td>Remember Me?</td>
               <td><input type="checkbox" name="remember-me" /></td>
            </tr>            
            <tr>
               <td><input name="submit" type="submit" value="submit" /></td>
            </tr>
         </table>
		</form>
	</div>

	<script
		src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
	<script src="${contextPath}/resources/js/bootstrap.min.js"></script>
</body>
</html>