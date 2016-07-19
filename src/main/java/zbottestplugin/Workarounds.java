/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Dennis
 */
public class Workarounds {

    public static String locate(String rdns) throws IOException {
        String ip = InetAddress.getByName(rdns).getHostAddress();
        URL u = new URL("http://www.geoplugin.net/php.gp?ip=" + URLEncoder.encode(ip, "UTF-8"));
        SerializedPhpParser serializedPhpParser = new SerializedPhpParser(new Scanner(u.openStream()).useDelimiter("\\Z").next());
        Map<Object, Object> qqq = (Map<Object, Object>) serializedPhpParser.parse();
        String loc = ip + "   /   " + (String) qqq.get("geoplugin_city") + "," + (String) qqq.get("geoplugin_region") + "," + (String) qqq.get("geoplugin_countryName");
        loc = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(loc);
        loc = org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(loc);
        return loc;
    }

}
