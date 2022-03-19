package com.ruyuan.careerplan.cookbook.converter;

import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.entity.CookbookDO;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateCookbookRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * @author zhonghuashishan
 */
@Mapper(componentModel = "spring")
public interface CookbookConverter {

    /**
     * 对象转换
     * @param request 对象
     * @return 对象
     */
    @Mappings({
            @Mapping(target = "cookbookDetail", ignore = true),
            @Mapping(target = "foods", ignore = true)
    })
    CookbookDO convertCookbookDO(SaveOrUpdateCookbookRequest request);

    /**
     * 对象转换
     * @param cookbookDO 对象
     * @return 对象
     */
    @Mappings({
            @Mapping(target = "userName", ignore = true),
            @Mapping(target = "cookbookDetail", ignore = true),
            @Mapping(target = "foods", ignore = true)
    })
    CookbookDTO convertCookbookDTO(CookbookDO cookbookDO);


    /**
     * 对象转换
     * @param cookbookDOS
     * @return
     */
    List<CookbookDTO> listConvertCookbookDTO(List<CookbookDO> cookbookDOS);
}
