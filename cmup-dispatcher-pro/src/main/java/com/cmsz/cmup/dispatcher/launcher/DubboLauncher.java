package com.cmsz.cmup.dispatcher.launcher;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.rpc.RpcException;
import com.cmsz.cmup.commons.logging.alarm.AlarmLogHandler;
import com.cmsz.cmup.commons.logging.system.SystemLogHandler;
import com.cmsz.cmup.commons.utils.SpringUtil;
import com.cmsz.cmup.frame.constant.FrameConstant;
import com.cmsz.cmup.frame.constant.Result;
import com.cmsz.cmup.frame.dubbo.service.DubboService;
import com.cmsz.cmup.frame.model.ReturnResult;

import cmsz.autoflow.engine.Constant;
import cmsz.autoflow.engine.DelegateTask;
import cmsz.autoflow.engine.Launcher;
import cmsz.autoflow.engine.helper.JsonHelper;

/**
 * @author hpcmsz DubboLauncher 默认的defaultDubboLauncher bean 与AutoFlow流程引擎集成。
 *         流程任务节点配置dubbo调用属性时，流程引擎默认调用defaultDubboLauncher，
 *         此类根据dubboId查找spring中的dubbo client， 然后执行dubboService.doDubboService()
 *         方法，完成远程调用。
 * 
 */

@Component("defaultDubboLauncher")
public class DubboLauncher implements Launcher {
	private static AlarmLogHandler alarmLogger = AlarmLogHandler.getLogger(DubboLauncher.class);
	private static SystemLogHandler sysLogger = SystemLogHandler.getLogger(DubboLauncher.class);

	@Autowired
	@Qualifier("springUtil")
	private SpringUtil springUtil;

	@Override
	public String launch(DelegateTask dtask) {

		ReturnResult ret = null;
		String dubboId = "";
		Map<String, String> comMap = new HashMap<>();
		try {
			dubboId = dtask.getDubboId();
			Map<String, Object> varMap = JsonHelper.fromJson(dtask.getVariables(), Map.class);
			comMap.putAll((Map<String, String>) varMap.get("Common"));
			varMap.remove("Common");
			comMap.put(FrameConstant.SERVICENAME, dtask.getComponentId());

			// 增加 flowId, flowName, taskId, taskName 参数
			comMap.put("flowId", dtask.getFlowId());
			comMap.put("flowName", dtask.getFlowName());
			comMap.put("taskId", dtask.getId());
			comMap.put("taskName", dtask.getName());

			if (StringUtils.isEmpty(dubboId)) {
				String message = "默认DubboLauncher执行launch方法时出错，DelegateTask的dubboId字段不能为空！";
				alarmLogger.error(message, comMap);
				sysLogger.error(message);
				return Constant.State.ERROR;
			}

			DubboService dubboService = (DubboService) springUtil.getBean(dubboId);
			try {
				ret = dubboService.doDubboService(comMap);
			} catch (NullPointerException e) {
				String message = "执行默认DubboLauncher.dubboService时发生异常，可能是流程依赖的dubbo服务[" + dubboId + "]没有就绪,请检查";
				alarmLogger.error(message, comMap, e);
				sysLogger.error(message, comMap, e);
				return Constant.State.ERROR;
			}
		} catch (NoSuchBeanDefinitionException e) {
			StringBuilder messageBuilder = new StringBuilder("默认DubboLauncher执行launch方法时出错，框架spring容器中没有查找到id为[");
			messageBuilder.append(dubboId);
			messageBuilder.append("]的dubbo 接口 bean!");
			String message = messageBuilder.toString();
			alarmLogger.error(message, comMap, e);
			sysLogger.error(message, comMap, e);
			return Constant.State.ERROR;
		} catch (ClassCastException e) {
			StringBuilder messageBuilder = new StringBuilder("默认DubboLauncher执行launch方法时出错，框架spring容器中子系统dubbo接口bean[");
			messageBuilder.append(dubboId);
			messageBuilder.append("]没有实现接口com.cmsz.cmup.frame.dubbo.service.DubboService");
			String message = messageBuilder.toString();
			alarmLogger.error(message, comMap, e);
			sysLogger.error(message, comMap, e);
			return Constant.State.ERROR;
		} catch (BeanCreationException e) {
			String exceptionMessage = e.getMessage();
			String message = null;
			if (exceptionMessage.contains("No provider available for")) {
				message = "执行默认DubboLauncher.dubboService时发生异常，可能是流程依赖的dubbo服务[" + dubboId + "]没有就绪，请检查.更多信息请查看堆栈";
			} else {
				message = "执行默认DubboLauncher.dubboService时发生异常：" + e.getMessage();
			}
			alarmLogger.error(message, comMap, e);
			sysLogger.error(message, comMap, e);
			return Constant.State.ERROR;
		} catch (RpcException e) {
			String message = "执行默认DubboLauncher.dubboService时发生RpcException，可能是dubbo服务[" + dubboId + "]已经停止了";
			alarmLogger.error(message, comMap, e);
			sysLogger.error(message, comMap, e);
			return Constant.State.ERROR;
		} catch (Exception e) {
			String message = "执行默认DubboLauncher.dubboService时捕获到异常:" + e.getMessage();
			alarmLogger.error(message, comMap, e);
			sysLogger.error(message, comMap, e);
			return Constant.State.EXCEPTION;
		}

		// 更新参数
		Map<String, Object> tmap = new HashMap<String, Object>();
		// 将执行任务的IP, 以及返回description 写入到 返回的Map参数中
		if (StringUtils.isNotEmpty(ret.getIp()) && StringUtils.isNotBlank(ret.getIp())) {
			tmap.put(Constant.ARGS_R_NODE, ret.getIp());
		}
		if (StringUtils.isNotEmpty(ret.getDescription()) && StringUtils.isNotBlank(ret.getDescription())) {
			String message = ret.getDescription();
			if (message.length() >= 128) {
				message = message.substring(0, 127);
			}
			tmap.put(Constant.ARGS_R_MESSAGE, message);
		}
		if (ret.getVariableMap() != null) {
			Map<String, String> vMap = ret.getVariableMap();
			Map<String, Object> map = new HashMap<String, Object>();
			for (String ke : vMap.keySet()) {
				map.put(ke, vMap.get(ke));
			}
			tmap.putAll(map);
		}
		dtask.updateVariables(tmap);

		if (Result.ERROR.equals(ret.getResult())) {
			return Constant.State.ERROR;
		} else if (Result.FAILED.equals(ret.getResult())) {
			return Constant.State.FAILED;
		} else {
			return Constant.State.SUCCESS;
		}
	}
}