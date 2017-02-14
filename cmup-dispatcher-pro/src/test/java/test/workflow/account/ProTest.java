package test.workflow.account;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import cmsz.autoflow.engine.AutoEngine;
import cmsz.autoflow.engine.entity.Flow;
import cmsz.autoflow.engine.helper.StreamHelper;

public class ProTest {
	
	public static void main(String[] args) throws IOException{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring.xml");
		AutoEngine engine = (AutoEngine) context.getBean("engine");
		
		String processFileName = "pro_test.xml";
	    InputStream in = StreamHelper.openStream("autoflow/"+processFileName);
	    String result = engine.process().deploy(in);
	    System.out.println("流程["+result+"]已部署成功！");	
	    
	    Map<String, Object> CommonMap = new HashMap<String, Object>();
	    Map<String, Object> variableMap = new HashMap<String, Object>();
	    CommonMap.put("settleDate", "20160418");
	    CommonMap.put("province", "");
	    CommonMap.put("busiLine", "");
	    variableMap.put("Common", CommonMap);
	  
	    Flow flow = engine.startInstanceById("pro_test", variableMap);
	    
	    System.out.println("------------流程成功启动.."+",流程状态:"+flow.getState()+",流程实例id:"+flow.getId()+",ProcessId:"+flow.getProcessId());
	    System.in.read();
	}
}
