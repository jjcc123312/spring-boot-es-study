/*
 * Copyright(c) 2022 长沙市希尚网络科技有限公司
 * 注意：本内容仅限于长沙市希尚网络科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.leo.service.study.elasticsearch.service.impl;

import com.leo.service.study.elasticsearch.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

/**
 * TODO
 *
 * @author Leo
 * @version 1.0 2023/3/15
 */
@Service
public class ProductServiceImpl implements ProductService {


    @Autowired
    @Lazy
    private ElasticsearchRestTemplate elasticsearchRestTemplate;


}
