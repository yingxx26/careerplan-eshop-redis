package com.ruyuan.careerplan.cookbook;

import com.ruyuan.careerplan.cookbook.domain.dto.CookbookUserDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateUserDTO;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookUserQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateUserRequest;
import com.ruyuan.careerplan.cookbook.enums.SexEnum;
import com.ruyuan.careerplan.cookbook.service.CookbookUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = CookbookApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CookbookUserServiceTest {

    @Autowired
    private CookbookUserService cookbookUserService;

    @Test
    public void saveOrUpdateUser() {
        SaveOrUpdateUserRequest request = SaveOrUpdateUserRequest.builder()
                .userName("会做饭的刘德华")
                .profile("url")
                .personal("大家好，我是会做饭的刘德华")
                .birthday("2021-01-13")
                .sex(SexEnum.MALE.getCode())
                .operator(1)
                .build();

        SaveOrUpdateUserDTO dto = cookbookUserService.saveOrUpdateUser(request);
        System.out.println(dto);
    }


    @Test
    public void getUserInfo() {
        CookbookUserQueryRequest request = CookbookUserQueryRequest.builder()
                .userId(1L)
                .build();

        CookbookUserDTO dto = cookbookUserService.getUserInfo(request);
        System.out.println(dto);
    }

}