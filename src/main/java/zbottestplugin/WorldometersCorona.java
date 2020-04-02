/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.javatuples.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Dennis
 */
public class WorldometersCorona {

    public static enum Line1Type {
        CUM_CONF,
        CUM_DEAD,
        CUM_HEAL,
        ACT_CONF,
        ACT_CRIT
    }

    public static enum Line2Type {
        CUM_CONF_HIST,
        CUM_DEAD_HIST,
        ACT_CONF_HIST,
        CUM_HEAL_HIST,
        NUM_OVER_VIEW,
        TOP_FIVE_NATN
    }

    private static final String GLOBAL_URL;
    private static final String COUNTRY_URL;

    public static Pair<String, String> query(Line1Type type1, Line2Type type2) {
        String html = makeQuery(GLOBAL_URL);
        Pair<String, String> line1 = makeLine1(html, type1);
        List<Pair<String, String>> line2 = makeLine2(html, type2);
        return makeOutput(line1, line2);
    }

    public static Pair<String, String> query(String country, Line1Type type1, Line2Type type2) {
        String html = makeQuery(COUNTRY_URL + country);
        Pair<String, String> line1 = makeLine1(html, type1);
        List<Pair<String, String>> line2 = makeLine2(html, type2);
        return makeOutput(line1, line2);
    }

