package com.leo.service.study.elasticsearch.repository;

import com.leo.service.study.elasticsearch.domain.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDao extends ElasticsearchRepository<Product, String>{



}
