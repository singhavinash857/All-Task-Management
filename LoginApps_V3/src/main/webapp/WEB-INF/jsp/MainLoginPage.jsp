<!DOCTYPE html>
<html>
<head>
<link
	href="//maxcdn.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css"
	rel="stylesheet" id="bootstrap-css">
<script
	src="//maxcdn.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js"></script>
<script
	src="//cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>
<title>Login Page</title>
<meta name="viewport" content="width=device-width, initial-scale=1">

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

<!-- Custom styles
		<link rel="stylesheet" type="text/css" href="styles.css">-->

<style>
html, body {
	background-image: url("yellowbackground.jpg");
	background-size: cover;
	background-repeat: no-repeat;
	height: 100%;
	font-family: 'Numans', sans-serif;
}

.container {
	height: 100%;
	align-content: center;
}

.card {
	height: 370px;
	margin-top: auto;
	margin-bottom: auto;
	width: 400px;
	background-color: rgba(0, 0, 0, 0.5) !important;
}

.card-header h3 {
	color: white;
}

.input-group-prepend span {
	width: 50px;
	background-color: #FFC312;
	color: black;
	border: 0 !important;
}

input:focus {
	outline: 0 0 0 0 !important;
	box-shadow: 0 0 0 0 !important;
}

.login_btn {
	color: black;
	background-color: #FFC312;
	width: 100px;
}

.login_btn:hover {
	color: black;
	background-color: white;
}

.links {
	color: white;
}

.links a {
	margin-left: 4px;
}
</style>
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>

<script type="text/javascript">
	$(document).ready(function() {
		$("#loginbtn").click(function() {
			login();
		});
		$("#resetbtn")
		.click(
				function(e) {

					resetPassword();
					  //e.preventDefault();
				});
					

		function login() {
			var formData = {
				'userName' : $('input[name=userName]').val(),
				'password' : $('input[name=password]').val()
			};
			console.log(formData);

			$.ajax({
				type : "POST",
				contentType : "application/json",
				url : "/getlogin",
				data : JSON.stringify(formData),
				dataType : 'json',
				success : function(data) {
					console.log(data);

					if (false) {
						//redirect to home page
						window.location = "http://www.google.com";
						//document.location.href = window.location.href;
					} else {
						//redirect to same page
						window.location = "http://www.google.com";

					}
				},
				error : function(e) {
					alert("ERROR!!")

					console.log('errorThrown', e);
				}
			});
		}
		
		function resetPassword() {

			 var formData = {
					 'email' : $(
						'input[name=username]').val()
			 }; 
			 document.getElementById("resetbtn").disabled=true;
            //$("input[type=submit]").prop("disabled", "disabled");

			console.log(formData);

		$
					.ajax({
						crossDomain : true,
						url : 'http://192.168.1.8:8089/home/password_reset_controller/password_reset_request',
						method : 'POST',
						dataType : 'json',
						contentType : "application/json; charset=utf-8",
						async : false,
						data : JSON.stringify(formData),
						success : function(data) {
							console.log(data);
							//var message1 = "Reset Link Sent To Your Mail Id, Please Check!"; 
							document.getElementById("message").value = data.message;
							//alert(document.getElementById("message").value);
							document.getElementById("message").style.color = "red";
							document.location.href = window.location.href
							
							},
						error : function(e) {
							alert("error");
						}
					});
		}
	});
</script>
</head>

<body>
	<div class="container">
		<div class="d-flex justify-content-center h-100">
			<div class="card">
				<div class="card-header">
					<h3>Sign In</h3>
				</div>
				<div class="card-body">
					<form>
						<div class="input-group form-group">
							<div class="input-group-prepend">
								<span class="input-group-text"><i class="fas fa-user"></i></span>
							</div>
							<input type="text" class="form-control" placeholder="username"
								name="userName" required>

						</div>
						<div class="input-group form-group">
							<div class="input-group-prepend">
								<span class="input-group-text"><i class="fas fa-key"></i></span>
							</div>
							<input type="password" class="form-control"
								placeholder="password" name="password" required>
						</div>
						<div class="form-group">
						 <input type="submit" value="Login" id="loginbtn"
								class="btn float-right login_btn">
								
						</div>
					</form>
				</div>
				<div class="card-footer">
					<div class="d-flex justify-content-center">
						<button type="button"  data-toggle="modal"
							data-target="#myModal" style="background-color: rgba(0,0,0,0.4) !important;border-color:rgba(0,0,0,0.4) !important;color:rgba(30,144,255);">Forgot Password?</button>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div>
		<div class="panel panel-default modal" id="myModal">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="container">

						<button type="button" class="close" data-dismiss="modal">&times;</button>
						<div class="text-center">
							<h3>
								<i class="fa fa-lock fa-4x"></i>
							</h3>
							<h2 class="text-center">Forgot Password?</h2>
							<p>You can reset your password here.</p>

							<div class="panel-body">

								<form role="form" autocomplete="off" class="form">

									<div class="form-group">
										<div class="input-group">
											<span class="input-group-addon"><i
												class="glyphicon glyphicon-envelope color-blue"></i></span> <input
												id="username" name="username" placeholder="user name"
												 class="form-control" type="email">
										</div>
										<div align="center" class="okStatusText">
										<label id="message"></label>
											<!-- here you have to show you message-->
										</div>
									</div>
									<div class="form-group">
										<input name="recover-submit"
											class="btn btn-lg btn-primary btn-block"
											value="Reset Password" type="submit" id="resetbtn">
									</div>

									<input type="hidden" class="hide" name="token" id="token"
										value="">
								</form>

							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>