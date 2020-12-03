package uk.ac.ed.inf.aqmaps;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;




public class App 
{   
	private final int allowed_moves = 150;
	static ArrayList<Point> list_point = new ArrayList<Point>();
	static int nr_steps = 0;
	static ArrayList<String> list_sensors_visited = new ArrayList<String>();
	static ArrayList<Integer> list_angles = new ArrayList<Integer>();
	static int visited = 0;
    public static void main( String[] args )
    {
    	String day = args[0],month = args[1],year = args[2],latit = args[3],longi = args[4];
    	String port = args[6];
    	JsonParser.parseJSon(day,month,year,port);
    	var  buildings = new ArrayList<Polygon>();
    	buildings = JsonParser.get_buildings();
    	var coordinates = new ArrayList<ArrayList<Double>>();
    	var start = new ArrayList<Double>();
    	var longitude = Double.valueOf(longi);
    	var latitude = Double.valueOf(latit);
    	start.add(longitude);
    	start.add(latitude);
    	coordinates.add(start);
    	coordinates.addAll(JsonParser.get_coordinates());
    	//do this for maintainability.Nr of sensors we want to visit might change in the future
    	int nr_points = coordinates.size();
    	// get pathplanner to work given current coordinates and buildings
    	PathPlanner.getPath(coordinates,nr_points,buildings);
    	var perm = new int[34];
    	perm = PathPlanner.get_permutation();
    	var list_sensors = new ArrayList<Sensor>();
    	var list_battery = new ArrayList<Double>();
    	list_battery = JsonParser.get_battery();
    	var list_readings = new ArrayList<String>();
    	list_readings = JsonParser.get_readings();
    	var mappings = new HashMap<ArrayList<Double>,String>();
    	mappings = JsonParser.get_mappings();
    	for(int i=1;i<coordinates.size();i++) {
    		var location_what3Words = mappings.get(coordinates.get(i));
    		var reading = list_readings.get(i-1);
    		var battery = list_battery.get(i-1);
    		var sensor = new Sensor(location_what3Words,reading,battery,i);
    		list_sensors.add(sensor);
    	}
    	var current_location = new ArrayList<Double>();
    	current_location.add(start.get(0));
    	current_location.add(start.get(1));
    	list_point.add(Point.fromLngLat(start.get(0), start.get(1)));
    	for(int i=1;i<coordinates.size();i++) {
    		var order_visit = perm[i];
    		var dest = new ArrayList<Double>();
    		dest = coordinates.get(order_visit);
    		while(true) {
    			var bool = avoidObstacles(i,order_visit,current_location, dest,buildings,list_sensors);
    			if (bool == true) {
    				break;
    			}
    		}
    	}
       System.out.println(visited);
       var end = new ArrayList<Double>();
       end.add(start.get(0));
       end.add(start.get(1)); 
	   // i have already read all the sensors so i finish if i am within 0.0003
       while(true) {
			int i=34,order_visit = 35;
			current_location.set(0, list_point.get(list_point.size()-1).longitude());
			current_location.set(1,list_point.get(list_point.size()-1).latitude());
			var bool = avoidObstacles(i,order_visit,current_location, end,buildings,list_sensors);
			if (bool == true) {
				break;
			}
       }
       System.out.println(nr_steps);
       System.out.println(list_point.size());
       System.out.println(list_angles.size());
       System.out.println(list_sensors_visited.size());
       
       Output.generateJson(list_sensors, coordinates, list_point, buildings,day,month,year);
       Output.generateText(list_sensors_visited, list_angles, list_point, day, month, year);
    	
    }
    
