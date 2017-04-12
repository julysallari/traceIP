import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class IPData {

	private String ip;
	private String code;
	private String code3;
	private String name;
	private String nativeName;
	private Map<String, Double> currs = new HashMap<String, Double>();
	private LinkedList<String> t_zones = new LinkedList<String>();
	private Map<String, String> langs = new HashMap<String, String>(); 
	private double lat;
	private double lon;
	private double dist_lat;
	private double dist_long;
	private static final Double ROUND_ZERO = 10000.0;
	
	public IPData(String ip, Double bsasLat, Double bsasLong) {
		this.ip = ip;
		this.dist_lat = bsasLat;
		this.dist_long = bsasLong;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public void setCode3(String code3) {
		this.code3 = code3;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void setNativeName(String nativeName) {
		this.nativeName = nativeName;
	}

	public void setCurrs(JSONArray jsonArray) {
		if(jsonArray == null){
			return;
		}
		JSONObject jsonObject = null;
		for (Object object : jsonArray) {
			jsonObject = (JSONObject) object;
			this.currs.put((String) jsonObject.get("code"), 0.0);
		}
	}

	public void setT_zones(JSONArray jsonArray) {
		if(jsonArray == null){
			return;
		}
		for (Object object : jsonArray) {
			this.t_zones.addLast((String)object);
		}
	}

	public void setLangs(JSONArray jsonArray) {
		if(jsonArray == null){
			return;
		}
		JSONObject jsonObject = null;
		for (Object object : jsonArray) {
			jsonObject = (JSONObject) object;
			this.langs.put((String) jsonObject.get("iso639_1"), (String) jsonObject.get("nativeName"));
		}
	}

	public void setLatLong(JSONArray jsonArray) {
		if(jsonArray == null || jsonArray.size() != 2){
			return;
		}
		this.lat = (Double) jsonArray.get(0);
		this.lon = (Double) jsonArray.get(1);
	}
	
	public void setRates(JSONObject jsonObject) {
		if(jsonObject == null){
			return;
		}
		Double r_code = 0.0;
		for (String k_cur : currs.keySet()) {
			r_code = (Double) jsonObject.get(k_cur);
			if(r_code != null){
				currs.put(k_cur, Math.round (1/r_code * ROUND_ZERO) / ROUND_ZERO);
			}
		}
	}
	
	@Override
	public String toString() {
		String value= "";
		String codeVal = this.code == null ? this.code3 : this.code;
		value += "IP: " + ip + ", fecha actual " + getDateTime();
		value += '\n';
		value += addName();
		value += "ISO Code: " + codeVal;
		value += '\n';
		value += "Idiomas: " + addLanguages();
		value += '\n';
		value += addCurrencies();
		value += "Hora: " + addTZones();
		value += '\n';
		value += "Distancia Estimada: " + addDistance();
		
		return value;
	}

	private String addName() {
		if(this.nativeName != null){
			return "País: " + this.nativeName + "(" + this.name + ")" + '\n';
		}
		return "";
	}

	private String getDateTime() {
		String val = "";
		LocalDateTime currentTime = LocalDateTime.now();
		val += String.format("%02d", currentTime.getDayOfMonth()) + "/" + String.format("%02d", currentTime.getMonthValue()) + "/" + String.format("%02d", currentTime.getYear());
		val += " " + String.format("%02d", currentTime.getHour()) + ":" + String.format("%02d", currentTime.getMinute()) + ":" + String.format("%02d", currentTime.getSecond());
		return val;
	}

	private String addDistance() {
		return String.valueOf(distFrom(dist_lat, dist_long, this.lat, this.lon)) + " kms " + "("+ dist_lat +", "+ dist_long +") a " + "("+ this.lat +", "+ this.lon +")";
	}

	private float distFrom(double d, double lng1, double e, double f) {
	    double earthRadius = 6371000; //meters
	    double dLat = Math.toRadians(e-d);
	    double dLng = Math.toRadians(f-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(d)) * Math.cos(Math.toRadians(e)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    float dist = (float) (earthRadius * c);

	    return (int) (dist/1000);
	}
	
	private String addTZones() {
		String times = "";
		for (String tz : this.t_zones) {
			times += getTime(tz) + " o ";  
		}
		if(times.length() > 0){
			times = times.substring(0, times.length()-3);
		}
		return times;
	}

	private String getTime(String tz) {
		LocalDateTime ldt = LocalDateTime.now(ZoneId.of(tz));
		return String.format("%02d", ldt.getHour()) + ":" + String.format("%02d", ldt.getMinute()) + ":" + String.format("%02d", ldt.getSecond()) + " (" + tz + ")";
	}

	private String addCurrencies() {
		String currencs = "";
		Double val = 0.0;
		for (Entry<String, Double> entry : this.currs.entrySet()) {
			val = entry.getValue();
			if(val != null && val != 0.0){
				currencs += entry.getKey() + " (" + "1 " + entry.getKey() + " = " + val + " U$S" + "). ";
			}
		}
		if(currencs.equals("")){
			return "";
		}
		return "Moneda: " + currencs + '\n';
	}

	private String addLanguages() {
		String langs = "";
		for (Entry<String, String> entry: this.langs.entrySet()) {
			langs += entry.getValue() + " (" + entry.getKey() + "). "; 
		}
		return langs;
	}
	
}
