package com.cps.middleware.db.router.util;

/**
 * @author cps
 * @description: String工具包
 * @date 2024/3/20 16:20
 * @OtherDescription: Other things
 */
public class StringUtils {

    public static String middleScoreToCamelCase(String input){
        StringBuilder outPut = new StringBuilder();
        boolean nextUpperCase = false;
        for(int i = 0 ; i < input.length(); i++){
            char currentChar = input.charAt(i);

            if(currentChar == '-') {
                nextUpperCase = true;
            }
            else {
                if(nextUpperCase){
                    outPut.append(Character.toUpperCase(currentChar));
                    nextUpperCase = false;
                }else {
                    outPut.append(currentChar);
                }
            }
        }
        return outPut.toString();
    }
}
