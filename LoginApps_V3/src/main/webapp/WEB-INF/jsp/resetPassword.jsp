<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, shrink-to-fit=no">
<link
	href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.0/css/bootstrap.min.css"
	rel="stylesheet" id="bootstrap-css">
<script
	src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.0/js/bootstrap.min.js"></script>

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">

<script type="text/javascript">
	function check() {
		if (document.getElementById("password").value == document
				.getElementById("confirm_password").value) {
			document.getElementById('message').style.color = 'green';
			document.getElementById('message').innerHTML = 'Password Matches';
			document.getElementById("submit").disabled = false;

		} else {
			document.getElementById('message').style.color = 'red';
			document.getElementById('message').innerHTML = 'Password does not match';
			document.getElementById("submit").disabled = true;

		} 
		if(document.getElementById("password").value.length <= 3) {
			document.getElementById('message').style.color = 'red';
			document.getElementById('message').innerHTML = 'Password should be above 3 letters';
			document.getElementById("submit").disabled = true;
		}
	};
	</script>
</head>
<body>
	<div class="form-gap"></div>
	<div class="container">
		<br>
		<div align="center">
			<b style="color: green; font-size: 15px;">${msg}</b>
		</div>
		<div align="center">
			<b style="color: red; font-size: 15px;">${errorMessage}</b>
		</div>
		<br>
		<div class="row">
			<div class="col-md-4 col-md-offset-4">
				<div class="panel panel-default">
					<div class="panel-body">
						<div class="text-center">
							<h3>
								<i class="fa fa-lock fa-4x"></i>
							</h3>
							<h2 class="text-center">Reset Password</h2>
							<div class="panel-body">

								<form:form action="/reset_password" method="POST">
									<input type="hidden" value="${resetToken}" name="token">

									<div class="form-group">
										<div class="input-group">
											<span class="input-group-addon"><i
												class="glyphicon glyphicon-lock"></i></span> <input type="password"
												class="form-control" placeholder="New Password"
												onchange="check();" name="password" id="password">
										</div>
									</div>

									<div class="form-group">
										<div class="input-group">
											<span class="input-group-addon"><i
												class="glyphicon glyphicon-lock"></i></span><input type="password"
												class="form-control" placeholder="Confirm Password"
												onchange="check();" name="confirmedpassword"
												id="confirm_password">
										</div>
										<span id="message"></span>

									</div>

									<div class="form-group">
										<input name="recover-submit"
											class="btn btn-lg btn-primary btn-block" value="submit"
											type="submit" id="submit">
									</div>
								</form:form>

							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>