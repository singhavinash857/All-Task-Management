<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>TransformEdge</title>
<spring:url value="/customCss/headerPageCss.css" var="headerPageCss" />
<link href="${headerPageCss}" rel="stylesheet" />

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>

<link rel="SHORTCUT ICON" href="favicon.ico">

<!-- <!--Bootsrap 4 CDN-->
<link rel="stylesheet"
	href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"
	integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO"
	crossorigin="anonymous">

<!--  Fontawesome CDN -->
<link rel="stylesheet"
	href="https://use.fontawesome.com/releases/v5.3.1/css/all.css"
	integrity="sha384-mzrmE5qonljUremFsqc01SB46JvROS7bZs3IO2EmfFsd15uHvIt+Y8vEf7N7fWAU"
	crossorigin="anonymous">

<style>
.dropbtn {
	background-color: black;
	color: #1e90ff;
	padding: 6px;
	font-size: 16px;
	border: none;
}

.dropdown {
	position: relative;
	display: inline-block;
}

.dropdown-content {
	display: none;
	position: absolute;
	background-color: #f1f1f1;
	min-width: 170px;
	box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.2);
	z-index: 1;
}

.dropdown-content a {
	color: black;
	padding: 5px 7px;
	text-decoration: none;
	display: block;
}

.dropdown-content a:hover {
	background-color: #ddd;
}

.dropdown:hover .dropdown-content {
	display: block;
}

.dropdown:hover .dropbtn {
	background-color: black;
}

#totalUserLoggedin{
	color: #FFC312;
	font-size: 18px;
	font-family: Cambria;
}
</style>
<style>
a:hover {
	color: #ffc312;
}
</style> 

<script type="text/javascript">
	$(document).ready(function() {
		getLoggedInUserData();
	});

	function getLoggedInUserData() {
		$
				.ajax({
					crossDomain : true,
					url : "/home/employee_controller/loggedUsers",
					type : 'GET',
					dataType : 'json',
					success : function(data) {
						console.log(data);
						var numbers = data;
						
						document
						.getElementById("totalUserLoggedin").innerHTML = numbers.length;

						var option = '';
						for (var i = 0; i < numbers.length; i++) {
							option += '<a>'
									+ numbers[i] + '</a>';
						}
						$('#loggedinUserDropdown').append(option);
					},
					error : function(e) {
						console.log('errorThrown', e);
					}
				});
	}
</script>
</head>
<body>
	<nav
		class="navbar navbar-expand-md bg-dark navbar-dark navbar-expand-md fixed-top top-nav"
		id="navbarCss">

		<button class="navbar-toggler" type="button" data-toggle="collapse"
			data-target="#collapsibleNavbar">
			<span class="navbar-toggler-icon"></span>
		</button>
		<div class="collapse navbar-collapse" id="collapsibleNavbar">
			<a class="navbar-brand" href="/welcome_page" id="homeBtnCss"><i
				class="navbar-brand fas fa-home" style="color: #FFC312;"></i></a>

			<ul class="navbar-nav" id="dropdownsOprionCss">
				<li class="dropdown"><a class="dropdown-toggle"
					data-toggle="dropdown" href="#">Project Management <span
						class="caret"></span></a>
					<ul class="dropdown-content">
						<li><a href="/home/redirect_controller/taskPage/Task">Assign
								New Project</a></li>
						<li><a
							href="/home/redirect_controller/taskPage/MainTaskTablePage">Project
								Details</a></li>
					</ul> &nbsp;</li>
				<security:authorize access="hasAuthority('ADMIN')">
					<li class="dropdown">&nbsp; <a class="dropdown-toggle"
						data-toggle="dropdown" href="#">Employee Management <span
							class="caret"></span></a>
						<ul class="dropdown-content">
							<li><a
								href="/home/redirect_controller/registerPage/MainRegisterPage">Register
									New Employee</a></li>
							<li><a href="/home/redirect_controller/projectPage/ProjectReportPage">View Employee Status</a></li>
						</ul></li>

				</security:authorize>
				
				<li class="dropdown">&nbsp; <a class="dropdown-toggle"
						 href="#">Total <label id="totalUserLoggedin"></label> Logged-in</a>
						<div class="dropdown-content" id="loggedinUserDropdown">
						</div>
					</li>
			</ul>

		</div>
		<h5 id="welcomeCss">
			Welcome : <b id="welcomeMsgCss"> <security:authentication
					property="principal.username" />
			</b>
		</h5>
		<div style="padding-right: 10px; padding-left: 10px;"></div>
		<ul class="nav navbar-nav navbar-right">
			<li><form:form method="post"
					action="${pageContext.request.contextPath}/logout" id="nameform">

					<button type="submit" id="loginBtnCss"
						class="btn float-left login_btn">
						<i class="fas fa-sign-out-alt"></i>
					</button>
					<!-- <input type="submit" value="Logout"
						class="btn float-left login_btn" id="loginBtnCss" /> -->
				</form:form></li>


		</ul>

	</nav>

</body>
</html>