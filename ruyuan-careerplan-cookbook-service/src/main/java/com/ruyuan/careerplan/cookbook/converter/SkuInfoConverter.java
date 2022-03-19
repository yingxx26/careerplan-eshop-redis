package com.ruyuan.careerplan.cookbook.converter;

import com.ruyuan.careerplan.cookbook.domain.dto.SkuInfoDTO;
import com.ruyuan.careerplan.cookbook.domain.entity.SkuInfoDO;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateSkuRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * @author zhonghuashishan
 */
@Mapper(componentModel = "spring")
public interface SkuInfoConverter {

    /**
     * 对象转换
     * @param skuInfoDO 对象
     * @return 对象
     */
    @Mappings({
            @Mapping(target = "skuImage", ignore = true),
            @Mapping(target = "detailImage", ignore = true)
    })
    SkuInfoDTO convertSkuInfoDTO(SkuInfoDO skuInfoDO);


    /**
     * 对象转换
     * @param request 对象
     * @return 对象
     */
    @Mappings({
            @Mapping(target = "skuImage", ignore = true),
            @Mapping(target = "detailImage", ignore = true)
    })
    SkuInfoDO convertSkuInfoDO(SaveOrUpdateSkuRequest request);
}
