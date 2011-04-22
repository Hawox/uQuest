package hawox.uquest.questclasses;

import hawox.uquest.Quester;
import hawox.uquest.UQuest;
import hawox.uquest.interfaceevents.QuestFinishEvent;
import hawox.uquest.questclasses.QuestLoader.YamlQuest;
import hawox.uquest.questclasses.QuestLoader.ymlReward;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LoadedQuest {
	//protected final UQuest plugin;

	String name;
	String startInfo;
	String finishInfo;
	HashSet<Reward> rewards = new HashSet<Reward>();
	HashSet<Objective> objectives = new HashSet<Objective>();
	//Again... I forgot this would create multiple keys with different values. I'm changing it to a hashset of type:name
	//HashMap<String,String> objectiveTypes = new HashMap<String,String>(); //We only care if there is one instance of the type(quests with 2 of the same type) so hashset is perfect
	HashSet<String> objectiveTypes = new HashSet<String>();
	
	//Here to allow a custom creation process. Setup all members outside with setters
	public LoadedQuest() {
		
		//this.name = theQuest.name;
		//this.finishInfo = theQuest.finishInfo;
    }
	
	public LoadedQuest(YamlQuest theQuest){
		//I don't know how messy this is going to turn out. The YamlQuest object is just to mess with the look of the YamlFile
		/*public String Name;
		public String Start_Info;
		public String Finish_Info;
		public HashSet<ymlReward> rewards;
		public HashSet<Objective> objectives = new HashSet<Objective>();*/
		
		this.name = theQuest.getName();
		this.startInfo = theQuest.getStart_Info();
		this.finishInfo = theQuest.getFinish_Info();
		for(ymlReward reward : theQuest.getRewards())
			this.rewards.add(new Reward(reward));

		this.objectives = theQuest.getObjectives();
		
		for(Objective objective : this.objectives){
			this.objectiveTypes.add(objective.getType() + ":" + objective.getObjectiveName());
			//system.out.println(objective.getType());
		}
		
		//System.out.println(this.name);
		//System.out.println(this.startInfo);
		//System.out.println(this.finishInfo);
		//System.out.println(this.rewards.toString());
		//System.out.println(this.objectives.toString());
		
	}
	
	public void printInfo(UQuest plugin, Player player){
		Quester quester = plugin.getQuestInteraction().getQuester(player);
		player.sendMessage(ChatColor.GOLD + this.name);
		player.sendMessage(ChatColor.GREEN + this.startInfo);
		//Progress
		player.sendMessage("Objective:");
		for(Objective objective : this.objectives){
			player.sendMessage(objective.getPrintInfo(player, quester.getTracker(plugin, objective.getObjectiveName())));
		}if(this.objectives.size() == 0){
			player.sendMessage(ChatColor.RED + "None");
		}
		//Rewards
		if(!(plugin.isHideQuestRewards())){
			player.sendMessage("Reward:");
			for(Reward reward : this.rewards){
				player.sendMessage(reward.getPrintInfo());
			}if(this.rewards.size() ==0){
				player.sendMessage(ChatColor.RED + "None");
			}
		}
	}
	
	public boolean doneCheck(UQuest plugin, Player player){
		try{
			Quester quester = plugin.getQuestInteraction().getQuester(player);
			int doneAmount = 0;
			for(Objective objective : this.objectives){
				if(objective.doneCheck(player, quester.getTracker(plugin, objective.getObjectiveName())))
					doneAmount++;
			}
			if(doneAmount == this.objectives.size()){
				return true;
			}else{
				player.sendMessage(ChatColor.RED + "You only have " + ChatColor.stripColor(Integer.toString(doneAmount)) + " objectives done!");
				return false;
			}
		}catch(ArrayIndexOutOfBoundsException aiobe){
			System.err.println("[Hawox's uQuest]:LoadedQuest:doneCheck:ArrayIndexOutOfBoundsException: Player didn't have a correct quest tracker.");
			return false;
		}
	}
	
	public boolean checkObjective(UQuest plugin, Location point, String type, String name){
		//system.out.println(type + " _____ " + name);
		//system.out.println(this.objectiveTypes.contains(type + ":" + name));
		if(this.objectiveTypes.contains(type + ":" + name)){
			Objective obj = this.getObjectiveFromTypes(type, name);
			if(obj != null){
				return (obj.locationCheck(plugin, point));
			}
		}
		
		//If, for some reason, the new location check above fails and we get a null obj. We will still have this old fallback here.
		return ( this.objectiveTypes.contains(type + ":" + name) );
	}
	
	public Objective getObjectiveFromTypes(String type, String name){
		for(Objective obj : this.objectives){
			if(obj.type.equalsIgnoreCase(type) && obj.getObjectiveName().equalsIgnoreCase(name))
				return obj;
		}
		//no matching objective
		return null;
	}
	
	public void finish(UQuest plugin, Player player, boolean showText){
		Quester quester = plugin.getQuestInteraction().getQuester(player);
		int id = quester.getQuestID(); //For the event to know which quest id the player had

		//finish info
		if(showText == true){
			player.sendMessage(ChatColor.LIGHT_PURPLE + "**** Quest finished! ****");
			player.sendMessage(ChatColor.GOLD + this.name);
			player.sendMessage(ChatColor.GREEN + this.finishInfo);
		}
		
		//Consume the number of items used
		for(Objective objective : this.objectives)
			objective.done(player);
		
		//Increase player's finished quests meter thing
		if(showText == true)
			player.sendMessage(ChatColor.WHITE + "   *Your total completed quests has increased by 1!");

		quester.setQuestsCompleted(quester.getQuestsCompleted() + 1);
		
		//Reward Stuff
		for(Reward reward : this.rewards)
			reward.giveReward(plugin, player);
				
		//set them to having no active quest
		quester.setQuestID(-1);
		//set them to having no quest progress thingy stored
		quester.clearTracker();
		//save them to file
		if(!(plugin.isUseSQLite()))
			plugin.saveQuesterToFile(quester);
		
		if(plugin.isUseSQLite())
			plugin.getDB().put(player.getName(), quester);

		//call event
		plugin.getServer().getPluginManager().callEvent(new QuestFinishEvent(plugin.getServer().getPlayer(quester.getTheQuestersName()), quester, id));
	}

	
	//getters and setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStartInfo() {
		return startInfo;
	}

	public void setStartInfo(String startInfo) {
		this.startInfo = startInfo;
	}

	public String getFinishInfo() {
		return finishInfo;
	}

	public void setFinishInfo(String finishInfo) {
		this.finishInfo = finishInfo;
	}
	
	public HashSet<Reward> getRewards() {
		return rewards;
	}

	public void setRewards(HashSet<Reward> rewards) {
		this.rewards = rewards;
	}

	public HashSet<Objective> getObjectives() {
		return objectives;
	}

	public void setObjectives(HashSet<Objective> objectives) {
		this.objectives = objectives;
	}
	
}














