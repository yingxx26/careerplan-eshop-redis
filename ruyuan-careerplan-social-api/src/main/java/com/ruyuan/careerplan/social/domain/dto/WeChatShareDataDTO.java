package com.ruyuan.careerplan.social.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信小程序分享参数
 *
 * @author zhonghuashishan
 */
@Data
@Builder
public class WeChatShareDataDTO implements Serializable {

    /**
     * 唯一标识
     */
    private String organizationCode;

    /**
     * 小程序标题
     */
    private String miniTitle;

    /**
     * 小程序描述
     */
    private String miniDesc;

    /**
     * 小程序链接
     */
    private String miniUrl;

    /**
     * 图片地址
     */
    private String imageUrl;

}
