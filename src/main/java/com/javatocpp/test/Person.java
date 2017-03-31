package com.javatocpp.test;

import java.io.*;
/**
 * Created by ZhuChao on 2017/3/15.
 */
public class Person {
    private String name = "";
    private int age;
    private int[] grades;
    private String[] test;
    private StringBuilder builder;

    public Person() {
        name = new String("lllll");
        grades = new int[10];
        for (int i = 0; i < 10; i++) {
            grades[i] = i;
        }
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int[] getGrades() {
        return grades;
    }
    public String[] getTest() {
        return  test;
    }
}
