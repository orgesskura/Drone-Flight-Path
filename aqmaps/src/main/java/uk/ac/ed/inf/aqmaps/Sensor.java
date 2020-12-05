package uk.ac.ed.inf.aqmaps;

public class Sensor {
	// declare field of the sensor
	private String location;
	private String reading;
	private double battery;
	private boolean visited;
	// declare constructor for Sensor
	public Sensor(String loc,String read ,double batteryLevel) {
		this.location = loc;
		this.reading = read;
		this.battery = batteryLevel;
		this.visited = false;
	}
	//getter for location
	protected String get_location() {
		return this.location;
	}
	// getter for reading 
	protected String get_reading() {
		return this.reading;
	}
	// getter for battery level
	protected double get_batteryLevel() {
		return this.battery;
	}
	//getter for seeing if sensor is visited
	protected boolean get_status() {
		return this.visited;
	}
	// read sensor and set its status to read
	protected void read() {
		this.visited = true;
	}
	
	

}
