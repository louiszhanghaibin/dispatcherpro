package com.cmsz.cmup.dispatcher.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.cmsz.cmup.commons.logging.alarm.AlarmLogHandler;
import com.cmsz.cmup.commons.logging.system.SystemLogHandler;
import com.cmsz.cmup.commons.utils.constant.WorkFlowKeyConstant;
import com.cmsz.cmup.frame.constant.Result;
import com.cmsz.cmup.frame.model.ReturnResult;

import cmsz.autoflow.engine.AutoEngine;
import cmsz.autoflow.engine.entity.Flow;
import cmsz.autoflow.engine.helper.StreamHelper;

/**
 *
 * 流程Controller，用于接收外部系统流程操作的Http需求，请求有两种:
 * <p>启动流程请求路径（“http://部署地址/startProcess”）参数:</p>
 * 		processId 流程Id，必须参数、settleDate 账期日(可为空)、province 省编码(可为空)、busiLine 业务线编码(可为空)
 * <p>部署流程请求路径（“http://部署地址/deployProcess”）参数:fileName，流程定义文件名，需要放到dispatcher项目的“src/main/resources/autoflow”目录下
 * <p>重做流程请求路径（“http://部署地址/redoFlow”）参数:flowId，流程实例id
 * @author JinChao
 * 
 * @date 2015年12月8日 下午4:11:23
 *
 */

@Controller
public class AutoFlowController {
	private static SystemLogHandler sysLogger = SystemLogHandler.getLogger(AutoFlowController.class);
	private static AlarmLogHandler alarmLogger = AlarmLogHandler.getLogger(AutoFlowController.class);
	@Autowired
	private AutoEngine engine = null;
	
	/**
	 * 启动流程,processId为http请求中的必须参数
	 * 
	 * @param processId 流程Id，必须参数
	 * @param settleDate 账期日
	 * @param province 省编码
	 * @param busiLine 业务线编码
	 * @param model SpringMVC自带Model参数，用于回传请求结果给请求端
	 * @return 返回JSON字符串给客户端，Json字符串包含两个字段：1.result:"SUCCESS"、"FAILED",2.description:详细的描述信息(流程启动成功的情况下，此字段值为流程实例id)
	 */
	@RequestMapping(value="/startProcess")
	public String startProcess(
			@RequestParam(value ="processId")String processId,
			@RequestParam(value ="settleDate",required=false)String settleDate,
			@RequestParam(value ="province",required=false)String province,
			@RequestParam(value ="busiLine",required=false)String busiLine,
			Model model){
		
		//处理为null的参数，否则流程引擎会抛异常
		settleDate = (settleDate == null)? "" : settleDate;
		province = (province == null)? "" : province;
		busiLine = (busiLine == null)? "" : busiLine;
		
		//组装工作流必须的参数Map
		Map<String, Object> variableMap = new HashMap<>();
		Map<String, String> commonMap = new HashMap<>();
		commonMap.put(WorkFlowKeyConstant.settleDate, settleDate);
		commonMap.put(WorkFlowKeyConstant.province, province);
		commonMap.put(WorkFlowKeyConstant.busiLine, busiLine);
	    variableMap.put(WorkFlowKeyConstant.Common, commonMap);
		
		//收到启动流程请求，打印请求参数日志
		StringBuilder logMessageBuilder = new StringBuilder("dispatcher controller收到http启动流程请求，参数：[").append("processId:")
				.append(processId).append(",settleDate:").append(settleDate).append(",province:")
				.append(province).append(",busiLine:").append(busiLine).append("]");
		sysLogger.info(logMessageBuilder.toString(),commonMap);

		//controller返回的页面路径
		String returnPage = "result.jsp";
		ReturnResult returnResult = new ReturnResult();
		
		//验证processId是否为空
		if (StringUtils.isEmpty(processId)) {
			String result = "启动流程失败，processId不能为空";
			returnResult.setResult(Result.FAILED);
			returnResult.setDescription(result);
			
			//返回的Json字符串
			model.addAttribute("result",JSON.toJSONString(returnResult));
			sysLogger.error(result,commonMap);
			alarmLogger.error(result,commonMap);
			return returnPage;
		}else {
			//验证参数是否合法，不能含有特殊字符，不能有sql关键字
			if (!validateParameter(processId) || !validateParameter(settleDate) || !validateParameter(province)
			        || !validateParameter(busiLine)) {
				String result = "启动流程失败，参数中不能含有特殊字符及sql关键字";
				returnResult.setResult(Result.FAILED);
				returnResult.setDescription(result);
				
				//返回的Json字符串
				model.addAttribute("result",JSON.toJSONString(returnResult));
				sysLogger.error(result,commonMap);
				alarmLogger.error(result,commonMap);
				return returnPage;
			}
		}
	    
	    //启动流程
		Flow flow = engine.startInstanceById(processId, variableMap);
		
		//判断流程启动结果
		String result = null;
		if (flow==null) {
			result = new StringBuilder("dispatcher controller流程启动失败，流程[").append(processId).append("]不存在，请检查该流程是否已部署").toString();
			sysLogger.error(result,commonMap);
			alarmLogger.error(result,commonMap);
			returnResult.setResult(Result.FAILED);
		}else {
			result = flow.getId();
			returnResult.setResult(Result.SUCCESS);
			sysLogger.info(result,commonMap);
		}
		returnResult.setDescription(result);
		
		//返回的Json字符串
		model.addAttribute("result",JSON.toJSONString(returnResult));
		
		//跳转到执行结果页面
		return returnPage;
	}
	
