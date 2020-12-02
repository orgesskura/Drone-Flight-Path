package uk.ac.ed.inf.aqmaps;
import java.util.ArrayList;
import java.util.HashSet;
import com.mapbox.geojson.Polygon;
import java.lang.Math;
import java.awt.geom.Line2D;
import java.io.FileWriter;
import java.io.IOException;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.LineString;


public class PathPlanner {
   // have global dist matrix and permutation array  to show distance between points and order of visiting
   private static double[][] dist = new double[34][34];
   private static int[] perm = new int[34];
   // direction variable that may be used to indicate direction of points with respect to polygon
   private static int direction = 0;
   // add polygon number and line number to an array and return it
   private static ArrayList<Integer> nr = new ArrayList<Integer>();
   //think about not hardcoding 33 as number of sensors might change
   private static ArrayList<ArrayList<ArrayList<Point>>> visitedPoints = new ArrayList<ArrayList<ArrayList<Point>>>();
   public static void main( String[] args )
   {
   	   JsonParser.parseJSon("11","11","2020","8888");
   	   var buildings = new ArrayList<Polygon>();
   	   buildings = JsonParser.get_buildings();
   	   var coordinates = new ArrayList<ArrayList<Double>>();
   	   var start = new ArrayList<Double>();
   	   start.add(-3.188396);
   	   start.add(55.944425);
   	   coordinates.add(start);
   	   coordinates.addAll(JsonParser.get_coordinates());
   	   //do this for maintainability.Nr of sensors we want to visit might change in the future
   	   int nr_points = coordinates.size();
   	   getPath(coordinates,nr_points,buildings);
   	   double steps = tourValue() / 0.0003;
   	   System.out.println(steps);
   	   for(var i : perm) {
   		   System.out.print(i + "   ");
   	   }
   	 // Uncomment this part to write geJson after doing the algorithm
   	   var features = new ArrayList<Feature>();
   	   for (var pol : buildings) {
   		   var geo = (Geometry) pol;
   		   var feat = Feature.fromGeometry(geo);
   		   feat.addStringProperty("rgb-string", "#ff0000");
		   feat.addStringProperty("fill", "#ff0000");
		   features.add(feat);
   	   }
   	   var lines = writeTofile(coordinates);
   	   features.addAll(lines);
   	   var fc = FeatureCollection.fromFeatures(features);
   	   var str = fc.toJson();
   	   try (FileWriter file = new FileWriter("Attemp1.geojson")) {
		 
        file.write(str);
        file.flush();

      } catch (IOException e) {
        e.printStackTrace();
      }
   	   
   	
   }
   
   private static void initialize_perm(int n) {
	   for(int i=0;i<n;i++) {
		   perm[i] = i;
	   }
   }
   
// find  euclidean distance between 2 points
   public static double euclid_dist(ArrayList<Double> point1,ArrayList<Double> point2) {
	   double lng1 = point1.get(0),lng2 = point2.get(0);
	   double lat1 = point1.get(1),lat2 = point2.get(1);
	   double squared_dist = Math.pow((lng1-lng2), 2) + Math.pow((lat1-lat2), 2);
	   return Math.sqrt(squared_dist);
   }
   
   // buid a distance matrix
   private static void buildDistMatrix(ArrayList<ArrayList<Double>> list_coordinates,int nr_points,ArrayList<Polygon> pols) {
	   // find distance from sensor i to sensor j

	   for (int i=0;i<nr_points;i++) {
		   var  list_points = new ArrayList<ArrayList<Point>>();
		   for(int j=0;j<nr_points;j++) {
//			    this part is commented and will be copy - pasted after algorithm is finished
			   // use this dist variable later when finding a path out of no fly zones
			   var res = intersectsBuildings(list_coordinates.get(i),list_coordinates.get(j),pols);
			   var points = new ArrayList<Point>();
			   var start = Point.fromLngLat(list_coordinates.get(i).get(0), list_coordinates.get(i).get(1));
			   points.add(start);
			   var distance = euclid_dist(list_coordinates.get(i),list_coordinates.get(j));
           
			   if (res) {
				   System.out.println(i +":" + j);
				   for (var k : nr) {
					   System.out.println(k);
				   }
//				   points = buildDist(i,j,list_coordinates.get(i),list_coordinates.get(j),pols);
//     			   list_points.add(points);
				   distance = 1000;
			   }
			   dist[i][j] += distance;
		   }
	   }
   }
   
