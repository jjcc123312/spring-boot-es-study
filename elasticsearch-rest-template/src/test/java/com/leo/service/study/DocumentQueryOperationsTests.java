package com.leo.service.study;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.leo.service.study.elasticsearch.domain.Product;
import com.leo.service.study.elasticsearch.domain.Student;
import com.leo.service.study.elasticsearch.domain.Teacher;
import com.leo.service.study.elasticsearch.domain.TestIndexObject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;

/**
 * es 索引文档复杂查询
 *
 * @author Leo
 * @version 1.0 2023/3/27
 */
@SpringBootTest
@Slf4j
public class DocumentQueryOperationsTests {


    @Autowired
    public ElasticsearchRestTemplate elasticsearchRestTemplate;


    /**
     * 分页查询与排序
     * @author Leo
     * @param pageNo 页码
     * @param pgeSize 页数
     * @param sort 排序
     * @param query 查询条件
     * @param tClass 查询索引对应的实体类
     */
    private <T> void pageSearch(int pageNo, int pgeSize, Sort sort, QueryBuilder query, Class<T> tClass) {
        NativeSearchQuery build = new NativeSearchQueryBuilder()
            .withQuery(query)
            .withPageable(PageRequest.of((pageNo - 1), pgeSize))
            .build();

        if (ObjectUtil.isNotNull(sort)) {
            build.addSort(sort);
        }

        String indexName = tClass.getAnnotation(Document.class).indexName();
        SearchHits<T> response = elasticsearchRestTemplate.search(build, tClass, IndexCoordinates.of(indexName));
        log.info("page search result:{}", JSONObject.toJSONString(response));
    }

    /**
     * 分页查询与排序
     * @author Leo
     */
    @Test
    public void queryForPage() {
        Sort sort = Sort.by(Order.desc("tAge"));
        Sort tAge = Sort.by(Direction.DESC, "tAge");
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("tAddress", "山东省").analyzer("ik_smart");
        pageSearch(1, 10, sort, queryBuilder, Teacher.class);
    }

    /**
     * 分词
     * @author Leo
     */
    @Test
    @SneakyThrows
    public void analyzer() {
        RestHighLevelClient restHighLevelClient = elasticsearchRestTemplate.execute(client -> client);
        // tokenizer：粗细粒度分词(粗粒度:ik_smart   细粒度:ik_max_word)
        // text: 需要分词的入参
        AnalyzeRequest smartAnalyze = AnalyzeRequest.buildCustomAnalyzer("ik_smart").build("北京天安门");
        AnalyzeRequest maxWordAnalyze = AnalyzeRequest.buildCustomAnalyzer("ik_max_word").build("北京天安门");

        AnalyzeResponse smartResponse = restHighLevelClient.indices().analyze(smartAnalyze, RequestOptions.DEFAULT);
        AnalyzeResponse maxWordResponse = restHighLevelClient.indices().analyze(maxWordAnalyze, RequestOptions.DEFAULT);
        log.info("ik smart analyzer result:{}", JSONObject.toJSONString(smartResponse));
        log.info("ik max word analyzer result:{}", JSONObject.toJSONString(maxWordResponse));
    }

