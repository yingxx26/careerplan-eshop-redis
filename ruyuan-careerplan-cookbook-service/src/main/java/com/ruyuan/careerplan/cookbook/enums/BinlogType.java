
package com.ruyuan.careerplan.cookbook.enums;


/**
 * Binlog类型枚举值
 * 
 * @author zhonghuashishan
 */
public enum BinlogType
{
    /**
     * 新增：INSERT
     */
    INSERT ("新增", "INSERT"),
    /**
     * 修改：UPDATE
     */
    UPDATE ("修改", "UPDATE"),
    /**
     * 删除：DELETE
     */
    DELETE ("删除", "DELETE");

    /**
     * 枚举显示名称
     */
    private final String name;
    /**
     * 枚举的值
     */
    private final String value;

    BinlogType(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * 取得枚举类型的值
     * 
     * @return 枚举的值
     */
    public String getValue ()
    {
        return this.value;
    }

    /**
     * 取得枚举类型的名称
     * 
     * @return 枚举显示名称
     */
    public String getName ()
    {
        return this.name;
    }

    /**
     * 根据枚举类型的值取得枚举类型
     * 
     * @param typeValue 枚举类型的值
     * @return 枚举类型
     */
    public static BinlogType findByValue (String typeValue)
    {
        for (BinlogType type : values ())
        {
            if (type.getValue ().equals (typeValue))
            {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据枚举类型的值取得枚举类型的名称
     * 
     * @param typeValue 枚举类型的值
     * @return 枚举显示名称
     */
    public static String getNameByValue (String typeValue)
    {
        for (BinlogType type : values ())
        {
            if (type.getValue ().equals (typeValue))
            {
                return type.getName ();
            }
        }
        return null;
    }
}
