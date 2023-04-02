package com.leo.service.study;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.leo.service.study.elasticsearch.domain.Headmaster;
import com.leo.service.study.elasticsearch.domain.Student;
import com.leo.service.study.elasticsearch.domain.Teacher;
import java.lang.annotation.Native;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ScriptType;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ByQueryResponse;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import org.springframework.data.elasticsearch.core.suggest.Completion;

/**
 * es 索引文档简单 curd
 *
 * @author Leo
 * @version 1.0 2023/3/23
 */
@SpringBootTest
@Slf4j
public class DocumentSimpleOperationsTests {

    @Autowired
    public ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * save document
     * @author Leo
     */
    @Test
    public void documentSave() {
        Headmaster headmaster = new Headmaster("1", "小白主任", "山东", 7500,
            Arrays.asList("一班", "二班"), new Date());//班主任
        List<String> colorList = new ArrayList<>();//颜色
        colorList.add("red");
        colorList.add("white");
        colorList.add("black");

        List<Teacher> teacherList = new ArrayList<>();//所有科目老师
        teacherList.add(new Teacher("2", "小黑", "black", "一班",
            "山东省济南市历下区", 13, new Date()));
        teacherList.add(new Teacher("2", "小蓝", "blue", "二班",
            "山东省菏泽市单县", 14, new Date()));

        Student student = new Student("1", "mhh", "济南", 12, new Date(),
            headmaster, new String[]{"语文", "数学", "英语"}, colorList, teacherList);
        elasticsearchRestTemplate.save(student);

        Teacher teacher = new Teacher("1", "小黑老师", "black",
            "一班", "河北省保定市莲池区", 14, new Date());
        elasticsearchRestTemplate.save(teacher);

        List<Teacher> teacherList2 = new ArrayList<>();
        teacherList2.add(new Teacher("1", "小黑老师", "black", "一班",
            "山东省泰安市岱岳区","且随疾风前行,身后亦须留心", 15, new Date(), new Completion(new String[]{"山东省泰安市岱岳区"})));
        teacherList2.add(new Teacher("2", "小白老师", "white", "二班",
            "山东省济南市历下区", "此剑之势,愈斩愈烈",17, new Date(), new Completion(new String[]{"山东省济南市历下区"})));
        teacherList2.add(new Teacher("3", "小黄老师", "yellow", "二班",
            "河北省保定市莲池区", "荣耀存于心,而非流于形",18, new Date(), new Completion(new String[]{"河北省保定市莲池区"})));
        teacherList2.add(new Teacher("5", "小蓝教授", "blue", "二班",
            "山东省济南市高新区", "吾之初心,永世不忘",21, new Date(), new Completion(new String[]{"山东省济南市高新区"})));
        teacherList2.add(new Teacher("4", "小绿教授", "green", "一班",
            "山西省太原市杏花岭区", "有些失误无法犯两次",24, new Date(), new Completion(new String[]{"山西省太原市杏花岭区"})));
        elasticsearchRestTemplate.save(teacherList2);
    }

    /**
     * 通过主键删除文档
     * @author Leo
     */
    @Test
    public void deleteDocumentById() {
        // 根据实体类删除文档，实体类其他数据没用用
        Headmaster headmaster1 = new Headmaster();
        headmaster1.setHId("DTEOD4cB9rewxB62h3gx");
        elasticsearchRestTemplate.delete(headmaster1);
        elasticsearchRestTemplate.delete("DTEOD4cB9rewxB62h3gx", Headmaster.class);
    }

    /**
     * 通过查询条件删除文档
     * @author Leo
     */
    @Test
    public void deleteDocumentByQuery() {
        // 查询条件(词条查询：对应ES query里的match 分词查询)
        // operator：分词后匹配使用and串联，即分词后的词需要全部匹配的才符合条件
        // ik_smart：分词查询时使用的分词器
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("tAddress", "山东省济南市")
            .operator(Operator.AND).analyzer("ik_smart");
        NativeSearchQuery build = new NativeSearchQueryBuilder()
            .withQuery(matchQueryBuilder)
            .build();

        ByQueryResponse response = elasticsearchRestTemplate.delete(build, Teacher.class);
        log.info("delete result:{}", JSONObject.toJSONString(response));
    }

    /**
     * 通过主键修改文档字段；如果主键不存在会报错
     * @author Leo
     */
    @Test
    public void updateById() {
        Teacher teacher = new Teacher();
        teacher.setTName("小钟老师");
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(teacher, false, true);
        Document document = Document.from(stringObjectMap);
        UpdateQuery updateQuery = UpdateQuery.builder("1")
            .withDocument(document)
            .build();

        String indexName = Teacher.class
            .getAnnotation(org.springframework.data.elasticsearch.annotations.Document.class).indexName();

        UpdateResponse response = elasticsearchRestTemplate.update(updateQuery, IndexCoordinates.of(indexName));
        log.info("update result:{}", JSONObject.toJSONString(response));
    }

    /**
     * 通过查询条件修改文档字段；<br/>
     * <a href="https://github.com/spring-projects/spring-data-elasticsearch/blob/4.2.x/src/test/java/org/springframework/data/elasticsearch/core/ElasticsearchTemplateTests.java">参考</a>
     * @author Leo
     */
    @Test
    @SneakyThrows
    public void updateByQuery() {
        Teacher teacher = new Teacher();
        teacher.setTClassName("一班");
        Map<String, Object> teacherMap = BeanUtil.beanToMap(teacher, false, true);

        // 查询条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.termQuery("tId", "1"))
            .build();
        // 根据查询条件更新文档只能只通过es脚本方式
        UpdateQuery updateQuery = UpdateQuery.builder(searchQuery)
            // painless脚本
            .withScript("ctx._source['tClassName'] = params['tClassName']")
            // es脚本分两种类型，一种是inline,另一种是stored
            // inline：内联脚本；stored：事先存储好的脚本，通过id指定需要执行的脚本
            .withScriptType(ScriptType.INLINE)
            // 脚本参数值，map类型；params['?']中的？是map的key
            .withParams(teacherMap)
            // 脚本类型，es内置了painless
            .withLang("painless")
            .withAbortOnVersionConflict(true)
            .build();

        String indexName = Teacher.class
            .getAnnotation(org.springframework.data.elasticsearch.annotations.Document.class).indexName();

        ByQueryResponse response = elasticsearchRestTemplate.updateByQuery(updateQuery, IndexCoordinates.of(indexName));
        log.info("update result:{}", JSONObject.toJSONString(response));

        TimeUnit.SECONDS.sleep(1);
        SearchHits<Teacher> search = elasticsearchRestTemplate.search(searchQuery, Teacher.class);
        log.info("search result:{}", JSONObject.toJSONString(search));
    }

    /**
     * 根据主键id查询文档
     * @author Leo
     */
    @Test
    public void queryById() {
        Teacher teacher = elasticsearchRestTemplate.get("1", Teacher.class);
        log.info("query result: {}", teacher);
    }

    /**
     * 文档是否存在
     * @author Leo
     */
    @Test
    public void documentExist() {
        String tId = "1";
        boolean exists = elasticsearchRestTemplate.exists(tId, Teacher.class);
        log.info("文档id:{}存在结果:{}", tId, exists);
    }

    /**
     * 索引中文档总数
     * @author Leo
     */
    @Test
    public void documentCount() {
        NativeSearchQuery build = new NativeSearchQueryBuilder()
            .build();
        long count = elasticsearchRestTemplate.count(build, Teacher.class);
        log.info("index document size:{}", count);
    }


}








