package com.xuanjia.smartInterview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjia.smartInterview.model.entity.Post;
import java.util.Date;
import java.util.List;

/**
 * 帖子数据库操作
 *
 * @author <a href="Give you fight!">程序员宣佳</a>
 * @from <a href="https://xuanjia.icu">今天也要敲代码哟！</a>
 */
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 查询帖子列表（包括已被删除的数据）
     */
    List<Post> listPostWithDelete(Date minUpdateTime);

}




