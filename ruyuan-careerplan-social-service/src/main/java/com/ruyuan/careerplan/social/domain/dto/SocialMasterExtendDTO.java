package com.ruyuan.careerplan.social.domain.dto;

import lombok.Data;

/**
 * 团长信息
 *
 * @author zhonghuashishan
 */
@Data
public class SocialMasterExtendDTO {
    
    /**
     * 本次活动总需人数
     */
    private Integer totalMember;
    
    /**
     * 待到账金额 单位分
     */
    private Integer waitAmount;
    
    /**
     * 已提的金额 单位分
     */
    private Integer withdrawAmount;
    
    /**
     * 待提现金额 单位分
     */
    private Integer remainAmount;
    
    /**
     * 已到账金额
     */
    private Integer receiveAmount;
    
    /**
     * 助力次数
     */
    private Integer helpCount;
    
    /**
     * 已经分享次数
     */
    private Long alreadyShareCount;
    
    /**
     * 第2次分享是否已打款
     */
    private Boolean shareTwoPayingAmount;
    
    /**
     * 第二次分享传给前端的立即到账金额
     */
    private Integer shareTwoAmount;

}
