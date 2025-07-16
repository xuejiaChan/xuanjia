package com.xuanjia.smartInterview.esdao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QuestionEsDaoTest {
    @Resource
    private QuestionEsDao questionEsDao;

    @Test
    public void findByUserID(){
        questionEsDao.findAll();
    }
}