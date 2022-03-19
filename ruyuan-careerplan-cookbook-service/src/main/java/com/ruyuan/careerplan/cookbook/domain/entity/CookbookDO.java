package com.ruyuan.careerplan.cookbook.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ruyuan.careerplan.common.domain.BaseEntity;
import lombok.Data;

import java.io.Serializable;


/**
 * @author zhonghuashishan
 */
@Data
@TableName("cookbook")
public class CookbookDO extends BaseEntity implements Serializable {

    /**
     * 作者id
     */
    private Long userId;

    /**
     * 菜谱名称
     */
    private String cookbookName;

    /**
     * 菜谱类型
     */
    private Integer cookbookType;

    /**
     * 菜谱描述
     */
    private String description;

    /**
     * 分类标签  1:私房菜  2:下饭菜  3:快手菜
     */
    private Integer categoryTag;

    /**
     * 主图链接
     */
    private String mainUrl;

    /**
     * 视频链接
     */
    private String videoUrl;

    /**
     * 制作时长  1:10分钟以内  2:10-30分钟  3:30-60分钟  4:1小时以上
     */
    private Integer cookingTime;

    /**
     * 难度  1:简单  2:一般  3:较难  4:极难
     */
    private Integer difficulty;

    /**
     * 菜谱详情
     * step：步骤，content：步骤内容，img：步骤图片
     * [{"step":"1", "content":"步骤1xx", "img":"url"}]
     */
    private String cookbookDetail;

    /**
     * 食材清单
     */
    private String foods;

    /**
     * 小贴士
     */
    private String tips;

    /**
     * 菜谱状态  0:有效  1:删除
     */
    private Integer cookbookStatus;

    /**
     * 创建人
     */
    private Integer createUser;

    /**
     * 修改人
     */
    private Integer updateUser;
}