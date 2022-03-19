package com.ruyuan.careerplan.cookbook.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CookbookUpdateMessage implements Serializable {

    /**
     * 菜谱id
     */
    private Long cookbookId;

    /**
     * 作者id
     */
    private Long userId;
}
