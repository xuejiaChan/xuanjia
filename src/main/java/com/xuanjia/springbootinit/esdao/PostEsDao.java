package com.xuanjia.springbootinit.esdao;

import com.xuanjia.springbootinit.model.dto.post.PostEsDTO;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 帖子 ES 操作
 *
 * @author <a href="https://github.com/lixuanjia">程序员宣佳</a>
 * @from <a href="https://xuanjia.icu">今天也要敲代码哟！</a>
 */
public interface PostEsDao extends ElasticsearchRepository<PostEsDTO, Long> {

    List<PostEsDTO> findByUserId(Long userId);
}