   // find current tour value
   public static double tourValue() {
       double tour_value=0;
       for (int i=0;i<34;i++) {
    	 //find current tour value 
         tour_value+=dist[perm[i]][perm[(i+1)%34]];
       }
       return tour_value;
   }
   
   
   
   // Attempt the swap of cities i and i+1 in perm and 
   // commit to the swap if it improves the cost of the tour.
  // Return True/False depending on success.
   
   private static Boolean trySwap(int i){
       int previous = perm[(i-1)%34];
       int first = perm[i];
       int second = perm[(i+1)%34];
       int next = perm[(i+2)%34];
       double firstTime = dist[previous][first] + dist[first][second] + dist[second][next];
       double secondTime = dist[previous][second] + dist[second][first] + dist[first][next];
       if(firstTime <= secondTime) {
           return false;
       }
       else {
           perm[i] = second;
           perm[(i+1)%34] = first;
           return true;
       }
        		   
   }
   
   // Consider the effect of reversing the segment between
   // self.perm[i] and self.perm[j], and commit to the reversal
   // if it improves the tour value.
   // Return True/False depending on success.
   private static Boolean tryReverse(int i,int j) {
	   double currentTour = 0;
	   double probableTour=0;
	   currentTour+=dist[perm[(i-1)%34]][perm[i]];
	   probableTour+=dist[perm[(i-1)%34]][perm[j]];
	   currentTour+=dist[perm[j]][perm[(j+1)%34]];
	   probableTour+=dist[perm[i]][perm[(j+1)%34]];
	   int first=i;
	   int second=j;
	   if (currentTour <= probableTour) {
            return false;
               }
	   while(first <= second) {
		    int temp = perm[first];
		    perm[first] = perm[second];
		    perm[second] = temp;
		    first = first + 1;
		    second = second - 1;
		    }
	   return true;
   }
   
   // try continous swaps until a local optimum is reached
   private static void swapHeuristic(int nr_points) {
	   boolean better = true;
		        while (better) {
		            better = false;
		            for (int i=1;i<nr_points-1;i++) {
		                if (trySwap(i)) {
		                    better = true;
		                }
		            }
		        }
   }
   
   // try reversing segments until a local maximum is reached
   private static void TwoOptHeuristic(int nr_points) {
       boolean better = true;
       while (better==true) {
           better = false;
           for (int j=1;j<nr_points;j++) {
               for(int i=1;i<j;i++) {
                   if (tryReverse(i,j)) {
                       better = true;
                   }
               }
           }
       }
                       
   }
   
