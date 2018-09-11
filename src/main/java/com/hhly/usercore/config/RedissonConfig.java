
package com.hhly.usercore.config;

import javax.annotation.Resource;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @desc    
 * @author  cheng chen
 * @date    2018年8月3日
 * @company 益彩网络科技公司
 * @version 1.0
 */
@Configuration
public class RedissonConfig {
	
	@Resource
	private JedisConfigProperties jedisConfigProperties;
	
	private Config clusterServersConfig() {
		Config config = new Config();
		ClusterServersConfig clusterConfig = config.useClusterServers();
		clusterConfig.setScanInterval(300000);
		clusterConfig.setPassword(jedisConfigProperties.getPassword());
		clusterConfig.setConnectTimeout(jedisConfigProperties.getConnectionTimeout());
		clusterConfig.setTimeout(jedisConfigProperties.getSoTimeout());
		clusterConfig.setReconnectionTimeout(jedisConfigProperties.getSoTimeout());
		String[] serverArray = jedisConfigProperties.getNodes().split(",");
		for (String server : serverArray) {
			clusterConfig.addNodeAddress(server);
		}
		return config;
	}
	
	@Bean
	public RedissonClient redissonClient(){
		return Redisson.create(clusterServersConfig());
	}

}
