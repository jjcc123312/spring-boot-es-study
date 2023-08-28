/*
 * Copyright(c) 2022 长沙市希尚网络科技有限公司
 * 注意：本内容仅限于长沙市希尚网络科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.leo.service.study.controller;

import com.leo.service.study.elasticsearch.domain.TestDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 *
 * @author Leo
 * @version 1.0 2023/8/14
 */
@RestController
public class TestController {

    @PostMapping("/test/asd")
    public String asdasd(@RequestBody TestDTO testDTO) {

        return "ssss";
    }
}
