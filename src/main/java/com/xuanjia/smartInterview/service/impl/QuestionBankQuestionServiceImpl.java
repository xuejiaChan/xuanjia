package com.xuanjia.smartInterview.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuanjia.smartInterview.common.ErrorCode;
import com.xuanjia.smartInterview.constant.CommonConstant;
import com.xuanjia.smartInterview.exception.BusinessException;
import com.xuanjia.smartInterview.exception.ThrowUtils;
import com.xuanjia.smartInterview.mapper.QuestionBankQuestionMapper;
import com.xuanjia.smartInterview.model.dto.questionBankQuestion.QuestionBankQuestionQueryRequest;
import com.xuanjia.smartInterview.model.entity.Question;
import com.xuanjia.smartInterview.model.entity.QuestionBank;
import com.xuanjia.smartInterview.model.entity.QuestionBankQuestion;
import com.xuanjia.smartInterview.model.entity.User;
import com.xuanjia.smartInterview.model.vo.QuestionBankQuestionVO;
import com.xuanjia.smartInterview.model.vo.UserVO;
import com.xuanjia.smartInterview.service.QuestionBankQuestionService;
import com.xuanjia.smartInterview.service.QuestionBankService;
import com.xuanjia.smartInterview.service.QuestionService;
import com.xuanjia.smartInterview.service.UserService;
import com.xuanjia.smartInterview.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题库题目关联服务实现
 *
 * @author <a href="Give you fight!">程序员宣佳</a>
 * @from <a href="fighting!">程序员宣佳</a>
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion>implements QuestionBankQuestionService {

    @Resource
    private UserService userService;
    @Autowired
    @Lazy
    private QuestionBankService questionBankService;

    @Autowired
    @Lazy
    private QuestionService questionService;
    @Autowired
    private QuestionBankQuestionService questionBankQuestionService;

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);
        Long questionId = questionBankQuestion.getQuestionId();
        Long questionBankId = questionBankQuestion.getQuestionBankId();
        if(questionBankId != null){
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR,"查询的数据库不存在！");
        }
        if(questionId != null){
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(questionId == null,ErrorCode.NOT_FOUND_ERROR,"查询题目不存在！");
        }
    }

/**
 * 获取查询条件
 *
 * @param questionBankQuestionQueryRequest
 * @return
 */
@Override
public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }

        // todo 从对象中取值
        Long id = questionBankQuestionQueryRequest.getId();
        Long notId = questionBankQuestionQueryRequest.getNotId();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();
        String sortField = questionBankQuestionQueryRequest.getSortField();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();
        Long userId = questionBankQuestionQueryRequest.getUserId();
        // todo 补充需要的查询条件
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId),"questionBankId",questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId),"questionId",questionId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题库题目关联封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankQuestionVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态

        // endregion

        return questionBankQuestionVO;
    }

    /**
     * 分页获取题库题目关联封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList.stream().map(questionBankQuestion -> {
            return QuestionBankQuestionVO.objToVo(questionBankQuestion);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));


        questionBankQuestionVOPage.setRecords(questionBankQuestionVOList);
        return questionBankQuestionVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionTOBank(List<Long> questionIdList, Long questionBankId, User loginUser){
        ThrowUtils.throwIf(questionBankId == null || questionBankId < 0, ErrorCode.OPERATION_ERROR, "传入的数据库 ID 非法！");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.OPERATION_ERROR, "传入的数据库用户信息为空！");
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.OPERATION_ERROR,"传入的题目信息为空！");
        List<Question> questionList1 = questionService.listByIds(questionIdList);
        List<Long> questionIds = questionList1.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIds), ErrorCode.OPERATION_ERROR,"传入的题目信息 ID 不合法！");

        QuestionBank questionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.OPERATION_ERROR, "传入的数据库信息不合法！");

        for (Long questionId : questionIds){
            QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
            questionBankQuestion.setQuestionBankId(questionBankId);
            questionBankQuestion.setQuestionId(questionId);
            questionBankQuestion.setUserId(loginUser.getId());
            boolean save = this.save(questionBankQuestion);
            if(!save){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"无法插入该条数据信息！");
            }
        }
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDeleteQuestionTOBank(List<Long> questionIds, Long questionBankId){
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIds),ErrorCode.OPERATION_ERROR,"要删除的题目信息为空！");
        for(Long questionId : questionIds){
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class);
            lambdaQueryWrapper.eq(QuestionBankQuestion::getQuestionId, questionId)
                    .eq(QuestionBankQuestion::getId,questionBankId);
            boolean remove = this.remove(lambdaQueryWrapper);
            if(!remove){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"题目删除失败！");
            }
        }
    }




}
