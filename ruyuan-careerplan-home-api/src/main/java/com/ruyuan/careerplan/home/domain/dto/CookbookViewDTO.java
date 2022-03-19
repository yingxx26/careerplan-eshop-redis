package com.ruyuan.careerplan.home.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 菜谱视图信息
 *
 * @author zhonghuashishan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CookbookViewDTO implements Serializable {

    /**
     * 菜谱ID
     **/
    private Long id;

    /**
     * 菜谱名称
     **/
    private String cookbookName;

    /**
     * 主图
     **/
    private String mainUrl;

    /**
     * 作者昵称
     **/
    private String userName;

    /**
     * 作者头像
     **/
    private String profile;

    /**
     * 菜谱描述
     */
    private String description;

}
