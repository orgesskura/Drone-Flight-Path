package uk.ac.ed.inf.aqmaps;

public enum Colours {
	// purpose of this is to make my algorithm more user-friendly
	// declare color and what is its associated colour code 
	    Green  ("#00ff00"),  
	    Medium_Green("#40ff00"),  
	    Light_Green   ("#80ff00"),   
	    Lime_Green ("#c0ff00"),
	    Gold ("#ffc000"),
	    Orange("#ff8000"),
	    Red_Orange("#ff4000"),
	    Red("#ff0000"),
	    Black("#000000"),
	    Gray("#aaaaaa");
	    
	    // final field of enum
	    private final String color_code;
        //constructor
	    private Colours(String color_code) {
	        this.color_code = color_code;
	    }
	    // getter to get colour code
	    public String get_color_code() {
	    	return this.color_code;
	    }
}
