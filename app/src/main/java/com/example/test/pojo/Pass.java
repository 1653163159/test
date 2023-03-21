package com.example.test.pojo;

/**
 * @author : hqx
 * @date : 17/3/2023 下午 12:39
 * @descriptions:
 */
public class Pass {
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    int index, times;
    String name, state;

    public Pass(int index, int times, String name, String state) {
        this.index = index;
        this.times = times;
        this.name = name;
        this.state = state;
    }
}
