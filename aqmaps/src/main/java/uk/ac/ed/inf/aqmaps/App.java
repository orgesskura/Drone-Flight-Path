package uk.ac.ed.inf.aqmaps;
import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;

import java.lang.reflect.Type;



public class App 
{
	private static final HttpClient client = HttpClient.newHttpClient();
	//declare a static variable to contain list of coordinates of each sensor
	private static ArrayList<ArrayList<Double>> list_coordinates_sensors = new ArrayList<ArrayList<Double>>();
	//declare a static variable to contain list of charge of battery of each sensor
	private static ArrayList<Double> list_batteries = new ArrayList<Double>();
	//declare a static variable to contain list of readings for that sensor
	private static ArrayList<Double> list_readings = new ArrayList<Double>();
	
	// used as class to parse maps JSon files
	public static class Map {
		String location;
	    double battery;
	    double reading;
	}
	
	// used as class to parse  JSon files in what3Words
	public static class what3Words{
		String country;
		Square square;
		public static class Square{
			Southwest southwest;
			public static class Southwest{
				double lng;
				double lat;
			}
			Northeast northeast;
			public static class Northeast{
				double lng;
				double lat;
			}
		}
		String nearestPlace;
		Coordinates coordinates;
		public static class Coordinates{
			double lng;
			double lat;
		}
		String words;
		String language;
		String map;
	}
	
	//get list of coordinates of sensors
	public static ArrayList<ArrayList<Double>> get_coordinates(){
		return list_coordinates_sensors;
	}
	
	// get list of readings of battery
	public static ArrayList<Double> get_battery(){
		return list_batteries;
	}
	
	//get list of readings of sensors
	public static ArrayList<Double> get_readings(){
		return list_readings;
	}
	
    public static void main( String[] args )
    {
    	
    	
    }
    
    
    
    public static void readMap(int day,int month,int year ,int port) {
    	//create a string s to feed it as a request to get the map data for a particular day
    	String s = "http://localhost:" + Integer.toString(port) + "/";
    	s+= "maps/" + Integer.toString(year) + "/" + Integer.toString(month) + "/";
    	s+= Integer.toString(day) + "/" + "air-quality-data.json";
    	//make a request to get data in the specified path
    	String urlString = s;
    	var request = HttpRequest.newBuilder()
    			      .uri(URI.create(urlString))
    			      .build();
    	// The response object is of class HttpResponse<String>
    	try {
			var response = client.send(request, BodyHandlers.ofString());
			response.statusCode();
			//get JSon string
			var data = response.body();
			//get type of JSon string and then assign it to the map class as defined in lectures
			Type listType =new TypeToken<ArrayList<Map>>(){}.getType();
			ArrayList<Map> mapList =new Gson().fromJson(data, listType);
			// for each record of list get its what3Words in location and find its real coordinates
			for (var i :mapList) {
				//read reading of each battery and add it to list of batteries
				var battery = i.battery;
				list_batteries.add(battery);
				//read the readings of pollution and add it to the list of readings
				var reading = i.reading;
				list_readings.add(reading);
				// read the what3Word location
				var location = i.location;
				// use split method to split string on "." character to get locaction
				String[] str = location.split("//.");
				//form the path for the what3Word
				var path = "http://localhost:" + Integer.toString(port) + "/" + "words" + "/";
				path +=  str[0] +"/" +str[1] + "/" + str[2] + "/details.json";
				//read this path via readWord function
				var coordinates = readWord(path);
				//add coordinates of that sensor to the list of coordinates of sensors
				list_coordinates_sensors.add(coordinates);
			}
				
		// catch exceptions			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    
    
    public static ArrayList<Double> readWord(String path) {
    	//give the path as argument and get the JSon file associated with that path
    	String urlString = path;
    	var list = new ArrayList<Double>();
    	var request = HttpRequest.newBuilder()
    			      .uri(URI.create(urlString))
    			      .build();
    	// The response object is of class HttpResponse<String>
    	try {
			var response = client.send(request, BodyHandlers.ofString());
			response.statusCode();
			//get the JSon string associated with the path
			var data = response.body();
			// assign data to the what3Words class format
			var detail = new Gson().fromJson(data, what3Words.class);
			// get coordinates of that class
			var lng = detail.coordinates.lng;
            var lat = detail.coordinates.lat;
            list.add(lng);
            list.add(lat);
		//catch exceptions			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//return list of coordinates
    	return list;
    	
    }
    
    
    public static ArrayList<Polygon> getBuildings() {
    	// HttpClient assumes that it is a GET request by default.
    	var polygons = new ArrayList<Polygon>();
    	String urlString = "http://localhost:8888/buildings/no-fly-zones.geojson";
    	var request = HttpRequest.newBuilder()
    			      .uri(URI.create(urlString))
    			      .build();
    	// The response object is of class HttpResponse<String>
    	try {
			var response = client.send(request, BodyHandlers.ofString());
			response.statusCode();
			//get the JSon string from the response
			var data = response.body();
			//get a list of polygons from the geojson file by turning to list of collections and then
			// to a list of features and then to a list of polygons
            var collection = FeatureCollection.fromJson(data);
            var features = collection.features();
            for(var feat : features) {
            	var geo = feat.geometry();
            	var pol = (Polygon)geo;
            	polygons.add(pol);
            }
            
		// try to catch errors 			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//for each polygon there is a .coordinates method to get the list of list of points
    	//return the list of polygons
    	return polygons;
    }
    
}
