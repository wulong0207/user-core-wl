
package com.hhly.usercore.agent.service;

import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hhly.skeleton.base.util.EncryptUtil;
import com.hhly.skeleton.base.util.ExcelUtil;
import com.hhly.skeleton.user.bo.UserInfoBO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年3月10日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)    
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AgentRegisterServiceTest {

	@Test
	public void testValidate() {
		fail("Not yet implemented");
	}

	@Test
	public void testRegister() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testMoveData(){
		String agent = "INSERT INTO `agent_info` (`user_id`, `agent_code`,`parent_agent_id`,`agent_level`, `agent_status`, `agent_time`, `update_time`) VALUES ((select id from m_user_info where cus_mobile = '{0}'), '{1}', null, '1', '1', now(), now());";
		
		String rebate = "INSERT INTO `agent_rebate` (`agent_id`, `add_time`, `default_flag`, `create_by`, `create_time`) VALUES ((select id from agent_info where user_id = (select id from m_user_info where cus_mobile = '{0}')), now(), '0', 'system', now());";
		
		String config = "INSERT INTO `agent_rebate_config` (`rebate_id`, `direct_min_money`, `direct_max_money`, `direct_ratio`, `agent_min_money`, `agent_max_money`, `agent_ratio`, `create_by`, `create_time`) "
				+ "(select (select id from agent_rebate where agent_id = (select id from agent_info where user_id = (select id from m_user_info where cus_mobile = '{0}'))) rebate_id,b.direct_min_money, b.direct_max_money, b.direct_ratio, b.agent_min_money, b.agent_max_money, b.agent_ratio, 'system', now() from agent_rebate_config b left join agent_rebate a on a.id = b.rebate_id where a.default_flag = 1);";
		
		String updateCode = "update m_user_info set agent_code = (select agent_code from agent_info where user_id = '{0}') where user_type = 1 and agent_code = '{1}';";
		
		List<UserInfoBO> bolist = new ArrayList<UserInfoBO>();
		List<List<String>> list = ExcelUtil.readExcel2007("D://agent//2018-06-13_createAgent.xlsx");
		
		for (int i = 0; i < list.size(); i++) {
			UserInfoBO bo = new UserInfoBO();
			List<String> childList = list.get(i);
//			bo.setId(new Double(childList.get(0)).intValue());
			bo.setMobile(childList.get(0));
//			String agentCode = "";
//			if(i != 0){
//				agentCode = new Double(childList.get(1)).intValue() + "";
//			}else{
//				agentCode = childList.get(1);
//			}
//			bo.setAgentCode(agentCode);
//			bo.setMobile(childList.get(1));
			bolist.add(bo);
		}
		
		StringBuffer sb = new StringBuffer();
		for (UserInfoBO bo : bolist) {
			sb.append(agent.replace("{0}", bo.getMobile().toString()).replace("{1}", EncryptUtil.getRandomString(8)) + "\r\n")
			.append(rebate.replace("{0}", bo.getMobile().toString())+ "\r\n")
			.append(config.replace("{0}", bo.getMobile().toString()) + "\r\n");
//			.append(updateCode.replace("{0}", bo.getId().toString()).replace("{1}", bo.getAgentCode()) + "\r\n");
		}
		
		createFile(sb.toString(), "D://agent//", "2018-06-13_agent.sql");
	}
	
    public void createFile(String content, 
            String outPutPath, String fileName) {
        BufferedWriter csvFileOutputStream = null;
        try {
            File file = new File(outPutPath);
            if (!file.exists()) {
                file.mkdir();
            }
            // 定义文件名格式并创建
            File csvFile = new File(outPutPath + fileName);
            csvFile.createNewFile();
            System.out.println("File：" + csvFile);
            // UTF-8使正确读取分隔符","
            csvFileOutputStream = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(csvFile)),1024);
            System.out.println("csvFileOutputStream：" + csvFileOutputStream);
            
            csvFileOutputStream.write(content);
 
            csvFileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                csvFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }		
}
