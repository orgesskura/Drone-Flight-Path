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
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {   
    	var input = new int[10][10];
    	int count=0;
    	try {
    	      File myObj = new File("predictions.txt");
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
    	    } catch (FileNotFoundException e) {
    	      System.out.println("An error occurred.");
    	      e.printStackTrace();
    	    }
    	Point forestHill = Point.fromLngLat(55.946233,-3.192473);
    	Point kfc = Point.fromLngLat(55.946233,-3.1843193);
    	Point meadows = Point.fromLngLat(55.942617,-3.192473);
    	Point buccleuch = Point.fromLngLat(55.942617,-3.184319);
    	var stepH = (kfc.latitude() - forestHill.latitude())/10;
    	var stepV = (forestHill.longitude() - meadows.longitude())/10;
        var listFeat = new ArrayList<Feature>();
    	
    	for(int i=0;i<10;i++) {
    		for(int j=0;j<10;j++) {
    			Point a1 = Point.fromLngLat(forestHill.longitude()+i*stepV,forestHill.latitude() + j * stepH);
    			Point a2 = Point.fromLngLat(a1.longitude()+stepV, a1.latitude());
    			Point a3 = Point.fromLngLat(a1.longitude() + stepV, a1.latitude() + stepH);
    			Point a4 = Point.fromLngLat(a1.longitude(), a1.latitude()+stepH);
    			var pol1 = new ArrayList<List<Point>>();
    			var pol2 = new ArrayList<Point>();
    			pol2.add(a1);pol2.add(a2);pol2.add(a3);pol2.add(a4);
    			pol1.add(pol2);
    			Polygon polygon3 = Polygon.fromLngLats(pol1);
    			var geo = (Geometry)polygon3;
    			var feat = Feature.fromGeometry(geo);
    			feat.addNumberProperty("fill-opacity",0.75);
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
    			listFeat.add(feat);
    			
    			}
    		}
    	var featColl = FeatureCollection.fromFeatures(listFeat);
    	var Json = featColl.toJson();
    	try (FileWriter file = new FileWriter("heatmap.geojson")) {
    		 
            file.write(Json);
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    	}
    }

