package com.xuanjia.smartInterview.model.dto.questionBankQuestion;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QuestionBankQuestionReomveRequest implements Serializable {

    private Long questionBankId;

    private List<Long> questionId;

    private static final Long serialVersionUID = 1L;

}
