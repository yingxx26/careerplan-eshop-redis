package com.ruyuan.careerplan.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruyuan.careerplan.common.domain.BinlogDataDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL binlog解析工具类
 *
 * @author zhonghuashishan
 */
public abstract class BinlogUtils
{
    /**
     * 解析binlog json字符串 (表数据以实体类Map的形式返回)
     * @param binlogStr binlog json字符串
     * @return BinlogData
     */
    public static BinlogDataDTO getBinlogData(String binlogStr)
    {
        // isJson方法里面会判断字符串是不是为空，所以这里不需要重复判断
        if (JSONUtil.isJson(binlogStr))
        {
            JSONObject binlogJson = JSONUtil.parseObj(binlogStr);
            BinlogDataDTO binlogData = new BinlogDataDTO();
            // 表名
            String tableName = binlogJson.getStr("table");
            binlogData.setTableName(tableName);
            // 操作类型
            String operateType = binlogJson.getStr("type");
            binlogData.setOperateType(operateType);
            // 操作时间
            Long operateTime = binlogJson.getLong("ts");
            binlogData.setOperateTime(operateTime);
            // 获取数据json数组
            JSONArray dataArray = binlogJson.getJSONArray("data");
            if (null != dataArray) {

                Iterable <JSONObject> dataArrayIterator = dataArray.jsonIter();
                // 遍历data节点并反射生成对象
                if (null != dataArrayIterator){
                     // binlog的data数组里数据的类型为Map
                     List<Map<String, Object>> dataMap = new ArrayList<>();
                     while (dataArrayIterator.iterator().hasNext()){
                         JSONObject jsonObject = dataArrayIterator.iterator().next();
                         Map <String, Object> data = new HashMap<>();
                         jsonObject.keySet().forEach(key -> {
                            String camelKey = StrUtil.toCamelCase(StrUtil.lowerFirst(key));
                             data.put(camelKey, jsonObject.get(key));
                         });
                         dataMap.add(data);
                     }
                     binlogData.setDataMap(dataMap);
                }
            }
            return binlogData;
        }
        return null;
    }
}
