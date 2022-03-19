package com.ruyuan.careerplan.cookbook.converter;

import com.ruyuan.careerplan.cookbook.domain.dto.CookbookUserDTO;
import com.ruyuan.careerplan.cookbook.domain.entity.CookbookUserDO;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author zhonghuashishan
 */
@Mapper(componentModel = "spring")
public interface CookbookUserConverter {

    /**
     * 对象转换
     * @param request 对象
     * @return 对象
     */
    @Mappings({
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUser", ignore = true)
    })
    CookbookUserDO convertCookbookUserDO(SaveOrUpdateUserRequest request);

    /**
     * 对象转换
     * @param cookbookUserDO 对象
     * @return 对象
     */
    CookbookUserDTO convertCookbookUserDTO(CookbookUserDO cookbookUserDO);

}
