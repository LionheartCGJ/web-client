package com.cgj.web.http;

import java.util.StringTokenizer;

public final class StringUtils {

    public static String[] split(String s, char delim) {
        String[] retVal = {s, ""};
        int delimIndex = s.indexOf(delim);
        if (delimIndex != -1) {
            retVal[0] = s.substring(0, delimIndex);
            retVal[1] = s.substring(delimIndex + 1);
        }
        return retVal;
    }
    
    public static String[] split(String s, String delim) {
        String[] retVal = {s, ""};
        int delimIndex = s.indexOf(delim);
        if (delimIndex != -1) {
            retVal[0] = s.substring(0, delimIndex);
            retVal[1] = s.substring(delimIndex + 1);
        }
        return retVal;
    }

    public static String[] splitCompletely(String s, String delim) {
        StringTokenizer stringTokenizer = new StringTokenizer(s, delim);

        int tokenCount = stringTokenizer.countTokens();
        String[] st = new String[tokenCount];

        for (int i = 0; i < tokenCount; i++) {
            st[i] = stringTokenizer.nextToken();
        }
        return st;
    }
    
    public static boolean isEmpty(String o) {
		boolean expr = (o == null)
				|| ((o instanceof String) && (((String) o).length() == 0));
		return expr;
	}
}