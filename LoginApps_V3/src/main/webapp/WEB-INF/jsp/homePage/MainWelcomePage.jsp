<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<style>
body {
	background-image: url("images/yellowbackground.jpg");
	height: 100%;
}
</style>

<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		var currentPageNumber = 1;
		var pageSize = 10;
		loadData(currentPageNumber,pageSize);
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
		
	});

	function loadData(currentPageNumber,pageSize) {
		$.ajax({
					crossDomain : true,
					url : "/home/employee_controller/get_loggedin_user_info?page_number="+currentPageNumber+"&pageSize="+pageSize,
					type : 'GET',
					dataType : 'json',
					success : function(data) {
						console.log(data);
						var emptable = $('#list tbody');
						$('#list tbody').empty();
						if (data.userLoginDetailsInfo.values != "" || data.userLoginDetailsInfo.values != null) {
							if(data.userLoginDetailsInfo.hashMore == false){
								document
								.getElementById("next_button_id").disabled = true;
							}else{
								document
								.getElementById("next_button_id").disabled = false;
							}
							
							document
							.getElementById("pageNumberId").innerHTML = ( data.userLoginDetailsInfo.pageNum + 1) + " Of " + data.userLoginDetailsInfo.toalPages;
							
							$(data)
									.each(
											function() {
												document
														.getElementById("fullName").innerHTML = data.employeeFirstName
														+ " "
														+ data.employeeLastName;
												document
														.getElementById("photo").src = data.employeeFileDetailsDb.fileDownloadUri;
												document
														.getElementById("employeeName").innerHTML = data.employeeFirstName
														+ " "
														+ data.employeeLastName;
												document
														.getElementById("employeeCode").innerHTML = data.employeeCode;
												document
														.getElementById("employeeMailId").innerHTML = data.employeeMailId;
												document
														.getElementById("employeeDesignation").innerHTML = data.employeeDesignation;

												$
														.each(
																data.userLoginDetailsInfo.values,
																function(
																		loginindex2,
																		loginDateAndTime) {
																	emptable
																			.append("<tr>"
																					+ "<td>"
																					+ data.employeeCode
																					+ "</td>"
																					+ "<td>"
																					+ data.employeeFirstName
																					+ " "
																					+ data.employeeLastName
																					+ "</td>"
																					+ "<td>"
																					+ data.employeeMailId
																					+ "</td>"
																					+ "<td>"
																					+ loginDateAndTime.loginTime
																					+ "</td>"
																					+ "<td>"
																					+ loginDateAndTime.logoutTime
																					+ "</td>"
																					+ "<td>"
																					+ loginDateAndTime.loginDate
																					+ "</td>"
																					+ "<td>"
																					+ loginDateAndTime.systemIpAddress
																					+ "</td>"
																					+ "</tr>")
																});
											});
						}
					},
					error : function(e) {
						console.log('errorThrown', e);
					}
				});
	}
</script>
</head>
<body>
	<div id="header">
		<jsp:include page="/WEB-INF/jsp/headerPage/header.jsp" />
	</div>
	<div style="padding-bottom: 80px;padding-top: 1px;"></div>
	<div class="container">
	<div class="card" id="welcomePageCardCss">
		<img class="card-img-top rounded-circle" src="" alt="Card image cap"
			id="photo" style='border-radius: 50%; height:100%; width:100%;'>
			 <div class="card-body">
			<h5 class="card-title" style="text-align: center;">
				<label id="fullName"></label>
			</h5>
		</div>
	</div>
		<div class="form">
			<div class="form-content">
				<div class="row">
					<div class="col-sm-6">
						<div class="form-group">
							<label for="employeeName"><b>Employee Name :</b></label> <label
								id="employeeName"></label>
						</div>
						<div class="form-group">
							<label for="employeeDesignation" id=""><b>Employee
									Designation :</b></label> <label id="employeeDesignation"></label>
						</div>
					</div>
					<div class="col-sm-6">
						<div class="form-group">
							<label for="employeeCode"><b>Employee Code :</b></label> <label
								id="employeeCode"></label>
						</div>
						<div class="form-group">
							<label for="employeeMailId"><b>Employee Email : </b></label> <label
								id="employeeMailId"></label>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div id="tab" class="table-responsive" style="color: white;">
		<div>
				<div align="left">
				</div>
				<div align="center">
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
			<table class="table table-bordered" id="list">
				<thead>
					<tr>
						<th id="employeeCode">Employee Code</th>
						<th id="employeeName">Employee Name</th>
						<th id="employeeMailId">Employee Email</th>
						<th id="loginTime">Login Time</th>
						<th id="LogoutTime">Logout</th>
						<th id="LogoutTime">Date</th>
						<th id="systemIpAddress">IP Address</th>
					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</div>
	</div>
</body>
</html>
