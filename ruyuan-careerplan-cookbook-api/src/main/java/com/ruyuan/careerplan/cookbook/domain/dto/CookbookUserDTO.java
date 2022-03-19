package com.ruyuan.careerplan.cookbook.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜谱作者信息
 *
 * @author zhonghuashishan
 */
@Data
public class CookbookUserDTO implements Serializable {

    /**
     * 主键ID
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

}