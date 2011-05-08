package hawox.uquest.questclasses;

import hawox.uquest.UQuest;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;


/*
 *    #:
 *       Name:
 *       Start Info:
 *       Finish Info:
 *       Rewards:
 *          money:
 *          item:
 *          item:
 *       Objectives:
 *          #:
 *             quest type:
 *             display name:
 *             item id:
 *             amount:
 *             amount to take away:
 *             location:
 *                point:
 *                   world:
 *                   x:
 *                   y:
 *                   z:
 *                give range:
 *                   world:
 *                   x:
 *                   y:
 *                   z:
 */

//TODO Locations Fix
@SuppressWarnings("unchecked")
public class QuestLoader {
	private final UQuest plugin;
	
	Yaml y;
	
	HashMap<Object,Object> allQuests = new HashMap<Object,Object>();
	//Object fromFile;
	
	public QuestLoader(UQuest plugin){
		this.plugin = plugin;
		Reader reader = null;
		try{
			reader = new FileReader("plugins/uQuest/Quests.yml");
		}catch(FileNotFoundException fnfe){ System.err.println("[Hawox's uQuest]:QuestLoader:reader:FileNotFoundException!!!"); }
		
		y = new org.yaml.snakeyaml.Yaml();
		//y.setBeanAccess(org.yaml.snakeyaml.introspector.BeanAccess.FIELD);

		allQuests = (HashMap<Object, Object>) y.load(reader);
	}
	
	public void test(){
		
//		System.out.println("Below me.\n");
//		System.out.println(y.dump(allQuests));
//		System.out.println("Above me.\n");
		
//		System.out.println("Below me.\n");
//		System.out.println(y.dump(allQuests.get("0")));
//		System.out.println("Above me.\n");
		
		//Map<String,Object> map = (LinkedHashMap<String,Object>) y.load(reader);
		//YamlQuest quest = this.getYamlQuestFromHashMap((HashMap<Object,Object>)y.load(reader));
//		System.out.println("Below me.\n");
//		HashMap<Object, Object> map = (HashMap<Object, Object>) allQuests.get(0);
//		System.out.println(y.dump(getYamlQuestFromHashMap(map)));
//		System.out.println("Above me.\n");
		
		
		/*
		YamlQuest quest = new YamlQuest();
		System.out.println("Below me.\n");
		System.out.println(y.dump(quest));
		System.out.println("Above me.\n");
		
		//allQuests.put("0", quest);
		
		//quest = (YamlQuest) y.load(reader);
		
		System.out.println("Below me.\n");
		System.out.println(y.dump(allQuests));
		System.out.println("Above me.\n");
		
		System.out.println("Below me.\n");
		System.out.println(y.dump(allQuests.get(0)));
		System.out.println("Above me.\n");
		
		quest = (YamlQuest) allQuests.get(0);
		
		System.out.println("Below me.\n");
		System.out.println(y.dump(quest));
		System.out.println("Above me.\n");
		*/
	}

	
	public ArrayList<LoadedQuest> loadAllQuests(){
		ArrayList<LoadedQuest> returnMe = new ArrayList<LoadedQuest>();
		HashMap<Object, Object> map;
		
		for(int i=0; i< allQuests.size(); i++){
			map = (HashMap<Object, Object>) allQuests.get(i);
			
			//For testing
			//System.out.println(y.dump(getYamlQuestFromHashMap(map)));
			
			returnMe.add(new LoadedQuest(getYamlQuestFromHashMap(i,map)));
		}
		
		return returnMe;
	}
	
	public class YamlQuest{	
		public String Name;
		public String Start_Info;
		public String Finish_Info;
		public HashSet<ymlReward> rewards;
		public HashSet<Objective> objectives = new HashSet<Objective>();
		public HashMap<String,Object> extras = new HashMap<String,Object>();
		
		YamlQuest(Object name, Object start, Object finish, HashSet<ymlReward> rewards, HashSet<Objective> objectives, HashMap<String,Object> extras){
			this.Name = (String) name;
			this.Start_Info = (String) start;
			this.Finish_Info = (String) finish;
			this.rewards = rewards;
			this.objectives = objectives;
			this.extras = extras;
		}
		
		//For testing
		YamlQuest(){
			Name = "The Name";
			Start_Info = "The Start Info";
			Finish_Info = "The Finish Info";
			//rewards.put("Money", "512");
			//ymlItem item1 = new ymlItem("17","10","0");
			//rewards.put("item", item1);
			//ymlObjective obj1 = new ymlObjective(); obj1.Type = "gather"; obj1.Display_Name = "Logs"; obj1.Amount_Needed = 5; obj1.Objective_ID = "17";
			//objectives.put("0", obj1);
		}

		public String getName() {
			return Name;
		}

