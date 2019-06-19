<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

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
	function openProjectDetails(taskId) {
		window.location = '/home/redirect_controller/taskPage/comment_page_with_task/'
				+ taskId;

	}
</script>
<script type="text/javascript">
	$(document)
			.ready(
					function() {

						var currentPageNumber = 1;
						var pageSize = 10;
						loadDataToTable(currentPageNumber,pageSize);
						
						$("#next_button_id").click(function() {
							currentPageNumber = currentPageNumber + 1;
							loadDataToTable(currentPageNumber,pageSize);
						});

						$("#previous_button_id").click(function() {
							if(currentPageNumber > 0){
								currentPageNumber = currentPageNumber - 1;
								loadDataToTable(currentPageNumber,pageSize);
							}
						});
						//loading data into table..
						function loadDataToTable(page_number,pageSize) {
							console.log("inside the method loadData!!");
							$
									.ajax({
										url : "/home/task_controller/get_task_assigned_to_supervisor?page_number="+page_number
												+"&pageSize="+pageSize,
										crossDomain : true,
										type : 'GET',
										dataType : 'json',
										success : function(data) {

											console.log(data);
											var tasktable = $('#list tbody');
											$('#list tbody').empty();
											if (data.values != "" || data.values != null) {
												if(data.hashMore == false){
													document
													.getElementById("next_button_id").disabled = true;
												}else{
													document
													.getElementById("next_button_id").disabled = false;
												}
												
												document.getElementById("list").style.display = "block";
												
												document
												.getElementById("pageNumberId").innerHTML = data.pageNum+1 +" Of "+data.toalPages;
												
												$(data.values)
												.each(
														function(index,
																task1) {
															tasktable
																	.append("<tr>"
																			+ "<td>"
																			+ "<button id='btn' type='button' onclick='openProjectDetails("
																			+ task1.taskId
																			+ ");'  style='background-color: rgba(0,0,0,0.4) !important;border-color:rgba(0,0,0,0.4) !important;color:rgba(30,144,255);'>"
																			+ task1.taskName
																			+ "</button>"
																			+ "</td>"
																			+ "<td>"
																			+ task1.taskType
																			+ "</td>"
																			+ "<td>"
																			+ task1.taskAssignmentType
																			+ "</td>"
																			+ "<td>"
																			+ task1.taskAssigner
																			+ "</td>"
																			+ "<td>"
																			+ task1.taskPriority
																			+ "</td>"
																			+ "<td>"
																			+ task1.taskAssignedDate
																			+ "</td>"
																			+ "<td>"
																			+ task1.taskStatus
																			+ "</td>"
																			+ "</tr>")
														});
											}
											
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
	<div class="container">

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
			<table class="table table-bordered" id="list">
				<thead>
					<tr>
						<td>Project Name</td>
						<td>Project Type</td>
						<td>Assignment Type</td>
						<td>Assigned By</td>
						<td>Priority</td>
						<td>Assigned Date</td>
						<td>Status</td>
					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>

		</div>
	</div>
</body>
</html>
