package com.ruyuan.careerplan.cookbook.enums;

import com.ruyuan.careerplan.cookbook.constants.RedisKeyConstants;

/**
 * 存储关于配置的目前需要支持的缓存一致性的表相关信息
 * 后续可考虑升级为配置使用
 * @author zhonghuashishan
 */
public enum ConsistencyTableEnum {
    /**
     * 商品表缓存配置
     */
    SKU_INFO ("sku_info", RedisKeyConstants.GOODS_INFO_PREFIX,"id");
    /**
     * 配置相关的表名称
     */
    private final String tableName;
    /**
     * 缓存的前缀key
     */
    private final String cacheKey;

    /**
     * 缓存的标识字段
     */
    private final String cacheField;


    ConsistencyTableEnum(String tableName, String cacheKey,String cacheField)
    {
        this.tableName = tableName;
        this.cacheKey = cacheKey;
        this.cacheField = cacheField;
    }
    /**
     * 取得配置的缓存前缀key
     *
     * @return 枚举的值
     */
    public String getTableName ()
    {
        return this.tableName;
    }
    /**
     * 取得配置的缓存前缀key
     *
     * @return 枚举的值
     */
    public String getCacheKey ()
    {
        return this.cacheKey;
    }

    /**
     * 取得配置的缓存前缀key
     *
     * @return 枚举的值
     */
    public String getCacheField ()
    {
        return this.cacheField;
    }

    /**
     * 根据枚举类型的值取得枚举类型
     *
     * @param tableName 枚举类型的值
     * @return 枚举类型
     */
    public static ConsistencyTableEnum findByEnum (String tableName)
    {
        for (ConsistencyTableEnum type : values ())
        {
            if (type.getTableName ().equals (tableName))
            {
                return type;
            }
        }
        return null;
    }
}
