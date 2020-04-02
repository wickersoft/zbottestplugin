/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Dennis
 */
public class ArcGISCorona {

    private static final String TOP5_CUM_NUMBERS_URL;
    private static final String TOP5_CUM_NUMBERS_NOCHINA_URL;
    private static final String RECENT_NEW_INFECTIONS_PER_COUNTRY_URL;
    private static final String WEEKDAY_DATE_FORMAT_PATTERN = "EEE";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(WEEKDAY_DATE_FORMAT_PATTERN, Locale.ENGLISH);

    public static Pair<String, String> friendlyTop5Numbers() {
        List<Pair<String, Long>> results = ArcGISCorona.top5CumNumbers();
        if (results == null) {
            return null;
        }
        StringBuilder line1 = new StringBuilder();
        line1.append("&6Cumulative Confirmed Globally: &6&l").append(results.get(0).getValue1());
        StringBuilder line2 = new StringBuilder();
        for (int i = 1; i < 6; i++) {
            if (i % 2 == 0) {
                line2.append("&7");
            } else {
                line2.append("&8");
            }
            line2.append(results.get(i).getValue0()).append(": ").append(results.get(i).getValue1()).append(" ");
        }
        return Pair.with(line1.toString(), line2.toString());
    }

    public static Pair<String, String> friendlyTop5NumbersNoChina() {
        List<Pair<String, Long>> results = ArcGISCorona.top5CumNumbersNoChina();
        if (results == null) {
            return null;
        }
        StringBuilder line1 = new StringBuilder();
        line1.append("&6Cumulative Confirmed Outside China: &6&l").append(results.get(0).getValue1());
        StringBuilder line2 = new StringBuilder();
        for (int i = 1; i < 6; i++) {
            if (i % 2 == 0) {
                line2.append("&7");
            } else {
                line2.append("&8");
            }
            line2.append(results.get(i).getValue0()).append(": ").append(results.get(i).getValue1()).append(" ");
        }
        return Pair.with(line1.toString(), line2.toString());
    }

    public static Pair<String, String> friendlyRecentNewInfectionsByCountry(String country) {
        List<Pair<String, Long>> results = ArcGISCorona.recentNewInfectionsPerCountry(country);
        if (results == null) {
            return null;
        }
        StringBuilder line1 = new StringBuilder();
        line1.append("&6Cumulative Confirmed in ").append(results.get(0).getValue0()).append(": &6&l").append(results.get(0).getValue1());
        StringBuilder line2 = new StringBuilder();
        for (int i = 1; i < results.size(); i++) {
            if (i % 2 == 0) {
                line2.append("&7");
            } else {
                line2.append("&8");
            }
            line2.append(results.get(i).getValue0()).append(": +").append(results.get(i).getValue1()).append(" ");
        }
        return Pair.with(line1.toString(), line2.toString());
    }

    //##################### BACKEND ########################################
    public static List<Pair<String, Long>> top5CumNumbers() {
        JSONObject json = makeQuery(TOP5_CUM_NUMBERS_URL);
        if (json == null) {
            return null;
        }
        List features = (List) json.get("features");
        if (features.size() == 0) {
            return null;
        }

        List<Pair<String, Long>> results = new LinkedList<>();

        long totalCases = 0;

        for (Object feature : features) {
            JSONObject country = (JSONObject) feature;
            JSONObject attributes = (JSONObject) country.get("attributes");
            String adm0_viz_name = (String) attributes.get("ADM0_VIZ_NAME");
            long cum_conf = (Long) attributes.get("cum_conf");
            Pair<String, Long> item = Pair.with(adm0_viz_name, cum_conf);
            results.add(item);
            totalCases += cum_conf;
        }

        Pair<String, Long> item = Pair.with("", totalCases);
        results.add(0, item);

        return results;
    }

    public static List<Pair<String, Long>> top5CumNumbersNoChina() {
        JSONObject json = makeQuery(TOP5_CUM_NUMBERS_NOCHINA_URL);
        if (json == null) {
            return null;
        }
        List features = (List) json.get("features");
        if (features.size() == 0) {
            return null;
        }

        List<Pair<String, Long>> results = new LinkedList<>();

        long totalCases = 0;

        for (Object feature : features) {
            JSONObject country = (JSONObject) feature;
            JSONObject attributes = (JSONObject) country.get("attributes");
            String adm0_viz_name = (String) attributes.get("ADM0_VIZ_NAME");
            long cum_conf = (Long) attributes.get("cum_conf");
            Pair<String, Long> item = Pair.with(adm0_viz_name, cum_conf);
            results.add(item);
            totalCases += cum_conf;
        }

        Pair<String, Long> item = Pair.with("", totalCases);
        results.add(0, item);

        return results;
    }