		public void setName(String name) {
			Name = name;
		}

		public String getStart_Info() {
			return Start_Info;
		}

		public void setStart_Info(String start_Info) {
			Start_Info = start_Info;
		}

		public String getFinish_Info() {
			return Finish_Info;
		}

		public void setFinish_Info(String finish_Info) {
			Finish_Info = finish_Info;
		}

		public HashSet<ymlReward> getRewards() {
			return rewards;
		}

		public void setRewards(HashSet<ymlReward> rewards) {
			this.rewards = rewards;
		}

		public HashSet<Objective> getObjectives() {
			return objectives;
		}

		public void setObjectives(HashSet<Objective> objectives) {
			this.objectives = objectives;
		}

		public HashMap<String, Object> getExtras() {
			return extras;
		}

		public void setExtras(HashMap<String, Object> extras) {
			this.extras = extras;
		}
		

	}

	public class ymlReward{
		public String Type;
		public String Item_ID;
		public String Display_Name;
		public String Amount;
		public String Durability;
		
		public ymlReward( String Item_ID, String Display, String Amount, String Durability){
			this.Type = "item";
			this.Amount = Amount;
			this.Display_Name = Display;
			this.Durability = Durability;
			this.Item_ID = Item_ID;
		}
		
		public ymlReward(String Amount, String moneyName){
			this.Type = "money";
			this.Amount = Amount;
			this.Display_Name = moneyName;
		}
		
		public ItemStack toItem(){
			return (new ItemStack(Integer.parseInt(Item_ID),Integer.parseInt(Amount),Short.parseShort(Durability)));
		}
	}
	
	public class ymlLocation{
		//Location locationNeeded;	 //The point where the quest needs to be
		//Location locationGiveRange;  //How far from the point players can be in any direction.
		public HashMap<String,Object> Point;
		public HashMap<String,Object> Give_Range;
		
		ymlLocation(HashMap<String,Object> Point,HashMap<String,Object> Give_Range){
			this.Point = Point;
			this.Give_Range = Give_Range;
		}
		
		// world:x:y:z
		public String toStringFromPoint(){
			//String returnMe = "world:";
			//returnMe += Point.get("World");
			//returnMe += "~x:";
			//returnMe += Integer.toString((Integer)Point.get("X"));
			//returnMe += "~y:";
			//returnMe += Integer.toString((Integer)Point.get("Y"));
			//returnMe += "~z:";
			//returnMe += Integer.toString((Integer)Point.get("Z"));
			
			String returnMe = (String) Point.get("World");
			returnMe += ":";
			returnMe += Integer.toString((Integer)Point.get("X"));
			returnMe += ":";
			returnMe += Integer.toString((Integer)Point.get("Y"));
			returnMe += ":";
			returnMe += Integer.toString((Integer)Point.get("Z"));
			return returnMe;
		}
		
		// x:y:z
		public String toStringFromGive(){
			//String returnMe = "x:";
			//returnMe += Integer.toString((Integer)Give_Range.get("X"));
			//returnMe += "~y:";
			//returnMe += Integer.toString((Integer)Give_Range.get("Y"));
			//returnMe += "~z:";
			//returnMe += Integer.toString((Integer)Give_Range.get("Z"));
			String returnMe = Integer.toString((Integer)Give_Range.get("X"));
			returnMe += ":";
			returnMe += Integer.toString((Integer)Give_Range.get("Y"));
			returnMe += ":";
			returnMe += Integer.toString((Integer)Give_Range.get("Z"));
			return returnMe;
		}
	}
	
	/*
	 * A bunch of methods to convert the data gotten from a HashMap<Object,Object> to ymlClasses
	 */
	public YamlQuest getYamlQuestFromHashMap(int questNumber, HashMap<Object,Object> map){
			HashMap<String,Object> rewardsMap = (HashMap<String,Object>)map.get("Rewards");
			HashSet<ymlReward> ymlRewards = getymlRewardsFromHashMap(questNumber,rewardsMap);
			
			HashMap<Object,Object> objectivesMap = (HashMap<Object,Object>)map.get("Objectives");
			HashSet<Objective> objectives = getymlObjectivesFromHashMap(questNumber,objectivesMap);
			
			HashMap<String,Object> extrasMap = (HashMap<String,Object>)map.get("Extras");
			
		YamlQuest returnMe = new YamlQuest(map.get("Name"), map.get("Start_Info"), map.get("Finish_Info"), ymlRewards, objectives, extrasMap);
		return returnMe;
	}
	
