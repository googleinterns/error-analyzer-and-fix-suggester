<!-- Copyright 2020 Google LLC
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        https://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
<!DOCTYPE html>
<html>

<head>
	<!--info for browser -->
	<meta charset="UTF-8">
	<meta name="keywords" content="error, logs, search, stacktrace">
	<meta name="viewport" content="width=device-width, 
        initial-scale=1.0">
	<!-- favicon -->
	<link rel="shortcut icon" type="image/x-icon" href="images/gSuite.png" />
	<!-- title -->
	<title>Error analyzer</title>

	<!-- add css -->
	<link rel="stylesheet" href="result.css">
	<!-- add fontawesome icons -->
	<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.5.0/css/all.css"
	integrity="sha384-B4dIYHKNBt8Bc12p+WXckhzcICo0wtJAoU8YZTY5qE0Id1GSseTk6S+L3BlXeVIU" 
    	crossorigin="anonymous">
	<!-- bootstrap css -->
	<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css"
	integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk" 
    	crossorigin="anonymous">
    <!-- script for jquery -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>

</head>

<body onload="changePage(1)">	
	<!-- header -->
    	<%@include  file="resultPageHeader.html" %>
	<!-- log or error -->
	<%@include  file="logErrorButton.html" %>

	<!-- show result -->
   	 <%@include  file="carousel.html" %>
    
	<!-- page no and delete button -->
    	<%@include  file="resultPageFooter.html" %>

	<!--  JS  -->
	<script src="pagination.js"></script>
    	<script src="constants.js"></script>
    	<script src="onClickEvents.js"></script>
    	<script src="servlet.js"></script>
    	<script src="globalVariables.js"></script>
    	<script src="stackTrace.js"></script>
    	<script src="dataWindow.js"></script>
    	<script src="carousel.js"></script>

	<!-- bootstrap JS -->
	<script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"
	integrity="sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj" 
    	crossorigin="anonymous">
	</script>
	<script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js"
	integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" 
    	crossorigin="anonymous">
	</script>
	<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"
	integrity="sha384-OgVRvuATP1z7JjHLkuOU7Xw704+h835Lr+6QL9UvYjZE3Ipu6Tp75j7Bh/kR0JKI" 
    	crossorigin="anonymous">
	</script>
    <!-- script for tooltip -->
    <script>
        $(document).ready(function(){
        $('[data-toggle="tooltip"]').tooltip();   
        });
    </script>
</body>

</html
