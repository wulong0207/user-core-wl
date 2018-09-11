package com.hhly.usercore.remote.member.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hhly.skeleton.base.bo.PagingBO;
import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.common.LotteryEnum;
import com.hhly.skeleton.base.constants.CacheConstants;
import com.hhly.skeleton.base.constants.Constants;
import com.hhly.skeleton.base.constants.DrawLotteryConstant;
import com.hhly.skeleton.base.constants.MessageCodeConstants;
import com.hhly.skeleton.base.constants.SymbolConstants;
import com.hhly.skeleton.base.constants.UserConstants;
import com.hhly.skeleton.base.page.IPageService;
import com.hhly.skeleton.base.page.ISimplePage;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.base.util.StringUtil;
import com.hhly.skeleton.lotto.base.dic.bo.DicDataDetailBO;
import com.hhly.skeleton.lotto.base.order.vo.OrderInfoVO;
import com.hhly.skeleton.user.bo.UserInfoBO;
import com.hhly.skeleton.user.bo.UserMsgConfigBO;
import com.hhly.skeleton.user.bo.UserMsgInfoBO;
import com.hhly.skeleton.user.vo.PassportVO;
import com.hhly.skeleton.user.vo.UserMsgConfigVO;
import com.hhly.skeleton.user.vo.UserMsgInfoVO;
import com.hhly.usercore.base.utils.RedisUtil;
import com.hhly.usercore.base.utils.UserUtil;
import com.hhly.usercore.base.utils.ValidateUtil;
import com.hhly.usercore.persistence.member.dao.UserInfoDaoMapper;
import com.hhly.usercore.persistence.member.po.UserInfoPO;
import com.hhly.usercore.persistence.message.dao.UserMsgInfoDaoMapper;
import com.hhly.usercore.remote.member.service.IMemberMessageService;

/**
 * @author zhouyang
 * @version 1.0
 * @desc 用户消息中心接口实现类
 * @date 2017/11/8
 * @company 益彩网络科技公司
 */
@Service("iMemberMessageService")
public class MemberMessageServiceImpl implements IMemberMessageService {

    public static final Map<Integer, String> lotteryDescMap = new HashMap<>();

    public static final Map<Integer, String> switchTypeMap = new HashMap<>();

    public static final Map<String, String> switchTypeDescMap = new HashMap<>();

    @Autowired
    private IPageService pageService;

    @Autowired
    private UserMsgInfoDaoMapper userMsgInfoDaoMapper;

    @Autowired
    private UserInfoDaoMapper userInfoDaoMapper;

    @Autowired
    private RedisUtil redisUtil;
	
	@Autowired
	private UserUtil userUtil;

    static {
        switchTypeDescMap.put("1", "开奖后第一时间知晓开奖结果");
        switchTypeDescMap.put("2", "投注后方案状态或中奖通知提醒");
        switchTypeDescMap.put("3", "个人用户中心信息变化及异常提醒");
        switchTypeDescMap.put("4", "设置闹钟定时提醒，以免错过彩期");
        switchTypeDescMap.put("5", "最新活动、资讯和产品上线等提醒");
        switchTypeDescMap.put("6", "最新活动、资讯和产品上线等提醒");
    }

    static {
        switchTypeMap.put(1, "中奖通知");
        switchTypeMap.put(2, "优惠活动");
        switchTypeMap.put(3, "网站重要通知");
        switchTypeMap.put(4, "抄单");
        switchTypeMap.put(5, "交易提醒");
        switchTypeMap.put(6, "账户提醒");
    }