	public HashSet<ymlReward> getymlRewardsFromHashMap(int questNumber, HashMap<String,Object> map){
		//Since there can be different types of the same reward as well as an undefined amount of them... iterate
		HashSet<ymlReward> returnMe = new HashSet<ymlReward>();
				
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			//Should be a string if their list is setup correctly
		    String key = (String) entry.getKey();
		    Object value = entry.getValue();
		    if(key.equalsIgnoreCase("Money")){
		    	//Money Type
		    	returnMe.add( new ymlReward( Integer.toString((Integer) value), plugin.getMoneyName() ) );
		    }else
		    if(key.equalsIgnoreCase("Item")){
		    	//Item Type
		    	//public ymlReward( String Item_ID, String Display, String Amount, String Durability){
		    	HashMap<String,Object> itemInfo = (HashMap<String, Object>) value;
		    	returnMe.add(getymlItemFromHashMap(itemInfo));
		    }else{
		    	questLoadError(questNumber, "Unknown reward format:\n Key:" + key + " | Value:" + value);
		    }
		}
		return returnMe;
	}
	
	public HashSet<Objective> getymlObjectivesFromHashMap(int questNumber, HashMap<Object,Object> map){
		HashSet<Objective> returnMe = new HashSet<Objective>();
		for (Entry<Object, Object> entry : map.entrySet()) {
			//Should be a string if their list is setup correctly
		    Object key = entry.getKey(); // this is just the objective number. Not sure if I need it here anymore.
		    Object value = entry.getValue();
		    
		    //All of the maps and values that may be used when setting up a quest. It's fine if they are null. They are just here so we can get all the situations needed.
			HashMap<String,Object> moreInfo = (HashMap<String,Object>) value;
		    HashMap<String,Object> itemInfo;
		    HashMap<String,Object> locationInfo;
		    ymlLocation theLocation;
		    String type;
		    //Fail safe incase type is not vaild. This way nothing is added when the objective is not formated right.
		    	//Did not use a try here since there are trys all over the place to catch the nulls of locations
		    do{
		    	type = (String) moreInfo.get("Type");
		    	if(type == null){ break; }
		    	itemInfo = (HashMap<String, Object>) moreInfo.get("Item");
		    	locationInfo = (HashMap<String, Object>) moreInfo.get("Location");
		    	theLocation = getymlLocationFromHashMap(locationInfo);
		    
		    
		    	//Different types of set-ups for each objective type
		    	//	public Objective(String type, String displayName, String objectiveName, int amountNeeded, String point, String give){
		    	if(type.equalsIgnoreCase("Gather")){
	    			ymlReward item = getymlItemFromHashMap(itemInfo);
		    		try{
		    			returnMe.add(new Objective("gather", item.Display_Name, item.toItem(), theLocation.toStringFromPoint(), theLocation.toStringFromGive()));
		    		}catch(NullPointerException npe){ 
		    			try{
		    				returnMe.add(new Objective("gather", item.Display_Name, item.toItem(), null, null));
		    			}catch(NullPointerException npe2){
		    				//player did not format their quest correctly at this point.
		    				questLoadError(questNumber, "Objective format error:\n Key:" + key + "\nType:" + type + " \n Value:" + value);
		    			}
		    		}

		    	}else
		    	
		    	if(type.equalsIgnoreCase("Block_Destroy")){
		    		try{
		    			returnMe.add(new Objective("blockdestroy", (String)moreInfo.get("Display_Name"), Integer.toString((Integer)moreInfo.get("Objective_ID")), (Integer)moreInfo.get("Amount"), theLocation.toStringFromPoint(), theLocation.toStringFromGive()));
		    		}catch(NullPointerException npe){
		    			try{
		    				returnMe.add(new Objective("blockdestroy", (String)moreInfo.get("Display_Name"), Integer.toString((Integer)moreInfo.get("Objective_ID")), (Integer)moreInfo.get("Amount"), null, null));
		    			}catch(NullPointerException npe2){
		    				//player did not format their quest correctly at this point.
		    				questLoadError(questNumber, "Objective format error:\n Key:" + key + "\nType:" + type + " \n Value:" + value);
		    			}
		    		}
		    	}else
		    	
		    	if(type.equalsIgnoreCase("Block_Damage")){
			  		try{
			  			returnMe.add(new Objective("blockdamage", (String)moreInfo.get("Display_Name"), Integer.toString((Integer)moreInfo.get("Objective_ID")), (Integer)moreInfo.get("Amount"), theLocation.toStringFromPoint(), theLocation.toStringFromGive()));
			  		}catch(NullPointerException npe){
			  			try{
			  				returnMe.add(new Objective("blockdamage", (String)moreInfo.get("Display_Name"), Integer.toString((Integer)moreInfo.get("Objective_ID")), (Integer)moreInfo.get("Amount"), null, null));
			  			}catch(NullPointerException npe2){
		    				//player did not format their quest correctly at this point.
		    				questLoadError(questNumber, "Objective format error:\n Key:" + key + "\nType:" + type + " \n Value:" + value);
		    			}
			  		}
				}else
				
				if(type.equalsIgnoreCase("Block_Place")){
			  		try{
			  			returnMe.add(new Objective("blockplace", (String)moreInfo.get("Display_Name"), Integer.toString((Integer)moreInfo.get("Objective_ID")), (Integer)moreInfo.get("Amount"), theLocation.toStringFromPoint(), theLocation.toStringFromGive()));
			  		}catch(NullPointerException npe){
			  			try{
			  				returnMe.add(new Objective("blockplace", (String)moreInfo.get("Display_Name"), Integer.toString((Integer)moreInfo.get("Objective_ID")), (Integer)moreInfo.get("Amount"), null, null));
			  			}catch(NullPointerException npe2){
		    				//player did not format their quest correctly at this point.
		    				questLoadError(questNumber, "Objective format error:\n Key:" + key + "\nType:" + type + " \n Value:" + value);
		    			}
			  		}

				}else
				
				if(type.equalsIgnoreCase("Kill")){
			  		try{
			  			returnMe.add(new Objective("kill", (String)moreInfo.get("Display_Name"), (String)moreInfo.get("Objective_ID"), (Integer)moreInfo.get("Amount"), theLocation.toStringFromPoint(), theLocation.toStringFromGive()));
			  		}catch(NullPointerException npe){
			  			try{
			  				returnMe.add(new Objective("kill", (String)moreInfo.get("Display_Name"), (String)moreInfo.get("Objective_ID"), (Integer)moreInfo.get("Amount"), null, null));
			  				}catch(NullPointerException npe2){
			    				//player did not format their quest correctly at this point.
			    				questLoadError(questNumber, "Objective format error:\n Key:" + key + "\nType:" + type + " \n Value:" + value);
			    			}
			  		}

				}else
					
					if(type.equalsIgnoreCase("Move")){
				  		try{
				  			returnMe.add(new Objective("move", (String)moreInfo.get("Display_Name"), (String)moreInfo.get("Objective_ID"), (Integer)moreInfo.get("Amount"), theLocation.toStringFromPoint(), theLocation.toStringFromGive()));
				  		}catch(NullPointerException npe){
				  			try{
				  				returnMe.add(new Objective("move", (String)moreInfo.get("Display_Name"), (String)moreInfo.get("Objective_ID"), (Integer)moreInfo.get("Amount"), null, null));
				  			}catch(NullPointerException npe2){
			    				//player did not format their quest correctly at this point.
			    				questLoadError(questNumber, "Objective format error:\n Key:" + key + "\nType:" + type + " \n Value:" + value);
			    			}
				  		}
				  		
				}else{
					questLoadError(questNumber, "Unknown Objective:\n Type:" + type + " \n Value:" + value);
		    	}
		    }while(false);
		    if(type == null){ questLoadError(questNumber, "Got a null quest type. Huge formating error!!! >:C"); }
		}
		    
		return returnMe;
	}
	
	public ymlReward getymlItemFromHashMap(HashMap<String,Object> itemInfo){
		//HashMap<String,Object> itemInfo = (HashMap<String, Object>) value;
		try{
			return(new ymlReward( Integer.toString((Integer) itemInfo.get("Item_ID")), (String)itemInfo.get("Display_Name"), Integer.toString((Integer) itemInfo.get("Amount")), Integer.toString((Integer) itemInfo.get("Durability")) ) );
		}catch(NullPointerException npe){
			questLoadError("Error loading item reward!!! Did you include all fields and capitalize everything?");
			return null;
		}
	}
	
	public ymlLocation getymlLocationFromHashMap(HashMap<String,Object> locationInfo){
		//HashMap<String,Object> itemInfo = (HashMap<String, Object>) value;
		try{
			return(new ymlLocation( (HashMap<String,Object>)locationInfo.get("Point"),(HashMap<String,Object>)locationInfo.get("Give_Range") ) );
		}catch(NullPointerException npe){ return null; }
}
	
	public void questLoadError(String error){
		String theText = "\n\n";
		theText += "-----------------------\n";
		theText += "[uQuest]\n";
		theText += "Error loading quest number: unknown \n";
		theText += "Problem:\n";
		theText += error + "\n";
		theText += "-----------------------\n\n";
		System.err.println(theText);
	}
	
	public void questLoadError(int number, String error){
		String theText = "\n\n";
		theText += "-----------------------\n";
		theText += "[uQuest]\n";
		theText += "Error loading quest number: " + Integer.toString(number) + "\n";
		theText += "Problem:\n";
		theText += error + "\n";
		theText += "-----------------------\n\n";
		System.err.println(theText);
	}
	
}