	/**
	 * 部署流程,fileName为流程文件的名字，需要放到cmup-dispatcher项目的“src/main/resources/autoflow”目录下
	 * 
	 * @param fileName 流程文件的名字，需要放到cmup-dispatcher项目的“src/main/resources/autoflow”目录下
	 * @param model SpringMVC自带Model参数，用于回传请求结果给请求端
	 * @return 返回JSON字符串给客户端，Json字符串包含两个字段：1.result:"SUCCESS"、"FAILED",2.description:详细的描述信息
	 */
	@RequestMapping(value="/deployProcess")
	public String deployProcess(@RequestParam(value ="fileName")String fileName,Model model){
		
		//收到部署流程的请求，打印请求参数日志
		StringBuilder logMessageBuilder = new StringBuilder("dispatcher controller收到http部署流程请求，fileName：[").append(fileName).append("]");
		sysLogger.info(logMessageBuilder.toString());
		
		//controller返回的页面路径
		String returnPage = "result.jsp";
		ReturnResult returnResult = new ReturnResult();
		String result = null;
		
		//验证fileName是否为空
		if (StringUtils.isEmpty(fileName)) {
			returnResult.setResult(Result.FAILED);
			result = "部署流程失败，fileName不能为空";
			returnResult.setDescription(result);
			
			//返回的Json字符串
			model.addAttribute("result",JSON.toJSONString(returnResult));
			sysLogger.error(result);
			return returnPage;
		}else {
			if (!validateParameter(fileName)) {//验证是否含有非法字符或sql关键字
				returnResult.setResult(Result.FAILED);
				result = "部署流程失败，fileName值不合法";
				returnResult.setDescription(result);
				
				//返回的Json字符串
				model.addAttribute("result",JSON.toJSONString(returnResult));
				sysLogger.error(result);
				return returnPage;
			}
		}
	    
	   
	  	InputStream in = null;
	    try {
	    	//部署流程
			in = StreamHelper.openStream("autoflow/"+fileName);
		    result = engine.process().deploy(in);
		    result = "流程["+result+"]已部署成功！";
		    returnResult.setResult(Result.SUCCESS);
		    sysLogger.info(result);
		} catch (NullPointerException e) {
			result = "流程部署失败！可能是流程文件["+fileName+"]不存在，请检查改流程文件是否存在于autoflow目录下!";
			returnResult.setResult(Result.FAILED);
			sysLogger.error(result,e);
		} catch (Exception e) {
			result = "流程文件["+fileName+"]部署失败！部署时发生异常，请检查!"+e.getMessage();
			returnResult.setResult(Result.FAILED);
			sysLogger.error(result,e);
		} finally {
			try {
				if (in!=null) {
					in.close();
				}
			} catch (Exception e2) {
				String message = "部署流程时，关闭InputStream流发生异常";
				sysLogger.error(message,e2);
			}
		}
	    
	    returnResult.setDescription(result);
		//返回的Json字符串
		model.addAttribute("result",JSON.toJSONString(returnResult));
		
		//跳转到执行结果页面
		return "result.jsp";
	}
	
	
	/**
	 * 重做某一个流程，flowId：流程实例id，如:FLOW-20151013-177698
	 * 
	 * @param flowId 流程实例id，如:FLOW-20151013-177698
	 * @param model SpringMVC自带Model参数，用于回传请求结果给请求端
	 * @return 返回JSON字符串给客户端，Json字符串包含两个字段：1.result:"SUCCESS"、"FAILED",2.description:详细的描述信息(redoFlow成功的情况下，此字段值为redoFlow后返回的实例id)
	 */
	@RequestMapping(value="/redoFlow")
	public String redoFlow(@RequestParam(value ="flowId")String flowId,Model model){
		
		//收到部署流程的请求，打印请求参数日志
		StringBuilder logMessageBuilder = new StringBuilder("dispatcher controller收到http redoFlow请求，flowId：[").append(flowId).append("]");
		sysLogger.info(logMessageBuilder.toString());
	    
		//controller返回的页面路径
		String returnPage = "result.jsp";
		ReturnResult returnResult = new ReturnResult();
		String result = null;
		
		//验证fileName是否为空
		if (StringUtils.isEmpty(flowId)) {
			returnResult.setResult(Result.FAILED);
			result = "redoFlow失败，flowId不能为空";
			returnResult.setDescription(result);
			
			//返回的Json字符串
			model.addAttribute("result",JSON.toJSONString(returnResult));
			sysLogger.error(result);
			return returnPage;
		}else {
			if (!validateParameter(flowId)) {//验证是否含有非法字符或sql关键字
				returnResult.setResult(Result.FAILED);
				result = "redoFlow失败，flowId值不合法";
				returnResult.setDescription(result);
				
				//返回的Json字符串
				model.addAttribute("result",JSON.toJSONString(returnResult));
				sysLogger.error(result);
				return returnPage;
			}
		}
		
	    try {
	    	 //redoFlow
			Flow flow = engine.redoFlow(flowId);
			if (flow!=null) {
				String flowIdRedo = flow.getId();
			    result = "流程["+flowIdRedo+"]重做成功！";
			    returnResult.setResult(Result.SUCCESS);
			    sysLogger.info(result);
			    result = flowIdRedo;
			}else {
				result = "流程["+flowId+"]redoFlow失败，流程id不存在！";
			    returnResult.setResult(Result.FAILED);
			    sysLogger.error(result);
			}
		} catch (Exception e) {
			result = "流程["+flowId+"]redoFlow失败！redo时发生异常，请检查!"+e.getMessage();
			returnResult.setResult(Result.FAILED);
			sysLogger.error(result,e);
		} 
	    returnResult.setDescription(result);
		//返回的Json字符串
		model.addAttribute("result",JSON.toJSONString(returnResult));
		
		//跳转到执行结果页面
		return "result.jsp";
	}
	
