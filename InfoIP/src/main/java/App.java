import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class App {

	private static final String GEO_IP = "https://api.ip2country.info/";
	private static final String COUNTRY_INFO_CODE = "https://restcountries.eu/rest/v2/alpha/";
	private static final String COUNTRY_INFO_NAME = "https://restcountries.eu/rest/v2/name/";
	private static final String EXCH_INFO = "http://api.fixer.io/latest?base=USD";
	private static final Double BSAS_LAT = -34.0;
	private static final Double BSAS_LONG = -64.0;
	private IPData data;
	
	public static void main(String[] args) {
		if(args == null || args.length == 0 || args[0].equals("")){
			System.out.println("PARAM NEEDED!");
			return;
		}
		String ip_value = args[0];
		App app = new App(ip_value);
		app.print(ip_value);
	}
	
	public App(String ip) {
		data = new IPData(ip, BSAS_LAT, BSAS_LONG);
	}
	
	private void print(String ip){
		try {
			setCountry(ip, data);
			setDataCountry(data);
			setExchangeRate(data);
			System.out.println(data);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setCountry(String ip, IPData data) throws KeyManagementException, NoSuchAlgorithmException, IOException, ParseException{
		String https_url = GEO_IP + "ip?" + ip;
	      URL url = new URL(https_url);
	      HttpsURLConnection conn = getConnection(url);
	      JSONObject jsonObject = getData(conn);
	      if(jsonObject == null){
	    	  return;
	      }
	      data.setCode((String) jsonObject.get("countryCode"));
	      data.setCode3((String) jsonObject.get("countryCode3"));
	      data.setName((String) jsonObject.get("countryName"));
	}
	
	private void setDataCountry(IPData data) throws KeyManagementException, NoSuchAlgorithmException, IOException, ParseException{
		String https_url = COUNTRY_INFO_CODE + data.getCode();
	      URL url = new URL(https_url);
	      HttpsURLConnection conn = getConnection(url);
	      JSONObject jsonObject = getData(conn);
	      if(jsonObject == null){
	    	  https_url = COUNTRY_INFO_NAME + data.getName();
	    	  url = new URL(https_url);
	    	  conn = getConnection(url);
	    	  JSONArray jsonArray = getDataArray(conn);
	    	  if(jsonArray == null){
	    		  return;
	    	  }
		      jsonObject = (JSONObject) jsonArray.get(0);
	    	  if(jsonObject == null){
	    		  return;
	    	  }
	      }
	      data.setNativeName((String) jsonObject.get("nativeName"));
	      data.setLatLong((JSONArray) jsonObject.get("latlng"));
	      data.setT_zones((JSONArray)jsonObject.get("timezones"));
	      data.setCurrs((JSONArray) jsonObject.get("currencies"));
	      data.setLangs((JSONArray) jsonObject.get("languages"));
	}
	
	private void setExchangeRate(IPData data) throws IOException, ParseException {
		String https_url = EXCH_INFO;
	    URL url = new URL(https_url);
	    HttpURLConnection conn = getSimpleConn(url);
	    JSONObject jsonObject = getData(conn);
	    if(jsonObject == null){
	    	  return;
	      }
	    data.setRates((JSONObject) jsonObject.get("rates"));
	}
	
	private HttpURLConnection getSimpleConn(URL url) throws IOException{
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		//conn.setUseCaches(true);
        if(conn.getResponseCode() != 200){
    		  System.out.println("Error connecting to APIs");
    		  return null;
    	  }
        return conn;
	}
	
	private HttpsURLConnection getConnection(URL url) throws NoSuchAlgorithmException, KeyManagementException, IOException {
		//Security issue: MITM
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };
		// Install the all-trusting trust manager
		final SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
		if(conn.getResponseCode() != 200){
			System.out.println("Error connecting to APIs");
			return null;
		}
		return conn;
	}
	
	   private JSONObject getData(URLConnection con) throws IOException, ParseException{
		   JSONObject jsonObject = null;
		   if(con!=null){
			   InputStream is = con.getInputStream();
			   BufferedReader input = new BufferedReader(new InputStreamReader(is));
			   String buildJson =  readAll(input);
			   JSONParser parser = new JSONParser();
			   Object obj = parser.parse(buildJson);
			   jsonObject = (JSONObject) obj;

			   input.close();
			   is.close();
		   }
		   return jsonObject;
	}

	   private JSONArray getDataArray(HttpsURLConnection con) throws IOException, ParseException{
		   JSONArray jsonArray = null;
		   if(con!=null){
			   InputStream is = con.getInputStream();
			   BufferedReader input = new BufferedReader(new InputStreamReader(is));
			   String buildJson =  readAll(input);
			   JSONParser parser = new JSONParser();
			   Object obj = parser.parse(buildJson);
			   jsonArray = (JSONArray) obj;

			   input.close();
			   is.close();
		   }
		   return jsonArray;
	   }

	private String readAll(Reader rd) throws IOException {
	   StringBuilder sb = new StringBuilder();
	   int ret;
	   while ((ret = rd.read()) != -1) {
		   sb.append((char) ret);
	   }
	   return sb.toString();
	}
}
