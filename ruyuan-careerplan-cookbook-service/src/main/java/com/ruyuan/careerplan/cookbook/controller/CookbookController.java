package com.ruyuan.careerplan.cookbook.controller;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.common.page.PagingInfo;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateCookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateCookbookRequest;
import com.ruyuan.careerplan.cookbook.service.CookbookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhonghuashishan
 */
@Slf4j
@RestController
@RequestMapping("/api/cookbook")
public class CookbookController {

    @Autowired
    private CookbookService cookbookService;

    @RequestMapping("/saveOrUpdate")
    public JsonResult<SaveOrUpdateCookbookDTO> saveOrUpdateCookbook(@RequestBody SaveOrUpdateCookbookRequest request){
        log.info("新增菜谱:{}", request);
        SaveOrUpdateCookbookDTO dto = cookbookService.saveOrUpdateCookbook(request);
        return JsonResult.buildSuccess(dto);
    }

    @GetMapping("/info/{cookbookId}")
    public JsonResult<CookbookDTO> getCookbookInfo(@PathVariable Long cookbookId){
        log.info("查询菜谱信息，cookbookId:{}", cookbookId);
        CookbookQueryRequest request = CookbookQueryRequest.builder()
                .cookbookId(cookbookId)
                .build();
        CookbookDTO dto = cookbookService.getCookbookInfo(request);
        return JsonResult.buildSuccess(dto);
    }

    @GetMapping("/list")
    public JsonResult<PagingInfo<CookbookDTO>> listCookbookInfo(@RequestBody CookbookQueryRequest request){
        log.info("查询菜谱信息列表：{}", request);
        PagingInfo<CookbookDTO> pagingInfo = cookbookService.listCookbookInfo(request);
        return JsonResult.buildSuccess(pagingInfo);
    }

}
