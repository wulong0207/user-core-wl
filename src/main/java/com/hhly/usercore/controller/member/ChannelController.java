package com.hhly.usercore.controller.member;

import com.alibaba.fastjson.JSONObject;
import com.hhly.skeleton.base.bo.ResultBO;
import com.hhly.skeleton.base.util.ObjectUtil;
import com.hhly.skeleton.task.order.vo.OrderChannelVO;
import com.hhly.usercore.local.channel.service.ChannelInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @des 渠道管理
 * @author zhouyang
 * @date 2018.6.12
 */

@RestController
@RequestMapping("/channel")
public class ChannelController {

    @Autowired
    private ChannelInfoService channelInfoService;

    @RequestMapping("/id")
    public Object getChannelTopId(@RequestBody OrderChannelVO vo) {
        String channelTopId = channelInfoService.getChannelTopId(vo);
        JSONObject json = new JSONObject();
        if (!ObjectUtil.isBlank(channelTopId)) {
            json.put("channelTopId", channelTopId);
        }
        return ResultBO.ok(json);
    }
}
