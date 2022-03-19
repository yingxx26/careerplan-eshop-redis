package com.ruyuan.careerplan.home.domain.request;

import lombok.Data;

/**
 * feed流请求入参
 */
@Data
public class HomeFeedRequest {

    /**
     * 授权Token
     */
    private String accessToken;

    /**
     * feed流请求版本号，首次进入页面可不传
     */
    private String feedVersion;

    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 是否下拉刷新
     */
    private Boolean pullToRefresh;

}
