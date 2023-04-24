/*
 * Copyright(c) 2022 长沙市希尚网络科技有限公司
 * 注意：本内容仅限于长沙市希尚网络科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.leo.service.study;

import com.alibaba.fastjson.JSONObject;
import com.leo.service.study.elasticsearch.domain.Teacher;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.suggest.SortBy;
import org.elasticsearch.search.suggest.Suggest.Suggestion;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder.SuggestMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

/**
 * es suggest 功能 test
 * @author Leo
 * @version 1.0 2023/4/16
 */
@SpringBootTest
@Slf4j
public class ElasticsearchSuggestTests {

    @Autowired
    public ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * completion suggestion 搜索建议补全
     * @author Leo
     */
    @Test
    public void completionQuery() {
        // 构建搜索建议补全对象
        CompletionSuggestionBuilder completionSuggestionBuilder = SuggestBuilders.completionSuggestion("completion")
            .prefix("山东")
            .skipDuplicates(true);
        // 创建搜索提示对象 进行封装搜索补全
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("my-completion", completionSuggestionBuilder);
        //查询es并反参
        SearchResponse suggest = elasticsearchRestTemplate.suggest(suggestBuilder,
            elasticsearchRestTemplate.getIndexCoordinatesFor(Teacher.class));
        //获取反参中的搜索补全结果
        Suggestion<? extends Entry<? extends Option>> suggestion = suggest.getSuggest().getSuggestion("my-completion");
        List<String> suggests = suggestion.getEntries().stream().map(x -> x.getOptions().stream().map(y -> y.getText().toString())
            .collect(Collectors.toList())).findFirst().orElse(new ArrayList<>());
        log.info("completion suggest option:{}", JSONObject.toJSONString(suggests));
    }


    /**
     * 单个词的搜索纠错
     * @author Leo
     */
    @Test
    public void termSuggestion() {
        // 构建搜索建议补全对象
        TermSuggestionBuilder termSuggestionBuilder = SuggestBuilders.termSuggestion("tName")
            .text("小钟老师")
            .suggestMode(SuggestMode.POPULAR);
        // 排序
        termSuggestionBuilder.sort(SortBy.SCORE);
        // 构建纠正词条对象  词条建议器(只要是词,短的 比如姓名)
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("my-termSuggest", termSuggestionBuilder);

        //查询es并反参
        SearchResponse suggest = elasticsearchRestTemplate.suggest(suggestBuilder, IndexCoordinates.of("teacher"));

        log.info("term suggestion result:{}", suggest);
    }

    /**
     * 短语纠错
     * @author Leo
     */
    @Test
    public void phraseSuggestion() {
        PhraseSuggestionBuilder phraseSuggestionBuilder = SuggestBuilders.phraseSuggestion("tFamous")
            .text("吾之初心,泳世不忘")
            .highlight("<em>", "</em>");

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("my-phraseSuggest", phraseSuggestionBuilder);

        SearchResponse suggest = elasticsearchRestTemplate.suggest(suggestBuilder, IndexCoordinates.of("teacher"));

        log.info("term suggestion result:{}", suggest);


    }

}
