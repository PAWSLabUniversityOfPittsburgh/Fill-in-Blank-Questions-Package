<%@page import="javax.print.attribute.standard.Severity"%>
<%@page import="Util.Coding"%>
<%@page import="java.util.*"%>
<%@page import="bean.Jquiz"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta charset="UTF-8">
<title>Document</title>
<link rel="stylesheet" href="css/arduino-light.css">
<link rel="stylesheet" href="css/style.css">
<script src="https://ace.c9.io/build/src/ace.js" type="text/javascript"
	charset="utf-8"></script>



<%
	
	//decide which kind of language it tests
	String languagString = getServletContext().getInitParameter("language");
	if ("py".equals(languagString)) {
		languagString = "python";
	}
	//get the title of this quiz
	//String title = (String) request.getAttribute("title");
	//get the full info of quiz
	Jquiz jquiz = (Jquiz) request.getAttribute("jquiz");
	int type = jquiz.getQuesType();
	//get the main class's code part so that it can show on the screen
	String codeStr = (String) request.getAttribute("codeStr");
	
	
	//get the answer which is generated by compiling and executing 
	String userCode=null;
	//String answer = (String) request.getAttribute("answer");
	//the following is the answer which wil display on the screen
	//Notice: answer and displayAnswer are similar, but there is a little difference. You can print it to shwo where is the difference
	String displayAnswer = (String) request.getAttribute("displayAnswer");
	//whether it has more than one class
	boolean multiClasses = false;
	if (request.getAttribute("multiClasses") != null) {
		multiClasses = (boolean) request.getAttribute("multiClasses");
	} else if (request.getParameter("multiClasses") != null) {
		multiClasses = request.getParameter("multiClasses").equals("true") ? true : false;
	}
	//get all extra classes's name. i.e. all classes except main class
	List<String> fileNames = null;
	String[] extraCode = null;
	if (multiClasses) {
		fileNames = (ArrayList<String>) request.getAttribute("fileNames");
		extraCode = new String[fileNames.size()];
		extraCode = (String[]) request.getAttribute("extraCodes");
	}

	//if this page is redirected from EvaluateAnswer servlet, it will have "correct" paramter
	if (request.getAttribute("correct") == null) {
		userCode = Coding.spilitCode(codeStr, jquiz.getStart(),
				jquiz.getEnd(), this.getServletContext().getInitParameter("language"));
	} else{
		userCode = (String) request.getAttribute("userCode");
	}
	boolean correct;
	String result = null;
	String resultColor = "red";
	if (request.getAttribute("correct") != null) {
		correct = (boolean) request.getAttribute("correct");
		result = correct ? "CORRECT!" : "WRONG!";
		if (correct) {
			resultColor = "green";
		}
	}
%>
</head>
<body>

	<div class="mainbody">
	<!-- question bar -->
			<div id="question">
				<div id="question-title" class="downarrow">
					Question
				</div>
				<div id="question-body">
				<c:if test="${jquiz.quesType==1 }">
					<font color=blue>Fill the blank to make the final value of
						<b>result</b>:
					</font>
				</c:if>
				<c:if test="${jquiz.quesType==2}">
					<font color=blue>Fill the blank to make the output </font>
				</c:if>
				<br> <b>${displayAnswer}</b>
				</div>
			</div>
	<!-- message bar -->
		<c:if test="${! empty correct }">
			<div id="answer">
			<div id="answer-title" class="correct">
				<div id="answer-status" class="leftarrow">
					<font color="<%=resultColor%>"><%=result%>! &nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp; Click to See Instructor’s Solution</font>
				</div>
			</div>
			<div id="answer-body">
					<!-- <p>Correct answer is:</p> -->
				<pre><code class="<%=languagString%>"><%=codeStr%></code></pre>
			</div>
		</div>
		</c:if>
	
		<div class="content">
			<div class="content-nav">
				<ul>
					<li class="content-nav-active">${title }</li>
					<%if (multiClasses) {
						for (int j = 0; j < fileNames.size(); j++) {%>
					<li><%=fileNames.get(j)%></li>
					<%}
						}%>
				</ul>
			</div>
			<form method="post">
				<input type="hidden" name="rdfID" value="${jquiz.rdfID}">
				<input type="hidden" name="answer" value="${requestScope.answer}"> 
				<input type="hidden" name="title" value="${title }"> 
				<input type="hidden" name="codeStr" value="<%=codeStr%>"> 
				<input type="hidden" name="type" value="${jquiz.quesType }" /> 
				<input type="hidden" name="userCode" id="userCode" />
				
				<!-- here is the code -->
				<div id="editor" class="content-content" name='editor'><%=userCode%></div>
				
				<%if (multiClasses) {
					for (int j = 0; j < fileNames.size(); j++) {%>
				<div id="editor<%=j%>" class="content-content hide" name='editor'><%=extraCode[j]%></div>
				<script>
				  var editor = ace.edit("editor<%=j%>");
				  editor.setTheme("ace/theme/github");
				  editor.getSession().setMode('ace/mode/<%=languagString%>');
				  editor.setFontSize('16px');
				 </script>
					<%}
				}%>
				<div class="content_submit">
					<%if (request.getAttribute("correct") == null) {%>
					<input type="submit" value="Submit" onclick="submitFunction(this.form,1)">
					<%} else {%>
					<input type="submit" value="Try Again" onclick="submitFunction(this.form,1)" />
					<%}%>
				</div>
			</form>
		</div>

	</div>
	<script language="JavaScript">
	function ReplaceSeperator(mobiles) {
	    var i;
	    var result = "";
	    var c;
	    for (i = 0; i < mobiles.length; i++) {
	        c = mobiles.substr(i, 1);
	        if (c == "\n")
	            result = result + "<br/>";
	        else if (c != "\r")
	            result = result + c;
	    }
	    return result;
	}
	function submitFunction(obj, i) {
		document.getElementById('userCode').value=ReplaceSeperator(editor.getValue());
		if(<%=request.getAttribute("correct") == null||(boolean)request.getAttribute("correct") == false%>){
			obj.action = "EvaluateAnswer?kind=1";
		}else{
			obj.action = "EvaluateAnswer?kind=2";
		}
		obj.submit(); 

	}
</script>
	
	<script>
  var editor = ace.edit("editor");
  /* editor.setTheme("ace/theme/tomorrow_night_blue"); */
  editor.setTheme("ace/theme/github");
  editor.getSession().setMode('ace/mode/<%=languagString%>');
		editor.setFontSize('16px');
	</script>

	<script src="js/highlight.pack.js"></script>
	<script src="js/jquery-2.1.4.min.js"></script>
	<script src="js/script.js"></script>
	<script>
		hljs.initHighlightingOnLoad();
	</script>
</body>
</html>