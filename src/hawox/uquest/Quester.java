package hawox.uquest;

import hawox.uquest.interfaceevents.QuestGetEvent;
import hawox.uquest.interfaceevents.TrackerAddEvent;
import hawox.uquest.questclasses.LoadedQuest;
import hawox.uquest.questclasses.Objective;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

/**
 * //stored player info will look like: PlayerName = QuestId:QuestsCompleted:MoneyEarnedFromQuests:questtracker
	//		(-1 quest id means no active quest!)
	private String questDefaultPlayer = "-1:0:0:0";
 * @author Hawox
 *
 */

public class Quester implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;

	//UQuest plugin;
	
	String theQuestersName;
	
	int questID;
	int questsCompleted;
	int moneyEarnedFromQuests;
	//With more objectives this can't be a simple int. It will look like "wood.5~pig.2" ""
	HashMap<String,Integer> questTracker = new HashMap<String,Integer>();
	int questLevel;
	
	//int questsFailed; TODO for a later time.
	
	public Quester(String[] questerInfoInString, Player player) {
		//this.plugin = plugin;
		
		this.theQuestersName = player.getName();
		this.questID = Integer.parseInt(questerInfoInString[0]);
		this.questsCompleted = Integer.parseInt(questerInfoInString[1]);
		this.moneyEarnedFromQuests = Integer.parseInt(questerInfoInString[2]);
		//this.questsFailed 
		
		HashMap<String,Integer> tracker = new HashMap<String,Integer>();
		try{
			for(String info : questerInfoInString[3].split("~")){

				//system.err.println("Info: " + info);
				String[] moreInfo = info.split(",");
				tracker.put(moreInfo[0], Integer.parseInt(moreInfo[1]));
			}
		}catch(ArrayIndexOutOfBoundsException aiobe){  }
		this.questTracker = tracker;
		//defineQuestLevel();
	}
	
	public boolean giveQuest(UQuest plugin, int questID, LoadedQuest quest){
		this.setQuestID(questID);
		
		HashMap<String,Integer> tracker = new HashMap<String,Integer>();
		for(Objective objective : quest.getObjectives()){
			tracker.put(objective.getObjectiveName(), 0);
		}
		
		this.setQuestTracker(tracker);
		
		//call event
		plugin.getServer().getPluginManager().callEvent(new QuestGetEvent(plugin.getServer().getPlayer(this.theQuestersName), this, questID));
		return true;
	}
	
	public String toString(){
		String stringToReturn;
		//try{
			stringToReturn = Integer.toString(this.questID) + ":" + Integer.toString(this.questsCompleted) + ":" + Integer.toString(this.moneyEarnedFromQuests) + ":" + toStringFromTracker();
		//}catch(NullPointerException npe){//If their tracker is empty don't print it
			//stringToReturn = Integer.toString(this.questID) + ":" + Integer.toString(this.questsCompleted) + ":" + Integer.toString(this.moneyEarnedFromQuests) + ":";
		//}
			return stringToReturn;
	}
	
	public String toStringFromTracker(){
		String returnMe = "";
		try{
		for (Entry<String, Integer> entry : this.questTracker.entrySet()) {
			//Should be a string if their list is setup correctly
		    String key = entry.getKey(); // Objective name
		    int value = entry.getValue(); // actual tracker
		    returnMe += key + "," + Integer.toString(value);
		    returnMe += "~";
		}
		}catch(NullPointerException npe){ }
		//kill the trailing ~
		if(returnMe.length() > 0)
			returnMe = returnMe.substring(0, returnMe.length() - 1);
		return returnMe;
	}
	
	public void clearTracker(){
		this.setQuestTracker(null);
	}
	
	public boolean setTracker(String which, int toWhat){
		
		this.questTracker.remove(which);
		this.questTracker.put(which, toWhat);
		
		return true;
	}
	
	public int getTracker(UQuest plugin, String which){
		try{
			return this.questTracker.get(which);
		}catch(NullPointerException npe){
			System.err.println(UQuest.pluginNameBracket() + " Quester:getTracker: Players tracker does not match their quest!");
			System.err.println(UQuest.pluginNameBracket() + " This is most likely due to editing a quest a players has.");
			System.err.println(UQuest.pluginNameBracket() + " Dropping their quest and giving it back to them to fix it.");
			this.giveQuest(plugin,this.questID, plugin.getQuestersQuest(this));
			return getTracker(plugin, which); //try again.
		}
	}
	
	public boolean addToTracker(UQuest plugin, String which, int addWhat){
		this.setTracker(which, addWhat + this.getTracker(plugin, which));
		//call event
		plugin.getServer().getPluginManager().callEvent(new TrackerAddEvent(plugin.getServer().getPlayer(this.theQuestersName), which, addWhat));
		return true;
	}
	
	public String arrayToString(String[] stringToChange, String withWhat) {
		// take each part of the array and separate it with ':'s
		String stringToReturn = stringToChange[0];
		try {
			// start with the first index on the array
			for (int i = 1; i < stringToChange.length; i++) {
				stringToReturn = stringToReturn.concat(withWhat);
				stringToReturn = stringToReturn.concat(stringToChange[i]);
			}
			return stringToReturn;
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			System.err.println("There was a problem converting an array to a string.");
			return null;
		}
	}
	
	public void setQuestLevel(int questLevel) {
		this.questLevel = questLevel;
	}

	public int getQuestID() {
		return questID;
	}

	public void setQuestID(int questID) {
		this.questID = questID;
	}

	public int getQuestsCompleted() {
		return questsCompleted;
	}

	public void setQuestsCompleted(int questsCompleted) {
		this.questsCompleted = questsCompleted;
	}

	public int getMoneyEarnedFromQuests() {
		return moneyEarnedFromQuests;
	}

	public void setMoneyEarnedFromQuests(int moneyEarnedFromQuests) {
		this.moneyEarnedFromQuests = moneyEarnedFromQuests;
	}

	public String getTheQuestersName() {
		return theQuestersName;
	}

	public void setTheQuestersName(String theQuestersName) {
		this.theQuestersName = theQuestersName;
	}
	
	public int getQuestLevel() {
		return questLevel;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public void setQuestTracker(HashMap<String,Integer> questTracker){
		this.questTracker = questTracker;
	}
}
