package com.ruyuan.careerplan.cookbook.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ruyuan.careerplan.common.domain.BaseEntity;
import lombok.Data;

import java.io.Serializable;


/**
 * @author zhonghuashishan
 */
@Data
@TableName("cookbook_user")
public class CookbookUserDO extends BaseEntity implements Serializable {

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
     * 创建人
     */
    private Integer createUser;

    /**
     * 修改人
     */
    private Integer updateUser;
}