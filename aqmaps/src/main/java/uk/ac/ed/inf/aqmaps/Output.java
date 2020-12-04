package uk.ac.ed.inf.aqmaps;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Output {
	  // generate a JSON file with sensors and flight path
      static void generateJson(ArrayList<Sensor> list_sensors,ArrayList<ArrayList<Double>> coordinates,ArrayList<Point> list_point,ArrayList<Polygon> buildings,String day,String month, String year,int[] perm){
    	  //declare an array-list of features
    	   var features = new ArrayList<Feature>();
    	   // get a line string out of list_point array-list of point and then turn it into a feature
    	   var line =  LineString.fromLngLats(list_point);
    	   var geo = (Geometry)line;
    	   var feat = Feature.fromGeometry(geo);
    	   features.add(feat);
    	   // add the markers for sensors gotten through mark_sensors method
     	   features.addAll(mark_sensors(list_sensors,coordinates,perm));
     	   // get a feature collection out of it
     	   var fc = FeatureCollection.fromFeatures(features);
    	   var str = fc.toJson();
    	   // declare the name of the file
    	   String s = "readings-" + day +"-" + month + "-" + year+".geojson";
    	   // try to write the file
    	   try (FileWriter file = new FileWriter(s)) {
   		 
              file.write(str);
              file.flush();
           
          }// catch the error and print stack trace
    	   catch (IOException e) {
              e.printStackTrace();
          }
       }
      
      // generate flight path text file
      static void generateText(ArrayList<String>list_sensors_visited,ArrayList<Integer> list_angles,ArrayList<Point> list_point,String day,String month, String year) {
    	  // declare name of file as in instructions
    	  String file_name = "flightpath-" + day+ "-" + month + "-" + year + ".txt";
    	  String str = "";
    	  int n = list_sensors_visited.size();
    	  // iterate and put all of the required values as instructed
    	  for(int i=0;i<n;i++) {
    		 var Point1 = list_point.get(i);
    		 var Point2 = list_point.get(i+1);
    		 str += String.valueOf(i+1) +","+ String.valueOf(Point1.longitude()) + "," + String.valueOf(Point1.latitude()) + "," + String.valueOf(list_angles.get(i)) + ",";
    		 str += String.valueOf(Point2.longitude()) + "," + String.valueOf(Point2.latitude()) + "," + list_sensors_visited.get(i);
    		 str +="\n";
    		 
    	  }
          // try writing to a file
    	  try {
              FileWriter writer = new FileWriter(file_name, true);
              writer.write(str);
              writer.close();
          }// print the error in case of an exception 
    	  catch (IOException e) {
              e.printStackTrace();
          }
    	  
      }
       
       // create a list of features of markers out of sensors
       private static ArrayList<Feature> mark_sensors(ArrayList<Sensor> sensors,ArrayList<ArrayList<Double>> coordinates,int[] perm){
       	var fl = new ArrayList<Feature>();
       	for(int i=1;i<coordinates.size();i++) {
       		// go through each sensor and get its attributes
       		var sensor = sensors.get(perm[i]-1);
       		var loc = coordinates.get(perm[i]);
       		// create a feature out of a point
       		var point = Point.fromLngLat(loc.get(0), loc.get(1));
       		var geo = (Geometry) point;
       		var feat = Feature.fromGeometry(geo);
       		var reading = sensor.get_reading();
       		var location = sensor.get_location();
       		var battery = sensor.get_batteryLevel();
       		var visited = sensor.get_status();
       		boolean hasBattery = false;
       		//if battery is less than 10 set hasBattery to true
       		if(battery >=10) {
       			hasBattery = true;
       		}
       		var list_string = new ArrayList<String>();
       		var input = 0.0;
       		// if reading is not null or Nan convert string reading to a double
       		if(!reading.equals("null") && !reading.equals("Nan")) {
       			input = Double.valueOf(reading);
       		}
       		else {
       			input = -1;
       		}
       		// add all of the properties of the markers that have been taken through mapValue function
       		list_string.addAll(map_value(visited,hasBattery,input));
       		// list_string has size 2 if it is not visited
       		if(list_string.size()==2) {
       			feat.addStringProperty("location", location);
       			feat.addStringProperty("rgb-string", list_string.get(0));
       			feat.addStringProperty("marker-color", list_string.get(1));
       		}
       		else if (list_string.size()==3) {
       			// if sensor has no battery notify the user that the sensor needs replacement
       			if (!hasBattery) {
       				System.out.println("Sensor at location " + location+ " needs battery replacement!!!!");
       			}
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
       private static ArrayList<String> map_value(boolean visited, boolean hasBattery,double input) {
       	var list = new ArrayList<String>();
        // if sensor is visited
       	if(visited) {
       		// if sensor has battery
       		if(hasBattery) {
       			    // put the properties according to the assignment specification
   			    	if (input>=0 && input< 32) {
   			    		Colours green = Colours.Green;
   			    		list.add(green.get_color_code());
   			    		list.add(green.get_color_code());
   						list.add("lighthouse");
   					}
   					else if (input <64) {
   						Colours medium_green = Colours.Medium_Green;
   						list.add(medium_green.get_color_code());
   						list.add(medium_green.get_color_code());
   			            list.add("lighthouse");
   					}
   					else if(input< 96) {
   						Colours light_green = Colours.Light_Green;
   						list.add(light_green.get_color_code());
   						list.add(light_green.get_color_code());
   						list.add("lighthouse");
   					}
   					else if  (input < 128) {
   						Colours lime_green = Colours.Lime_Green;
   						list.add(lime_green.get_color_code());
   						list.add(lime_green.get_color_code());
   						list.add("lighthouse");
   					}
   					else if (input< 160) {
   						Colours gold = Colours.Gold;
   						list.add(gold.get_color_code());
   						list.add(gold.get_color_code());
   						list.add("danger");
   					}
   					else if(input<192) {
   						Colours orange = Colours.Orange;
   						list.add(orange.get_color_code());
   						list.add(orange.get_color_code());
   						list.add("danger");
   					}
   					else if(input<224) {
   						Colours red_orange = Colours.Red_Orange;
   						list.add(red_orange.get_color_code());
   						list.add(red_orange.get_color_code());
   						list.add("danger");
   					}
   					else {
   						Colours red = Colours.Red;
   						list.add(red.get_color_code());
   						list.add(red.get_color_code());
   						list.add("danger");
   					}
       		}
       		// sensor has no battery
       		else {
       			  Colours black = Colours.Black;
       			  list.add(black.get_color_code());
       			  list.add(black.get_color_code());
       			  list.add("cross");
       		}
       }
       	// sensor is not visited
       	else {
       		Colours gray = Colours.Gray;
       		list.add(gray.get_color_code());
       		list.add(gray.get_color_code());
       	}
       	// return list
       	return list;
       
       }
}