   // generate the path using swapHeuristic and TwoOptHeuristic
   //using them together generates better results
   public static void getPath(ArrayList<ArrayList<Double>> list_coordinates,int nr_points,ArrayList<Polygon> pols) {
	   initialize_perm(nr_points);
	   buildDistMatrix(list_coordinates,nr_points,pols);
	   swapHeuristic(nr_points);
	   TwoOptHeuristic(nr_points);
   }
   
   
   
   
   private static Boolean intersectsBuildings(ArrayList<Double>point1,ArrayList<Double>point2,ArrayList<Polygon> pols) {
	   nr.removeAll(nr);
	   // create a new line
	   var line = new Line2D.Double(point1.get(0),point1.get(1),point2.get(0),point2.get(1));
	   //hashset that contains if line crosses with any of the lines of the polygons
	   var intersec = new HashSet<Integer>();
	   // just a counter to use to address polygon number
	   int counter = 1;
	   // use this to address line number
	   int counter2 = 0;
	   // use this to get polygon number
	   int counter1 = 0;
	   // use this to find the closest line
	   double min = 10000;
	   for(var pol : pols) {
		   // get List<List<Point>> from a polygon object
		   var points = pol.coordinates();
		   // this is where list of lines of the polygon will be stored
		   var list_points = new ArrayList<ArrayList<Double>>();
		   for (var point : points.get(0)) {
			   // get longitude and latitude from polygon
			   double lng = point.longitude();
			   double lat = point.latitude();
			   // create a  list that stores points
			   var list_point = new ArrayList<Double>();
			   list_point.add(lng);
			   list_point.add(lat);
			   list_points.add(list_point);
		   }
		   double lng1 = point1.get(0);
		   double lng2 = point1.get(1);
		   double lat1 = point2.get(0);
		   double lat2 = point2.get(1);
		   var list1 = new ArrayList<Double>();
		   var list2 = new ArrayList<Double>();
		   list1.add(lng1);
		   list1.add(lat1);
		   list2.add(lng2);
		   list2.add(lat2);
		   // get all the lines of the polygon
		   for (int i=0;i<list_points.size()-1;i++) {
			   int y = (i+1) % list_points.size();
			   var line1 = new Line2D.Double(list_points.get(i).get(0),list_points.get(i).get(1),
					   list_points.get(y).get(0),list_points.get(y).get(1));
			   //if my line and one of the lines intersect, then 
			   if(line.intersectsLine(line1)) {
				   intersec.add(counter);
				   if (findDist(list_points.get(i),list_points.get(y),list1,list2) < min ) {
					   min = findDist(list_points.get(i),list_points.get(y),list1,list2);
					   counter1 = counter;
					   counter2 = i+1;
				   }
			   }
		   }
		  counter ++; 
	   }
		     // add polygon number and line number to an array and return it
	    nr.add(counter1);
	    nr.add(counter2);
		if(intersec.size()==0) {
		    	return false;
		    }
		return true;
   }
   
   
   
   //function to find distance from intersection of 2 lines
   private static double findDist(ArrayList<Double> point1,ArrayList<Double> point2, ArrayList<Double> point3,ArrayList<Double>point4) {
	   if(point3.get(0) == point4.get(0)) {
		   double lng = point3.get(0);
		   double lat = ((point2.get(1)-point1.get(1))/(point2.get(0)-point1.get(0))) * (lng - point1.get(0)) + point1.get(1);
		   double squared_dist = Math.pow((point3.get(1)-lat), 2);
		   return Math.sqrt(squared_dist);
	   }
	   else {
		   // calculations done on paper
		  double diff = (point2.get(1) - point1.get(1))/(point2.get(0) - point1.get(0));
		  diff -= (point4.get(1) - point3.get(1))/(point4.get(0) - point3.get(0));
		  double right_hand = point1.get(0)*((point2.get(1) - point1.get(1))/(point2.get(0) - point1.get(0)));
		  right_hand -= point3.get(0)*((point4.get(1) - point3.get(1))/(point4.get(0) - point3.get(0)));
		  right_hand += point3.get(1) - point1.get(1);
		  double lng = right_hand / diff;
		  double lat = ((point4.get(1) - point3.get(1))/(point4.get(0) - point3.get(0))) * (lng - point3.get(0)) + point3.get(1);
		  double squared_dist = Math.pow((point3.get(0)-lng), 2) + Math.pow((point3.get(1)-lat), 2);
		  return Math.sqrt(squared_dist);
	   }
   }
   
   
   
