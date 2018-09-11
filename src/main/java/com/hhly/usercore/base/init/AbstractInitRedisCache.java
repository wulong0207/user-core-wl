package com.hhly.usercore.base.init;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * @version 1.0
 * @auth chenkangning
 * @date 2017/3/13
 * @desc 项目启动时需要初始化进redis里面的信息
 * @compay 益彩网络科技有限公司
 */
@Service
public abstract class AbstractInitRedisCache {

    private static final Logger logger = Logger.getLogger(AbstractInitRedisCache.class);

    /**
     * 初始化总入口
     */
    @PostConstruct
    public void init() {
        Long startTime = System.currentTimeMillis();

        innerMethod();

        logger.info("InitRedisCache init end >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> totalTime : " + proceeTimeToSecond(startTime));
    }


    private void innerMethod() {
        Long startTime = System.currentTimeMillis();

        initParam();

        logger.info("InitRedisCache innerMethod end >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> totalTime : " + proceeTimeToSecond(startTime));
    }

    /**
     * 具体实现方法
     */
    abstract void initParam();


    /**
     * 返回计算总耗时
     *
     * @param startTime 开始时间戳
     * @return long 总共耗时秒数
     */
    private Long proceeTimeToSecond(Long startTime) {
        Long totalTime = System.currentTimeMillis() - startTime;
        return new Date(totalTime).getTime();

    }

}
