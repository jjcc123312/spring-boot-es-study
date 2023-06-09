package com.leo.service.study.config;

import java.net.InetSocketAddress;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "elasticsearch")
@Configuration
@Data
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    private String host;
    private Integer port;
    //重写父类方法
    @Override
    @Bean(destroyMethod = "close")
    public RestHighLevelClient elasticsearchClient() {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);

        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
            .connectedTo(inetSocketAddress)
            .build();

        return RestClients.create(clientConfiguration).rest();

    }
}