    static {
        lotteryDescMap.put(LotteryEnum.Lottery.SSQ.getName(), "每周二、四、日 21:15开奖");
        lotteryDescMap.put(LotteryEnum.Lottery.DLT.getName(), "每周一、三、六 20:30开奖");
        lotteryDescMap.put(LotteryEnum.Lottery.QLC.getName(), "每周一、三、五 21:15开奖");
        lotteryDescMap.put(LotteryEnum.Lottery.QXC.getName(), "每周二、五、日 20:30开奖");
        lotteryDescMap.put(LotteryEnum.Lottery.F3D.getName(), "每日 21:15开奖");
        lotteryDescMap.put(LotteryEnum.Lottery.PL3.getName(), "每天 20:30开奖");
        lotteryDescMap.put(LotteryEnum.Lottery.PL5.getName(), "每天 20:30开奖");
        lotteryDescMap.put(LotteryEnum.Lottery.SFC.getName(), "不定期开奖，最高奖金500万");
        lotteryDescMap.put(LotteryEnum.Lottery.ZC_NINE.getName(), "不定期开奖，最高奖金500万");
        lotteryDescMap.put(LotteryEnum.Lottery.JQ4.getName(), "不定期开奖，最高奖金500万");
        lotteryDescMap.put(LotteryEnum.Lottery.ZC6.getName(), "不定期开奖，最高奖金500万");
        lotteryDescMap.put(LotteryEnum.Lottery.FB.getName(), "猜足球比赛，天天赢大奖");
        lotteryDescMap.put(LotteryEnum.Lottery.BB.getName(), "猜篮球比赛，返奖率69%");
        lotteryDescMap.put(LotteryEnum.Lottery.BJDC.getName(), "猜中一场也有奖，全天销售");
        lotteryDescMap.put(LotteryEnum.Lottery.SFGG.getName(), "永无和局，跨界更精彩");
    }

    @Override
    public ResultBO<?> findMsgMenu(UserMsgInfoVO vo) {
        /*List<UserMsgInfoBO> menu = userMsgInfoDaoMapper.findMsgInfoMenu(vo);
        if (!ObjectUtil.isBlank(menu)) {
            return ResultBO.ok(menu.get(0));
        }*/
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        vo.setUserId(userInfoBO.getId());
        List<DicDataDetailBO> msgType= userMsgInfoDaoMapper.findMsgDic("2102");
        List<UserMsgInfoBO> list = new ArrayList<>();
        Integer initNum = 1;
        if (vo.getSendType().equals(UserConstants.MsgSendTypeEnum.SITE.getValue())) {
            initNum = 0;
        }
        for (int i = initNum; i<msgType.size(); i++) {
            UserMsgInfoBO bo = new UserMsgInfoBO();
            vo.setMsgType(Short.valueOf(msgType.get(i).getDicDataValue()));
            int msgInfoCount = userMsgInfoDaoMapper.findMsgInfoCount(vo);
            bo.setMsgCount(msgInfoCount);
            bo.setMsgType(Short.valueOf(msgType.get(i).getDicDataValue()));
            bo.setMsgName(msgType.get(i).getDicDataName());
            list.add(bo);
        }
        return ResultBO.ok(list);
    }

    @Override
    public ResultBO<?> findList(UserMsgInfoVO vo) {
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        vo.setUserId(userInfoBO.getId());
        PagingBO<UserMsgInfoBO> msgInfo = findMsgInfo(vo);
        return ResultBO.ok(msgInfo);
    }

