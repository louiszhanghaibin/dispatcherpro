package test.workflow;

import java.io.IOException;
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

/**
 * 启动流程
 *
 * @author JinChao
 * 
 * @date 2015年12月10日 下午4:03:28
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring.xml" })
public class StartProcessTest {
	
	@Autowired
	@Qualifier("engine")
	AutoEngine engine;

	@Test
	public void testStartFlow() throws IOException {

	    Map<String, Object> CommonMap = new HashMap<>();
	    Map<String, Object> variableMap = new HashMap<>();
	    CommonMap.put("settleDate", "20151011");
	    CommonMap.put("province", "100");
	    CommonMap.put("busiLine", "0064");
	    variableMap.put("Common", CommonMap);
	    System.out.println("启动流程.....");
	    Flow flow = engine.startInstanceById("Proc_miGuTest", variableMap);
	    
	    System.out.println("------------流程成功启动.."+",流程状态:"+flow.getState()+",流程实例id:"+flow.getId()+",ProcessId:"+flow.getProcessId());
	    System.in.read();

	}
	
	@Test
	public void testStartScheduleFlow() throws IOException {
	    System.out.println("-----通过AF_schedule启动流程-----");
	    System.in.read();

	}

}
