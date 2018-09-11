
package com.hhly.usercore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.caucho.HessianServiceExporter;

import com.hhly.usercore.remote.activity.service.IMemberActivityService;
import com.hhly.usercore.remote.exporter.LottoHessianServiceExporter;
import com.hhly.usercore.remote.member.service.IMemberBankcardService;
import com.hhly.usercore.remote.member.service.IMemberCouponService;
import com.hhly.usercore.remote.member.service.IMemberInfoService;
import com.hhly.usercore.remote.member.service.IMemberMessageService;
import com.hhly.usercore.remote.member.service.IMemberSecurityService;
import com.hhly.usercore.remote.member.service.IMemberWinningService;
import com.hhly.usercore.remote.member.service.ITransTakenService;
import com.hhly.usercore.remote.member.service.IVerifyCodeService;
import com.hhly.usercore.remote.passport.service.IMemberLoginService;
import com.hhly.usercore.remote.passport.service.IMemberRegisterService;
import com.hhly.usercore.remote.passport.service.IMemberRetrieveService;
import com.hhly.usercore.remote.passport.service.IThirdPartyLoginService;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年8月3日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Configuration
public class HessionServerConfig {
	
	@Autowired
	IMemberLoginService iMemberLoginService;
	
	@Autowired
	IThirdPartyLoginService iThirdPartyLoginService;
	
	@Autowired
	IMemberRegisterService iMemberRegisterService;
	
	@Autowired
	IMemberRetrieveService iMemberRetrieveService;
	
	@Autowired
	IVerifyCodeService iVerifyCodeService;
	
	@Autowired
	IMemberInfoService iMemberInfoService;
	
	@Autowired
	IMemberBankcardService iMemberBankcardService;
	
	@Autowired
	IMemberSecurityService iMemberSecurityService;
	
	@Autowired
	IMemberMessageService iMemberMessageService;
	
	@Autowired
	IMemberActivityService iMemberActivityService;
	
	@Autowired
	IMemberWinningService iMemberWinningService;
	
	@Autowired
	IMemberCouponService iMemberCouponService;
	
	@Autowired
	ITransTakenService transTakenService;

    @Bean(name = "/remote/iMemberLoginService")
    public HessianServiceExporter iMemberLoginService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iMemberLoginService);
        exporter.setServiceInterface(IMemberLoginService.class);
        return exporter;
    }
    
    @Bean(name = "/remote/iThirdPartyLoginService")
    public HessianServiceExporter iThirdPartyLoginService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iThirdPartyLoginService);
        exporter.setServiceInterface(IThirdPartyLoginService.class);
        return exporter;
    }
    
    @Bean(name = "/remote/iMemberRegisterService")
    public HessianServiceExporter iMemberRegisterService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iMemberRegisterService);
        exporter.setServiceInterface(IMemberRegisterService.class);
        return exporter;
    }
    
    @Bean(name = "/remote/iMemberRetrieveService")
    public HessianServiceExporter iMemberRetrieveService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iMemberRetrieveService);
        exporter.setServiceInterface(IMemberRetrieveService.class);
        return exporter;
    } 
    
    @Bean(name = "/remote/iVerifyCodeService")
    public HessianServiceExporter iVerifyCodeService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iVerifyCodeService);
        exporter.setServiceInterface(IVerifyCodeService.class);
        return exporter;
    }
    
    @Bean(name = "/remote/iMemberInfoService")
    public HessianServiceExporter iMemberInfoService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iMemberInfoService);
        exporter.setServiceInterface(IMemberInfoService.class);
        return exporter;
    }
    
    @Bean(name = "/remote/iMemberBankcardService")
    public HessianServiceExporter iMemberBankcardService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iMemberBankcardService);
        exporter.setServiceInterface(IMemberBankcardService.class);
        return exporter;
    }
    
    @Bean(name = "/remote/iMemberSecurityService")
    public HessianServiceExporter iMemberSecurityService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iMemberSecurityService);
        exporter.setServiceInterface(IMemberSecurityService.class);
        return exporter;
    }
    
    @Bean(name = "/remote//iMemberMessageService")
    public HessianServiceExporter iMemberMessageService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iMemberMessageService);
        exporter.setServiceInterface(IMemberMessageService.class);
        return exporter;
    }
    
    @Bean(name = "/remote//iMemberActivityService")
    public HessianServiceExporter iMemberActivityService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iMemberActivityService);
        exporter.setServiceInterface(IMemberActivityService.class);
        return exporter;
    }
    
    @Bean(name = "/remote//iMemberWinningService")
    public HessianServiceExporter iMemberWinningService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iMemberWinningService);
        exporter.setServiceInterface(IMemberWinningService.class);
        return exporter;
    }
    
    @Bean(name = "/remote//iMemberCouponService")
    public HessianServiceExporter iMemberCouponService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(iMemberCouponService);
        exporter.setServiceInterface(IMemberCouponService.class);
        return exporter;
    }
    
    @Bean(name = "/remote//transTakenService")
    public HessianServiceExporter transTakenService() {
    	LottoHessianServiceExporter exporter = new LottoHessianServiceExporter();
        exporter.setService(transTakenService);
        exporter.setServiceInterface(ITransTakenService.class);
        return exporter;
    }    
    
}