    public PagingBO<UserMsgInfoBO> findMsgInfo(final UserMsgInfoVO vo) {
        return pageService.getPageData(vo, new ISimplePage<UserMsgInfoBO>() {

            @Override
            public int getTotal() {
                return userMsgInfoDaoMapper.findMsgInfoTotal(vo);
            }

            @Override
            public List<UserMsgInfoBO> getData() {
                List<DicDataDetailBO> dicList = userMsgInfoDaoMapper.findMsgDic("2102");
                Map<String, String> msgNameMap = new HashMap<>();
                for (DicDataDetailBO detailBO : dicList) {
                    msgNameMap.put(detailBO.getDicDataValue(), detailBO.getDicDataName());
                }
                List<UserMsgInfoBO> list = userMsgInfoDaoMapper.findMsgInfo(vo);
                for (UserMsgInfoBO msgInfoBO : list) {
                    if (msgInfoBO.getMsgType().equals((short)2) ||
                            msgInfoBO.getMsgType().equals((short)4) ||
                            msgInfoBO.getMsgType().equals((short)5)) {
                        msgInfoBO.setMsgName(msgNameMap.get("2"));
                    } else if (msgInfoBO.getMsgType().equals((short)3) || msgInfoBO.getMsgType().equals((short)8)) {
                        msgInfoBO.setMsgName(msgNameMap.get("3"));
                    } else {
                        msgInfoBO.setMsgName(msgNameMap.get(msgInfoBO.getMsgType().toString()));
                    }
                    if (!ObjectUtil.isBlank(msgInfoBO.getMsgDesc())) {
                        Map<String,String> descMap = getDescMap(msgInfoBO.getMsgDesc());
                        msgInfoBO.setActivityUrl(descMap.get("activityUrl"));
                        if (!ObjectUtil.isBlank(descMap.get("toBuyLotteryCode"))) {
                            msgInfoBO.setToBuyLotteryCode(Integer.valueOf(descMap.get("toBuyLotteryCode")));
                        }
                        if (!ObjectUtil.isBlank(descMap.get("lotteryCode"))) {
                            msgInfoBO.setLotteryCode(Integer.valueOf(descMap.get("lotteryCode")));
                        }
                        //toBuyLotteryCode位15跳开奖公告，将开奖公告彩种简称返回，供开奖公告使用
						if (msgInfoBO.getToBuyLotteryCode() != null && msgInfoBO.getToBuyLotteryCode().intValue() == Constants.NUM_15
								&& msgInfoBO.getLotteryCode() != null) {
                        	msgInfoBO.setLotteryKey(DrawLotteryConstant.getLotteryKey(msgInfoBO.getLotteryCode()));
                        }
                        if (!ObjectUtil.isBlank(descMap.get("orderCode"))) {
                            msgInfoBO.setOrderCode(descMap.get("orderCode"));
                        }
                        if (!ObjectUtil.isBlank(descMap.get("orderId"))) {
                        	msgInfoBO.setOrderId(descMap.get("orderId"));
                        }
                        if(!ObjectUtil.isBlank(descMap.get("expertUserId"))) {
                        	msgInfoBO.setExpertUserId(Integer.parseInt(descMap.get("expertUserId")));
                        }
                        if (!ObjectUtil.isBlank(descMap.get("buyType"))) {
                            if (descMap.get("buyType").equals("代购")) {
                                msgInfoBO.setBuyType((short)1);
                            } else if (descMap.get("buyType").equals("追号")) {
                                msgInfoBO.setBuyType((short)2);
                            } else if (descMap.get("buyType").equals("合买")) {
                                msgInfoBO.setBuyType((short)3);
                            } else {
                                msgInfoBO.setBuyType(Short.valueOf(descMap.get("buyType")));
                            }
                        }
                        if (msgInfoBO.getMsgType().equals((short)7)
                                && !ObjectUtil.isBlank(descMap.get("orderCode"))) {
                            OrderInfoVO orderInfoVO = new OrderInfoVO();
                            orderInfoVO.setOrderCode(descMap.get("orderCode"));
                            UserMsgInfoBO orderDetail = userMsgInfoDaoMapper.findOrderInfo(orderInfoVO);
                            if (!ObjectUtil.isBlank(orderDetail)) {
                                msgInfoBO.setWinMoney(orderDetail.getWinMoney());
                                msgInfoBO.setBuyType(orderDetail.getBuyType());
                                msgInfoBO.setBetMoney(orderDetail.getBetMoney());
                                msgInfoBO.setBetTime(orderDetail.getBetTime());
                                msgInfoBO.setIssueCode(orderDetail.getIssueCode());
                            }
                        }
                    }
                    if (!ObjectUtil.isBlank(msgInfoBO.getMsgContent()) && vo.getPlatform().equals(UserConstants.PlatformEnum.PLATFORM_WAP.getKey()) ) {
                        if (msgInfoBO.getMsgContent().contains("<a>") || msgInfoBO.getMsgContent().contains("【")) {
                            String str = msgInfoBO.getMsgContent().replaceAll("【[^】]*】", "");
                            msgInfoBO.setMsgContent(str);
                        }
                    }
                }
                return list;
            }
        });
    }

    @Override
    public ResultBO<?> findMsgSwitch(UserMsgInfoVO vo) {
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        vo.setUserId(userInfoBO.getId());
        if (vo.getSendType().equals(UserConstants.MsgSendTypeEnum.SITE.getValue())) {
            return findPcMsgSwith(vo);
        } else {
            return findAppMsgSwitch(vo);
        }
    }

