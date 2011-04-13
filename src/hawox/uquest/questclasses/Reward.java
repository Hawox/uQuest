package hawox.uquest.questclasses;

import hawox.uquest.UQuest;
import hawox.uquest.questclasses.QuestLoader.ymlReward;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/*
 * Right now there are only two types of quest rewards, but this is here to make it easier
 *    For expansion later.
 */
public class Reward{
	//private final UQuest plugin;

	/*public String Type;
	public String Item_ID;
	public String Display_Name;
	public String Amount;
	public String Durability;*/
	
	String type;
	String displayName;
	int howMuch;
	
	//may not be used
	ItemStack item;
	
	public Reward(ymlReward reward){
		if(reward.Type.equalsIgnoreCase("money")){
			this.type = "money";
			this.howMuch = Integer.parseInt(reward.Amount);
			this.displayName = reward.Display_Name;
		}else
		if(reward.Type.equalsIgnoreCase("item")){
			this.type = "item";
			this.displayName = reward.Display_Name;
			this.item = reward.toItem();
			this.howMuch = this.item.getAmount();
		}else{
			System.err.println("[Hawox's uQuest]:Reward:Invalid reward type!!! ->" + reward.Type);
		}
	}
	
	//Used for itemrewards
	public Reward(String type, String displayName, ItemStack item){
		this.type = type;
		this.displayName = displayName;
		this.item = item;
	}
	//just money
	public Reward(String type, int howMuch, String moneyName){
		this.type = type;
		this.howMuch = howMuch;
		this.displayName = moneyName;
	}
	
	//copy constructor
	public Reward(Reward old){
		this.type = old.type;
		this.displayName = old.displayName;
		this.howMuch = old.howMuch;
		this.item = old.item;
	}
	
	 //false if it's not a money reward
	public boolean scaleMoneyReward(int factor){
		if(this.type.equalsIgnoreCase("money")){
			this.setHowMuch(factor * this.getHowMuch());
			return true;
		}
		return false;
	}
	
	/*
	 * Different based on the type of reward
	 */
	public void giveReward(UQuest plugin, Player player){
		if(this.type.equalsIgnoreCase("money")){
			plugin.getQuestInteraction().addMoney(player, this.howMuch);
		}else
		if(this.type.equalsIgnoreCase("item")){
			player.getInventory().addItem(this.item);
			player.sendMessage(ChatColor.YELLOW + "*You got " + this.item.getAmount() + " " + this.displayName + " from a quest!");
		}
	}
	
	
	public String getPrintInfo(){
		String returnMe = "   " + ChatColor.GREEN + Integer.toString(this.howMuch) + " " + this.displayName;
		return returnMe;
	}
	
	
	
	
	//Generic getters and setters
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getHowMuch() {
		return howMuch;
	}
	public void setHowMuch(int howMuch) {
		this.howMuch = howMuch;
	}
	public ItemStack getItem() {
		return item;
	}
	public void setItem(ItemStack item) {
		this.item = item;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}