	/**
	 * 验证参数合法性
	 * @param s
	 * @return
	 */
	private boolean validateParameter(String s){
		if (StringUtils.isEmpty(s)) {
			return true;
		}
		if (!validateSpecialChars(s)) {
			String message = "AutoFlowController处理请求时，参数不合法，不能含有特殊字符，问题参数:"+s;
			sysLogger.error(message);
			return false;
		}
		if (!validateSqlKeyword(s)) {
			String message = "AutoFlowController处理请求时，参数不合法，不能含有sql关键字，问题参数:"+s;
			sysLogger.error(message);
			return false;
		}else {
			return true;
		}
	}
	
	
	/**特殊字符正则表达式**/
	private static Pattern specialCharsPattern = Pattern.compile("^[a-zA-Z0-9_.\\-\\(\\)\u4e00-\u9fa5]+$");
	/**sql关键字正则表达式**/
	private static Pattern sqlPattern = Pattern.compile("(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)");
	/**
	 * 验证特殊字符,不含有特殊字符，则返回true
	 * @param s
	 * @return
	 */
	private boolean validateSpecialChars(String s){
		if(!specialCharsPattern.matcher(s).matches()){
			return false;
		}
		return true;
	}
	
	/**
	 * 验证sql关键字，不含有则返回true
	 * @param s
	 * @return
	 */
	private boolean validateSqlKeyword(String s){
		if(sqlPattern.matcher(s).find()) {  
	    	return false;
	    }
	    return true;
	}
}
