
package com.hhly.usercore.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.hhly.skeleton.base.constants.SymbolConstants;


/**
 * @desc    
 * @author  cheng chen
 * @date    2018年7月26日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Configuration
public class RabbitMqConfig {
	
	/**
	 * ip地址
	 */
	@Value("${spring.rabbitmq.host}")
	private String host;
	
	/**
	 * 端口
	 */
	@Value("${spring.rabbitmq.port}")
	private Integer port;
	
	/**
	 * 账号
	 */
	@Value("${spring.rabbitmq.user-name}")
	private String userName;
	
	/**
	 * 密码
	 */
	@Value("${spring.rabbitmq.password}")
	private String password;
	
	/**
	 * 虚拟ip地址
	 */
	@Value("${spring.rabbitmq.virtual-host}")	
	private String virtualHost;
	
	@Bean
	public ThreadPoolTaskExecutor threadFactory(){
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(10);
		threadPoolTaskExecutor.setMaxPoolSize(50);
		threadPoolTaskExecutor.setQueueCapacity(3000);
		threadPoolTaskExecutor.setKeepAliveSeconds(300);
		threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		return threadPoolTaskExecutor;
	}
	
    // 构建mq实例工厂
    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(host + SymbolConstants.COLON + port);
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);
//        connectionFactory.setPublisherConfirms(publisherConfirms);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setConnectionThreadFactory(threadFactory());
        return connectionFactory;
    }
    
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RabbitTemplate rabbitTemplate(){
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
//        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }  
}
