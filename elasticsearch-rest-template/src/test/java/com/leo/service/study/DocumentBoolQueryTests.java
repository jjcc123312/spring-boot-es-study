package com.leo.service.study;

import com.alibaba.fastjson.JSONObject;
import com.leo.service.study.elasticsearch.domain.Teacher;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

/**
 * @author Leo
 * @version 1.0 2023/4/2
 */
@SpringBootTest
@Slf4j
public class DocumentBoolQueryTests {

    @Autowired
    public ElasticsearchRestTemplate elasticsearchRestTemplate;


    /**
     * es bool query by filter
     * @author Leo
     */
    @Test
    public void boolQueryBuilderByFilter() {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        List<String> values = new ArrayList<>();
        values.add("小蓝教授");
        values.add("小黄老师");

        TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("tName", values);
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("tAddress", "山东省")
            .analyzer("ik_smart").operator(Operator.OR);

        boolQuery.filter(termsQueryBuilder);
        boolQuery.filter(matchQueryBuilder);

        NativeSearchQuery searchQuery = new NativeSearchQuery(boolQuery);

        SearchHits<Teacher> search = elasticsearchRestTemplate.search(searchQuery, Teacher.class);

        log.info("boole by filter search result:{}", JSONObject.toJSONString(search));
    }

}







