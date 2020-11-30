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
   //think about not hardcoding 33 as number of sensors might change
   public static void main( String[] args )
   {
   	   JsonParser.parseJSon("04","04","2020","8888");
   	   var buildings = new ArrayList<Polygon>();
   	   buildings = JsonParser.get_buildings();
   	   var coordinates = new ArrayList<ArrayList<Double>>();
   	   var start = new ArrayList<Double>();
   	   start.add(-3.188396);
   	   start.add(55.944425);
   	   coordinates.add(start);
   	   coordinates.addAll(JsonParser.get_coordinates());
   	// do this for maintainability.Nr of sensors we want to visit might change in the future
   	   int nr_points = coordinates.size();
   	  // getPath(coordinates,nr_points,buildings);
   	   double steps = tourValue() / 0.0003;
   	   System.out.println(steps);
   	   
   	 // Uncomment this part to write geJson after doing the algorithm
   	   var features = new ArrayList<Feature>();
   	   for (var pol : buildings) {
   		   var geo = (Geometry) pol;
   		   var feat = Feature.fromGeometry(geo);
   		   feat.addStringProperty("rgb-string", "#ff0000");
		   feat.addStringProperty("fill", "#ff0000");
		   features.add(feat);
   	   }
   	   var lines = buildDistMatrix(coordinates,nr_points,buildings);
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
   private static ArrayList<Feature> buildDistMatrix(ArrayList<ArrayList<Double>> list_coordinates,int nr_points,ArrayList<Polygon> pols) {
	   // find distance from sensor i to sensor j
	   var features = new ArrayList<Feature>();
	   for (int i=0;i<nr_points;i++) {
		   for(int j=0;j<nr_points;j++) {
//			    this part is commented and will be copy - pasted after algorithm is finished
			   var res = intersectsBuildings(list_coordinates.get(i),list_coordinates.get(j),pols);
			   var list_point = new ArrayList<Point>();
			   var point1 =  Point.fromLngLat(list_coordinates.get(i).get(0), list_coordinates.get(i).get(1));
			   var point2 =  Point.fromLngLat(list_coordinates.get(j).get(0), list_coordinates.get(j).get(1));
			   list_point.add(point1);
			   list_point.add(point2);
			   var line =  LineString.fromLngLats(list_point);
			   var geo = (Geometry)line;
			   var feat = Feature.fromGeometry(geo);
			   feat.addNumberProperty("fill-opacity",0.75);
			   if(res) {
				   feat.addStringProperty("stroke", "#ff0000");
				   feat.addStringProperty("rgb-string", "#ff0000");
				   feat.addStringProperty("fill", "#ff0000");
			   }
			   else {
					feat.addStringProperty("rgb-string", "#00ff00");
					feat.addStringProperty("fill", "#00ff00");
					}
			   features.add(feat);
			   // use this dist variable later when finding a path out of no fly zones
			   var distance = euclid_dist(list_coordinates.get(i),list_coordinates.get(j));
			   dist[i][j] = distance;
		   }
	   }
	   return features;
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
		            for (int i=1;i<nr_points;i++) {
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
   
   
   
   
   private static boolean intersectsBuildings(ArrayList<Double>point1,ArrayList<Double>point2,ArrayList<Polygon> pols) {
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
		   double lng1 = line.getX1();
		   double lng2 = line.getX2();
		   double lat1 = line.getY1();
		   double lat2 = line.getY2();
		   var list1 = new ArrayList<Double>();
		   var list2 = new ArrayList<Double>();
		   list1.add(lng1);
		   list1.add(lat1);
		   list2.add(lng1);
		   list2.add(lat2);
		   // get all the lines of the polygon
		   for (int i=0;i<list_points.size();i++) {
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
		    var nr = new ArrayList<Integer>();
		    if(intersec.size()==0) {
		    	return false;
		    }
		    nr.add(counter1);
		    nr.add(counter2);
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
		  double right_hand = point1.get(0)*(point2.get(1) - point1.get(1))/(point2.get(0) - point1.get(0));
		  right_hand -= point3.get(0)*(point4.get(1) - point3.get(1))/(point4.get(0) - point3.get(0));
		  right_hand += point3.get(1) - point1.get(1);
		  double lng = right_hand / diff;
		  double lat = ((point4.get(1) - point3.get(1))/(point4.get(0) - point3.get(0))) * (lng - point3.get(0)) + point3.get(1);
		  double squared_dist = Math.pow((point3.get(0)-lng), 2) + Math.pow((point3.get(1)-lat), 2);
		  return Math.sqrt(squared_dist);
	   }
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
