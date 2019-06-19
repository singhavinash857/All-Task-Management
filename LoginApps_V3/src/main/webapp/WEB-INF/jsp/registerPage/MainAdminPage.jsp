<%-- <%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="f"  uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h"  uri="http://java.sun.com/jsf/html"%> --%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<style>
html, body {
	background-image: url("/images/yellowbackground.jpg");
	height: 100%;
}
</style>

<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script type="text/javascript">
	$(document)
			.ready(
					function() {
						/* 						http://localhost:8089/home/employee_controller/search_by_conditions?mailId=&projectName=&pageNumber=0
						 */var emailid = $("#emailid").val();
						var projectname = $("#projectname").val();

						var currentPageNumber = 1;
						var pageSize = 10;
						searchFeild(emailid, projectname, currentPageNumber, pageSize);
						$(function() {
							$("#emailid,#projectname").change(
									function(event) {
										var emailid = $("#emailid").val();
										var projectname = $("#projectname")
												.val();
										searchFeild(emailid, projectname,
												currentPageNumber,pageSize);
										console.log(emailid);
										console.log(projectname);
									});
						});

						$("#next_button_id").click(
								function() {
									currentPageNumber = currentPageNumber + 1;
									searchFeild(emailid, projectname,
											currentPageNumber, pageSize);
								});

						$("#previous_button_id")
								.click(
										function() {
											if (currentPageNumber > 0) {
												currentPageNumber = currentPageNumber - 1;
												searchFeild(emailid,
														projectname,
														currentPageNumber,pageSize);
											}
										});
						function searchFeild(emailid, projectname,
								currentPageNumber,pageSize) {
							console.log("emailid :" + emailid);
							console.log("projectname :" + projectname);
							$
									.ajax({
										url : "/home/employee_controller/search_by_conditions?mailId="
												+ emailid
												+ "&projectName="
												+ projectname
												+ "&pageNumber="
												+ currentPageNumber
												+ "&pageSize="
												+ pageSize,
										crossDomain : true,
										type : 'GET',
										dataType : 'json',
										success : function(data) {
											console.log(data);
											var tasktable = $('#list tbody');
											$('#list tbody').empty();
											if (data == "" || data == null) {
												alert("No record found for  number");
											}

											$(data.values)
													.each(

															function(index,
																	employee) {
																$(
																		employee.tasks)
																		.each(
																				function(
																						index1,
																						tasks) {

																					tasktable
																							.append("<tr>"
																									+ "<td>"
																									+ "<div class='card' style='width: 5rem;height:5rem;margin:0 auto;background-color: rgba(0,0,0,0.4) !important;'>"
																									+ "<img class='card-img-top rounded-circle'  src='"
																									+ employee.employeeFileDetailsDb.fileDownloadUri
																									+ "' alt='Card image cap' id='photo' style='border-radius: 50%; height:100%; width:100%;'>"
																									+ "</div>"
																									+ "<td>"
																									+ employee.employeeFirstName
																									+ " "
																									+ employee.employeeLastName
																									+ "</td>"
																									+ "<td>"
																									+ employee.employeeMailId
																									+ "</td>"
																									+ "<td>"
																									+ employee.employeeCode
																									+ "</td>"
																									+ "<td>"
																									+ tasks.taskName
																									+ "</td>"
																									+ "<td>"
																									+ tasks.taskType
																									+ "</td>"
																									+ "<td>"
																									+ tasks.taskAssignmentType
																									+ "</td>"
																									+ "<td>"
																									+ tasks.taskStatus
																									+ "</td>"
																									+ "<td>"
																									+ tasks.taskAssignedDate
																									+ "</td>"
																									+ "</tr>")
																					console
																							.log("taskname"
																									+ tasks.taskName);
																				});
															});
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
											console.log('errorThrown',
													errorString);
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
	<div class="container py-4 my-4">
		<!-- <div class="row">
		<div class="col-md-8">
		<div class="input-group">
		          <select id="emailid" class="form-control">
						<option value="" selected>... Search by mail id...</option>
						<option>avinash.singh@transformedge.com</option>
						<option>madhushree.mg@transformedge.com</option>
						<option></option>
						<option></option>
					</select>
					
					<select id="projectname" class="form-control">
						<option value="" selected>... Search by Project...</option>
						<option>Spirax sarco</option>
						<option>Roots info</option>
						<option></option>
						<option></option>
					</select>
		</div>
		
		</div>
		</div> -->
	</div>
	<div class="container-fluid">

		<div class="row">
			<div class="col-md-6">
				<div class="input-group">
					<select id="emailid" class="form-control"
						style="background: transparent;">
						<option value="" selected style="background: transparent;">--
							Search By Email --</option>
						<option>avinash.singh@transformedge.com</option>
						<option>madhushree.mg@transformedge.com</option>
						<option></option>
						<option></option>
					</select> &nbsp; <select id="projectname" class="form-control"
						style="background: transparent;">
						<option value="" selected style="color: white;">-- Search
							By Project --</option>
						<option>Spirax sarco</option>
						<option>Roots info</option>
						<option></option>
						<option></option>
					</select>
				</div>

			</div>
		</div>
		<br>
		<div id="showData" class="table-responsive dropDownDiv">
			<table class="table table-bordered" id="list">
				<thead>
					<tr>
						<td>Photo</td>
						<td>Employee Name</td>
						<td>Email id</td>
						<td>Code</td>
						<td>Project Name</td>
						<td>Project Type</td>
						<td>Assignment Type</td>

						<td>Current Status</td>

						<td>Date</td>

					</tr>
				</thead>
				<tbody id="myTable">
				</tbody>
			</table>
		</div>
	</div>

</body>
</html>