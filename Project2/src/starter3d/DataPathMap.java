package starter3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataPathMap {
	
	private static Map<String, String> DataPathMap;
	private static List<String> OBJs;

    public static void setOBJs() {
    	OBJs = new ArrayList<>();
        OBJs.add("armadillo");
        OBJs.add("bunny");
        OBJs.add("doraemon");
        OBJs.add("fertility");
        OBJs.add("gargoyle");
        OBJs.add("Lion_vase");
        OBJs.add("lion");
        OBJs.add("Lucy");
        OBJs.add("turtle");
    }
    
    public static void setMap() {
    	DataPathMap = new HashMap<>();
    	for (String obj: OBJs) {
    		DataPathMap.put(obj, "./test_data/" + obj + ".obj");
		}
    }

	public static Map<String, String> getDataPathMap() {
		setMap();
		return DataPathMap;
	}

	public static List<String> getOBJs() {
		setOBJs();
		return OBJs;
	}

}
