package com.xuanjia.smartInterview;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;


import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class ElasticSearchTest {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    private final String INDEX_NAME = "test_index";

    @Test
    public void indexDocument(){
        Map<String, Object> doc = new HashMap<>();
            doc.put("title", "Elasticsearch Introduction");
            doc.put("content", "Learn Elasticsearch basics and advanced usage.");
            doc.put("tags", "elasticsearch,search");
            doc.put("answer", "Yes");
            doc.put("userId", 1L);
            doc.put("editTime", "2023-09-01 10:00:00");
            doc.put("createTime", "2023-09-01 09:00:00");
            doc.put("updateTime", "2023-09-01 09:10:00");
            doc.put("isDelete", false);
            IndexQuery indexQuery = new IndexQueryBuilder().withId("1").withObject(doc).build();
            String documentId = elasticsearchRestTemplate.index(indexQuery, IndexCoordinates.of(INDEX_NAME));
            assertThat(documentId).isNotNull();
    }

    @Test
    public void getDocument(){
        String documentId = "1";
        Map<String, Object> document = elasticsearchRestTemplate.get(documentId, Map.class, IndexCoordinates.of(INDEX_NAME));
        assertThat(document).isNotNull();
        assertThat(document.get("title")).isEqualTo("Elasticsearch Introduction");
    }

    @Test
    public void deleteDocument(){
        String documentID = "1";
        String result = elasticsearchRestTemplate.delete(documentID, IndexCoordinates.of(INDEX_NAME));
        assertThat(result).isNotNull();
    }

    @Test
    public void deleteIndex(){
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(IndexCoordinates.of(INDEX_NAME));
        boolean delete = indexOperations.delete();
        assertThat(delete).isTrue();
    }
}
