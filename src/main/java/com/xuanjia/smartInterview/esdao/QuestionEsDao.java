package com.xuanjia.smartInterview.esdao;

import com.xuanjia.smartInterview.model.dto.post.PostEsDTO;
import com.xuanjia.smartInterview.model.dto.question.QuestionEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO, Long> {
    List<PostEsDTO> findByUserId(Long userId);
}
