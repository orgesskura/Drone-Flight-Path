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
    	JsonParser.parseJSon("01","01","2020","8888");
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
    	list_point.add(Point.fromLngLat(start.get(0), start.get(1)));
    	for(int i=1;i<coordinates.size();i++) {
    		var order_visit = perm[i];
    		var dest = new ArrayList<Double>();
    		dest = coordinates.get(order_visit);
    		var angle = angle(current_location,dest);
    		var angle_int = Math.round(angle/10.0) * 10;

    		double angle_radians = angle_int*Math.PI / 180.0 ;
    		
    		var point1 = Point.fromLngLat(current_location.get(0), current_location.get(1));
    		while(true) {
    			nr_steps++;
    			angle = angle(current_location,dest);
        		angle_int = Math.round(angle/10.0) * 10;
        		System.out.println(angle_int);
                angle_radians = angle_int*Math.PI / 180.0 ;
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
    		    		angle_radians = angle_int*Math.PI / 180.0;
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
            			System.out.println("1");
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
    			
    			if(PathPlanner.euclid_dist(current_location,dest )<= 0.0002) {
    				visited++;
    				System.out.println(visited);
    				var sensor = list_sensors.get(i-1);
    				sensor.set_status(true);
    				break;
    			}
    			
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
       System.out.println(list_point.size());
       
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
  	   features.addAll(mark_sensors(list_sensors,coordinates));
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
    //segment a line to find if parts of it are in its intersection
//    private static boolean segmentLine(ArrayList<Double> start, ArrayList<Double> end, 
//    		ArrayList<Polygon> pols) {
//    	double diff_x = end.get(0) - start.get(0);
//    	double diff_y = end.get(1) - start.get(1);
//    	diff_x /= 100;
//    	diff_y /=100;
//    	for(int i=1;i<=20;i++) {
//    		double x = start.get(0) + diff_x*i;
//    		double y = start.get(1) + diff_y*i;
//    		var list = new ArrayList<Double>();
//    		list.add(x);
//    		list.add(y);
//    		if(PathPlanner.inPolygons(list, pols)) {
//    			return true;
//    		}
//    	}
//    	return false;
//    }
    
    
    
    // create a list of features
    private static ArrayList<Feature> mark_sensors(ArrayList<Sensor> sensors,ArrayList<ArrayList<Double>> coordinates){
    	var fl = new ArrayList<Feature>();
    	for(int i=1;i<coordinates.size();i++) {
    		var sensor = sensors.get(i-1);
    		var loc = coordinates.get(i);
    		var point = Point.fromLngLat(loc.get(0), loc.get(1));
    		var geo = (Geometry) point;
    		var feat = Feature.fromGeometry(geo);
    		var reading = sensor.get_reading();
    		var location = sensor.get_location();
    		var battery = sensor.get_batteryLevel();
    		var visited = sensor.get_status();
    		boolean hasBattery = false;
    		if(battery >=10) {
    			hasBattery = true;
    		}
    		var list_string = new ArrayList<String>();
    		var input = 0.0;
    		if(!reading.equals("null")) {
    			input = Double.valueOf(reading);
    		}
    		else {
    			input = -1;
    		}
    		list_string = mapValue(visited,hasBattery,input);
    		if(list_string.size()==2) {
    			feat.addStringProperty("location", location);
    			feat.addStringProperty("rgb-string", list_string.get(0));
    			feat.addStringProperty("marker-color", list_string.get(1));
    		}
    		else if (list_string.size()==3) {
    			feat.addStringProperty("location", location);
    			feat.addStringProperty("rgb-string", list_string.get(0));
    			feat.addStringProperty("marker-color", list_string.get(1));
    			feat.addStringProperty("marker-symbol", list_string.get(2));
    		}
    		fl.add(feat);
    	}
    	return fl;
    }
    
    
    // map data to markers
    private static ArrayList<String> mapValue(boolean visited, boolean hasBattery,double input) {
    	var list = new ArrayList<String>();
    	if(visited) {
    		if(hasBattery) {
			    	if (input>=0 && input< 32) {
			    		list.add("#00ff00");
			    		list.add("#00ff00");
						list.add("lighthouse");
					}
					else if (input <64) {
						list.add("#40ff00");
						list.add("#40ff00");
			            list.add("lighthouse");
					}
					else if(input< 96) {
						list.add("#80ff00");
						list.add("#80ff00");
						list.add("lighthouse");
					}
					else if  (input < 128) {
						list.add("#c0ff00");
						list.add("#c0ff00");
						list.add("lighthouse");
					}
					else if (input< 160) {
						list.add("#ffc000");
						list.add("#ffc000");
						list.add("danger");
					}
					else if(input<192) {
						list.add("#ff8000");
						list.add("#ff8000");
						list.add("danger");
					}
					else if(input<224) {
						list.add("#ff4000");
						list.add("#ff4000");
						list.add("danger");
					}
					else {
						list.add("#ff0000");
						list.add("#ff0000");
						list.add("danger");
					}
    		}
    		else {
    			  list.add("#000000");
    			  list.add("#000000");
    			  list.add("cross");
    		}
    }
    	else {
    		list.add("#aaaaaa");
    		list.add("#aaaaaa");
    	}
    	return list;
    
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
