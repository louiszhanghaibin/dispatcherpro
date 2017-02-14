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
 * 在线计费对账流程联调测试
 * 与clearing-all 模块DubboServer.java
 *
 * @author yaoQingCan
 * @version 创建时间：2016年2月4日 上午9:26:05
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring.xml" })
public class ZxjfAccountProcessTest {
	
	@Autowired
	@Qualifier("engine")
	AutoEngine engine;
	
	@Test
	public void testZxjfAccountProcess() throws IOException{
			
		String processFileName = "account_zxjf.xml";
	    InputStream in = StreamHelper.openStream("autoflow/"+processFileName);
	    String result = engine.process().deploy(in);
	    System.out.println("流程["+result+"]已部署成功！");	
			
		
	    Map<String, Object> CommonMap = new HashMap<>();
	    Map<String, Object> variableMap = new HashMap<>();
	    CommonMap.put("settleDate", "20160303");
	    CommonMap.put("province", "991");
	    CommonMap.put("busiLine", "0068");
	    variableMap.put("Common", CommonMap);
	  
	    Flow flow = engine.startInstanceById("Proc_Account_ZXJF", variableMap);
	    
	    System.out.println("------------流程成功启动.."+",流程状态:"+flow.getState()+",流程实例id:"+flow.getId()+",ProcessId:"+flow.getProcessId());
	    System.in.read();

	}
	


}
