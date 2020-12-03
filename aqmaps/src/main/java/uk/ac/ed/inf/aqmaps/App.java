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
	
    public static void main( String[] args )
    {
    	JsonParser.parseJSon("05","05","2020","8888");
    	var buildings = new ArrayList<Polygon>();
    	buildings = JsonParser.get_buildings();
    	var coordinates = new ArrayList<ArrayList<Double>>();
    	var start = new ArrayList<Double>();
    	start.add(-3.188396);
    	start.add(55.944425);
    	coordinates.add(start);
    	coordinates.addAll(JsonParser.get_coordinates());
    	int visited = 0;
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
    	var fl = new ArrayList<Feature>();
 	    var list_point = new ArrayList<Point>();
    	var current_location = new ArrayList<Double>();
    	current_location.add(start.get(0));
    	current_location.add(start.get(1));
    	int nr_steps = 0;
    	for(int i=1;i<coordinates.size();i++) {
    		var order_visit = perm[i];
    		var dest = new ArrayList<Double>();
    		dest = coordinates.get(order_visit);
    		var angle = angle(current_location,dest);
    		var angle_int = Math.round(angle/10.0) * 10;
//    		if(PathPlanner.intersectsBuildings(current_location, dest , buildings)) {
//    		   System.out.println("point at " + i + " is fucked up!!");
//    		   if(current_location.get(1) > dest.get(1) ) {
//    			   if(current_location.get(0) < dest.get(0)) {
//    				   dest.set(0, dest.get(0) + 0.0002);
//    			   }
//    			   else if (current_location.get(0) < dest.get(0)) {
//    				   dest.set(0, dest.get(0) - 0.0002);
//    			   }
//    		   }
//    		   else {
//    			   if(current_location.get(0) < dest.get(0)) {
//    				   dest.set(0, dest.get(0) - 0.0002);
//    			   }
//    			   else if (current_location.get(0) < dest.get(0)) {
//    				   dest.set(0, dest.get(0) + 0.0002);
//    			   }
//    			   
//    		   }
//    		
//    		}
    		double angle_radians = angle_int*Math.PI / 180.0 ;
    		System.out.println(angle);
    		var point1 = Point.fromLngLat(current_location.get(0), current_location.get(1));
    		list_point.add(point1);
    		while(true) {
    			nr_steps++;
    			var lng = current_location.get(0) + 0.0003 * Math.cos(angle_radians);
    			var lat = current_location.get(1) + 0.0003* Math.sin(angle_radians);
    			var temp = new ArrayList<Double>();
    			temp.add(lng);
    			temp.add(lat);
    			if(!PathPlanner.inPolygons(temp, buildings)) {
    				current_location.set(0, lng);
        			current_location.set(1, lat);
        			var point = Point.fromLngLat(lng, lat);
        			list_point.add(point);
        			
    			}// if temp is in polygons try optional route 
    			else {
    				var opt1 = new ArrayList<Double>();
    				var opt2 = new ArrayList<Double>();
    				var lng1 = current_location.get(0);
    				var lat1 = current_location.get(1);
    				opt1.add(lng1);
    				opt1.add(lat);
    				opt2.add(lng);
    				opt2.add(lat1);
    				if(!PathPlanner.inPolygons(opt1, buildings)) {
    					current_location.set(0, opt1.get(0));
    					current_location.set(1, opt1.get(1));
    					angle = angle(current_location,dest);
    		    		angle_int = Math.round(angle/10.0) * 10;
    		    		angle_radians = angle_int*Math.PI / 180.0 ;
    				}
    				else {
    					current_location.set(0, opt2.get(0));
    					current_location.set(1, opt2.get(1));
    					angle = angle(current_location,dest);
    		    		angle_int = Math.round(angle/10.0) * 10;
    		    		angle_radians = angle_int*Math.PI / 180.0 ;
    				}
    			}
    			
    			if(PathPlanner.euclid_dist(current_location,dest )<= 0.0002) {
    				visited++;
    				break;
    			}
//    			else if (PathPlanner.euclid_dist(current_location,dest )<= 0.0003) {
//    				
//    				var angle2 = angle(current_location,dest);
//    	    		var angle_int2 = Math.round(angle2/10.0) * 10;
//    	    		var angle_radians2 = angle_int2*Math.PI / 180.0;
//    	    		
//        			while(PathPlanner.euclid_dist(current_location,dest ) > 0.0002) {
//        				nr_steps++;
//        				var lng2 = current_location.get(0) + 0.0003 * Math.cos(angle_radians2);
//            			var lat2 = current_location.get(1) + 0.0003* Math.sin(angle_radians2);
//            			current_location.set(0, lng2);
//            			current_location.set(1, lat2);
//        			}
//        			visited++;
//        			break;
//        			
//    			}
    		}
    	}
       System.out.println(visited);
       var end = new ArrayList<Double>();
       end.add(start.get(0));
       end.add(start.get(1));
       var angle = angle(current_location,end);
	   var angle_int = Math.round(angle);
	   angle_int = (angle_int/10) * 10;
	   double angle_radians = angle*Math.PI / 180.0 ;
	   // i have already read all the sensors so i finish if i am within 0.0003
       while(true) {
    	   if(PathPlanner.euclid_dist(current_location, end)<= 0.0003) {
				break;
			}
    	    nr_steps++;
			var lng = current_location.get(0) + 0.0003 * Math.cos(angle_radians);
			var lat = current_location.get(1) + 0.0003* Math.sin(angle_radians);
			current_location.set(0, lng);
			current_location.set(1, lat);
			var point = Point.fromLngLat(lng, lat);
			list_point.add(point);
			
       }
       System.out.println(nr_steps);
       var line =  LineString.fromLngLats(list_point);
 	   var geo = (Geometry)line;
 	   var feat = Feature.fromGeometry(geo);
 	   fl.add(feat);
 	   var features = new ArrayList<Feature>();
  	   for (var pol : buildings) {
  		   var geo2 = (Geometry) pol;
  		   var feat2 = Feature.fromGeometry(geo2);
  		   feat2.addStringProperty("rgb-string", "#ff0000");
		   feat2.addStringProperty("fill", "#ff0000");
		   features.add(feat2);
  	   }
  	   features.addAll(fl);
  	   var fc = FeatureCollection.fromFeatures(features);
 	   var str = fc.toJson();
 	   try (FileWriter file = new FileWriter("Attemp1.geojson")) {
		 
           file.write(str);
           file.flush();

       } catch (IOException e) {
           e.printStackTrace();
       }
  	   
    	
    	
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
    
    
    
    
}
