package zbottestplugin;

import zedly.zbot.StringUtil;

public class SimpleJson {

    public static String parse(String json) {
        String content = StringUtil.extract(json, "translatedText\":\"", "\"");
        content = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(content);
        content = org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(content);
        return content;
    }

    public static String parse0(String json) {
        int i1 = 0;
        String re = "";
        while (i1 < json.length() - 1) {
            i1 = json.indexOf("[", i1);
            int i2 = json.indexOf("]", i1 + 1);
            if (i1 < 0 | i2 < 0) {
                break;
            }
            String s1 = json.substring(i1, i2 + 1);
            i1 = i2;
            int i3 = s1.indexOf("[\"") + 1;
            int i4 = s1.indexOf("\",\"", i3 + 1);
            int i5 = s1.indexOf("\"", i4 + 1);
            int i6 = s1.indexOf("\",\"", i5 + 1);
            int i7 = s1.indexOf("\"", i6 + 1);
            int i8 = s1.indexOf("\",\"", i7 + 1);
            String s2 = s1.substring(i7 + 1, i8);
            if (s2.length() > 0) {
                re += s2;
            } else {
                re += s1.substring(i3 + 1, i4);
            }
        }
        return re.replaceAll("\\\\\"", "\"");
    }
}