    private static Pair<String, String> makeOutput(Pair<String, String> line1Data, List<Pair<String, String>> line2Data) {
        if (line1Data == null || line2Data == null) {
            return null;
        }
        StringBuilder line1 = new StringBuilder();
        line1.append("&6").append(line1Data.getValue0()).append(": &6&l").append(line1Data.getValue1());
        StringBuilder line2 = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i % 2 == 0) {
                line2.append("&7");
            } else {
                line2.append("&8");
            }
            line2.append(line2Data.get(i).getValue0()).append(": ").append(line2Data.get(i).getValue1()).append(" ");
        }
        return Pair.with(line1.toString(), line2.toString());
    }

    private static Pair<String, String> makeLine1(String html, Line1Type type1) {
        if (html.contains("404 Not Found")) {
            return null;
        }
        Document doc = Jsoup.parse(html);
        Pair<String, String> pair = null;
        String context = "Globally";
        String title = doc.select("title").get(0).text();
        if (!title.startsWith("Coronavirus")) {
            String[] words = title.split(" ");
            String countryName = words[0];
            context = "in " + countryName;
        }
        
        switch (type1) {
            case CUM_CONF:
                String string = doc.select("div#maincounter-wrap:contains(Cases) > div.maincounter-number > span").get(0).text().replace(",", "");
                return Pair.with("Cumulative Confirmed " + context, string);
            case CUM_DEAD:
                string = doc.select("div#maincounter-wrap:contains(Deaths) > div.maincounter-number > span").get(0).text().replace(",", "");
                return Pair.with("Cumulative Deceased " + context, string);
            case CUM_HEAL:
                string = doc.select("div#maincounter-wrap:contains(Recovered) > div.maincounter-number > span").get(0).text().replace(",", "");
                return Pair.with("Cumulative Recovered " + context, string);
            case ACT_CONF:
                string = doc.select("div.col-md-6:contains(ACTIVE) div.number-table-main").get(0).text().replace(",", "");
                return Pair.with("Active Infections " + context, string);
            case ACT_CRIT:
                string = doc.select("div.col-md-6:contains(ACTIVE) span.number-table[style=color:red]").get(0).text().replace(",", "");
                return Pair.with("In Critical Condition " + context, string);
        }
        return pair;
    }

    private static List<Pair<String, String>> makeLine2(String html, Line2Type type2) {
        if (html.contains("404 Not Found")) {
            return null;
        }
        if (type2 != Line2Type.NUM_OVER_VIEW) {
            return calculateLine2(html, type2).stream().map((p) -> {
                return Pair.with(p.getValue0(), (p.getValue1() >= 0 ? "+" : "") + p.getValue1());
            }).collect(Collectors.toList());
        }
        List<Pair<String, String>> curatedList = new LinkedList<>();
        curatedList.add(makeLine1(html, Line1Type.ACT_CONF).setAt0("Active"));
        curatedList.add(makeLine1(html, Line1Type.ACT_CRIT).setAt0("Critical"));
        curatedList.add(makeLine1(html, Line1Type.CUM_DEAD).setAt0("Deceased"));
        curatedList.add(makeLine1(html, Line1Type.CUM_HEAL).setAt0("Recovered"));
        curatedList.add(Pair.with("Updated", StringUtil.extract(html, "Last updated:", "</div>")));
        return curatedList;
    }

    private static List<Pair<String, Long>> calculateLine2(String html, Line2Type type2) {
        List<Pair<String, Long>> curatedList = new LinkedList<>();
        switch (type2) {
            case CUM_HEAL_HIST:
                List<Pair<String, Long>> dConfList = calculateLine2(html, Line2Type.CUM_CONF_HIST);
                List<Pair<String, Long>> dDeadList = calculateLine2(html, Line2Type.CUM_DEAD_HIST);
                List<Pair<String, Long>> dActiveList = calculateLine2(html, Line2Type.ACT_CONF_HIST);
                for (int i = 0; i < dConfList.size() - 1; i++) {
                    curatedList.add(dActiveList.get(i).setAt1(dConfList.get(i).getValue1() - dActiveList.get(i).getValue1()
                            - dDeadList.get(i).getValue1()));
                }
                return curatedList;
            case CUM_CONF_HIST:
            case CUM_DEAD_HIST:
            case ACT_CONF_HIST:
                List<Pair<String, Long>> list = prepareLine2(html, type2);
                for (int i = 0; i < list.size() - 1; i++) {
                    curatedList.add(list.get(i).setAt1(list.get(i).getValue1() - list.get(i + 1).getValue1()));
                }
                return curatedList;
            default:
                return prepareLine2(html, type2);
        }
    }

    private static List<Pair<String, Long>> prepareLine2(String html, Line2Type type2) {
        Document doc = Jsoup.parse(html);
        List<Pair<String, Long>> results = new LinkedList<>();
        String[] x_axis = null;
        String[] y_axis = null;
        String s_json = null;
        switch (type2) {
            case TOP_FIVE_NATN:
                Element table = doc.selectFirst("table#main_table_countries_today");
                Elements trs = table.select("tr");
                for (int i = 0; i < 5; i++) {
                    Elements tds = trs.get(i).select("td");
                    String countryName = tds.get(0).text();
                    String s_countryNumber = tds.get(1).text().replace(",", "");
                    results.add(Pair.with(countryName, Long.parseLong(s_countryNumber)));
                }
                return results;
            case CUM_CONF_HIST:
                s_json = StringUtil.extract(html, "Highcharts.chart('coronavirus-cases-linear', ", ");");
                break;
            case CUM_DEAD_HIST:
                s_json = StringUtil.extract(html, "Highcharts.chart('coronavirus-deaths-linear', ", ");");
                break;
            case ACT_CONF_HIST:
                s_json = StringUtil.extract(html, "Highcharts.chart('total-currently-infected-linear', ", ");");
                break;
        }
        if (s_json == null) {
            return null;
        }

        String s_x_axis = StringUtil.extract(s_json, "xAxis: {\n"
                + "                categories: [", "]");
        String s_y_axis = StringUtil.extract(s_json, "data: [", "]");
        x_axis = StringUtil.extractAll(s_x_axis, "\"", "\"");
        y_axis = s_y_axis.split(",");
        for (int i = 0; i < x_axis.length; i++) {
            results.add(Pair.with(x_axis[i], Long.parseLong(y_axis[i])));
        }
        Collections.reverse(results);
        return results;
    }

    private static String makeQuery(String url) {
        try {
            HTTP.HTTPResponse http = HTTP.http(url);
            String html = new String(http.getContent());
            return html;
        } catch (IOException ex) {
            return null;
        }
    }

    static {
        GLOBAL_URL = "https://www.worldometers.info/coronavirus/";
        COUNTRY_URL = "https://www.worldometers.info/coronavirus/country/";
    }
}