   // function to determine direction of the point with respect to the intersecting line
   private static ArrayList<Double> encodingDirection(ArrayList<Double> point1,int polNumber,int lineNumber,ArrayList<Polygon> pols) {
	   var return_points = new ArrayList<Double>();
	// get List<List<Point>> from a polygon object
	   var points = pols.get(nr.get(0)-1).coordinates();
	   // this is where list of lines of the polygon will be stored
	   var list_points = new ArrayList<ArrayList<Double>>();
	   
	   for (var point : points.get(0)) {
		   // get longitude and latitude from polygon
		   double lng = point.longitude();
		   double lat = point.latitude();
		   // create a  list that stores points
		   var list_point = new ArrayList<Double>();
		   list_point.add(lng);
		   list_point.add(lat);
		   list_points.add(list_point);
            }
	  var startPoint = new ArrayList<Double>();
	  startPoint = list_points.get(lineNumber-1);
	  var endPoint = new ArrayList<Double>();
	  endPoint = list_points.get(lineNumber);
	  double start_lng = startPoint.get(0);
	  double start_lat = startPoint.get(1);
	  double end_lng = endPoint.get(0);
	  double end_lat = endPoint.get(1);
	  double x_coord = point1.get(0);
	  double y_coord = point1.get(1);
	  return_points.add(start_lng);
	  return_points.add(start_lat);
	  return_points.add(end_lng);
	  return_points.add(end_lat);
	  System.out.println(end_lng);
	  System.out.println(end_lat);
	  if(y_coord >= end_lat && y_coord <= start_lat && x_coord < start_lng && x_coord < end_lng) {
		  return_points.add(180.0);
	  }
	  else if (x_coord <= end_lng && x_coord >= start_lng && y_coord < start_lat && y_coord < end_lat) {
		  return_points.add(270.0);
	  }
	  else if (y_coord <= end_lat && y_coord >= start_lat && x_coord > start_lng && x_coord > end_lng) {
		  return_points.add(0.0);
	  }
	  else  {
		  return_points.add(90.0);
	  }
	  return return_points;
	  
   }
   
   private static ArrayList<Point> buildDist(int i,int j,ArrayList<Double> start, ArrayList<Double> dest,ArrayList<Polygon> pols) {
	   boolean result=true;
	   var points = new ArrayList<Point>();
	   var list = new ArrayList<Double>();
	   list.add(start.get(0));
	   list.add(start.get(1));
	   while(result) {
		       
    	       var list2 = new ArrayList<Double>();
    	       list2.add(dest.get(0));
	           list2.add(dest.get(1));
		       while(intersectsBuildings(list,list2,pols)) {
		    	   var return_points = new ArrayList<Double>();
		           return_points = encodingDirection(list,nr.get(0),nr.get(1),pols);
		       //case when point is north of polygon
			       if(return_points.get(4)==90.0) {
				    if (list.get(0) > list2.get(0) ) {
						   double lng = return_points.get(0);
						   double lat = return_points.get(1);
						   lng+=0.0003;
						   list2.removeAll(list2);
						   list2.add(lng);
						   list2.add(lat);
					   }
				    else {
					   double lng = return_points.get(2);
					   double lat = return_points.get(3);
					   lng-=0.0003;
					   list2.removeAll(list2);
					   list2.add(lng);
					   list2.add(lat);
				   }
			   
			   }
			   // case when point is east of polygon 
			   else if (return_points.get(4)==0.0) {
				   double lng = return_points.get(0);
				   double lat = return_points.get(1);
				   lng+=0.0003;
				   list2.removeAll(list2);
				   list2.add(lng);
				   list2.add(lat);
			   }
			   // case when point is south of polygon
			   else if (return_points.get(4)==270.0) {
				   double lng = return_points.get(0);
				   double lat = return_points.get(1);
				   lng-=0.0003;
				   list2.removeAll(list2);
				   list2.add(lng);
				   list2.add(lat);
			   }
			   // case when is north of polygon
			   else {
				   double lng = return_points.get(2);
				   double lat = return_points.get(3);
				   lng-=0.0003;
				   list2.removeAll(list2);
				   list2.add(lng);
				   list2.add(lat);
			   }
		   }
     	   dist[i][j] += euclid_dist(list,list2);
		   list.clear();
		   list.add(list2.get(0));
		   list.add(list2.get(1));
		   
		   Point point = Point.fromLngLat(list.get(0),list.get(1));
		   points.add(point);
		   if (list2.get(0)==dest.get(0) && list2.get(1) == dest.get(1)) {
			   break;
		   }
	   }
	   return points;
   }
   
