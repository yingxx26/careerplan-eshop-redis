package com.ruyuan.careerplan.cookbook.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 新增/修改菜谱作者请求入参
 *
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveOrUpdateUserRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 作者名称
     */
    private String userName;

    /**
     * 头像
     */
    private String profile;

    /**
     * 个人签名
     */
    private String personal;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 操作人
     */
    private Integer operator;

}