package uk.ac.ed.inf.heatmap;
import com.mapbox.geojson.FeatureCollection;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class App 
{
    public static void main( String[] args )
    {   
    	// declare input where values of files will be stored
    	var input = new int[10][10];
    	int count=0;
    	// write with try and catch in case there are errors with trading file
    	try {
    		  // read file and save inputs to input array
    	      File myObj = new File(args[0]);
    	      Scanner myReader = new Scanner(myObj);
    	      while (myReader.hasNextLine()) {
    	        String data = myReader.nextLine();
    	        String[] parts = data.split(",");
    	        for(int i=0;i<10;i++) {
    	        	input[count][i] = Integer.parseInt(parts[i]);
    	        }
               count++;
    	      }
    	      myReader.close();
    	    }catch (FileNotFoundException e) {
    	      System.out.println("An error occurred.");
    	      e.printStackTrace();
    	    }
    	// declare the 4 confinement points
    	Point forestHill = Point.fromLngLat(-3.192473,55.946233);
    	Point kfc = Point.fromLngLat(-3.1843193,55.946233);
    	Point meadows = Point.fromLngLat(-3.192473,55.942617);
    	// calculate how much is the width and depth of the polygons we want
    	var stepH = (kfc.longitude() - forestHill.longitude())/10;
    	var stepV = (forestHill.latitude() - meadows.latitude())/10;
        var listFeat = new ArrayList<Feature>();
    	for(int i=0;i<10;i++) {
    		for(int j=0;j<10;j++) {
    			// Point a1 will be the leftmost,upper point. Then we go to a2,a3 ,a4 anti-clockwise 
    			Point a1 = Point.fromLngLat(forestHill.longitude()+j*stepH,forestHill.latitude() - i * stepV);
    			Point a2 = Point.fromLngLat(a1.longitude(), a1.latitude()-stepV);
    			Point a3 = Point.fromLngLat(a1.longitude()+stepH , a1.latitude() - stepV);
    			Point a4 = Point.fromLngLat(a1.longitude()+stepH, a1.latitude());
    			// declare list of list of points,add those points and create a polygon out of them
    			var pol1 = new ArrayList<List<Point>>();
    			var pol2 = new ArrayList<Point>();
    			pol2.add(a1);pol2.add(a2);pol2.add(a3);pol2.add(a4);pol2.add(a1);
    			pol1.add(pol2);
    			Polygon polygon3 = Polygon.fromLngLats(pol1);
    			// Create Geometry,then a Feature out of the polygon object
    			var geo = (Geometry)polygon3;
    			var feat = Feature.fromGeometry(geo);
    			// Add a feature property to the feature object
    			feat.addNumberProperty("fill-opacity",0.75);
    			// depending on the value of the input, we add the feature property rgb-string and fill
    			if (input[i][j] >=0 && input[i][j]< 32) {
    				feat.addStringProperty("rgb-string", "#00ff00");
    				feat.addStringProperty("fill", "#00ff00");
    			}
    			else if (input[i][j]<64) {
    				feat.addStringProperty("rgb-string", "#40ff00");
    				feat.addStringProperty("fill", "#40ff00");
    			}
    			else if(input[i][j]< 96) {
    				feat.addStringProperty("rgb-string", "#80ff00");
    				feat.addStringProperty("fill", "#80ff00");
    			}
    			else if  (input[i][j] < 128) {
    				feat.addStringProperty("rgb-string", "#c0ff00");
    				feat.addStringProperty("fill", "#c0ff00");
    			}
    			else if (input[i][j] < 160) {
    				feat.addStringProperty("rgb-string", "#ffc000");
    				feat.addStringProperty("fill", "#ffc000");
    			}
    			else if(input[i][j]<192) {
    				feat.addStringProperty("rgb-string", "#ff8000");
    				feat.addStringProperty("fill", "#ff8000");
    			}
    			else if(input[i][j]<224) {
    				feat.addStringProperty("rgb-string", "#ff4000");
    				feat.addStringProperty("fill", "#ff4000");
    			}
    			else {
    				feat.addStringProperty("rgb-string", "#ff0000");
    				feat.addStringProperty("fill", "#ff0000");
    			}
    			// add this feature to the list of features
    			listFeat.add(feat);
    			
    			}
    		}
    	// Create Feature Collection out of the list of features and then a JSON out of it
    	var featColl = FeatureCollection.fromFeatures(listFeat);
    	var Json = featColl.toJson();
    	// Write the JSON to the heatmap.geojson file and print stack trace if we catch an error
    	try (FileWriter file = new FileWriter("heatmap.geojson")) {
    		 
            file.write(Json);
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    	}
    }

