/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Dennis
 */
public class StringUtil {

    private static final Pattern STACK_NOTATION_PATTERN = Pattern.compile("^(\\d+)x(\\d{1,2})\\+(\\d{1,2})", 0);
    private static final char[] hexValues = {0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66};

    public static String extract(String text, String start, String end) {
        int left = text.indexOf(start) + start.length();
        if (left == -1) {
            return null;
        }
        int right = text.indexOf(end, left);
        if (right == -1) {
            return null;
        }
        return text.substring(left, right);
    }

    public static String extract(String text, String start, String end, int offset) {
        int left = text.indexOf(start, offset);
        if (left == -1) {
            return null;
        }
        int right = text.indexOf(end, left + start.length());
        if (right == -1) {
            return null;
        }
        return text.substring(left + start.length(), right);
    }

    public static String[] extractAll(final String text, final String start, final String end) {
        String result = "";
        LinkedList<String> res_ = new LinkedList<>();
        int offset = 0;
        while (true) {
            int left = text.indexOf(start, offset);
            if (left == -1) {
                String[] res = new String[res_.size()];
                res = res_.toArray(res);
                return res;
            }
            int right = text.indexOf(end, left + start.length());
            if (right == -1) {
                String[] res = new String[res_.size()];
                res = res_.toArray(res);
                return res;
            }
            res_.add(text.substring(left + start.length(), right));
            offset = right + end.length();
        }
    }

    public static String hexString(byte[] binary) {
        char[] hex = new char[binary.length * 2];
        for (int i = 0; i < binary.length; i++) {
            hex[2 * i] = hexValues[(binary[i] >> 4) & 0xF];
            hex[2 * i + 1] = hexValues[binary[i] & 0xF];
        }
        return new String(hex);
    }

    private static int findWrapPoint(String sentence, int offset, int length) {
        if (sentence.length() <= length + offset) {
            return sentence.length();
        }
        int i = length + offset;
        int k = offset;
        while ((k = sentence.indexOf(" ", k + 1)) < length + offset && k != -1) {
            i = k;
        }
        return i;
    }

    public static ArrayList<String> wrap(String sentence, int length) {
        ArrayList<String> lines = new ArrayList<>();
        int k = 0, l;
        while (true) {
            l = findWrapPoint(sentence, k, length);
            lines.add(sentence.substring(k, l));
            if (l == sentence.length()) {
                break;
            }
            k = l + 1;
        }
        return lines;
    }

    public static boolean isAnagram(String firstWord, String secondWord) {
        char[] word1 = firstWord.replaceAll("[\\s]", "").toCharArray();
        char[] word2 = secondWord.replaceAll("[\\s]", "").toCharArray();
        Arrays.sort(word1);
        Arrays.sort(word2);
        return Arrays.equals(word1, word2);
    }

    /**
     * Reconstructs the true number of items from the stacks+items notation.
     *
     * @param amount
     * @return
     */
    public static long parseStackNotation(String amount) {
        Matcher matcher = STACK_NOTATION_PATTERN.matcher(amount);
        if (!matcher.find()) {
            if (amount.matches("\\d+")) {
                return Long.parseLong(amount);
            } else {
                return -1;
            }
        }
        String s_stacks = matcher.group(1);
        String s_stackSize = matcher.group(2);
        String s_items = matcher.group(3);
        return Long.parseLong(s_stacks) * Long.parseLong(s_stackSize) + Long.parseLong(s_items);
    }

}
