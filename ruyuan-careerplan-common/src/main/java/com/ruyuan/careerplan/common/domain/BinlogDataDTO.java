package com.ruyuan.careerplan.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * MySQL的binlog对象
 *
 * @author zhonghuashishan
 */
@Data
public class BinlogDataDTO implements Serializable
{
    private static final long serialVersionUID = 4579426193672749274L;

    /**
     * binlog对应的表名
     */
    private String tableName;
    /**
     * 操作时间
     */
    private Long operateTime;
    /**
     * 操作类型
     */
    private String operateType;
    /**
     * data节点转换成的Map，key对应的是bean里的属性名，value一律为字符串（它和datas只会有一个有值）
     */
    private List<Map<String, Object>> dataMap;
    /**
     * data节点转换成的bean（它和dataMap只会有一个有值）
     */
    private List<Object> datas;

    @Override
    public String toString()
    {
        return new StringJoiner(", ", BinlogDataDTO.class.getSimpleName() + "[", "]")
                .add("tableName=" + tableName)
                .add("operateTime=" + operateTime)
                .add("operateType='" + operateType + "'")
                .add("dataMap=" + dataMap)
                .add("datas=" + datas)
                .toString();
    }
}