    public ResultBO<?> findPcMsgSwith(UserMsgInfoVO vo) {
        List<DicDataDetailBO> dic = userMsgInfoDaoMapper.findMsgDic("1705");
        Map<String, String> map = new HashMap<>();
        for (DicDataDetailBO dataDetailBO : dic) {
            map.put(dataDetailBO.getDicDataValue(), dataDetailBO.getDicDataName());
        }
        List<UserMsgConfigBO> list = userMsgInfoDaoMapper.findMsgSwitch(vo);
        for (UserMsgConfigBO userMsgConfigBO : list) {
            if (userMsgConfigBO.getMsgType().equals((short)7)) {
                userMsgConfigBO.setSwitchType(1);
                userMsgConfigBO.setMsgName(switchTypeMap.get(1));
                userMsgConfigBO.setEnable(UserConstants.IS_FALSE);
                userMsgConfigBO.setTypeName(map.get(userMsgConfigBO.getTypeNode()));
            }
            if (userMsgConfigBO.getMsgType().equals((short)3)) {
                userMsgConfigBO.setSwitchType(2);
                userMsgConfigBO.setMsgName(switchTypeMap.get(2));
                userMsgConfigBO.setEnable(UserConstants.IS_TRUE);
                userMsgConfigBO.setTypeName(map.get(userMsgConfigBO.getTypeNode()));
            }
            if (userMsgConfigBO.getMsgType().equals((short)1)) {
                userMsgConfigBO.setSwitchType(3);
                userMsgConfigBO.setMsgName(switchTypeMap.get(3));
                userMsgConfigBO.setEnable(UserConstants.IS_TRUE);
                userMsgConfigBO.setTypeName(map.get(userMsgConfigBO.getTypeNode()));
            }
            if (userMsgConfigBO.getMsgType().equals((short)6)) {
                userMsgConfigBO.setSwitchType(4);
                userMsgConfigBO.setMsgName(switchTypeMap.get(4));
                userMsgConfigBO.setEnable(UserConstants.IS_TRUE);
                userMsgConfigBO.setTypeName(map.get(userMsgConfigBO.getTypeNode()));
            }
            if (userMsgConfigBO.getMsgType().equals((short)4)) {
                userMsgConfigBO.setSwitchType(5);
                userMsgConfigBO.setMsgName(switchTypeMap.get(5));
                userMsgConfigBO.setEnable(UserConstants.IS_TRUE);
                if (userMsgConfigBO.getTypeNode().equals("13")) {
                    userMsgConfigBO.setEnable(UserConstants.IS_FALSE);
                }
                userMsgConfigBO.setTypeName(map.get(userMsgConfigBO.getTypeNode()));
            }
            if (userMsgConfigBO.getMsgType().equals((short)2)) {
                userMsgConfigBO.setSwitchType(6);
                userMsgConfigBO.setMsgName(switchTypeMap.get(6));
                userMsgConfigBO.setEnable(UserConstants.IS_TRUE);
                userMsgConfigBO.setTypeName(map.get(userMsgConfigBO.getTypeNode()));
                if (userMsgConfigBO.getTypeNode().equals("11") || userMsgConfigBO.getTypeNode().equals("12") || userMsgConfigBO.getTypeNode().equals("1")) {
                    userMsgConfigBO.setEnable(UserConstants.IS_FALSE);
                }
            }
        }
        return ResultBO.ok(list);
    }

    public ResultBO<?> findAppMsgSwitch(UserMsgInfoVO vo) {
        List<DicDataDetailBO> msgType = userMsgInfoDaoMapper.findMsgDic("2103");
        for (DicDataDetailBO detailBO : msgType) {
            detailBO.setDesc(switchTypeDescMap.get(detailBO.getDicDataValue()));
        }
        return ResultBO.ok(msgType);
    }

