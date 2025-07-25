package com.xuanjia.smartInterview.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuanjia.smartInterview.common.ErrorCode;
import com.xuanjia.smartInterview.constant.CommonConstant;
import com.xuanjia.smartInterview.esdao.QuestionEsDao;
import com.xuanjia.smartInterview.exception.ThrowUtils;
import com.xuanjia.smartInterview.mapper.QuestionMapper;
import com.xuanjia.smartInterview.model.dto.question.QuestionQueryRequest;
import com.xuanjia.smartInterview.model.entity.Question;
import com.xuanjia.smartInterview.model.entity.QuestionBank;
import com.xuanjia.smartInterview.model.entity.QuestionBankQuestion;
import com.xuanjia.smartInterview.model.entity.User;
import com.xuanjia.smartInterview.model.vo.QuestionVO;
import com.xuanjia.smartInterview.model.vo.UserVO;
import com.xuanjia.smartInterview.service.QuestionBankQuestionService;
import com.xuanjia.smartInterview.service.QuestionBankService;
import com.xuanjia.smartInterview.service.QuestionService;
import com.xuanjia.smartInterview.service.UserService;
import com.xuanjia.smartInterview.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目服务实现
 *
 * @author <a href="Give you fight!">程序员宣佳</a>
 * @from <a href="fighting!">程序员宣佳</a>
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;
    @Autowired
    private QuestionBankService questionBankService;
    @Autowired
    private QuestionBankQuestionService questionBankQuestionService;
    @Autowired
    private QuestionEsDao questionEsDao;

    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = question.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        String searchText = questionQueryRequest.getSearchText();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        List<String> tagList = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        // 对象转封装类
        QuestionVO questionVO = QuestionVO.objToVo(question);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long questionId = question.getId();
        User loginUser = userService.getLoginUserPermitNull(request);


        return questionVO;
    }

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            return QuestionVO.objToVo(question);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> questionIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> questionIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);

        // 填充信息
        questionVOList.forEach(questionVO -> {
            Long userId = questionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
        });
        // endregion

        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

    /**
     * 获取题库详情接口
     * @param questionQueryRequest
     * @return
     */
    public Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest){
        int pageSize = questionQueryRequest.getPageSize();
        int current = questionQueryRequest.getCurrent();
        QueryWrapper<Question> queryWrapper = this.getQueryWrapper(questionQueryRequest);
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        if (questionBankId != null){
            LambdaQueryWrapper<QuestionBankQuestion> questionLambdaQueryWrapper = new LambdaQueryWrapper<>();
            questionLambdaQueryWrapper.select(QuestionBankQuestion::getQuestionId)
                    .eq(QuestionBankQuestion::getQuestionBankId,questionBankId);
            List<QuestionBankQuestion> bankQuestionList = questionBankQuestionService.list(questionLambdaQueryWrapper);
            if(CollUtil.isNotEmpty(bankQuestionList)){
                Set<Long> questionSet = bankQuestionList.stream()
                        .map(QuestionBankQuestion::getQuestionId)
                        .collect(Collectors.toSet());
                queryWrapper.in("id",questionSet);
            }

        }
        Page<Question> questionPage = this.page(new Page<>(current, pageSize), queryWrapper);
        return questionPage;
    }

    @Override
    public Page<Question> searchFromEs(QuestionQueryRequest questionQueryRequest) {
        Long quesitonId = questionQueryRequest.getId();
        String content = questionQueryRequest.getContent();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //构造过滤条件
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));

        return null;
    }





}
