
package com.hhly.usercore.member.service;

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
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.hhly.skeleton.base.util.ExcelUtil;
import com.hhly.skeleton.user.bo.UserInfoBO;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年1月9日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)    
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
//defaultRollback = true说明: true ? 测试数据不会污染数据库 : 会真正添加到数据库当中
@TransactionConfiguration(transactionManager = "transactionManager" , defaultRollback = true) 
public class UserInfoServiceTest {
	
	@Test
	public void testUserBatchCreate(){
		String user = "INSERT INTO `m_user_info` (`account_id`, `account_name`, `cus_mobile`, `cus_mail`, `mobile_login`, `email_login`, `nick_name`, `account_password`, `password_grade`, `sex`, `actual_name`, `id_num`, `head_url`, `head_status`, `address`, `channel_id`, `regist_time`, `last_login_time`, `account_status`, `forbit_end_time`, `qq_open_id`, `sina_blog_open_id`, `baidu_open_id`, `wechat_open_id`, `alipay_open_id`, `jd_open_id`, `mobile_check`, `email_check`, `account_modify`, `modify_by`, `modify_time`, `update_time`, `create_time`, `remark`, `rcode`, `head_check`, `ip`, `user_pay_id`, `msg_mob`, `msg_site`, `msg_app`, `msg_wechat`, `mob_not_disturb`, `app_not_disturb`, `qq_name`, `wechat_name`, `sina_name`, `platform`, `channel_open_id`, `user_pay_cardcode`, `agent_code`, `user_type`) VALUES (NULL, '{0}', '{1}', NULL, '1', '0', '{2}', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '{3}', now(), NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '0', '0', NULL, NULL, NULL, NULL, NULL, NULL, '0', NULL, NULL, '1', '1', '1', '1', '23:00-08:00', '23:00-08:00', NULL, NULL, NULL, '2', NULL, NULL, NULL, '1');";

		String wallet = "INSERT INTO `m_user_wallet` (`user_id`, `total_cash_balance`, `winning_balance`, `top_80_balance`, `top_20_balance`, `eff_red_balance`, `ready_red_balance`, `status`, `update_time`, `create_time`, `display`, `version`) VALUES ((select id from m_user_info where cus_mobile = '{0}'), '0.00', '0.00', '0.00', '0.00', '0.00', '0.00', '1', now(), now(), '1', '1');";

		String modify = "INSERT INTO `m_user_modify_log` (`user_id`, `user_action`, `operation_status`, `user_ip`, `log_before`, `log_after`, `create_time`, `remark`, `province`) VALUES ((select id from m_user_info where cus_mobile = '{0}'),'1', '1', '10.41.3.50', NULL, '{1}', now(), '注册成功', NULL);";

//		String dicDetail = "INSERT INTO `dic_data_detail` (`dic_code`, `dic_data_name`, `dic_data_value`, `is_fixed`, `status`, `order_by`, `update_time`, `create_time`) VALUES ('9000', '{0}', (select id from m_user_info where cus_mobile = '{0}'), '1', '1', (select * from (select IFNULL(max(order_by),0) + 1 from dic_data_detail where dic_code = '9000') b), now(), now());";

//		String dicValueDetail = "INSERT INTO `dic_data_detail` (`dic_code`, `dic_data_name`, `dic_data_value`, `is_fixed`, `status`, `order_by`, `update_time`, `create_time`) VALUES ('9000', '{0}', {1}, '1', '1', (select * from (select IFNULL(max(order_by),0) + 1 from dic_data_detail where dic_code = '9000') b), now(), now());";

		
		List<UserInfoBO> bolist = new ArrayList<UserInfoBO>();
		List<List<String>> list = ExcelUtil.readExcel2007("D://agent//2018-06-13_user.xlsx");
		
		for (int i = 0; i < list.size(); i++) {
			UserInfoBO bo = new UserInfoBO();
			List<String> childList = list.get(i);
			bo.setMobile(childList.get(0));
			bo.setChannelId("359");
			bolist.add(bo);
		}
		
		
		StringBuffer st = new StringBuffer();
		for (UserInfoBO bo : bolist) {
			st.append(user.replace("{0}", bo.getMobile()).replace("{1}", bo.getMobile()).replace("{2}", bo.getMobile()).replace("{3}", bo.getChannelId()) + "\r\n")
			.append(wallet.replace("{0}", bo.getMobile()) + "\r\n")
			.append(modify.replace("{0}", bo.getMobile()).replace("{1}", bo.getMobile()) + "\r\n");
//			.append(dicDetail.replace("{0}", bo.getMobile()) + "\r\n");
			
//			st.append(dicValueDetail.replace("{0}", bo.getMobile()).replace("{1}", bo.getId().toString())+ "\r\n");
		}
		
		
		createFile(st.toString(), "D://agent//", "2018-06-13_user1.sql");
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
