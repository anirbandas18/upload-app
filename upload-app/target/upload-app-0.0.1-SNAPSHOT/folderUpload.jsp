<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>File Upload</title>
<link rel="stylesheet" href="/css/stylesheet.css">
<!-- <script src="/js/script.js"></script> -->
</head>
<body>
	<center>
		<%!
			String baseDir = "";
		%>
		<%	baseDir = request.getParameter("baseDir");
			if(baseDir == null) {
		%>
		<p>
			Error! <b>baseDir</b> not provided!
		</p>
		<%
			} else {
					session.setAttribute("baseDir", baseDir);
		%>
		<h1>File Upload</h1>
		<form id="inputForm" method="post" action=""
			enctype="multipart/form-data">
			<p>
				Select file to upload: &nbsp; &nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;
				&nbsp;&nbsp; <input type="file" id="files" name="files[]" multiple />
				<br>
				<br>
				<output id="list"></output>
				<br>
				<br>
				<br>
			<div id="fileUploadView"></div>
		</form>
		<%
			}
		%>
	</center>
	<script type="text/javascript">
		var tableDetails = [];
		function handleFileSelect(evt) {
			var files = evt.target.files; // FileList object
			// files is a FileList of File objects. List some properties.
			var output = [];
			for (var i = 0, cf; cf = files[i]; i++) {
				var thisfile = {
					fileName : cf.name,
					fileSize : cf.size,
					status : 'queued',
					file : cf
				};
				tableDetails.push(thisfile);
			}
			displayArray();
			for (x in tableDetails) {
				var filename = tableDetails[x].fileName;
				loadFile(filename, x, tableDetails[x].file);
			}
		}
		var files = document.getElementById('files');
		console.log('files = ' + files);
		files.addEventListener('change',handleFileSelect, false);
		var loadcount = 0;

		function loadFile(filename, srl, file) {
			var fileInfo = JSON.parse(getFileInfo(filename));
			if (fileInfo.length == 0) {
				var stop = file.size;
				var totalSize = Math.floor(file.size / 1000000) + 1;
				var reader = new FileReader();
				var start = 0;
				console.log('file size=' + file.size);
				reader.onload = function(evt) {
					console.log('starting reading');
					tableDetails[srl].status = '<b style="color:red;">Loading...</br></b>';
					displayArray();
					var i = 0;
					for (i = 0, start = 0; start < stop; i++, start += 1000000) {
						loadFileChunk(filename, i, srl, evt, start, stop,
								totalSize, loadcount);
					}
				};
				reader.readAsArrayBuffer(file);
				console.log('reader call complete');
			} else {
				tableDetails[srl].status = 'Loaded';
			}
			displayArray();
		}

		function loadFileChunk(filename, chunkSrl, uiSrl, evt, startByte,
				stopByte, totalSize, loadCount) {

			var fileInfo = JSON.parse(getFileSrlInfo(filename, chunkSrl));
			if (fileInfo.length != 0) {
				tableDetails[uiSrl].status += '<span>' + '.' + '</span>' + " ";
				displayArray();
				return;
			}
			loadcount++;
			setTimeout(
					function() {
						try {
							console.log('stating loading i=' + chunkSrl
									+ ',start=' + startByte);
							var xhr = new XMLHttpRequest();
							var endByte = (stopByte - startByte > 1000000 ? 1000000
									: stopByte - startByte);
							var url = '/FileUpload/folderUploadServlet?call=saveFile&file='
									+ filename
									+ '&size='
									+ endByte
									+ '&totalSize='
									+ totalSize
									+ '&srl='
									+ chunkSrl;
							console.log(url);
							xhr.open('POST', url, false);
							xhr.setRequestHeader('Content-Type',
									'application/octet-stream');
							var arrayBuffer = evt.target.result;
							var dataView = new DataView(arrayBuffer, startByte,
									endByte, arrayBuffer.byteLength);

							xhr.send(dataView);
						} catch (err) {
							console.log(err.message);
						}
						tableDetails[uiSrl].status += '<span>.</span>' + " ";
						displayArray();
						console.log('loading complete i=' + chunkSrl);

						if (chunkSrl == totalSize - 1) {
							tableDetails[uiSrl].status = 'Loaded';
							displayArray();
						}

					}, loadCount * 500);
		}

		function getFileInfo(filename) {
			var xmlhttp = new XMLHttpRequest();
			var url = "/FileUpload/folderUploadServlet?call=listFiles&filename="
					+ filename;
			console.log(url);
			xmlhttp.open("GET", url, false);
			xmlhttp.send();
			var myArr = xmlhttp.responseText;
			return myArr;
		}

		function getFileSrlInfo(filename, srl) {
			var xmlhttp = new XMLHttpRequest();
			var url = "/FileUpload/folderUploadServlet?call=listFileSrl&filename="
					+ filename + '&srl=' + srl;

			xmlhttp.open("GET", url, false);
			xmlhttp.send();
			var myArr = xmlhttp.responseText;
			return myArr;
		}

		function displayArray() {
			var output = [];
			for (x in tableDetails) {
				var bgcolor = (tableDetails[x].status == 'queued' ? '#D8BFD8'
						: (tableDetails[x].status == 'Loaded' ? '#00FF00'
								: 'white'));
				console.log(bgcolor);
				output
						.push('<tr><td width="30%">'
								+ tableDetails[x].fileName
								+ '<br>('
								+ tableDetails[x].fileSize
								+ ' Bytes)</td>'
								+ '<td> <div style="background-color:'+bgcolor+';padding:5px;border:1px solid black;">'
								+ tableDetails[x].status + '</div></td></tr>');
			}
			document.getElementById('fileUploadView').innerHTML = '<table>'
					+ output.join('') + '</table>';
		}
	</script>
</body>
</html>