    public static List<Pair<String, Long>> recentNewInfectionsPerCountry(String adm0_name) {
        JSONObject json = makeQuery(RECENT_NEW_INFECTIONS_PER_COUNTRY_URL + adm0_name + "%25%27");
        if (json == null) {
            return null;
        }
        List features = (List) json.get("features");
        if (features.size() == 0) {
            return null;
        }

        List<Pair<String, Long>> results = new LinkedList<>();

        JSONObject country = (JSONObject) features.get(0);
        JSONObject attributes = (JSONObject) country.get("attributes");
        String ADM0_VIZ_NAME = (String) attributes.get("ADM0_VIZ_NAME");
        long cum_conf = (Long) attributes.get("cum_conf");
        Pair<String, Long> item = Pair.with(ADM0_VIZ_NAME, cum_conf);
        results.add(item);

        for (Object feature : features) {
            country = (JSONObject) feature;
            attributes = (JSONObject) country.get("attributes");
            long DateOfDataEntry = (Long) attributes.get("DateOfDataEntry");
            long NewCase = (Long) attributes.get("NewCase");
            String FriendlyDateOfDataEntry = SIMPLE_DATE_FORMAT.format(new Date(DateOfDataEntry));
            item = Pair.with(FriendlyDateOfDataEntry, NewCase);
            results.add(item);
        }
        return results;
    }

    private static JSONObject makeQuery(String url) {
        try {
            HTTP.HTTPResponse http = HTTP.http(url);
            String s_json = new String(http.getContent());
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(s_json);
            return json;
        } catch (IOException | ParseException ex) {
            return null;
        }
    }

    static {
        TOP5_CUM_NUMBERS_URL = "https://services.arcgis.com/5T5nSi527N4F7luB/arcgis/rest/services/COVID_19_CasesByCountry(pl)_VIEW/FeatureServer/0/query?where=1%3D1&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&resultType=none&distance=0.0&units=esriSRUnit_Meter&returnGeodetic=false&outFields=ADM0_VIZ_NAME%2Ccum_conf%2Ccum_death&returnGeometry=false&returnCentroid=false&featureEncoding=esriDefault&multipatchOption=xyFootprint&maxAllowableOffset=&geometryPrecision=&outSR=&datumTransformation=&applyVCSProjection=false&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnQueryGeometry=false&returnDistinctValues=false&cacheHint=false&orderByFields=cum_conf+desc&groupByFieldsForStatistics=&outStatistics=&having=&resultOffset=&resultRecordCount=500&returnZ=true&returnM=true&returnExceededLimitFeatures=true&quantizationParameters=&sqlFormat=none&f=pjson&token=";
        TOP5_CUM_NUMBERS_NOCHINA_URL = "https://services.arcgis.com/5T5nSi527N4F7luB/arcgis/rest/services/COVID_19_CasesByCountry(pl)_VIEW/FeatureServer/0/query?where=ADM0_NAME+NOT+LIKE+%27CHINA%27&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&resultType=none&distance=0.0&units=esriSRUnit_Meter&returnGeodetic=false&outFields=ADM0_VIZ_NAME%2Ccum_conf%2Ccum_death&returnGeometry=false&returnCentroid=false&featureEncoding=esriDefault&multipatchOption=xyFootprint&maxAllowableOffset=&geometryPrecision=&outSR=&datumTransformation=&applyVCSProjection=false&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnQueryGeometry=false&returnDistinctValues=false&cacheHint=false&orderByFields=cum_conf+desc&groupByFieldsForStatistics=&outStatistics=&having=&resultOffset=&resultRecordCount=500&returnZ=true&returnM=true&returnExceededLimitFeatures=true&quantizationParameters=&sqlFormat=none&f=pjson&token=";
        RECENT_NEW_INFECTIONS_PER_COUNTRY_URL = "https://services.arcgis.com/5T5nSi527N4F7luB/arcgis/rest/services/COVID_19_HistoricCasesByCountry(pt)View/FeatureServer/0/query?objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&resultType=none&distance=0.0&units=esriSRUnit_Meter&returnGeodetic=false&outFields=ADM0_VIZ_Name%2CDateOfDataEntry%2CNewCase%2Ccum_conf&returnGeometry=false&featureEncoding=esriDefault&multipatchOption=xyFootprint&maxAllowableOffset=&geometryPrecision=&outSR=&datumTransformation=&applyVCSProjection=false&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnQueryGeometry=false&returnDistinctValues=false&cacheHint=false&orderByFields=DateOfDataEntry+desc&groupByFieldsForStatistics=ADM0_NAME&outStatistics=&having=&resultOffset=&resultRecordCount=5&returnZ=false&returnM=false&returnExceededLimitFeatures=true&quantizationParameters=&sqlFormat=none&f=pjson&token=&where=ADM0_NAME+like+%27%25";
    }
}
