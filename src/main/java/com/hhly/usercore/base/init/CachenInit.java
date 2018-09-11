package com.hhly.usercore.base.init;

import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.pay.bo.PayBankSegmentBO;
import com.hhly.skeleton.user.bo.KeywordBO;
import com.hhly.usercore.persistence.dic.dao.DicDataDetailDaoMapper;
import com.hhly.usercore.persistence.dic.dao.KeywordMapper;
import com.hhly.usercore.persistence.pay.dao.PayBankSegmentMapper;
import com.hhly.usercore.base.utils.RedisUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @version 1.0
 * @auth chenkangning
 * @date 2017/3/13
 * @desc 初始加载缓存
 * @compay 益彩网络科技有限公司
 */
@Service
public class CachenInit extends AbstractInitRedisCache {

    private static final Logger logger = Logger.getLogger(CachenInit.class);

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private PayBankSegmentMapper bankSegmentMapper;

    @Resource
    private KeywordMapper keywordMapper;

    @Resource
    private DicDataDetailDaoMapper dicDataDetailDaoMapper;


    @Override
    void initParam() {
        try {
            //银行卡号段
            initPayBankSegmentSimple();
            initPayBankSegment();
            // 新增缓存-敏感信息
            initKeyWord();
            getMobileNumCollection();
        } catch (Exception e) {
            logger.error(ResultBO.getMsg(MessageCodeConstants.CACHE_INIT_ERROR_SYS, "CachenInit.initParam"), e);
        }
    }

    /**
     * 银行卡号码段信息
     */
    private void initPayBankSegmentSimple() {
        List<PayBankSegmentBO> payBankSegmentBOList = bankSegmentMapper.selectGroup();
        redisUtil.addObj(CacheConstants.PAY_BANK_SEGMENTBO_LIST_KEY, payBankSegmentBOList, null);
    }

    /**
     * 银行卡号码段信息所有字段
     */
    private void initPayBankSegment() {
        List<PayBankSegmentBO> list = bankSegmentMapper.getList();
        redisUtil.addObj(CacheConstants.P_CORE_PAY_BANK_CARD_SEGMENT_LIST, list, null);
    }

    /**
     * 敏感词信息
     */
    private void initKeyWord() {
        List<KeywordBO> keywordBOs = keywordMapper.queryKeywordInfo();
        redisUtil.addObj(CacheConstants.C_CORE_ACCOUNT_KEYWORD, keywordBOs, null);
    }

    /**
     * 初始化号码段
     * @return
     */
    @SuppressWarnings("unchecked")
	public List<String> getMobileNumCollection() {
        List<String> list = dicDataDetailDaoMapper.findMobileNumLimitList();
        if(!ObjectUtil.isBlank(list))
            redisUtil.addObj(CacheConstants.C_CORE_MEMBER_MOBILE_NUM_SEGMENT, list, null);
        return list;
    }
}
