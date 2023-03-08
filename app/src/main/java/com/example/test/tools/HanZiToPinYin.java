package com.example.test.tools;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.*;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * @author : hqx
 * @date : 27/2/2023 上午 10:38
 * @descriptions: null
 */
public class HanZiToPinYin {
    //    public static String toPinYin(char hanzi){
//        HanyuPinyinOutputFormat hanyuPinyin = new HanyuPinyinOutputFormat();
//        hanyuPinyin.setCaseType(HanyuPinyinCaseType.LOWERCASE);
//        hanyuPinyin.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
//        hanyuPinyin.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
//        String[] pinyinArray=null;
//        try {
//            //是否在汉字范围内
//            if(hanzi>=0x4e00 && hanzi<=0x9fa5){
//                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(hanzi, hanyuPinyin);
//            }
//        } catch (BadHanyuPinyinOutputFormatCombination e) {
//            e.printStackTrace();
//        }
//        //将获取到的拼音返回
//        return pinyinArray[0];
//    }
    public static String getPinYinAllChar(String zn_str, int caseType) throws BadHanyuPinyinOutputFormatCombination {
        char[] strChar = zn_str.toCharArray();
        HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();
        // 输出设置，大小写，音标方式等
        if (1 == caseType) {
            hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        } else {
            hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        }
        hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
        hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);

        StringBuffer pyStringBuffer = new StringBuffer();
        String[] strString = new String[strChar.length];
        try {
            for (int i = 0; i < strChar.length; i++) {
                if (Character.toString(strChar[i]).matches("[\\u4E00-\\u9FA5]+")) {//如果是汉字字符
                    //将汉字的几种全拼都存到strString数组中
                    strString = PinyinHelper.toHanyuPinyinStringArray(strChar[i], hanYuPinOutputFormat);
                    pyStringBuffer.append(strString[0]);//取出该汉字全拼的第一种读音并连接到字符串pyStringBuffer后
                } else {
                    //如果不是汉字字符，直接取出字符并连接到字符串pyStringBuffer后
                    pyStringBuffer.append(Character.toString(strChar[i]));
                }
                pyStringBuffer.append(' ');
            }
        } catch (
                BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return pyStringBuffer.toString();
    }
}
