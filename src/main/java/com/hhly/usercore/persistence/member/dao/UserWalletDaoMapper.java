package com.hhly.usercore.persistence.member.dao;

import com.hhly.skeleton.task.order.vo.OrderChannelVO;
import com.hhly.skeleton.user.bo.ChannelMemberWalletBO;
import org.apache.ibatis.annotations.Param;

import com.hhly.usercore.persistence.member.po.UserWalletPO;

import java.util.List;

public interface UserWalletDaoMapper {

    int insert(UserWalletPO po);
    
    UserWalletPO findByUserId(@Param("userId") Integer userId);

    List<ChannelMemberWalletBO> queryChannelMemberWalletList(OrderChannelVO vo);

}