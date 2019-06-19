<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="customCss/registrationPageCss.css">

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
	function deleteFunc(id) {
		var x = confirm("Are you sure you want to delete the task with ID "
				+ id + " ??");
		if (x) {
			$
					.ajax({
						url : "/home/comments_controller/delete_comments_by_id/"
								+ id,
						crossDomain : true,
						type : 'DELETE',
						dataType : 'json',
						success : function(data) {
							console.log(data);
							alert(data.message);
							document.location.href = window.location.href

						},
						error : function(e) {
							var errors = e.responseJSON.errors;
					        var errorString = '';

							$.each( errors, function( key, value) {
					            errorString += value.errorDescription;
					        });
						    alert(errorString);
						}
					});
		}
	}
</script>
<script type="text/javascript">
	$(document)
			.ready(
					function() {
						loadDataToTaskStatus();
						$("#btnSubmit").click(function(e) {
							  e.preventDefault();
							saveComment();
						});
						function saveComment() {
							
							var taskId = document
							.getElementById("taskId").value;
							
							var taskAssigner = document
							.getElementById("taskAssignedBy").value; 
							
							var taskSupervisor = document
							.getElementById("taskSupervisor").value;
						
							var formData = {
							    'taskAssignedBy' : taskAssigner,
								'taskSupervisor' : taskSupervisor,
								'taskStatus' : $(
								'#taskStatus option:selected')
								.val(),
								'taskWorkingHours' : $(
								'#workingHours option:selected')
								.val(),
								'taskDailyComments' : $('textarea[name=comments]').val()
							};

							console.log(formData);
							$
									.ajax({
										crossDomain : true,
										url : '/home/comments_controller/save_daily_comments/'
												+ taskId,
										method : 'POST',
										dataType : 'json',
										contentType : "application/json; charset=utf-8",
										async : false,
										data : JSON.stringify(formData),
										success : function(data) {
											console.log(data.status);
											if (data.status) {
												alert(data.message);
												loadDataToTaskStatus();
											}
										},
										error : function(e) {
											var errors = e.responseJSON.errors;
									        var errorString = '';

											$.each( errors, function( key, value) {
									            errorString += value.errorDescription;
									        });
										    alert(errorString);
										}
									});
						}
						function loadDataToTaskStatus() {
							$
									.ajax({
										crossDomain : true,
										url : "/home/task_controller/get_task/" + ${taskId},
										type : 'GET',
										dataType : 'json',
										success : function(data) {
											$(data)
													.each(
															function() {

																
																document
																		.getElementById("taskId").value = data.taskId;

																document
																.getElementById("taskAssignedBy").value = data.taskAssigner;
																
																
																document
																.getElementById("taskSupervisor").value = data.taskSupervisor;
																
																
																$(function() {
																	var defaultValue = $("#taskStatus").val();
																	$("#taskStatus").val(defaultValue);
																	document.getElementById("taskStatus").value = data.taskStatus;

																	});
																
																document
																		.getElementById("taskName").innerHTML = data.taskName;
																document
																		.getElementById("assignedBy").innerHTML = data.taskAssigner;
																document
																		.getElementById("assignedTo").innerHTML = data.taskSupervisor;
																document
																		.getElementById("projectDescription").innerHTML = data.taskDescription;

																var tasktStatusTable = $('#list tbody');
																$('#list tbody')
																		.empty();
																if (data.taskDailyComments == ""
																		|| data.taskDailyComments == null) {
																	document.getElementById("tableDiv").style.display = "none";
																}
																else{
																	document.getElementById("tableDiv").style.display = "block";
																}
																$
																		.each(
																				data.taskDailyComments,
																				function(
																						index2,
																						taskCommentedby1) {
																					tasktStatusTable
																							.append("<tr>"
																									+ "<td>"
																									+ "<button id='btn' type='button' onclick='deleteFunc("
																									+ taskCommentedby1.id
																									+ ");'  style='background-color: rgba(0,0,0,0.4) !important;border-color:rgba(0,0,0,0.4) !important;color:rgb(255,0,0);'>Delete Comment</button>"
																									+ "</td>"
																									+ "<td>"
																									+ data.taskName
																									+ "</td>"
																									+ "<td>"
																									+ data.taskSupervisor
																									+ "</td>"
																									+ "<td>"
																									+ taskCommentedby1.taskCommentedBy
																									+ "</td>"
																									+ "<td>"
																									+ taskCommentedby1.taskComments
																									+ "</td>"
																									+ "<td>"
																									+ taskCommentedby1.taskCommentDate
																									+ "</td>"
																									+ "<td>"
																									+ taskCommentedby1.taskWorkingHours
																									+ "</td>"
																									+ "</tr>")
																				});

															});
										},
										error : function(e) {
											console.log('errorThrown', e);
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
			<div class="row">
				<div class="col-md-4">
					<div class="form-group">
						<label for="taskName"><b>Task Name :</b></label> <label
							id="taskName"></label>
					</div>
				</div>
				<div class="col-md-4">
					<div class="form-group">
						<label for="assignedBy"><b>Assigned By :</b></label> <label
							id="assignedBy"></label>
					</div>
				</div>
				<div class="col-md-4">
					<div class="form-group">
						<label for="assignedTo" id=""><b>Assigned To :</b></label> <label
							id="assignedTo" name="assignedBy"></label>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-4">
					<div class="form-group">
						<label for="projectDescription"><b>Project Description
								:</b></label> <label id="projectDescription"></label>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-4">
					<div class="form-group">
						<label for="taskStatus"><b>Task Status :</b></label> <select
							class="form-control" name="taskStatus" id="taskStatus" required>
							<option value="">- - select - -</option>
							<option value="initialize">initialize</option>
							<option value="Yet To Start">Yet To Start</option>
							<option value="Started">Started</option>
							<option value="In Development">In Development</option>
							<option value="Yet To Finish">Yet To Finish</option>
							<option value="Close">Close</option>
						</select>
					</div>
				</div>
				<div class="col-md-4">
					<div class="form-group">
						<label for="workingHours"><b>Working Hours :</b></label> <select
							class="form-control" name="workingHours" id="workingHours"
							required>
							<option value="1">1 hour</option>
							<option value="2">2 hour</option>
							<option value="3">3 hour</option>
							<option value="4">4 hour</option>
							<option value="5">5 hour</option>
							<option value="6">6 hour</option>
							<option value="7">7 hour</option>
							<option value="8">8 hour</option>
							<option value="9">9 hour</option>
							<option value="10">10 hour</option>
							<option value="11">11 hour</option>
						</select>
					</div>
				</div>
				<div class="col-md-4"></div>
			</div>
			<div class="row">
				<div class="col-md-4">
					<div class="form-group">
						<label for="comments"><b>Comments* :</b></label>
						<textarea id="comments" name="comments"
							class="form-control col-xs-12" placeholder="Comments.." required></textarea>
					</div>
				</div>
				<div class="col-md-4">
					<input type="hidden" value="" id="taskId" /> <input type="hidden"
						value="" id="taskAssignedBy" /> <input type="hidden" value=""
						id="taskSupervisor" />

				</div>
				<div class="col-md-4"></div>
			</div>
			<div class="row">
				<div class="col-md-5"></div>
				<div class="col-md-2">
					<div class="form-group">
						<button type="submit" value="Submit" class="btn btn-primary"
							id="btnSubmit"
							style="background: -webkit-linear-gradient(right, #0072ff, #8811c5); border: none; border-radius: 1.5rem;">Save
							Comment</button>
					</div>
				</div>
				<div class="col-md-5"></div>
			</div>
		</form>
		<br>
		<div class="table-responsive" id="tableDiv">
			<table class="table table-bordered" id="list">
				<thead>
					<tr>
						<th id="Action">Action</th>
						<th id="taskName">Task Name</th>
						<th id="taskSupervisor">Task Supervisor</th>
						<th id="commentedBy">Commented By</th>
						<th id="comments">Comments</th>
						<th id="date">Date</th>
						<th id="workingHours">Working Hours</th>

					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</div>
	</div>
	<br>
	<br>


</body>
</html>
