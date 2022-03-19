package com.ruyuan.careerplan.cookbook.controller;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookUserDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateUserDTO;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookUserQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateUserRequest;
import com.ruyuan.careerplan.cookbook.service.CookbookUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhonghuashishan
 */
@RestController
@RequestMapping("/api/user")
public class CookbookUserController {

    @Autowired
    private CookbookUserService cookbookUserService;

    @RequestMapping("/saveOrUpdate")
    public JsonResult<SaveOrUpdateUserDTO> saveOrUpdateUser(@RequestBody SaveOrUpdateUserRequest request){
        SaveOrUpdateUserDTO dto = cookbookUserService.saveOrUpdateUser(request);
        return JsonResult.buildSuccess(dto);
    }

    @RequestMapping("/info")
    public JsonResult<CookbookUserDTO> getUserInfo(@RequestBody CookbookUserQueryRequest request) {
        CookbookUserDTO dto = cookbookUserService.getUserInfo(request);
        return JsonResult.buildSuccess(dto);
    }

}
