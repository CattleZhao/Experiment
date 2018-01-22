package JDBC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.mongodb.*;

public class MongoJDBC {
	public static DB connection(){
		@SuppressWarnings("deprecation")
		Mongo mongo = new Mongo("10.199.130.133",27017);
        @SuppressWarnings("deprecation")
		DB db = mongo.getDB("AllDate");
        return db;
	}
	public static String loadJSON (String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL oracle = new URL(url);
            URLConnection yc = oracle.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                                        yc.getInputStream()));
            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }

        return json.toString();
    }
}
