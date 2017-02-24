package test.workflow;

import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cmsz.autoflow.engine.AutoEngine;
import cmsz.autoflow.engine.helper.StreamHelper;

/**
 * 部署流程
 *
 * @author JinChao
 * 
 * @date 2015年12月10日 下午4:03:00
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring.xml" })
public class DeployProcessTest {

	@Autowired
	@Qualifier("engine")
	AutoEngine engine;

	@Test
	public void testProcessDeploy() {

		String processFileName = "clearing_zxjf.xml";
		InputStream in = StreamHelper.openStream(processFileName);
		String result = engine.process().deploy(in);
		System.out.println("流程[" + result + "]已部署成功！");

	}

}