    @Override
    public ResultBO<?> findMsgNodeSwitch(UserMsgInfoVO vo) {
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        vo.setUserId(userInfoBO.getId());
        List<UserMsgConfigBO> list = new ArrayList<>();
        if (vo.getTypeNode().equals("1")) {
            vo.setType((short)1);
            List<UserMsgConfigBO> switchlist = userMsgInfoDaoMapper.findMsgSwitch(vo);
            switchlist.get(0).setTypeName(getMap("2102").get("1"));
            list.add(switchlist.get(0));
            List<UserMsgConfigBO> msglist = userMsgInfoDaoMapper.findMsgLotterySwitch(vo);
            for (UserMsgConfigBO msgConfigBO : msglist) {
                msgConfigBO.setLotteryDesc(lotteryDescMap.get(msgConfigBO.getLotteryCode()));
                list.add(msgConfigBO);
            }
        } else if (vo.getTypeNode().equals("5")){
            vo.setType((short)2);
            list = userMsgInfoDaoMapper.findMsgLotterySwitch(vo);
            for (UserMsgConfigBO userMsgConfigBO : list) {
                userMsgConfigBO.setLotteryDesc(lotteryDescMap.get(userMsgConfigBO.getLotteryCode()));
            }
        } else {
            list = userMsgInfoDaoMapper.findMsgSwitch(vo);
            for (UserMsgConfigBO userMsgConfigBO : list) {
                userMsgConfigBO.setTypeName(getMap("1705").get(userMsgConfigBO.getTypeNode()));
            }
        }
        return ResultBO.ok(list);
    }

    @Override
    public ResultBO<?> updateMsgInfoStatus(UserMsgInfoVO vo) {
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        if (!ObjectUtil.isBlank(vo.getIds())) {
            List<String> batchList = Arrays.asList(vo.getIds().split(SymbolConstants.UNDERLINE));
            vo.setBatchList(batchList);
            vo.setUserId(userInfoBO.getId());
            userMsgInfoDaoMapper.updateMsgInfoStatus(vo);
        } else {
            vo.setUserId(userInfoBO.getId());
            userMsgInfoDaoMapper.updateAppMsgInfoStatus(vo);
        }
        return ResultBO.ok();
    }

    @Override
    public ResultBO<?> updateSwitchStatus(UserMsgConfigVO vo) {
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        List<UserMsgConfigVO> list = vo.getList();
        for (UserMsgConfigVO configVO : list) {
            configVO.setUserId(userInfoBO.getId());
            if (ObjectUtil.isBlank(configVO.getId())) {
                userMsgInfoDaoMapper.addSwitch(configVO);
            } else {
                userMsgInfoDaoMapper.updateSwitch(configVO);
            }
        }
        return ResultBO.ok();
    }

    @Override
    public ResultBO<?> updateLotterySwitchStatus(UserMsgConfigVO vo) {
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        for (UserMsgConfigVO configVO : vo.getList()) {
            configVO.setUserId(userInfoBO.getId());
            if (vo.getTypeNode().equals("1")) {
                configVO.setType((short)1);
            }
            if (vo.getTypeNode().equals("4")) {
                configVO.setType((short)2);
            }
        }
        userMsgInfoDaoMapper.updateLotterySwitchStatus(vo.getList());
        return ResultBO.ok();
    }

    @Override
    public ResultBO<?> recoverDefault(UserMsgConfigVO vo) {
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        for (UserMsgConfigVO configVO : vo.getList()) {
            configVO.setUserId(userInfoBO.getId());
        }
        userMsgInfoDaoMapper.recoverDefault(vo.getList());
        return ResultBO.ok();
    }

    @Override
    public ResultBO<?> deleteMsgInfoById(UserMsgInfoVO vo) {
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        List<String> batchList = Arrays.asList(vo.getIds().split(SymbolConstants.UNDERLINE));
        vo.setBatchList(batchList);
        vo.setUserId(userInfoBO.getId());
        userMsgInfoDaoMapper.deleteMsgInfoById(vo);
        return ResultBO.ok();
    }

