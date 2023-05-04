package com.leo.service.study;

import com.alibaba.fastjson.JSONObject;
import com.leo.service.study.elasticsearch.domain.Student;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

/**
 * es nested 类型查询
 *
 * @author Leo
 * @version 1.0 2023/4/24
 */
@SpringBootTest
@Slf4j
public class DocumentNestedQueryTests {

    @Autowired
    public ElasticsearchRestTemplate elasticsearchRestTemplate;


    /**
     * nested类型字段查询
     * @author Leo
     */
    @Test
    public void nestedQuery() {
        BoolQueryBuilder booleQuery = QueryBuilders.boolQuery();
        NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("sTeacherList",
            QueryBuilders.termQuery("sTeacherList.tName", "小蓝"), ScoreMode.Avg);
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("sCourseList", "java");
        booleQuery.must(termQueryBuilder);

        booleQuery.must(nestedQueryBuilder);

        NativeSearchQuery searchQuery = new NativeSearchQuery(booleQuery);


        SearchHits<Student> search = elasticsearchRestTemplate.search(searchQuery, Student.class);
        log.info("nested query result:{}", JSONObject.toJSONString(search));

    }

    /**
     * nested类型字段查询 + 排序
     * @author Leo
     */
    @Test
    public void nestedSortQuery() {
        BoolQueryBuilder booleQuery = QueryBuilders.boolQuery();
        NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("sTeacherList",
            QueryBuilders.termQuery("sTeacherList.tName", "小蓝"), ScoreMode.Avg);
        booleQuery.must(nestedQueryBuilder);

        NestedSortBuilder nestedSortBuilder = new NestedSortBuilder("sTeacherList");

        NativeSearchQuery searchQuery = new NativeSearchQuery(booleQuery);


        SearchHits<Student> search = elasticsearchRestTemplate.search(searchQuery, Student.class);
        log.info("nested query result:{}", JSONObject.toJSONString(search));

    }

}
