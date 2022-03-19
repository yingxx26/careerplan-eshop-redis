package com.ruyuan.careerplan.inventory.converter;

import com.ruyuan.careerplan.inventory.domain.entity.InventoryDO;
import com.ruyuan.careerplan.inventory.domain.entity.StorageDetailLogDO;
import com.ruyuan.careerplan.inventory.domain.entity.StorageInfoDO;
import com.ruyuan.careerplan.inventory.domain.request.InventoryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author zhonghuashishan
 */
@Mapper(componentModel = "spring")
public interface InventoryConverter {
    /**
     * 对象克隆转换
     * @param request
     * @return
     */
    InventoryDO converterRequest(InventoryRequest request);

    /**
     * 对象克隆转换
     * @param request
     * @return
     */
    @Mapping(target = "storageCode", source = "warehouseCode")
    @Mapping(target = "storageNum", source = "inventoryNum")
    StorageInfoDO converterStorageRequest(InventoryRequest request);
    /**
     * 对象克隆转换
     * @param inventoryDO
     * @return
     */
    @Mapping(target = "storageAfterNum", source = "inventoryNum")
    StorageDetailLogDO converterStorageLogRequest(InventoryDO inventoryDO);
}
