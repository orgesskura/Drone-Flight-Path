package uk.ac.ed.inf.aqmaps;
import java.util.ArrayList;
import java.util.HashMap;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;




public class App 
{   // declare the final values  number of moves and limit of moving so that users do not generate paths with starting location out of confinement area
	private final static int allowed_moves = 150;
    private final static double left_limit_x = -3.192473;
    private final static double right_limit_x = -3.184319;
    private final static double left_limit_y = 55.942617;
    private final static double right_limit_y = 55.946233;
    // declare number of sensors,step size and range read final as i do not want them to change
    static final int number_sensors = 33;
    static final double step_size = 0.0003;
    static final double range_read = 0.0002;
    // declare final list of sensors,coordinates, start point and buildings(no fly zones)
    private static final ArrayList<Sensor> list_sensors = new ArrayList<Sensor>();
    private static final ArrayList<ArrayList<Double>> coordinates = new ArrayList<ArrayList<Double>>();
    private static final ArrayList<Double> start  = new ArrayList<Double>();
    private static final ArrayList<Polygon> buildings = new ArrayList<Polygon>();
    static final int[] perm = new int[number_sensors+1];
    
    public static void main( String[] args )
    {
    	// read input via read_input function and assign values to objects accordingly
    	read_input(args[0],args[1],args[2],args[4],args[3],args[6]);
    	// declare current location and set it as starting point
    	var current_location = new ArrayList<Double>();
    	current_location.add(start.get(0));
    	current_location.add(start.get(1));
    	// add start point to list_point object in Obstacle Evader. this will save the path of the drone
    	ObstacleEvader.list_point.add(Point.fromLngLat(start.get(0), start.get(1)));
    	var point = Point.fromLngLat(start.get(0),start.get(1));
    	// declare a new drone having as maximum number of moves allowed moves and starting location the variable point
    	var drone = new Drone(allowed_moves,point);
    	// iterate through all of the sensors
    	for(int i=1;i<coordinates.size();i++) {
    		// if drone does not have any more moves left, stop the algorithm
    		if(!drone.has_moves_left()) break;
    		// declare order of  visit as the sensor number to be next visited
    		var order_visit = perm[i];
    		var dest = new ArrayList<Double>();
    		// get coordinates of sensor
    		dest = coordinates.get(order_visit);
    		while(true) {
    			if(!drone.has_moves_left()) break;
    			// run through the ObstacleEvader algorithm to find which should be next point
    			var bool = ObstacleEvader.avoidObstacles(order_visit,current_location, dest,buildings,list_sensors,drone);
    			if (bool == true) {
    				break;
    			}
    		}
    	}
       var end = new ArrayList<Double>();
       end.add(start.get(0));
       end.add(start.get(1)); 
	   // i have already read all the sensors(or the sensor is out of battery) so i try to go close to starting point
       while(true) {
            if(!drone.has_moves_left()) break;
			int order_visit = 0;
			var longit = ObstacleEvader.list_point.get(ObstacleEvader.list_point.size()-1).longitude();
			var latitud = ObstacleEvader.list_point.get(ObstacleEvader.list_point.size()-1).latitude();
			current_location.set(0, longit);
			current_location.set(1,latitud);
			var bool = ObstacleEvader.avoidObstacles(order_visit,current_location, end,buildings,list_sensors,drone);
			if (bool == true) {
				break;
			}
       }
       // set current_location as drone current position
       var drone_x = drone.get_location().longitude();
       var drone_y = drone.get_location().latitude();
       current_location.set(0, drone_x);
       current_location.set(1, drone_y);
       // genereate geojson and text file using the data we have collected
       Output.generateJson(list_sensors, coordinates, ObstacleEvader.list_point, buildings,args[0],args[1],args[2],perm);
       Output.generateText(ObstacleEvader.list_sensors_visited, ObstacleEvader.list_angles, ObstacleEvader.list_point, args[0], args[1], args[2]);
       
       //if algorithm has gone through all the sensors and back print this
       if(ObstacleEvader.nr_steps < allowed_moves) {
    	   System.out.println("Success!! Drone read all the sensors and arrived close to its initial position in " +ObstacleEvader.nr_steps +" moves!!!!!" );
       }
       // if drone is out of moves but has gone through all the sensors and is close to starting point, the drone was successful
       else if(ObstacleEvader.nr_steps == allowed_moves && PathPlanner.euclid_dist(current_location, start) < 0.0003 && ObstacleEvader.visited==33) {
    	   System.out.println("Success!! Drone read all the sensors and arrived close to its initial position in " + allowed_moves+" moves!!!!");
       }
       // if the drone is out of moves ,and it is not close to starting position or it has not visited all the sensors, drone was not successful
       else {
    	   System.out.println("Task was incomplete");
       }
       
      
    	
    }
    
    // read input
    private static void read_input(String day,String month, String year, String longi, String latit, String port) {
    	// convert arguments to double
    	var longitude = Double.valueOf(longi);
    	var latitude = Double.valueOf(latit);
    	// check if input is in confinement area
    	check_input(longitude,latitude);
    	// parse Json with given arguments
    	JsonParser.parseJSon(day,month,year,port);
    	// get no-fly-zones and put them in the buildings object
    	var  building = new ArrayList<Polygon>();
    	building = JsonParser.get_buildings();
    	buildings.addAll(building);
    	// get the starting location and put it in start object
    	var coordinate = new ArrayList<ArrayList<Double>>();
    	var start_location = new ArrayList<Double>();
    	start_location.add(longitude);
    	start_location.add(latitude);
    	start.addAll(start_location);
    	// get all of the coordinates of the sensors and put them in coordinates object
    	coordinate.add(start);
    	coordinate.addAll(JsonParser.get_coordinates());
    	coordinates.addAll(coordinate);
    	//initialize all the sensor parameters
    	var list_sensor = new ArrayList<Sensor>();
    	var list_battery = new ArrayList<Double>();
    	list_battery = JsonParser.get_battery();
    	var list_readings = new ArrayList<String>();
    	list_readings = JsonParser.get_readings();
    	var mappings = new HashMap<ArrayList<Double>,String>();
    	mappings = JsonParser.get_mappings();
    	// get each sensor data and save all of the sensors in list_sensors
    	for(int i=1;i<coordinates.size();i++) {
    		var location_what3Words = mappings.get(coordinates.get(i));
    		var reading = list_readings.get(i-1);
    		var battery = list_battery.get(i-1);
    		var sensor = new Sensor(location_what3Words,reading,battery);
    		list_sensor.add(sensor);
    	}
    	list_sensors.addAll(list_sensor);
    	// use PathPlanner class to find permutation of coordinates that gives smallest distance and assign that array to perm
    	PathPlanner.getPath(coordinates,number_sensors + 1,buildings);
    	var perms = new int[34];
    	perms = PathPlanner.get_permutation();
    	for(int i=0;i<perms.length;i++) {
    		perm[i] = perms[i];
    	}
    }
    
    // check if longitude and latitude is in confinement area. If not, exit the program
    private static void check_input(double longitude,double latitude) {
    	if(longitude < left_limit_x || longitude > right_limit_x || latitude < left_limit_y || latitude > right_limit_y) {
    		System.out.println("Starting location is out of the confinement area. Run the application again");
    		System.exit(0);
    	}
    }
    
   
    
}
