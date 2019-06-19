<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<script
	src="//cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>

<%-- <spring:url
	value="${pageContext.request.contextPath}/customCss/registrationPageCss.css"
	var="registerPageCss" />
<link href="${registerPageCss}" rel="stylesheet" /> --%>

<style>
body {
	background-image:
		url("${pageContext.request.contextPath}/images/yellowbackground.jpg");
	height: 100%;
}


/* input[type]{
  border-radius: 4px;
  background: transparent;
}  */

.custom-modal-header {
	text-align: center;
	color: #17b6bb;
	border-top: 4px solid;
}


#myModal .modal-dialog:after {
	content: '';
	height: 0px;
	width: 0px;
	/* border-right: 50px solid rgba(255, 0, 0, 0.98); */
	border-right: 50px solid #17b6bb;
	border-bottom: 50px solid transparent;
	position: absolute;
	top: 1px;
	right: 16px;
	z-index: 99;
}

#myModal .modal-dialog:before {
	content: '';
	height: 0px;
	width: 0px;
	border-left: 50px solid #17B6BB;
	border-right: 50px solid transparent;
	border-bottom: 50px solid transparent;
	position: absolute;
	top: 1px;
	left: -14px;
	z-index: 99;
}

#myModal .modal-dialog {
	padding: 0px;
	position: relative;
}

</style>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script>
	function deleteRole(empId) {
		document.getElementById("deleteRoleModal").style.display = "block";
		document.getElementById("deleteRoleHiddenEmpId").value = empId;

		/* 		getting employee roles
		 */$('#employeeRolesToDelete').empty();
		$
				.ajax({
					url : "/home/employee_controller/get_employee_roles_id/"
							+ empId,
					crossDomain : true,
					type : 'GET',
					dataType : 'json',
					success : function(data) {
						console.log(data);

						var numbers = data;
						var option = '';
						for (var i = 0; i < numbers.length; i++) {
							option += '<option value="'+ numbers[i].name + '">'
									+ numbers[i].name + '</option>';
						}
						$('#employeeRolesToDelete').append(option);

					},
					error : function(e) {
						var errors = e.responseJSON.errors;
						var errorString = '';

						$.each(errors, function(key, value) {
							errorString += value.errorDescription;
						});
						document.getElementById("deleteRoleModal").style.display = "none";
						alert(errorString);
					}
				});

		$("#deleteRoleModalBtnId").click(function() {
			document.getElementById("deleteRoleModal").style.display = "none";
		});

	}
</script>
<script type="text/javascript">
	function uploadPhoto(empId) {
		document.getElementById("myModal").style.display = "block";
		document.getElementById("uploadPhotoHiddenEmpId").value = empId;
	}
</script>
<script type="text/javascript">
	function update(employeeId) {
		$
				.ajax({
					url : "/home/employee_controller/get_employee_by_id/"
							+ employeeId,
					crossDomain : true,
					type : 'GET',
					dataType : 'json',
					success : function(data) {
						console.log(data);
						document.getElementById("employeeFirstName").value = data.employeeFirstName;
						document.getElementById("employeeLastName").value = data.employeeLastName;
						$(function() {
							var defaultValue = $("#employeeGender").val();
							$("#employeeGender").val(defaultValue);
							document.getElementById("employeeGender").value = data.employeeGender;

						});
						document.getElementById("employeePhone").value = data.employeePhone;
						document.getElementById("employeeMailId").value = data.employeeMailId;
						document.getElementById("employeeCode").value = data.employeeCode;
						document.getElementById("employeeStatuse").value = data.employeeStatus;
						//document.getElementById("employeeDesignation").value = data.employeeDesignation;

						$(function() {
							var defaultValue = $("#employeeDesignation").val();
							$("#employeeDesignation").val(defaultValue);
							document.getElementById("employeeDesignation").value = data.employeeDesignation;

						});
						document.getElementById("employeeRoles").value = data.employeeRoles;
						document.getElementById("employeeId").value = data.employeeId;
						document.getElementById("txtPassword").value = "";
						document.getElementById("txtConfirmPassword").value = "";
					}
				});
	}
