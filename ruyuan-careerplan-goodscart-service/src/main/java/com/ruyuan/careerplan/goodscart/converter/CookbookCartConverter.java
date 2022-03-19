package com.ruyuan.careerplan.goodscart.converter;

import com.ruyuan.careerplan.cookbook.domain.request.SkuSaleableRequest;
import com.ruyuan.careerplan.goodscart.domain.dto.CartSkuInfoDTO;
import com.ruyuan.careerplan.goodscart.domain.entity.CookBookCartDO;
import com.ruyuan.careerplan.goodscart.domain.request.CheckedCartRequest;
import com.ruyuan.careerplan.goodscart.domain.request.UpdateCookBookCartRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author zhonghuashishan
 */
@Mapper(componentModel = "spring")
public interface CookbookCartConverter {
    CookBookCartDO requestToEntity(UpdateCookBookCartRequest request);

    List<CartSkuInfoDTO> listDOtoDTO(List<CookBookCartDO> list);

    @Mapping(target = "addAmount", source = "price")
    CookBookCartDO dtoToDO(CartSkuInfoDTO cartSkuInfoDTO);

    CartSkuInfoDTO requestToDTO(UpdateCookBookCartRequest request);

    @Mapping(target = "checkStatus", source = "checkStatus")
    CartSkuInfoDTO requestToDTO(CheckedCartRequest request);
}
