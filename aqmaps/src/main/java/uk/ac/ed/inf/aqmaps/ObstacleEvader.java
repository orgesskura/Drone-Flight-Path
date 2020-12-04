package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class ObstacleEvader {
	static ArrayList<Point> list_point = new ArrayList<Point>();
	static int nr_steps = 0;
	static ArrayList<String> list_sensors_visited = new ArrayList<String>();
	static ArrayList<Integer> list_angles = new ArrayList<Integer>();
	static int visited = 0;
	
	static boolean avoidObstacles(int i,int perm,ArrayList<Double> current_location, ArrayList<Double> dest,ArrayList<Polygon> buildings,ArrayList<Sensor> list_sensors,Drone drone) {
    	if(visited ==33 && PathPlanner.euclid_dist(current_location,dest )< App.step_size) {
			return true;
		}
    	var step_size = App.step_size;
    	if(!drone.has_moves_left()) return false;
		nr_steps++;
		var angle = angle(current_location,dest);
		int angle_int = (int)Math.round(angle/10.0) * 10;
		if(angle_int == 360) angle_int = 0;
        double angle_radians = angle_int*Math.PI / 180.0 ;
		var lng = current_location.get(0) + step_size * Math.cos(angle_radians);
		var lat = current_location.get(1) + step_size* Math.sin(angle_radians);
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
			lng = current_location.get(0) + step_size * getsgn(Math.cos(angle_radians));
			lat = current_location.get(1) + step_size * getsgn(Math.sin(angle_radians));
			var lng1 = current_location.get(0);
			var lat1 = current_location.get(1);
			var lng2 = current_location.get(0) - step_size * getsgn(Math.cos(angle_radians));
			var lat2 = current_location.get(1) - step_size * getsgn(Math.sin(angle_radians));
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
					angle_int = 180;
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
		var last_point = list_point.get(list_point.size()-1);
		drone.move(last_point);
		drone.decrease_moves();
		list_angles.add(angle_int);
		
		if(drone.in_range(dest) && visited < 33) {
			visited++;
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