</script>
<script type="text/javascript">
	$(document)
			.ready(
					function() {
						
						var currentPageNumber = 1;
						var pageSize = 2;
						loadData(currentPageNumber,pageSize);
						$("#btnSubmit")
								.click(
										function(e) {
											var valid = this.form
													.checkValidity();
											if (valid) {
												e.preventDefault();
												var form = $(this);
												var hiddenEmployeeId = document
														.getElementById("employeeId").value;
												if (hiddenEmployeeId == "") {
													console
															.log("save employee calling ::");
													saveEmployeeData();
												} else {
													console
															.log("update employee calling ::");
													updateEmployeeData(hiddenEmployeeId);
												}
											}
											console.log("valid" + valid);
										});

						

						$("#uploadPhotoPopupCloseBtnId")
								.click(
										function() {
											document.getElementById("myModal").style.display = "none";
										});

						$("#uploadEmpPhotoId")
								.click(
										function fileSubmit() {
											event.preventDefault();
											var data = new FormData();
											var file = document
													.getElementById('uploadfile').files[0];
											data
													.append(
															"employeeId",
															document
																	.getElementById("uploadPhotoHiddenEmpId").value);
											data.append("file", file);
											$
													.ajax({
														url : "/home/file_controller/upload_photo_by_id",
														crossDomain : true,
														type : "POST",
														enctype : 'multipart/form-data',
														data : data,
														processData : false,
														contentType : false,
														cache : false,
														timeout : 600000,
														success : function(data) {
															console.log(data);
															loadData(currentPageNumber,pageSize);
															document
																	.getElementById("myModal").style.display = "none";
															alert(data.message);
														},
														error : function(e) {
															var errors = e.responseJSON.errors;
															var errorString = '';

															$
																	.each(
																			errors,
																			function(
																					key,
																					value) {
																				errorString += value.errorDescription;
																			});
															alert(errorString);
														}
													});
										});

						/* deleting the employee role*/
						$("#deleteRoleBtnId")
								.click(
										function(e) {
											e.preventDefault();
											var formData = {
												"roleName" : $(
														'#employeeRolesToDelete option:selected')
														.val(),
												"employeeId" : document
														.getElementById("deleteRoleHiddenEmpId").value
											};
											$
													.ajax({
														url : "/home/employee_controller/delete_role_employee_id",
														crossDomain : true,
														method : 'DELETE',
														dataType : 'json',
														contentType : "application/json; charset=utf-8",
														async : false,
														data : JSON
																.stringify(formData),
														success : function(data) {
															console.log(data);
															loadData(currentPageNumber,pageSize);
															document
																	.getElementById("deleteRoleModal").style.display = "none";
															alert(data.message);
														},
														error : function(e) {
															var errors = e.responseJSON.errors;
															var errorString = '';

															$
																	.each(
																			errors,
																			function(
																					key,
																					value) {
																				errorString += value.errorDescription;
																			});
															document
																	.getElementById("deleteRoleModal").style.display = "none";
															alert(errorString);
														}
													});
										});
						
						$("#next_button_id").click(function() {
							currentPageNumber = currentPageNumber + 1;
							loadData(currentPageNumber,pageSize);
						});

						$("#previous_button_id").click(function() {
							if(currentPageNumber > 0){
								currentPageNumber = currentPageNumber - 1;
								loadData(currentPageNumber,pageSize);
							}
						});

						/* 						loading data to the table
						 */function loadData(pageNum,pageSize) {
							$
									.ajax({
										url : "/home/employee_controller/get_all_employee?pageNum="
												+ pageNum+"&pageSize="+pageSize,
										crossDomain : true,
										type : 'GET',
										dataType : 'json',
										success : function(data) {
											console.log(data);
											var emptable = $('#list tbody');
											$('#list tbody').empty();
											if(data.values != "" || data.values != null){
												if(data.hashMore == false){
													document.getElementById("next_button_id").disabled = true;
												}else{
													document
													.getElementById("next_button_id").disabled = false;
												}
												document.getElementById("list").style.display = "block";
												document
												.getElementById("pageNumberId").innerHTML = (data.pageNum + 1) +" Of "+ data.toalPages;

												$(data.values)
												.each(
														function(index, emp) {
															var fruits = [];
															var empRoles = emp.employeeRoles;
															empRoles
																	.forEach(function(
																			obj) {
																		fruits
																				.push(obj.name);
																	});
															emptable
																	.append("<tr>"
																			+ "<td>"
																			+ "<button id='btn' type='button' value='Update' class='btn btn-primary' onclick='update("
																			+ emp.employeeId
																			+ ");'  style='background-image: linear-gradient(to right, #373b44, #4286f4);padding: 15px;flex: 1 1 auto; box-shadow: 0 0 5px #eee;border-radius: 10px;'><i class='fas fa-edit'></i></button>"
																			+ "</td>"
																			+ "<td>"
																			+ "<button id='btn' type='button'  onclick='uploadPhoto("
																			+ emp.employeeId
																			+ ");' class='btn' style='background-image: linear-gradient(to right, #373b44, #4286f4);padding: 15px;flex: 1 1 auto; box-shadow: 0 0 5px #eee;border-radius: 10px;'><i class='fas fa-upload' style='color:white'></i></button>"
																			+ "</td>"
																			+ "<td>"
																			+ "<button id='btn' type='button' value='Delete Role' class='btn btn-primary' onclick='deleteRole("
																			+ emp.employeeId
																			+ ");'  style='background-image: linear-gradient(to right, #373b44, #4286f4);padding: 15px;flex: 1 1 auto; box-shadow: 0 0 5px #eee;border-radius: 10px;'><i class='fas fa-trash'></i></button>"
																			+ "</td>"
																			+ "<td>"
																			+ "<div class='card' style='width: 5rem;height:5rem;margin:0 auto;background-color: rgba(0,0,0,0.4) !important;'>"
																			+ "<img class='card-img-top rounded-circle' src='"
																			+ emp.employeeFileDetailsDb.fileDownloadUri
																			+ "' alt='Card image cap' id='photo' style='border-radius: 50%; height:100%; width:100%;'>"
																			+ "</div>"
																			+ "</td>"
																			+ "<td>"
																			+ emp.employeeFirstName
																			+ "</td>"
																			+ "<td>"
																			+ emp.employeeLastName
																			+ "</td>"
																			+ "<td>"
																			+ emp.employeeGender
																			+ "</td>"
																			+ "<td>"
																			+ emp.employeeMailId
																			+ "</td>"
																			+ "<td>"
																			+ emp.employeeCode
																			+ "</td>"
																			+ "<td>"
																			+ emp.employeePhone
																			+ "</td>"
																			+ "<td>"
																			+ emp.employeeDesignation
																			+ "</td>"
																			+ "<td>"
																			+ emp.employeeStatus
																			+ "</td>"
																			+ "<td>"
																			+ fruits
																			+ "</td>"
																			+ "</tr>")
														});
											}
											
										}
									});
						}
						function saveEmployeeData() {

							var formData = {
								'employeeFirstName' : $(
										'input[name=employeeFirstName]').val(),
								'employeeLastName' : $(
										'input[name=employeeLastName]').val(),
								'employeeGender' : $(
										'#employeeGender option:selected')
										.val(),
								'employeeMailId' : $(
										'input[name=employeeMailId]').val(),
								'employeeCode' : $('input[name=employeeCode]')
										.val(),
								'employeePhone' : $('input[name=employeePhone]')
										.val(),
								'employeeDesignation' : $(
										'#employeeDesignation option:selected')
										.val(),

								'employeeStatus' : $(
										'#employeeStatuse option:selected')
										.val(),

								'employeePassword' : $(
										'input[name=employeePassword]').val(),
								'employeeConfirmPassword' : $(
										'input[name=employeeConfirmPassword]')
										.val(),
								'employeeRole' : [ $(
										"#employeeRoles option:selected").val() ]

							};

							console.log(formData);
							$
									.ajax({
										crossDomain : true,
										url : '/home/employee_controller/saveEmployee',
										method : 'POST',
										dataType : 'json',
										contentType : "application/json; charset=utf-8",
										async : false,
										data : JSON.stringify(formData),
										success : function(data) {
											console.log(data.status);
											if (data.status) {
												alert(data.message);
												loadData(currentPageNumber,pageSize);
											}
										},
										error : function(e) {
											var errors = e.responseJSON.errors;
											var errorString = '';

											$
													.each(
															errors,
															function(key, value) {
																errorString += value.errorDescription;
															});
											alert(errorString);
										}
									});
						}

						function updateEmployeeData(empId) {
							console
									.log("inside updateEmployeeData the update is ::"
											+ empId);
							var formData = {
								'employeeFirstName' : $(
										'input[name=employeeFirstName]').val(),
								'employeeLastName' : $(
										'input[name=employeeLastName]').val(),
								'employeeGender' : $(
										'#employeeGender option:selected')
										.val(),
								'employeeMailId' : $(
										'input[name=employeeMailId]').val(),
								'employeeCode' : $('input[name=employeeCode]')
										.val(),
								'employeePhone' : $('input[name=employeePhone]')
										.val(),
								'employeeDesignation' : $(
										'#employeeDesignation option:selected')
										.val(),

								'employeeStatus' : $(
										'#employeeStatuse option:selected')
										.val(),

								'employeePassword' : $(
										'input[name=employeePassword]').val(),
								'employeeConfirmPassword' : $(
										'input[name=employeeConfirmPassword]')
										.val(),
								'employeeRole' : [ $(
										"#employeeRoles option:selected").val() ]

							};

							console.log(formData);
							$
									.ajax({
										crossDomain : true,
										url : '/home/employee_controller/update_employee/'
												+ empId,
										method : 'PUT',
										dataType : 'json',
										contentType : "application/json; charset=utf-8",
										async : false,
										data : JSON.stringify(formData),
										success : function(data) {
											console.log(data.status);
											if (data.status) {
												alert(data.message);
												loadData(currentPageNumber,pageSize);
											}
										},
										error : function(e) {
											var errors = e.responseJSON.errors;
											var errorString = '';

											$
													.each(
															errors,
															function(key, value) {
																errorString += value.errorDescription;
															});
											alert(errorString);
										}
									});
						}

					});
