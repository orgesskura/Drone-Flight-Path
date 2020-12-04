package uk.ac.ed.inf.aqmaps;
import java.util.ArrayList;
import java.util.HashMap;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;




public class App 
{   
	private final static int allowed_moves = 150;
    private final static double left_limit_x = -3.192473;
    private final static double right_limit_x = -3.184319;
    private final static double left_limit_y = 55.942617;
    private final static double right_limit_y = 55.946233;
    static final int number_sensors = 33;
    static final double step_size = 0.0003;
    static final double range_read = 0.0002;
    private static final ArrayList<Sensor> list_sensors = new ArrayList<Sensor>();
    private static final ArrayList<ArrayList<Double>> coordinates = new ArrayList<ArrayList<Double>>();
    private static final ArrayList<Double> start  = new ArrayList<Double>();
    private static final ArrayList<Polygon> buildings = new ArrayList<Polygon>();
    private static final int[] perm = new int[number_sensors+1];
    
    public static void main( String[] args )
    {
    	read_input(args[0],args[1],args[2],args[4],args[3],args[6]);
    	var current_location = new ArrayList<Double>();
    	current_location.add(start.get(0));
    	current_location.add(start.get(1));
    	ObstacleEvader.list_point.add(Point.fromLngLat(start.get(0), start.get(1)));
    	var point = Point.fromLngLat(start.get(0),start.get(1));
    	var drone = new Drone(allowed_moves,point);
    	for(int i=1;i<coordinates.size();i++) {
    		if(!drone.has_moves_left()) break;
    		var order_visit = perm[i];
    		var dest = new ArrayList<Double>();
    		dest = coordinates.get(order_visit);
    		while(true) {
    			if(!drone.has_moves_left()) break;
    			var bool = ObstacleEvader.avoidObstacles(i,order_visit,current_location, dest,buildings,list_sensors,drone);
    			if (bool == true) {
    				break;
    			}
    		}
    	}
       var end = new ArrayList<Double>();
       end.add(start.get(0));
       end.add(start.get(1)); 
	   // i have already read all the sensors so i finish if i am within 0.0003
       while(true) {
            if(!drone.has_moves_left()) break;
			int i=0,order_visit = 0;
			var longit = ObstacleEvader.list_point.get(ObstacleEvader.list_point.size()-1).longitude();
			var latitud = ObstacleEvader.list_point.get(ObstacleEvader.list_point.size()-1).latitude();
			current_location.set(0, longit);
			current_location.set(1,latitud);
			var bool = ObstacleEvader.avoidObstacles(i,order_visit,current_location, end,buildings,list_sensors,drone);
			if (bool == true) {
				break;
			}
       }
       var drone_x = drone.get_location().longitude();
       var drone_y = drone.get_location().latitude();
       current_location.set(0, drone_x);
       current_location.set(1, drone_y);
       
       if(ObstacleEvader.nr_steps < allowed_moves) {
    	   System.out.println("Success!! Drone read all the sensors and arrived close to its initial position in " +ObstacleEvader.nr_steps +" moves!!!!!" );
       }
       else if(ObstacleEvader.nr_steps == allowed_moves && PathPlanner.euclid_dist(current_location, start) < 0.0003) {
    	   System.out.println("Success!! Drone read all the sensors and arrived close to its initial position in " + allowed_moves+" moves!!!!");
       }
       else {
    	   System.out.println("Task was incomplete");
       }
       
       Output.generateJson(list_sensors, coordinates, ObstacleEvader.list_point, buildings,args[0],args[1],args[2]);
       Output.generateText(ObstacleEvader.list_sensors_visited, ObstacleEvader.list_angles, ObstacleEvader.list_point, args[0], args[1], args[2]);
    	
    }
    
    
    private static void read_input(String day,String month, String year, String longi, String latit, String port) {
    	var longitude = Double.valueOf(longi);
    	var latitude = Double.valueOf(latit);
    	check_input(longitude,latitude);
    	JsonParser.parseJSon(day,month,year,port);
    	var  building = new ArrayList<Polygon>();
    	building = JsonParser.get_buildings();
    	buildings.addAll(building);
    	var coordinate = new ArrayList<ArrayList<Double>>();
    	var start_location = new ArrayList<Double>();
    	start_location.add(longitude);
    	start_location.add(latitude);
    	start.addAll(start_location);
    	coordinate.add(start);
    	coordinate.addAll(JsonParser.get_coordinates());
    	coordinates.addAll(coordinate);
    	var list_sensor = new ArrayList<Sensor>();
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
    		list_sensor.add(sensor);
    	}
    	list_sensors.addAll(list_sensor);
    	PathPlanner.getPath(coordinates,number_sensors + 1,buildings);
    	var perms = new int[34];
    	perms = PathPlanner.get_permutation();
    	for(int i=0;i<perms.length;i++) {
    		perm[i] = perms[i];
    	}
    }
    
    
    private static void check_input(double longitude,double latitude) {
    	if(longitude < left_limit_x || longitude > right_limit_x || latitude < left_limit_y || latitude > right_limit_y) {
    		System.out.println("Starting location is out of the confinement area. Run the application again");
    		System.exit(0);
    	}
    }
    
   
    
}