    private static boolean avoidObstacles(int i,int perm,ArrayList<Double> current_location, ArrayList<Double> dest,ArrayList<Polygon> buildings,ArrayList<Sensor> list_sensors) {
    	if(visited ==33 && PathPlanner.euclid_dist(current_location,dest )<= 0.0003) {
			return true;
		}
		nr_steps++;
		var angle = angle(current_location,dest);
		int angle_int = (int)Math.round(angle/10.0) * 10;
		if(angle_int == 360) angle_int = 0;
		System.out.println(angle_int);
		System.out.println(visited);
        double angle_radians = angle_int*Math.PI / 180.0 ;
		var lng = current_location.get(0) + 0.0003 * Math.cos(angle_radians);
		var lat = current_location.get(1) + 0.0003* Math.sin(angle_radians);
		var temp = new ArrayList<Double>();
		temp.add(lng);
		temp.add(lat);
		if(!PathPlanner.intersectsBuildings(current_location, temp, buildings)) {
			
			current_location.set(0, lng);
			current_location.set(1, lat);
			var point = Point.fromLngLat(lng, lat);
			list_point.add(point);
			System.out.println(temp.get(0) + ":" + temp.get(1));
			System.out.println(" ");
			System.out.println(dest.get(0)+ ":" + dest.get(1));
		}// if temp is in polygons try optional route 
		else {
			var opt1 = new ArrayList<Double>();
			var opt2 = new ArrayList<Double>();
			var opt3 = new ArrayList<Double>();
			var opt4 = new ArrayList<Double>();
			lng = current_location.get(0) + 0.0003* getsgn(Math.cos(angle_radians));
			lat = current_location.get(1) + 0.0003* getsgn(Math.sin(angle_radians));
			var lng1 = current_location.get(0);
			var lat1 = current_location.get(1);
			var lng2 = current_location.get(0) - 0.0003* getsgn(Math.cos(angle_radians));
			var lat2 = current_location.get(1) - 0.0003* getsgn(Math.sin(angle_radians));
			opt1.add(lng1);
			opt1.add(lat);
			opt2.add(lng);
			opt2.add(lat1);
			opt3.add(lng2);
			opt3.add(lat1);
			opt4.add(lng1);
			opt4.add(lat2);
			if(!PathPlanner.intersectsBuildings(current_location,opt1, buildings)) {
	    		if(opt1.get(1) >current_location.get(1) ) {
	    			angle_int = 90;
	    		}
	    		else {
	    			angle_int = 270;
	    		}
	    		System.out.println(temp.get(0) + ":" + temp.get(1));
				System.out.println(" ");
				System.out.println(dest.get(0)+ ":" + dest.get(1));
				current_location.set(0, opt1.get(0));
				current_location.set(1, opt1.get(1));
				var point = Point.fromLngLat(current_location.get(0), current_location.get(1));
    			list_point.add(point);
				
			}
			else if(!PathPlanner.intersectsBuildings(current_location,opt2, buildings)) {
				if(opt2.get(0)>current_location.get(0)) {
					angle_int = 0;
				}
				else {
					angle_int = 90;
				}
				current_location.set(0, opt2.get(0));
				current_location.set(1, opt2.get(1));
				var point = Point.fromLngLat(current_location.get(0), current_location.get(1));
    			list_point.add(point);
			}
			else if (!PathPlanner.intersectsBuildings(current_location,opt3, buildings)) {
				if(opt3.get(0) >current_location.get(0) ) {
	    			angle_int = 0;
	    		}
	    		else {
	    			angle_int = 180;
	    		}
	    		angle_radians = angle_int*Math.PI / 180.0;
				current_location.set(0, opt3.get(0));
				current_location.set(1, opt3.get(1));
				var point = Point.fromLngLat(current_location.get(0), current_location.get(1));
    			list_point.add(point);
				
			}
			else {
				if(opt4.get(1)>current_location.get(1)) {
					angle_int = 90;
				}
				else {
					angle_int = 270;
				}
				current_location.set(0, opt4.get(0));
				current_location.set(1, opt4.get(1));
				var point = Point.fromLngLat(current_location.get(0), current_location.get(1));
    			list_point.add(point);
			}
		}
		list_angles.add(angle_int);
		
		if(PathPlanner.euclid_dist(current_location,dest )<= 0.0002 && visited < 33) {
			visited++;
			System.out.println(visited);
			var sensor = list_sensors.get(perm-1);
			sensor.set_status(true);
			list_sensors_visited.add(sensor.get_location());
			return true;
		}
		
		list_sensors_visited.add("null");
		return false;
    }
    
    private static double angle(ArrayList<Double> start, ArrayList<Double> end) {
 	   double angle = Math.atan2(-start.get(1)+end.get(1), -start.get(0) + end.get(0));
 	   angle = rad_to_degree(angle);
 	   if (angle < 0) angle += 360;
 	   return angle;
    }

    private static double rad_to_degree(double angle) {
    	return angle * 180 / Math.PI;
    }
 
    
    private static int getsgn(double d) {
    	if(d > 0) {
    		return 1;
    	}
    	else {
    		return -1;
    	}
    }
    
    
}
