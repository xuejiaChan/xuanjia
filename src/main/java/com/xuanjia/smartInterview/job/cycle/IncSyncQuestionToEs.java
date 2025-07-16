package com.xuanjia.smartInterview.job.cycle;

import com.xuanjia.smartInterview.esdao.QuestionEsDao;
import com.xuanjia.smartInterview.mapper.QuestionMapper;
import com.xuanjia.smartInterview.model.dto.question.QuestionEsDTO;
import com.xuanjia.smartInterview.model.entity.Question;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class IncSyncQuestionToEs {

    @Resource
    private QuestionEsDao questionEsDao;

    @Resource
    private QuestionMapper questionMapper;

    @Scheduled(fixedRate = 60 * 1000)
    public void scheduledRun(){
        Date fiveMinUpdate = new Date(new Date().getTime() - 5 * 6000L);
        List<Question> questionList = questionMapper.listQuestionWithDelete(fiveMinUpdate);

        List<QuestionEsDTO> esDTOList = questionList.stream()
                .map(QuestionEsDTO::objToDto)
                .collect(Collectors.toList());

        int update = 500;
        int size = esDTOList.size();

        for(int i = 0; i < size; i = i + update){
            int min = Math.min(i + update, size);
            questionEsDao.saveAll(esDTOList.subList(i, min));
        }
    }
}