    /**
     * 入参分词: 山东省济南市  ik_smart粗粒度:[山东省,济南市] ik_max_word细粒度:[山东省,山东,省,济南市,济南,南市]
     *
     * @param operator  : Operator.OR(并集) [默认] 只要分的词有一个和索引字段上对应上则就返回
     *                  Operator.AND(交集)   分的词全部满足的数据返回
     * @param analyzer  : 选择分词器[ik_smart粗粒度,ik_max_word细粒度] 默认:ik_max_word细粒度
     * @param key       :  es里索引的域(字段名)
     * @param classType :  返回的list里的对象并且通过对象里面@Document注解indexName属性获取查询哪个索引
     * @param text    :  查询的值
     * @return java.util.List<T>
     */
    private <T> List<T> matchQuery(Operator operator, String analyzer, String key, Class<T> classType, String text) {
        //查询条件(词条查询：对应ES query里的match)
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(key, text).analyzer(analyzer).operator(operator);
        //创建查询条件构建器SearchSourceBuilder(对应ES外面的大括号)
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(matchQueryBuilder);
        //查询,获取查询结果
        SearchHits<T> search = elasticsearchRestTemplate.search(nativeSearchQuery, classType);
        //获取总记录数
        long totalHits = search.getTotalHits();
        //获取值返回
        return search.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    @Test
    public void matchQuery() {
        List<Teacher> teachers = matchQuery(Operator.OR, "ik_max_word", "tAddress", Teacher.class, "山东省");
        log.info("match query result:{}", teachers);
    }

    /**
     * wildcardQuery模糊查询(会对查询条件分词，还可以使用通配符)[?:表示任意单个字符][*:表示0或多个字符]
     * @param key       :  es里索引的域(字段名)
     * @param value     : 查询的值
     * @param classType : 返回的list里的对象并且通过对象里面@Document注解indexName属性获取查询哪个索引
     */
    private <T> void wildcardQuery(String key, String value, Class<T> classType) {
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(key, value);
        NativeSearchQuery searchQuery = new NativeSearchQuery(wildcardQueryBuilder);

        SearchHits<T> response = elasticsearchRestTemplate.search(searchQuery, classType);
        log.info("wildcardQuery result: {}", JSONObject.toJSONString(response));
    }

    /**
     * 使用wildcard相当于SQL的like，前后都可以拼接*，表示匹配0到多个任意字符，？表示匹配1个任意字符
     * 加.keyword是要匹配完整的词
     * @author Leo
     */
    @Test
    public void wildcardQuery() {
        wildcardQuery("tAddress", "*省", Teacher.class);
        wildcardQuery("tAddress", "山东省*", Teacher.class);
    }


    /**
     * prefixQuery 前缀查询 对keyword类型支持比较好(text也能用:索引库字段分词后 分的词前缀要是能匹配也是可以返回此数据)
     * @param key       :  es里索引的域(字段名)
     * @param value     : 查询的值
     * @param classType : 返回的list里的对象并且通过对象里面@Document注解indexName属性获取查询哪个索引
     */
    private  <T> void prefixQuery(String key, String value, Class<T> classType) {
        //查询条件(词条查询：对应ES query里的prefixQuery)
        PrefixQueryBuilder prefixQueryBuilder = QueryBuilders.prefixQuery(key, value);
        //创建查询条件构建器SearchSourceBuilder(对应ES外面的大括号)
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(prefixQueryBuilder);
        //查询,获取查询结果
        SearchHits<T> response = elasticsearchRestTemplate.search(nativeSearchQuery, classType);
        log.info("prefixQueryBuilder result: {}", JSONObject.toJSONString(response));

    }

    @Test
    public <T> void prefixQuery() {
        prefixQuery("tClassName", "一", Teacher.class);
    }

    /**
     * 正则表达式查询:regexpQuery
     * @author Leo
     */
    @Test
    public void regexpQuery() {
        RegexpQueryBuilder regexpQueryBuilder = QueryBuilders.regexpQuery("tEnglishName", "b+(.)*");
        NativeSearchQuery searchQuery = new NativeSearchQuery(regexpQueryBuilder);

        SearchHits<Teacher> response = elasticsearchRestTemplate.search(searchQuery, Teacher.class);
        log.info("prefixQueryBuilder result: {}", JSONObject.toJSONString(response));
    }

    /**
     * 纠错查询
     *
     * @author Leo
     */
    @Test
    public void fuzzyQuery() {
        FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery("tAddress", "山东1省");
        NativeSearchQuery searchQuery = new NativeSearchQuery(fuzzyQueryBuilder);

        SearchHits<Teacher> response = elasticsearchRestTemplate.search(searchQuery, Teacher.class);
        log.info("prefixQueryBuilder result: {}", JSONObject.toJSONString(response));
    }

    /**
     * 范围查询；es中可以对日期、数据、IP地址做范围查询
     * <pre>
     * from(Object, includeLower): 做”>=“查询或者">"查询
     *   Object: 范围查询的值，如果是null，表示不需要大于查询
     *   includeLower: 是否包含范围查询的值，true: ”>=“,false: ”>“
     *
     * to(Object, includeUpper): 做”<=“查询或者"<"查询，如果是null，表示不需要小于查询
     *   Object: 范围查询的值
     *   includeUpper: 是否包含范围查询的值，true: ”<=“,false: ”<“
     * </pre>
     * @author Leo
     */
    @Test
    public void rangeQueryByFromTo() {
        // tAge >= 15 and tAge < 18
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("tAge")
            .from(15).to(18, false);

        SourceFilter sourceFilter = new FetchSourceFilterBuilder()
            .withIncludes("tId", "tName", "tAge")
            .build();

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(rangeQueryBuilder)
            .withSourceFilter(sourceFilter)
            .build();

        SearchHits<Teacher> response = elasticsearchRestTemplate.search(searchQuery, Teacher.class);
        List<SearchHit<Teacher>> searchHits = response.getSearchHits();
        log.info("rangeQuery result: {}", JSONObject.toJSONString(searchHits));
    }

    /**
     * 范围查询；es中可以对日期、数据、IP地址做范围查询
     * @author Leo
     */
    @Test
    public void rangeQueryByGtLt() {
        // tAge >= 15 and tAge < 18
        RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery("tAge")
            .gte(15).lt(18);

        SourceFilter sourceFilter = new FetchSourceFilterBuilder()
            .withIncludes("tId", "tName", "tAge")
            .build();

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryBuilder)
            .withSourceFilter(sourceFilter)
            .build();

        SearchHits<Teacher> response = elasticsearchRestTemplate.search(searchQuery, Teacher.class);
        List<SearchHit<Teacher>> searchHits = response.getSearchHits();
        log.info("rangeQuery result: {}", JSONObject.toJSONString(searchHits));
    }

