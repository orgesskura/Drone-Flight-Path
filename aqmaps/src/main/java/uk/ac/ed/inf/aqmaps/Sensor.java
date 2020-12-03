package uk.ac.ed.inf.aqmaps;

public class Sensor {
	private String location;
	private String reading;
	private double battery;
	private boolean visited;
	private int id;
	public Sensor(String loc,String read ,double batteryLevel,int id) {
		this.location = loc;
		this.reading = read;
		this.battery = batteryLevel;
		this.visited = false;
		this.id = id;
	}
	protected String get_location() {
		return this.location;
	}
	protected String get_reading() {
		return this.reading;
	}
	protected double get_batteryLevel() {
		return this.battery;
	}
	protected boolean get_status() {
		return this.visited;
	}
	protected int get_id() {
		return this.id;
	}
	protected void set_status(boolean b) {
		this.visited = b;
	}
	
	

}
