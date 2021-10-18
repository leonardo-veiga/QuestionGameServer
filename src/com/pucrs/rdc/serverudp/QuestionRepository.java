package com.pucrs.rdc.serverudp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class QuestionRepository {

    List<Question> questionList;

    public QuestionRepository() {
        this.questionList = new ArrayList<>();
    }

    public List<Question> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }

    public void getQuestions(int difficulty) {
        String fileName = "src/com/pucrs/rdc/serverudp/questions/questions.txt";

        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

            stream.forEach(line -> {

            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