    /*
     *      多域查询的交并集理解:
     *                      OR:  只要有一个域中包含入参value被分词后的"一个值"时就返回
     *                      AND: 只要有一个域中包含入参value被分词后的"所有值"时返回
     *
     * @param fields    : Map<String,Float>类型:key为域名,Float为boost值
     *                  boost: 参数被用来提升一个语句的相对权重（ boost 值大于 1 ）或降低相对权重（ boost 值处于 0 到 1 之间），
     *                          但是这种提升或降低并不是线性的，换句话说，如果一个 boost 值为 2 ，并不能获得两倍的评分 _score 。
     * @param queryString   : 要查询的值 (会对查询条件进行分词)
     * @param analyzer  : 选择分词器[ik_smart粗粒度,ik_max_word细粒度] 默认:ik_max_word细粒度
     * @param operator  : Operator.OR(并集) [默认] 只要分的词有一个和索引字段上对应上则就返回
     *                  Operator.AND(交集)   分的词全部满足的数据返回
     * @param classType :  返回的list里的对象并且通过对象里面@Document注解indexName属性获取查询哪个索引
     * @return java.util.List<T>
     * @explain :  queryString 多条件查询
     * •会对查询条件进行分词。
     * •然后将分词后的查询条件和词条进行等值匹配
     * •默认取并集（OR）
     * •可以指定多个查询字段
     * •query_string：识别query中的连接符（or 、and）
     * @Author Mhh
     * @Date 2021/12/12 19:45
     */
    public <T> void queryStringQuery(Map<String, Float> fields, String queryString, String analyzer, Operator operator,
        Class<T> classType) {
        //查询条件(词条查询：对应ES query里的queryStringQuery)
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(queryString).fields(fields)
            .analyzer(analyzer).defaultOperator(operator);
        //创建查询条件构建器SearchSourceBuilder(对应ES外面的大括号)
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQueryBuilder);
        //查询,获取查询结果
        SearchHits<T> search = elasticsearchRestTemplate.search(nativeSearchQuery, classType);
        log.info("rangeQuery result: {}", JSONObject.toJSONString(search));
    }

    @Test
    public void queryStringQuery() {
        Map<String, Float> fieldMap = new HashMap<>();
        fieldMap.put("tAddress", (float) 1);
        fieldMap.put("tEnglishName", (float) 1);
        String queryString = "blue是山西省的";
        queryStringQuery(fieldMap, queryString, "ik_smart", Operator.OR, Teacher.class);

        QueryBuilders.multiMatchQuery("blue是山西省的", "tAddress", "tEnglishName");
    }


    /**
     * 深度分页 by scroll
     * @author Leo
     */
    @Test
    public void scrollSearch() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withPageable(PageRequest.of(0, 1))
            .build();
        // 深度分页有效期
        searchQuery.setScrollTime(Duration.ofMinutes(1));

        SearchHitsIterator<Product> studentSearchHitsIterator = elasticsearchRestTemplate.searchForStream(searchQuery,
            Product.class);

        List<SearchHit<Product>> collect = studentSearchHitsIterator.stream().collect(Collectors.toList());
        log.info("scroll search result:{}", JSONObject.toJSONString(collect));

    }



}









