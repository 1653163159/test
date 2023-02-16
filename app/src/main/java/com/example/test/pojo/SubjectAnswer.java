package com.example.test.pojo;

/**
 * @author : hqx
 * @date : 15/2/2023 下午 8:34
 * @descriptions: 题目答案
 */
public class SubjectAnswer {
    public SubjectAnswer(String number, String answer) {
        this.number = number;
        this.answer = answer;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    String number, answer;
}