    @Override
    public ResultBO<?> deleteMsgInfo(UserMsgInfoVO vo) {
        ResultBO<?> validateToken = ValidateUtil.validateToken(vo.getToken());
        if (validateToken.isError()) {
            return validateToken;
        }
        UserInfoBO userInfoBO = userUtil.getUserByToken(vo.getToken());
        if (ObjectUtil.isBlank(userInfoBO)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        vo.setUserId(userInfoBO.getId());
        userMsgInfoDaoMapper.deleteMsgInfo(vo);
        return ResultBO.ok();
    }

    /**
     * 消息设置-勿扰模式，设置勿扰时间段
     *
     * @param passportVO 参数
     * @return object
     * @throws Exception 异常
     */
    @Override
    public ResultBO<?> doNotDisturb (PassportVO passportVO) {
        UserInfoBO tokenInfo = userUtil.getUserByToken(passportVO.getToken());
        if(ObjectUtil.isBlank(tokenInfo)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        UserInfoPO userInfoPO = new UserInfoPO();
        userInfoPO.setId(tokenInfo.getId());
        if(passportVO.getSwitchStatus() == UserConstants.IS_TRUE.intValue()) {
            userInfoPO.setAppNotDisturb(passportVO.getTimeStr());
        }
        userInfoPO.setMsgApp(passportVO.getSwitchStatus());
        int row = userInfoDaoMapper.updateUserInfo(userInfoPO);
        if(row > 0) {
            return ResultBO.ok();
        } else {
            return ResultBO.err();
        }
    }

    /**
     * 根据用户ID获取用户设置的勿扰模式
     *
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public ResultBO<?> getDisturb (String token) throws Exception {
        UserInfoBO tokenInfo = userUtil.getUserByToken(token);
        if(ObjectUtil.isBlank(tokenInfo)) {
            return ResultBO.err(MessageCodeConstants.TOKEN_LOSE_SERVICE);
        }
        UserInfoBO userInfoBO = userInfoDaoMapper.findUserInfoByUserId(tokenInfo.getId());
        if(!StringUtil.isBlank(userInfoBO.getAppNotDisturb())) {
            String appNoteDisturb = userInfoBO.getAppNotDisturb();
            userInfoBO.setAppNotDisturbStart(appNoteDisturb.substring(0, appNoteDisturb.indexOf("-")));
            userInfoBO.setAppNotDisturbEnd(appNoteDisturb.substring(appNoteDisturb.length() - 5));
        }
        //0关1开
        userInfoBO.setSwitchStatus(userInfoBO.getMsgApp() == null ? UserConstants.IS_TRUE : userInfoBO.getMsgApp().intValue());
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("ss", userInfoBO.getSwitchStatus());
        jsonMap.put("as", userInfoBO.getAppNotDisturbStart());
        jsonMap.put("ae", userInfoBO.getAppNotDisturbEnd());

        return ResultBO.ok(jsonMap);
    }

    private OrderInfoVO getOrderInfo(String msgDesc) {
        OrderInfoVO vo = new OrderInfoVO();
        String [] msgArr = msgDesc.split(";");
        if (msgArr[2].length()>1) {
            String [] lotteryCode = msgArr[2].split(":");
            vo.setLotteryCode(Integer.valueOf(lotteryCode[1]));
        }
        if (msgArr[3].length()>1) {
            String [] issueCode = msgArr[3].split(":");
            vo.setLotteryIssue(issueCode[1]);
        }
        return vo;
    }

    private Map<String, String> getMap(String dicCode){
        List<DicDataDetailBO> dicList = userMsgInfoDaoMapper.findMsgDic(dicCode);
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i<dicList.size(); i++) {
            map.put(dicList.get(i).getDicDataValue().toString(), dicList.get(i).getDicDataName());
        }
        return map;
    }

    public static void main(String[] args) {
        String description = "toBuyLotteryCode:100;activityUrl:http://cp.2ncai.com/";
        String [] descArr = description.split(";");
        String [] code = descArr[0].split(":");
        System.out.println(code[1]);
        String [] url = descArr[1].split("rl:");
        System.out.println(url[1]);

        String content = "恭喜你，获得彩金红包一个【查看详情】【<a>www.baidu.com</a>】";
        String [] contentArr = content.split("[<【]");
        System.out.println(contentArr[0]);
        String str = "lotteryCode:300;buyType:1;orderCode:D17072614531000100062";
        String [] msgArr = str.split("[:;]");
        for (String s : msgArr) {
            System.out.println(s);
        }
    }

    private Map<String,String> getDescMap(String desc) {
        Matcher m = Pattern.compile("([^;:]+):([^;]+)").matcher(desc);
        Map<String, String> paramMap = new HashMap<String, String>();
        while (m.find()) {
            paramMap.put(m.group(1), m.group(2));
        }
        return paramMap;
    }

}
