
package com.hhly.usercore.remote.exporter;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.caucho.HessianServiceExporter;

import com.hhly.skeleton.base.constants.SymbolConstants;
import com.hhly.skeleton.base.util.PropertyUtil;


public class LottoHessianServiceExporter extends HessianServiceExporter {
	
	private Logger log = LoggerFactory.getLogger(LottoHessianServiceExporter.class);
	
	// 设置权限校验值
	private static final String AUTH;
	
	static{
		//计算hessian的校验值
		String user = PropertyUtil.getPropertyValue(PropertyUtil.getEnvPropertiesName(),"hessian_username");
		String password = PropertyUtil.getPropertyValue(PropertyUtil.getEnvPropertiesName(),"hessian_password");
		String str = user + SymbolConstants.COLON +password;
		AUTH = "Basic " + new String(Base64.getEncoder().encode(str.getBytes()));
	}
	
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.debug("hessian get Authorization !");
		String auth = request.getHeader("Authorization") ;
		if(!Objects.equals(auth, AUTH)){
			log.info("Authorization fail");
			return  ;
		}
		super.handleRequest(request, response);
	}

}
