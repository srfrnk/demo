package beam_file2file;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
// import java.util.ArrayList;
// import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SomeClient {
  private static Logger logger = LoggerFactory.getLogger(SomeClient.class);

  public static String callAPI(String query) {
    try {
      URL obj = new URL(String.format("http://enrich-api.default.svc:3000?q=%s",
          URLEncoder.encode(query, StandardCharsets.UTF_8)));
      HttpURLConnection con = (HttpURLConnection) obj.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("User-Agent", "demo");
      int responseCode = con.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();

        return response.toString();
      } else {
        logger.error("Bad Response {}", responseCode);
        return "";
      }
    } catch (Exception e) {
      logger.error("", e);
      return "";
    }
  }

  /* public static String callAPI(String query) {
    try {
      URL obj =
          new URL(String.format("https://www.google.com/complete/search?q=%s&client=gws-wiz&xssi=t",
              URLEncoder.encode(query, StandardCharsets.UTF_8)));
      HttpURLConnection con = (HttpURLConnection) obj.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("User-Agent", "demo");
      int responseCode = con.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();

        String resultString = String.join(" ; ",
            ((JSONArray) (new JSONArray(response.toString().substring(4))).get(0)).toList().stream()
                .map(ArrayList.class::cast).map((ArrayList result) -> (String) result.get(0))
                .toArray(String[]::new));
        return resultString;
      } else {
        logger.error("Bad Response {}", responseCode);
        return "";
      }
    } catch (Exception e) {
      logger.error("", e);
      return "";
    }
  } */
}
