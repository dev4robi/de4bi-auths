package com.robi.util;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomUtil {
    // [Class public constants]
    // SymOp
    public static final int ALPHABET = 0x01; // 0000 0001
    public static final int SPECIAL  = 0x02; // 0000 0010
    public static final int NUMERIC  = 0x04; // 0000 0100
    
    // [Class private constants]
    private static final Logger logger = LoggerFactory.getLogger(RandomUtil.class);
    private static final Random RANDOM_GENERATOR; // 난수 생성기
    private static final char[] SYMBOL_ALPHABET;  // 알파벳 문자 배열 
    private static final char[] SYMBOL_SPECIAL;   // 특수 문자 배열
    private static final char[] SYMBOL_NUMERIC;   // 숫자 문자 배열
    
    // [Static initializer]
    static {
        RANDOM_GENERATOR = new Random();
        SYMBOL_ALPHABET  = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                                        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
        SYMBOL_SPECIAL   = new char[] { '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '[', ']',
                                        '{', '}', '\\', '|', '\'', '"', '/', '?', '`', '~', ',', '.', '<', '>', ';', ':' };
        SYMBOL_NUMERIC   = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    }
    
    // [Methods]
    /**
     * <p>무작위 문자로 채워진 문자열을 생성합니다.</p>  
     * @param length : 생성할 문자열 길이. 
     * @param symOp : 생성에 사용할 문자 종류.<br>
     * <strong>({@link RandomUtil.ALPHABET} | {@link RandomUtil.SPECIAL} | {@link RandomUtil.NUMERIC})</strong>
     * @return 생성된 length길이의 무작위 문자열.
     */
    public static String genRandomStr(int length, int symOp) {
        try {
            // 파라미터 검사
            if (length < 1) {
                logger.error("'length' is less then 1! (length:" + length + ")");
                return null;
            }
    
            // 옵션 선택
            boolean useAlphabet = false;
            boolean useSpecial = false;
            boolean useNumeric = false;
            
            if ((symOp & ALPHABET) == ALPHABET) {
                useAlphabet = true;
            }
            
            if ((symOp & SPECIAL) == SPECIAL) {
                useSpecial = true;
            }
            
            if ((symOp & NUMERIC) == NUMERIC) {
                useNumeric = true;
            }
    
            // 옵션 검사
            if (!useAlphabet && !useNumeric && !useSpecial) {
                logger.error("Symbol option 'useAlphabet', 'useNumeric and 'useSpecial' are ALL false!");
                return null;
            }
    
            // 사용할 심볼배열 선택
            int selectedSymbolCnt = 0;
                
            selectedSymbolCnt += (useAlphabet ? 1 : 0);
            selectedSymbolCnt += (useNumeric ? 1 : 0);
            selectedSymbolCnt += (useSpecial ? 1 : 0);
                
            char[][] selectedSymbolAry = new char[selectedSymbolCnt][];
            int curSymbolAryCnt = 0;
    
            if (useAlphabet) {
                selectedSymbolAry[curSymbolAryCnt++] = SYMBOL_ALPHABET;
            }
    
            if (useSpecial) {
                selectedSymbolAry[curSymbolAryCnt++] = SYMBOL_SPECIAL;
            }
            
            if (useNumeric) {
                selectedSymbolAry[curSymbolAryCnt++] = SYMBOL_NUMERIC;
            }
            
            // 무작위 문자열 생성
            StringBuilder rtSb = new StringBuilder();
                
            for (int i = 0; i < length; ++i) {
                char[] selectedSymbolOps = selectedSymbolAry[RANDOM_GENERATOR.nextInt(selectedSymbolAry.length)];
                rtSb.append(selectedSymbolOps[RANDOM_GENERATOR.nextInt(selectedSymbolOps.length)]);
            }
            
            return rtSb.toString();
        }
        catch (Exception e) {
            logger.error("Util Exception!", e);
            return null;
        }
    }
    
    /**
     * <p>무작위 문자로 채워진 문자열을 생성합니다.</p>  
     * @param length : 생성할 문자열 길이. 
     * @param symAry : 생성에 사용할 문자 배열.<br>
     * @return 생성된 length길이의 무작위 문자열.
     */
    public static String genRandomStr(int length, char[] symAry) {
        try {
            // 파라미터 검사
            if (length < 0) {
                logger.error("'length' is less then 0! (length:" + length + ")");
                return null;
            }

            if (symAry == null || symAry.length == 0) {
                logger.error("'symAry' is null or zero length! (symAry:" + symAry.toString() + ")");
                return null;
            }
            
            // 무작위 문자열 생성
            StringBuilder rtSb = new StringBuilder();
            
            for (int i = 0; i < length; ++i) {
                rtSb.append(symAry[RANDOM_GENERATOR.nextInt(symAry.length)]);
            }
            
            return rtSb.toString();
        }
        catch (Exception e) {
            logger.error("Util Exception!", e);
            return null;
        }
    }
}
