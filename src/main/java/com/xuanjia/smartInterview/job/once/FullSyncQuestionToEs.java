package com.xuanjia.smartInterview.job.once;

import com.xuanjia.smartInterview.esdao.QuestionEsDao;
import com.xuanjia.smartInterview.model.dto.question.QuestionEsDTO;
import com.xuanjia.smartInterview.model.entity.Question;
import com.xuanjia.smartInterview.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Slf4j
public class FullSyncQuestionToEs implements CommandLineRunner {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionEsDao questionEsDao;

    /**
     * 全量同步内容到 elastic search 中
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        List<Question> list = questionService.list();
        List<QuestionEsDTO> esDTOList = list.stream()
                .map(QuestionEsDTO::objToDto)
                .collect(Collectors.toList());
        int tongbu = 500;
        int size = esDTOList.size();
        for(int i = 0; i < size; i = i + tongbu){
            int min = Math.min(i + tongbu, size);
            List<QuestionEsDTO> questionEsDTOS = esDTOList.subList(i, i + min);
              questionEsDao.saveAll(questionEsDTOS);
        }
    }
}
