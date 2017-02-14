package test.workflow.account;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cmsz.autoflow.engine.AutoEngine;
import cmsz.autoflow.engine.entity.Flow;
import cmsz.autoflow.engine.helper.StreamHelper;

/**
 * 
 * 物联网业务流程联调测试
 * 与cmup-clearing-pro 模块DubboServer.java
 *
 * @author yaoQingCan
 * @version 创建时间：2016年2月4日 上午9:26:05
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring.xml" })
public class IotFlowProcessTest {
	
	@Autowired
	@Qualifier("engine")
	AutoEngine engine;
	
	@Test
	public void testIotAccountProcess() throws IOException{
		

		String processFileName = "clearing_iot.xml";
	    InputStream in = StreamHelper.openStream("autoflow/"+processFileName);
	    String result = engine.process().deploy(in);
	    System.out.println("流程["+result+"]已部署成功！");	
	
		
	    Map<String, Object> CommonMap = new HashMap<>();
	    Map<String, Object> variableMap = new HashMap<>();
	    CommonMap.put("settleDate", "20150809");
	    CommonMap.put("busiLine", "0070");
	    CommonMap.put("province", "");
	    variableMap.put("Common", CommonMap);
	  
	    Flow flow = engine.startInstanceById("Proc_Clearing_IOT", variableMap);
	    
	    System.out.println("------------流程成功启动.."+",流程状态:"+flow.getState()+",流程实例id:"+flow.getId()+",ProcessId:"+flow.getProcessId());
	    
	    System.in.read(); //很关键，不能注释掉。流程刚刚启动，还没有执行完成。

	}
	

	@Test
	public void testIotDaySettlementProcess() throws IOException{
		
		/*String processFileName = "day_settlement_iot.xml";
	    InputStream in = StreamHelper.openStream("autoflow/"+processFileName);
	    String result = engine.process().deploy(in);
	    System.out.println("流程["+result+"]已部署成功！");	*/
			
		
	    Map<String, Object> CommonMap = new HashMap<>();
	    Map<String, Object> variableMap = new HashMap<>();
	    CommonMap.put("settleDate", "20160209");
	    CommonMap.put("busiLine", "0070");
	    CommonMap.put("province", "");
	    variableMap.put("Common", CommonMap);
	  
	    Flow flow = engine.startInstanceById("Proc_Day_Settlement_IOT", variableMap);
	    
	    System.out.println("------------流程成功启动.."+",流程状态:"+flow.getState()+",流程实例id:"+flow.getId()+",ProcessId:"+flow.getProcessId());
	    
	    System.in.read(); //很关键，不能注释掉。流程刚刚启动，还没有执行完成。

	}
	
	


}