/*
 * OLD
 */
/*
public class LoadedQuest {
	private final UQuest plugin;
	
	/*
		//quest_1=Get Wood\:gather\:Go gather me 10 wood please\!\:Thank you very much\!\:17\:10\:10\:0\:quest0kit\:0\:0\:Wood
	*
	
	int idNumber;
	
	
	String name;
	String type;
	String info;
	String finishInfo;
	String objective;
	int amountNeeded;
	int amountToConsume;
	int moneyReward;
	String materialName;
	//These two are for my leveling plugin
	int expToGive;
	int classToGiveItTo;
	
	public LoadedQuest(final UQuest plugin, String[] questInfoInString, int id) {
		this.plugin = plugin;
		
		this.idNumber = id;
		this.name = questInfoInString[0];
		this.type = questInfoInString[1];
		this.info = questInfoInString[2];
		this.finishInfo = questInfoInString[3];
		this.objective = questInfoInString[4];
		this.amountNeeded = Integer.parseInt(questInfoInString[5]);
		this.amountToConsume = Integer.parseInt(questInfoInString[6]);
		this.moneyReward = Integer.parseInt(questInfoInString[7]);
		this.materialName = questInfoInString[8];
		this.expToGive = Integer.parseInt(questInfoInString[9]);
		this.classToGiveItTo = Integer.parseInt(questInfoInString[10]);
    }
	@SuppressWarnings("static-access")
	public void printInfo(Player player){
		Quester quester = plugin.getQuestInteraction().getQuester(player);
		player.sendMessage(ChatColor.GOLD + this.name + ": " + ChatColor.GREEN + this.info);
		//String moneyName = storedMoneySettings.getString("money-name");
		player.sendMessage(ChatColor.GREEN + "  *Reward: " + ChatColor.DARK_GREEN + this.moneyReward + " " + plugin.getiConomy().getBank().getCurrency());
		//show the progress based on what type of quest it is
		if(this.type.equalsIgnoreCase("gather")){
			player.sendMessage(ChatColor.BLUE  + "  *Progress: " + Integer.toString(plugin.getQuestInteraction().countItems(player,Integer.parseInt(this.objective))) + "/" + this.amountNeeded + " " + ChatColor.GRAY + this.materialName);
		}
		if( (this.type.equalsIgnoreCase("blockdestroy")) || this.type.equalsIgnoreCase("blockdamage") || this.type.equalsIgnoreCase("blockplace")){
			player.sendMessage(ChatColor.BLUE + "  *Progress: " + Integer.toString(quester.questTracker) + "/" + this.amountNeeded + " " + ChatColor.GRAY + this.materialName);
		}
	}
	
	public boolean questDoneCheck(Player player){
		//quest_1=Get Wood\:gather\:Go gather me 10 wood please\!\:Thank you very much\!\:17\:10\:10\:0\:quest0kit\:0\:0\:Wood
		
		Quester quester = plugin.getQuestInteraction().getQuester(player);
		
		//check if it's a gather quest
		if(this.type.equalsIgnoreCase("gather")){
			//it's a gather mission, check the amount of the item they have and compare it to the mission reqs
			if(plugin.getQuestInteraction().countItems(player, Integer.parseInt(this.objective)) >= this.amountNeeded){
				//player should have enough to complete the gather quest
				return true;
			}
		}
		//check if it's a blockdestroy quest
		if(this.type.equalsIgnoreCase("blockdestroy")){
			//it's a blockdestroy mission, check the amount of the item they have and compare it to the mission reqs
			if(quester.questTracker >= this.amountNeeded){
				//player should have destroyed enough blocks for the quest
				return true;
			}
		}
		//check if it's a blockdamage quest
		if(this.type.equalsIgnoreCase("blockdamage")){
			//it's a blockdamage mission, check the amount of the item they have and compare it to the mission reqs
			if(quester.questTracker >= this.amountNeeded){
				//player should have damaged enough blocks for the quest
				return true;
			}
		}
		//check if it's a blockplace quest
		if(this.type.equalsIgnoreCase("blockplace")){
			//it's a blockplace mission, check the amount of the item they have and compare it to the mission reqs
			if(quester.questTracker >= this.amountNeeded){
				//player should have placed enough blocks for the quest
				return true;
			}
		}
		//check if it's a blockplace quest
		if(this.type.equalsIgnoreCase("kill")){
			//it's a blockplace mission, check the amount of the item they have and compare it to the mission reqs
			if(quester.questTracker >= this.amountNeeded){
				//player should have placed enough blocks for the quest
				return true;
			}
		} 
		return false; //quest is not complete
	}
	public int getIdNumber() {
		return idNumber;
	}
	public void setIdNumber(int idNumber) {
		this.idNumber = idNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getFinishInfo() {
		return finishInfo;
	}
	public void setFinishInfo(String finishInfo) {
		this.finishInfo = finishInfo;
	}
	public int getAmountNeeded() {
		return amountNeeded;
	}
	public void setAmountNeeded(int amountNeeded) {
		this.amountNeeded = amountNeeded;
	}
	public int getAmountToConsume() {
		return amountToConsume;
	}
	public void setAmountToConsume(int amountToConsume) {
		this.amountToConsume = amountToConsume;
	}
	public int getMoneyReward() {
		return moneyReward;
	}
	public void setMoneyReward(int moneyReward) {
		this.moneyReward = moneyReward;
	}
	public String getMaterialName() {
		return materialName;
	}
	public void setMaterialName(String materialName) {
		this.materialName = materialName;
	}
	public int getExpToGive() {
		return expToGive;
	}
	public void setExpToGive(int expToGive) {
		this.expToGive = expToGive;
	}
	public int getClassToGiveItTo() {
		return classToGiveItTo;
	}
	public void setClassToGiveItTo(int classToGiveItTo) {
		this.classToGiveItTo = classToGiveItTo;
	}
	public String getObjective() {
		return objective;
	}
	public void setObjective(String objective) {
		this.objective = objective;
	}
	
}*/
