package com.example.test.Practice;

/**
 * @author : hqx
 * @date : 3/3/2023 上午 9:08
 * @descriptions: 题目类型
 */
public class Flags {
    public final static String LISTEN_TYPE1 = "HSK听力真题";
    public final static String LISTEN_TYPE2 = "听力理解";
    public final static String SPEAK_TYPE1 = "发音练习";
    public final static String SPEAK_TYPE2 = "阅读材料";
    public final static String WRITE_TYPE1 = "阅读练习";
    public final static String WRITE_TYPE2 = "作文练习";
    public final static String WRITE_TYPE3 = "小测试";

    public static int COMPOSITION_COUNT = 41;
    public static int STACK_BEGINNER_COUNT = 1595;
    public static int STACK_INTERMEDIATE_COUNT = 7087;
    public static int STACK_ADVANCED_COUNT = 4002;
    public static int QUIZ_BEGINNER_COUNT = 3422;
    public static int QUIZ_INTERMEDIATE_COUNT = 2838;
    public static int QUIZ_ADVANCED_COUNT = 632;
    public static int WORD_BEGINNER_COUNT = 1195;
    public static int WORD_INTERMEDIATE_COUNT = 420;
    public static int WORD_ADVANCED_COUNT = 500;

    public final static String BEGINNER = "beginner";
    public final static String INTERMEDIATE = "intermediate";
    public final static String ADVANCED = "advanced";

    public final static String isRead = "yes";
    public final static String notRead = "no";

    //URL相关
    public final static String PREFIX = "http://124.223.115.35/rest/";
    public final static String COMPOSITION_URL = "composition/";//参数：level(可选);position(可选)
    public final static String STACK_URL = "getBookNameByLevel/";//参数：level;position(可选)
    public final static String STACK_BOOK_URL = "getChapterList/";//参数：bookName
    public final static String WORD_URL = "getWordList/";//参数：level;position(可选)
    public final static String QUIZ_URL = "getQuizByLevel/";//参数：level;position(可选)
    public final static String New_Friend_URL = "new_friend_request/";//参数：发起人/接收人
    public final static String GET_Friend_List_URL = "friend_request_list/";//参数：发起人/接收人
    public final static String DELETE_Friend_URL = "delete_friend_request/";//参数：发起人/接收人
}
