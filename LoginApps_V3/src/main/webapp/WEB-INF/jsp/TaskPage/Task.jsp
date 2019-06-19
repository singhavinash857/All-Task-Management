<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script
	src="//cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>

<style>
body {
	background-image:
		url("${pageContext.request.contextPath}/images/yellowbackground.jpg");
	height: 100%;
}
</style>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script>
	function deleteFunc(taskId) {
		var x = confirm("Are you sure you want to delete the task with ID : "
				+ taskId + " ??");
		if (x) {
			$.ajax({
				url : "/home/task_controller/delete_task/" + taskId,
				crossDomain : true,
				type : 'DELETE',
				dataType : 'json',
				success : function(data) {
					console.log(data);
					alert(data.message);
					document.location.href = window.location.href

				}
			});
		}
	}
</script>
<script>
	$(document)
			.ready(
					function() {

						var currentPageNumber = 1;
						var pageSize = 10;
						loadTaskData(currentPageNumber, pageSize);
						//loadTaskData();
						$("#btnSubmit").click(function(e) {
							var valid = this.form.checkValidity();
							if (valid) {
								/* document
								.getElementById("myModal").style.display = "block"; */
								e.preventDefault();
								$("#btnSubmit").attr("disabled", true);
								saveTask();
							}
						});

						$("#next_button_id").click(function() {
							currentPageNumber = currentPageNumber + 1;
							loadTaskData(currentPageNumber, pageSize);
						});

						$("#previous_button_id").click(function() {
							if (currentPageNumber > 0) {
								currentPageNumber = currentPageNumber - 1;
								loadTaskData(currentPageNumber, pageSize);
							}
						});

						/* load the data in the table */
						function loadTaskData(page_number, pageSize) {
							console.log("inside the method loadData!!"
									+ page_number);
							$
									.ajax({
										url : "/home/task_controller/get_all_task_by_Assigner?page_number="
												+ page_number + "&pageSize=" + pageSize,
										crossDomain : true,
										type : 'GET',
										dataType : 'json',
										success : function(data) {
											console.log(data);
											$("#btnSubmit").attr("disabled",
													false);

											var tasktable = $('#list tbody');
											$('#list tbody').empty();
											if (data.values != ""
													|| data.values != null) {
												if (data.hashMore == false) {
													document
															.getElementById("next_button_id").disabled = true;
												} else {
													document
															.getElementById("next_button_id").disabled = false;
												}
												document.getElementById("list").style.display = "block";

												document
														.getElementById("pageNumberId").innerHTML = data.pageNum+1 +" Of "+data.toalPages;

												$(data.values)
														.each(
																function(index,
																		task) {
																	tasktable
																			.append("<tr>"
																					+ "<td>"
																					+ "<button id='btn' type='button' value='Delete' class='btn btn-primary' onclick='deleteFunc("
																					+ task.taskId
																					+ ");'  style='background-color: rgba(0,0,0,0.4) !important;border-color:rgba(0,0,0,0.4) !important;color:rgb(255,0,0);'><i class='far fa-trash-alt'></i></button>"
																					+ "</td>"
																					+ "<td>"
																					+ task.taskName
																					+ "</td>"
																					+ "<td>"
																					+ task.taskType
																					+ "</td>"
																					+ "<td>"
																					+ task.taskAssignmentType
																					+ "</td>"
																					+ "<td>"
																					+ task.taskSupervisor
																					+ "</td>"
																					+ "<td>"
																					+ task.taskAssigner
																					+ "</td>"
																					+ "<td>"
																					+ task.taskStatus
																					+ "</td>"
																					+ "<td>"
																					+ task.taskPriority
																					+ "</td>"
																					+ "<td>"
																					+ task.taskDescription
																					+ "</td>"
																					+ "<td>"
																					+ task.taskAssignedDate
																					+ "</td>"
																					+ "</tr>")
																});
											}

										},
										error : function(e) {
											$("#btnSubmit").attr("disabled",
													false);
											console.log('errorThrown', e);
										}

									});
						}

						function saveTask() {
							var formData = {
								'taskName' : $('#taskName option:selected')
										.val(),
								'taskType' : $('#projectType option:selected')
										.val(),
								'taskAssignmentType' : $(
										'#assignmentType option:selected')
										.val(),
								'taskSupervisor' : $(
										'#taskSupervisor option:selected')
										.val(),
								/* 'taskAssigner' : $()
								.val(), */
								'taskStatus' : $('#taskStatus option:selected')
										.val(),
								'taskPriority' : $(
										'#taskPriority option:selected').val(),
								'taskDescription' : $(
										'textarea[name=taskDescription]').val()
							};

							console.log(formData);

							$
									.ajax({
										crossDomain : true,
										url : '/home/task_controller/save_task',
										method : 'POST',
										dataType : 'json',
										contentType : "application/json; charset=utf-8",
										async : true,
										data : JSON.stringify(formData),
										success : function(data) {
											console.log(data.status);
											document.getElementById("myModal").style.display = "none";
											if (data.status) {
												alert(data.message);
												loadTaskData(currentPageNumber,
														pageSize);
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
											document.getElementById("myModal").style.display = "none";
											alert(errorString);
										}
									});
						}
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
	<br>
	<div class="container-fluid">
		<form>
			<div class="row register-form">
				<div class="col-md-4">
					<div class="form-group">
						<label for="taskName"><b>Project Name :</b></label> <select
							class="form-control" name="taskName" id="taskName" required>
							<option value="" selected disabled>-- Select Task Name--</option>
							<option value="Roots info">Roots info</option>
							<option value="Spirax Sarco Sustem">Spirax Sarco Sustem</option>
							<option value="Task Management">Task Management</option>
						</select>
					</div>
				</div>
				<div class="col-md-4">

					<div class="form-group">
						<label for="projectType"><b>Project Type :</b></label> <select
							class="form-control" name="projectType" id="projectType" required>
							<option value="" selected disabled>-- Select Project
								Type--</option>
							<option value="Internal">Internal</option>
							<option value="Client">Client</option>
							<option value="Partner">Partner</option>
						</select>
					</div>
				</div>
				<div class="col-md-4">

					<div class="form-group">
						<label for="assignmentType"><b>Assignment Type :</b></label> <select
							class="form-control" name="assignmentType" id="assignmentType"
							required>
							<option value="" selected disabled>-- Select Assignment
								Type--</option>
							<option value="Internal R&D">Internal R&D</option>
							<option value="Learning & Development"></option>
							<option value=""></option>
						</select>
					</div>
				</div>
			</div>
			<div class="row register-form">
				<div class="col-md-4">
					<div class="form-group">
						<label for="taskSupervisor"><b>Task Supervisor :</b></label> <select
							class="form-control" name="taskSupervisor" id="taskSupervisor"
							required>
							<option value="" selected disabled>-- Select Task
								Supervisor--</option>
							<option value="avinash.singh@transformedge.com">avinash.singh@transformedge.com</option>
							<option value="madhushree.mg@transformedge.com">madhushree.mg@transformedge.com
							</option>
							<option value="malini.a@transformedge.com">malini.a@transformedge.com
							</option>
						</select>
					</div>
				</div>
				<!-- <div class="col-md-4">
				<div class="form-group">
					<label for="taskAssigner"><b>Task Assigner :</b></label> <select
						class="form-control" name="taskAssigner" id="taskAssigner"
						required>
						<option value="" selected disabled>-- Select Task
							Assigner --</option>
						<option value="kathir.k@transformedge.com">kathir.k@transformedge.com</option>
						<option value="avinashsingh857@gmail.com">avinashsingh857@gmail.com</option>
					</select>
				</div>
			</div> -->
				<div class="col-md-4">
					<div class="form-group">
						<label for="taskStatus"><b>Task Status :</b></label> <select
							class="form-control" name="taskStatus" id="taskStatus" required>
							<option value="initialize">Initialized</option>
							<option value="Yet To Start">Yet To Start</option>
							<option value="Started">Started</option>
							<option value="In Development">In Development</option>
							<option value="Yet To Finish">Yet To Finish</option>
							<option value="Close">Close</option>

						</select>
					</div>
				</div>
			</div>
			<div class="row register-form">
				<div class="col-md-4">
					<div class="form-group">
						<label for="taskPriority"><b>Task Priority :</b></label> <select
							class="form-control" name="taskPriority" id="taskPriority"
							required>
							<option value="" selected disabled>-- Select Task
								Priority--</option>
							<option value="Medium">Medium</option>
							<option value="High">High</option>
							<option value="Critical">Critical</option>
							<option value="Bug">Bug</option>
						</select>
					</div>
				</div>
				<div class="col-md-4">
					<div class="form-group">
						<label for="taskDescription"><b>Task Description :</b></label>
						<textarea id="taskDescription" name="taskDescription"
							class="form-control col-xs-12" placeholder="Task Description.."
							style="border-color: black" required></textarea>
					</div>
				</div>
				<div class="col-md-4"></div>
			</div>
			<div class="row register-form">
				<div class="col-md-6"></div>
				<div class="col-md-2">
					<button type="submit" value="Submit" class="btn btn-primary"
						id="btnSubmit"
						style="background: -webkit-linear-gradient(right, #0072ff, #8811c5); border: none; border-radius: 1.5rem;">Assign</button>
				</div>
				<div class="col-md-4"></div>
			</div>
		</form>

		<!-- <div id="myModal" class="modal">
			Modal content
			<div class="modal-content">
				<form id="fileUploadForm" method="POST">
					<input type='hidden' id='uploadPhotoHiddenEmpId' value=""
						class='form-control'> <input type='file' name='files'
						id='uploadfile' />
					<button onclick="fileSubmit()" id="uploadEmpPhotoId">Upload</button>
				</form>
				<button type="button" class="close" aria-label="Close"
					id="uploadPhotoPopupCloseBtnId">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
		</div>
 -->
		<div id="tab" class="table-responsive" style="color: white">

			<div class="d-flex justify-content-between">
				<div align="left" id="searchboxCss">
					<!-- <input type="button"
					onclick="tableToExcel('tblProductList', 'W3C Example Table')"
					value="Export to Excel" class="btn btn-default" id="searchboxCss">
				<label id="totalData" id="searchboxCss"></label> -->
				</div>

				<div align="right">
					<label id="dataloaded"></label>
					<button id="previous_button_id" class="btn btn-default btn-xs">
						<label id="paginationBtnCss"> << Previous</label>
					</button>
					Showing
					<lable id="pageNumberId"></lable>
					Page
					<button id="next_button_id" class="btn btn-default btn-xs">
						<label id="paginationBtnCss">Next >></label>
					</button>
				</div>
			</div>
			<table id="list" class="table table-bordered">
				<thead>
					<tr style="font-weight: bold" id="tableHeadingCSS">
						<td>Action</td>
						<td>Task Name</td>
						<td>Project Type</td>
						<td>Assignment Type</td>
						<td>Task Supervisor</td>
						<td>Task Assigner</td>
						<td>Task Status</td>
						<td>Task Priority</td>
						<td>Task Description</td>
						<td>Date</td>

					</tr>
				</thead>
				<tbody></tbody>
			</table>
		</div>
	</div>
	<div id="myModal" class="modal">
		<div class="modal-dialog">

			<div class="modal-body">
				<form id="" method="POST">
					<button class="buttonload">
						<i class="fa fa-spinner fa-spin"></i>Please wait ...
					</button>
				</form>
			</div>

		</div>
	</div>
	<br>
	<br>

</body>
</html>