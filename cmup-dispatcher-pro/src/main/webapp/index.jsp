<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>cmup-dispatcher-jsp</title>
	<script type="text/javascript">
		function checkNull(id){
			var obj = document.getElementById(id);
			var value = obj.value;
			if(trim(value)==""){
				alert(id+"不能为空!");
				obj.focus();
				return false;
			}
			return true;
		}
		function trim(str){ //删除左右两端的空格
			return str.replace(/(^\s*)|(\s*$)/g, "");
		}
	</script>
</head>
<body>
	<h1>cmup-dispatcher</h1><hr>
	
	
	<h3>启动流程</h3>
	<form action="startProcess" method="post" onsubmit="return checkNull('processId')">
		<table>
			<tr>
				<td align="right">processId(流程id):<span style="color: red">*</span></td><td align="left"><input  id="processId" name="processId" value="Proc_miGuTest"></td>
			</tr>
			<tr>
				<td align="right">settleDate(账期日):</td><td align="left"><input name="settleDate" id="settleDate"></td>
			</tr>
			<tr>
				<td align="right">province(省代码):</td><td align="left"><input name="province" id="province"></td>
			</tr>
			<tr>
				<td align="right">busiLine(业务线):</td><td align="left"><input name="busiLine" id="busiLine"></td>
			</tr>
			<tr>
				<td align="right"></td><td align="left"><input type="submit" value="提交">&nbsp;<input type="reset" value="重置"></td>
			</tr>
		</table>
	</form>
	
	
	<br/><br/><br/>
	<h3>部署流程</h3>
	<form action="deployProcess" method="post" onsubmit="return checkNull('fileName')">
		<table>
			<tr>
				<td align="right">fileName(流程文件名):<span style="color: red">*</span></td><td align="left"><input id="fileName" name="fileName"  value="example_migu.xml"></td>
			</tr>
			<tr>
				<td align="right"></td><td align="left"><input type="submit" value="提交">&nbsp;<input type="reset" value="重置"></td>
			</tr>
		</table>
	</form>
	
	<br/><br/><br/>
	<h3>redoFlow</h3>
	<form action="redoFlow" method="post" onsubmit="return checkNull('flowId')">
		<table>
			<tr>
				<td align="right">flowId(流程实例id):<span style="color: red">*</span></td><td align="left"><input id="flowId" name="flowId"  value="" ></td>
			</tr>
			<tr>
				<td align="right"></td><td align="left"><input type="submit" value="提交">&nbsp;<input type="reset" value="重置"></td>
			</tr>
		</table>
	</form>
</body>
</html>