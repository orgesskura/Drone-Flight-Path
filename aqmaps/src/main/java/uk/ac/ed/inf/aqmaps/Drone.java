package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Point;

public class Drone {
	  // declare fields of Drone
      private  int moves_left;
      private  Point location;
      //declare constructor for Drone
      public Drone(int moves , Point p) {
    	 this.moves_left = moves;
    	 this.location = p;
      }
      // check if drone has any moves left
      boolean has_moves_left() {
    	  return this.moves_left>0;
      }
      // getter of location field
      Point get_location() {
    	  return this.location;
      }
      // decrease number of moves
      void decrease_moves() {
    	  this.moves_left = this.moves_left - 1;
      }
      //move drone to a location 2
      void move(Point location2) {
    	  this.location = location2;
      }
      // check if drone is in range of a sensor
      boolean in_range(ArrayList<Double>dest) {
    	  var list = new ArrayList<Double>();
    	  list.add(this.location.longitude());
    	  list.add(this.location.latitude());
    	  if(PathPlanner.euclid_dist(list, dest) < App.range_read) {
    		  return true;
    	  }
    	  return false;
      }
}