   private static ArrayList<Point> anotherMethod(ArrayList<Double> start, ArrayList<Double> dest){
	   double dist1 = 0 ;
	   double dist2 = 0;
	   var point1 = new ArrayList<Point>();
	   var point2 = new ArrayList<Point>();
	   var start1 = new ArrayList<Double>();
	   var start2 = new ArrayList<Double>();
	   return new ArrayList<Point>();
   }
   
   // function to return 2 ends of polygon
   private static ArrayList<Point> findPoints(ArrayList<Double> start, int polNr, Polygon pol){
       var coordinates = pol.coordinates();
       var list_points = new ArrayList<Point>();
       for(var point : coordinates.get(0)) {
    	   var lng = point.longitude();
    	   var lat = point.latitude();
    	   var p = Point.fromLngLat(lng, lat);
    	   list_points.add(p);
       }
       double min = 360;
       double max = 0;
       int min_i = -1;
       int max_i = -1;
       int counter = 0;
       for (var p :list_points ) {
    	   double angle = angle(start,p);
    	   if(angle < min) {
    		   min = angle;
    		   min_i = counter;
    	   }
    	   if (angle > max) {
    		   max = angle;
    		   max_i = counter;
    	   }
    	   
    	   
    	   counter++;
       }
       var min_point = Point.fromLngLat(list_points.get(min_i).longitude(), list_points.get(min_i).latitude());
       var max_point = Point.fromLngLat(list_points.get(max_i).longitude(), list_points.get(max_i).latitude());
       
       var return_points =new ArrayList<Point>();
       return_points.add(min_point);
       return_points.add(max_point);
       return return_points;
       
       
   }
   
   private static double angle(ArrayList<Double> start, Point a) {
	   double angle = (float)Math.atan2(start.get(1)-a.latitude(), start.get(0) - a.longitude());
	   angle = toDegrees(angle);
	   if (angle < 0) angle += 360;
	   return angle;
   }
   
   private static double toDegrees(double a) {
	   return a *180/Math.PI;
   }
   
   private static ArrayList<Feature> writeTofile(ArrayList<ArrayList<Double>> coordinates){
	   var fc = new ArrayList<Feature>();
	   var list_point = new ArrayList<Point>();
	   for(var i : perm) {
		   var list = coordinates.get(i);
		   var point = Point.fromLngLat(list.get(0), list.get(1));
		   list_point.add(point);
	   }
	   var start = coordinates.get(0);
	   var point1 = Point.fromLngLat(start.get(0), start.get(1));
	   list_point.add(point1);
	   var line =  LineString.fromLngLats(list_point);
	   var geo = (Geometry)line;
	   var feat = Feature.fromGeometry(geo);
	   fc.add(feat);
	   return fc;
   }
   
 
   
   
   
   // this part is commented and will be copy - pasted after algorithm is finished
//   boolean res = intersectsBuildings(list_coordinates.get(i),list_coordinates.get(j),pols);
//   var list_point = new ArrayList<Point>();
//   var point1 =  Point.fromLngLat(list_coordinates.get(i).get(0), list_coordinates.get(i).get(1));
//   var point2 =  Point.fromLngLat(list_coordinates.get(j).get(0), list_coordinates.get(j).get(1));
//   list_point.add(point1);
//   list_point.add(point2);
//   var line =  LineString.fromLngLats(list_point);
//   var geo = (Geometry)line;
//   var feat = Feature.fromGeometry(geo);
//   feat.addNumberProperty("fill-opacity",0.75);
//   if(res) {
//	   feat.addStringProperty("stroke", "#ff0000");
//	   feat.addStringProperty("rgb-string", "#ff0000");
//		   feat.addStringProperty("fill", "#ff0000");
//   }
//   else {
//		feat.addStringProperty("rgb-string", "#00ff00");
//		feat.addStringProperty("fill", "#00ff00");
//		}
//   features.add(feat);
   
   
	   
   
   
   
   
   
	
}
