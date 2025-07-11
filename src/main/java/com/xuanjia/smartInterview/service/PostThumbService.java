package com.xuanjia.smartInterview.service;

import com.xuanjia.smartInterview.model.entity.PostThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuanjia.smartInterview.model.entity.User;

/**
 * 帖子点赞服务
 *
 * @author <a href="Give you fight!">程序员宣佳</a>
 * @from <a href="https://xuanjia.icu">今天也要敲代码哟！</a>
 */
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 点赞
     *
     * @param postId
     * @param loginUser
     * @return
     */
    int doPostThumb(long postId, User loginUser);

    /**
     * 帖子点赞（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostThumbInner(long userId, long postId);
}