</script>

<script type="text/javascript">
	function isPasswordMatch() {
		var password = $("#txtPassword").val();
		var confirmPassword = $("#txtConfirmPassword").val();

		if (password != confirmPassword)
			$("#divCheckPasswordMatch").html("Passwords do not match!").css(
					'color', 'red');
		else
			$("#divCheckPasswordMatch").html("Passwords match.").css('color',
					'green');
	}

	$(document).ready(function() {
		$("#txtConfirmPassword").keyup(isPasswordMatch);
	});
</script>
</head>
<body>
	<div id="header">
		<jsp:include page="/WEB-INF/jsp/headerPage/header.jsp" />
	</div>
	<br>
	<br>
	<br>
	<div id="myModal" class="modal">
		<div class="modal-dialog">
			<!-- Modal content -->
			<div class="modal-content row">
				<div class="modal-header custom-modal-header">
					<h3 style="text-align: center">Upload Photo</h3>
					<button type="button" class="close" aria-label="Close"
						id="uploadPhotoPopupCloseBtnId">
						<span aria-hidden="true">&times;</span>
					</button>
				</div>
				<div class="modal-body">
					<form id="fileUploadForm">
						<input type='hidden' id='uploadPhotoHiddenEmpId' value=""
							class='form-control'> <input type='file' name='files'
							id='uploadfile' />
						<button class="btn" id="uploadEmpPhotoId"
							style="background-color: #17b6bb">
							<i class="fa fa-upload" style="background-color: #17b6bb"></i>
						</button>
					</form>
				</div>

			</div>
		</div>
	</div>

	<div id="deleteRoleModal" class="modal">
		<div class="modal-dialog">
			<!-- Modal content -->
			<div class="modal-content row">
				<div class="modal-header custom-modal-header">
					<h3 style="text-align: center">Select Role To Delete</h3>

					<button type="button" class="close" aria-label="Close"
						id="deleteRoleModalBtnId">
						<span aria-hidden="true">&times;</span>
					</button>
				</div>
				<div class="modal-body">
					<form id="fileUploadForm">

						<div class="input-group">
							<input type='hidden' id='deleteRoleHiddenEmpId' value=""
								class='form-control'> <select class="form-control"
								name="employeeRolesToDelete" id="employeeRolesToDelete" required>
							</select>

							<button class="btn" id="deleteRoleBtnId"
								style="background-color: #17b6bb">
								<i class='fas fa-trash' style="color: red;"></i>
							</button>
						</div>

					</form>
				</div>

			</div>
		</div>
	</div>
	<div class="container-fluid">
		<form>
			<div class="row register-form">
				<div class="col-md-4">
					<div class="form-group">
						<label for="employeeFirstName"><b>First Name* :</b></label> <input
							type="text" class="form-control" id="employeeFirstName"
							placeholder="Enter First Name" name="employeeFirstName" value=""
							required />
					</div>
				</div>
				<div class="col-md-4">
					<div class="form-group">
						<label for="employeeLastName"><b>Last Name* :</b></label> <input
							type="text" class="form-control" id="employeeLastName"
							placeholder="Enter Last Name" name="employeeLastName" value=""
							required />
					</div>
				</div>
				<div class="col-md-4">

					<div class="form-group">
						<label for="employeeGender"><b>Gender* :</b></label> <select
							class="form-control" name="employeeGender" id="employeeGender"
							required>
							<option value="" selected disabled>-- Select Gender --</option>
							<option value="Male">Male</option>
							<option value="Female">Female</option>
							<option value="Others">Others</option>
						</select>
					</div>
				</div>

			</div>
			<div class="row register-form">
				<div class="col-md-4">
					<div class="form-group">
						<label for="employeePhone"><b>Phone Number* :</b></label> <input
							type="tel" class="form-control" id="employeePhone"
							pattern="[0-9]{10}" placeholder="Enter Phone Number"
							name="employeePhone" value="" required />
					</div>
				</div>
				<div class="col-md-4">

					<div class="form-group">
						<label for="employeeMailId"><b>Email* :</b></label> <input
							type="email" class="form-control" id="employeeMailId"
							placeholder="Enter mail id" name="employeeMailId" value=""
							required />
					</div>
				</div>
				<div class="col-md-4">

					<div class="form-group">
						<label for="employeeCode"><b>Employee Code* :</b></label> <input
							type="text" class="form-control" id="employeeCode"
							placeholder="Enter Code " name="employeeCode" value="" required />
					</div>
				</div>
			</div>
			<div class="row register-form">

				<div class="col-md-4">

					<!-- <div class="form-group">
						<label for="employeeDesignation"><b>Designation :</b></label> <input
							type="text" class="form-control" id="employeeDesignation"
							placeholder="Enter Designation *" name="employeeDesignation"
							value="" required />
					</div> -->

					<div class="form-group">
						<label for="employeeDesignation"><b>Designation* :</b></label> <select
							class="form-control" name="employeeDesignation"
							id="employeeDesignation" value="" required>
							<option value="Jr.Associate Consultant">Jr.Associate
								Consultant</option>
							<option value="Associate Consultant">Associate
								Consultant</option>
							<option value="Software Engineer">Software Engineer</option>
							<option value="Sr.Software Engineer">Sr.Software
								Engineer</option>
							<option value="HR">HR</option>
						</select>
					</div>
				</div>



				<div class="col-md-4">
					<div class="form-group">
						<label for="employeeStatuse"><b>Status* :</b></label> <select
							class="form-control" name="employeeStatuse" id="employeeStatuse"
							required>
							<option value="Active">Active</option>
							<option value="Inactive">Inactive</option>
						</select>
					</div>
				</div>
				<div class="col-md-4">

					<div class="form-group">
						<label for="employeeRoles"><b>Roles* :</b></label> <select
							class="form-control" name="employeeRoles" id="employeeRoles"
							required>
							<option value="" selected disabled>-- Select Roles --</option>
							<option value="ADMIN">ADMIN</option>
							<option value="MANAGER">MANAGER</option>
							<option value="EMPLOYEE">EMPLOYEE</option>
						</select>
					</div>
				</div>
			</div>
			<div class="row register-form">

				<div class="col-md-4">

					<div class="form-group">
						<label for="employeePassword"><b>Password* :</b></label> <input
							type="password" class="form-control" placeholder="Password *"
							id="txtPassword" name="employeePassword"
							pattern="(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{5,}"
							title="Must contain at least one number and one uppercase and lowercase letter, and at least 5 or more characters"
							required />
					</div>
				</div>
				<div class="col-md-4">
					<div class="form-group">
						<label for="employeeConfirmPassword"><b>Confirm
								Password* :</b> </label> <input type="password" class="form-control"
							placeholder="Confirm Password *" onChange="isPasswordMatch();"
							name="employeeConfirmPassword" id="txtConfirmPassword"
							pattern="(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{5,}"
							title="Must contain at least one number and one uppercase and lowercase letter, and at least 5 or more characters"
							required />
						<div class="registrationFormAlert" id="divCheckPasswordMatch"></div>
					</div>
				</div>
				<div class="col-md-4">
					<input type="hidden" value="" id="employeeId" />
				</div>
			</div>
			<div class="row">
				<div class="col-md-4">
					<button type="submit" value="Submit" class="btn btn-primary"
						id="btnSubmit">Register</button>
				</div>
				<div class="col-md-4"></div>
				<div class="col-md-4"></div>
			</div>
		</form>
		<div id="tab" class="table-responsive" style="color: white">
			<div class="d-flex justify-content-between">
				<div align="left">
				</div>
				<div align="right">
					<label id="dataloaded"></label>
					<button id="previous_button_id" class="btn btn-default btn-xs">
						<label id="paginationBtnCss"> << Previous</label>
					</button>
                      Showing  <lable id="pageNumberId"></lable> Page
					<button id="next_button_id" class="btn btn-default btn-xs">
						<label id="paginationBtnCss">Next >></label>
					</button>
				</div>
			</div>

			<table id="list" class="table table-bordered">
				<thead>
					<tr style="font-weight: bold" id="tableHeadingCSS">
						<td>Edit</td>
						<td>Upload Photo</td>
						<td>Delete Role</td>
						<td>Photo</td>
						<td>First Name</td>
						<td>Last Name</td>
						<td>Gender</td>
						<td>Mail Id</td>
						<td>Code</td>
						<td>Phone Number</td>
						<td>Designation</td>
						<td>Status</td>
						<td>Role</td>
					</tr>
				</thead>
				<tbody></tbody>
			</table>
		</div>
	</div>
</body>
</html>
