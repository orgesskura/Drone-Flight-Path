package uk.ac.ed.inf.aqmaps;
import java.util.ArrayList;
import java.util.HashSet;
import com.mapbox.geojson.Polygon;
import java.lang.Math;
import java.awt.geom.Line2D;
import com.mapbox.geojson.Point;


public class PathPlanner {
   
   // have global dist matrix and permutation array  to show distance between points and order of visiting
   static final int number = App.number_sensors;
   private static double[][] dist = new double[number + 1][number + 1];
   private static int[] perm = new int[number + 1];
   
   
   //get permutation that works
   static int[] get_permutation() {
	   return perm;
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
		   for(int j=0;j<nr_points;j++) {
//			    this part is commented and will be copy - pasted after algorithm is finished
			   // use this dist variable later when finding a path out of no fly zones
			   var res = intersectsBuildings(list_coordinates.get(i),list_coordinates.get(j),pols);
			   var points = new ArrayList<Point>();
			   var start = Point.fromLngLat(list_coordinates.get(i).get(0), list_coordinates.get(i).get(1));
			   points.add(start);
			   var distance = euclid_dist(list_coordinates.get(i),list_coordinates.get(j));
           
			   if (res) {
				   distance = 1000;
			   }
			   dist[i][j] += distance;
		   }
	   }
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
    static void getPath(ArrayList<ArrayList<Double>> list_coordinates,int nr_points,ArrayList<Polygon> pols) {
	   initialize_perm(nr_points);
	   buildDistMatrix(list_coordinates,nr_points,pols);
	   swapHeuristic(nr_points);
	   TwoOptHeuristic(nr_points);
   }
   
   
   
   
    static Boolean intersectsBuildings(ArrayList<Double>point1,ArrayList<Double>point2,ArrayList<Polygon> pols) {
	   // create a new line
	   var line = new Line2D.Double(point1.get(0),point1.get(1),point2.get(0),point2.get(1));
	   //hashset that contains if line crosses with any of the lines of the polygons
	   var intersec = new HashSet<Integer>();
	   // just a counter to use to address polygon number
	   int counter = 1;
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
			   }
		   }
		  counter ++; 
	   }
		     // add polygon number and line number to an array and return it
	   
		if(intersec.size()==0) {
		    	return false;
		    }
		return true;
   }
   
   	
}
