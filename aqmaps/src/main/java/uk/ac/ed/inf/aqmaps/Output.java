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
	  // generate a Json file with sensors and flightpath
      static void generateJson(ArrayList<Sensor> list_sensors,ArrayList<ArrayList<Double>> coordinates,ArrayList<Point> list_point,ArrayList<Polygon> buildings,String day,String month, String year){
    	   var fl = new ArrayList<Feature>();
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
    	   String s = "readings-" + day +"-" + month + "-" + year+".geojson";
    	   try (FileWriter file = new FileWriter(s)) {
   		 
              file.write(str);
              file.flush();

          } catch (IOException e) {
              e.printStackTrace();
          }
       }
      // generate flightpath textfile
      static void generateText(ArrayList<String>list_sensors_visited,ArrayList<Integer> list_angles,ArrayList<Point> list_point,String day,String month, String year) {
    	  String file_name = "flightpath-" + day+ "-" + month + "-" + year + ".txt";
    	  String str = "";
    	  int n = list_sensors_visited.size();
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
       		if(!reading.equals("null") && !reading.equals("Nan")) {
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
}
