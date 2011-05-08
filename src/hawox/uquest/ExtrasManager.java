package hawox.uquest;

import java.util.HashMap;

import hawox.uquest.questclasses.LoadedQuest;

public class ExtrasManager {
	
	//This is getting run with near every method in this class. Adding it here so it exists in only one place!
	//IE: This only exists to make the rest of the code simpler and smaller
	
	@SuppressWarnings("unchecked")
	private static HashMap<String,Object> theBaseLoop(LoadedQuest q, String what){
		//Set this up so we can check for the cool uQuest.levelplugin.exp.class.amount etc etc.
		//You can't split at a "." for a reason I don't know, so we convert it first! This way we can keep to bukkit's '.' standard!
		what = what.replace(".", "~");
		String[] theTree = what.split("~");
		HashMap<String,Object> theMap = q.getExtras();
		for(int i=0; i < theTree.length; i++){
			if(i == (theTree.length - 1) ){ //- to account for length starting at 1
				//end of string tree. If this runs without an error than it exists!
				if(theMap.get(theTree[i]) != null){
					return theMap;
				}else{
					return null;
				}
			}else{
				//not at the end so see if we can update the map!
				Object obj = theMap.get(theTree[i]);
				if(obj == null)
					return null;
				if(obj instanceof HashMap<?,?>){
					theMap = (HashMap<String, Object>) obj;
					continue; //jump to the next stage in the for loop
				}
			}
		}
		System.err.println(UQuest.pluginNameBracket() + " ExtrasManager:TheBaseLoop: Somehow got threw the loop with no errors yet it returned null... REPORT THIS!!!");
		return null; //don't know how it got here, but false.
	}
	
	/*
	 * We'll continue threw the loop of value.value.etc they gave us checking for errors.
	 * If we encounter an error then the value does not exist in the tree.
	 */
	public static boolean questHas(LoadedQuest q, String what){
		if(theBaseLoop(q,what) != null){
			return true;
		}else{
			return false;
		}
	}

	//Override to use has with loadedquest number... But you need to send the plugin and quester over to get it
	public static boolean questHas(UQuest plugin, Quester q, String what){
		return questHas(plugin.getQuestersQuest(q),what);
	}
	
	
	/*
	 * The same as has except it returns the object at the string location instead of a boolean
	 */
	public static Object questGetObject(LoadedQuest q, String what){
		what = what.replace(".", "~");
		String[] theTree = what.split("~");
		String last = theTree[theTree.length - 1]; //last string in the array
		try{
			return theBaseLoop(q,what).get(last);
		}catch(NullPointerException npe){
			return null;
		}
	}
	
	//same overloaded method deal
	public static Object questGetObject(UQuest plugin, Quester q, String what){
		return questGetObject(plugin.getQuestersQuest(q),what);
	}
	
	
	/**
	 * These are sort of redundant with the existance of getObject, however it follows the standard of the rest of
	 * BukkitLand with their getString, getInt, etc
	 *  |> boolean, int, double, string <|
	 */
	/*
	 * boolean
	 */
	public static boolean questGetBoolean(LoadedQuest q, String what, boolean defaultValue){
		Object object = questGetObject(q,what);
		if(object instanceof Boolean){
			return (Boolean)object;
		}else{
			//what to return if we can't load an int
			return defaultValue;
		}
	}
	
	public static boolean questGetBoolean(UQuest plugin, Quester q, String what, boolean defaultValue){
		return questGetBoolean(plugin.getQuestersQuest(q),what,defaultValue);
	}
	
	/*
	 * int
	 */
	public static int questGetInt(LoadedQuest q, String what, int defaultValue){
		Object object = questGetObject(q,what);
		if(object instanceof Integer){
			return (Integer)object;
		}else{
			//what to return if we can't load an int
			return defaultValue;
		}
	}
	
	public static int questGetInt(UQuest plugin, Quester q, String what, int defaultValue){
		return questGetInt(plugin.getQuestersQuest(q),what,defaultValue);
	}
	
	/*
	 * double
	 */
	public static double questGetDouble(LoadedQuest q, String what, double defaultValue){
		Object object = questGetObject(q,what);
		if(object instanceof Double){
			return (Double)object;
		}else{
			//what to return if we can't load an int
			return defaultValue;
		}
	}
	
	public static double questGetDouble(UQuest plugin, Quester q, String what, double defaultValue){
		return questGetDouble(plugin.getQuestersQuest(q),what,defaultValue);
	}
	
	/*
	 * string
	 */
	public static String questGetString(LoadedQuest q, String what, String defaultValue){
		Object object = questGetObject(q,what);
		if(object instanceof String){
			return (String)object;
		}else{
			//what to return if we can't load an int
			return defaultValue;
		}
	}
	
	public static String questGetString(UQuest plugin, Quester q, String what, String defaultValue){
		return questGetString(plugin.getQuestersQuest(q),what,defaultValue);
	}
	
}


/* Before I condensed it. Just storage because I KNOW this works if I break the rest
@SuppressWarnings("unchecked")
public static boolean has(LoadedQuest q, String what){
	//Set this up so we can check for the cool uQuest.levelplugin.exp.class.amount etc etc.
	//You can't split at a "." for a reason I don't know, so we convert it first! This way we can keep to bukkit's '.' standard!
	what = what.replace(".", "~");
	String[] theTree = what.split("~");
	HashMap<String,Object> theMap = q.getExtras();
	for(int i=0; i < theTree.length; i++){
		if(i == (theTree.length - 1) ){ //- to account for length starting at 1
			//end of string tree. If this runs without an error than it exists!
			if(theMap.get(theTree[i]) != null)
				return true;
		}else{
			//not at the end so see if we can update the map!
			Object obj = theMap.get(theTree[i]);
			if(obj == null)
				return false;
			if(obj instanceof HashMap<?,?>){
				theMap = (HashMap<String, Object>) obj;
				continue; //jump to the next stage in the for loop
			}
		}
	}
	System.err.println(UQuest.pluginNameBracket() + " ExtrasManager:has:Somehow got threw the loop with no errors yet it returned false... REPORT THIS!!!");
	return false; //don't know how it got here, but false.
}*/
