package hawox.uquest.questclasses;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Objective {
	String type;				 //Type of quest objective this will be
	String displayname;			 //Name that will be displayed to the users
	String objectiveName;		 //this will be the itemID/monster name based on the type of quest it is.
	int amountNeeded;			 //Amount of needed. Whether it be items, monster kills, etc
	Location locationNeeded;	 //The point where the quest needs to be
	Location locationGiveRange;  //How far from the point players can be in any direction.
	
	
	/*These may or may not be used depending on the type of quest it is*/
	ItemStack itemNeeded;
	//int itemID;
	//String monsterTypeID;
	
	
	//While everything is needed, you can place a null and the method checks here will do the rest
	public Objective(String type, String displayName, String objectiveName, int amountNeeded, String point, String give){
		
		this.type = type;
		this.displayname = displayName;
		this.objectiveName = objectiveName;
		this.amountNeeded = amountNeeded;
		//this.locationNeeded = locationNeeded;
		//this.locationGiveRange = locationGiveRange;
	}
	
	//Used for quests that require items
	public Objective(String type, String displayName, ItemStack itemNeeded, String point, String give){
		
		//String[] pointInfo = point.split("~");
		
		this.type = type;
		this.displayname = displayName;
		this.itemNeeded = itemNeeded;
		this.objectiveName = Integer.toString(itemNeeded.getTypeId());
		this.amountNeeded = itemNeeded.getAmount();
//		this.locationNeeded = new Location(
//		this.locationGiveRange = locationGiveRange;
	}
	
	
	
	/*
	 * Basicly delete items it has for now
	 */
	public void done(Player player){
		if(this.itemNeeded != null)
			removeItem(player, this.itemNeeded.getTypeId(), this.amountNeeded);
	}

	
	public String getPrintInfo(Player player, int questTracker){
		int howMuch = 0; //this is how far along they are in their current quest. It will be changed based on what quest type they have.
		String returnMe;
		
		//show the progress based on what type of quest it is
		if(this.type.equalsIgnoreCase("gather")){
			howMuch = countItems(player,this.itemNeeded.getTypeId());
		}
		if( (this.type.equalsIgnoreCase("blockdestroy")) || this.type.equalsIgnoreCase("blockdamage") || this.type.equalsIgnoreCase("blockplace") || this.type.equalsIgnoreCase("kill")){
			howMuch = questTracker;
		}
		//Objectives that are complete will look different than non completed objectives
		if(howMuch >= this.amountNeeded){
			//done
			returnMe = "   " + ChatColor.YELLOW + Integer.toString(howMuch) + "/" + this.amountNeeded + " " + this.displayname;
		}else{
			returnMe = "   " + ChatColor.AQUA + Integer.toString(howMuch)  + ChatColor.WHITE + "/" + this.amountNeeded + " " + ChatColor.GRAY + this.displayname;
		}
		return returnMe;

	}
	
	public boolean doneCheck(Player player, int questTracker){
		//quest_1=Get Wood\:gather\:Go gather me 10 wood please\!\:Thank you very much\!\:17\:10\:10\:0\:quest0kit\:0\:0\:Wood
				
		//check if it's a gather quest
		if(this.type.equalsIgnoreCase("gather")){
			//it's a gather mission, check the amount of the item they have and compare it to the mission reqs
			if(countItems(player, this.itemNeeded.getTypeId()) >= this.amountNeeded){
				//player should have enough to complete the gather quest
				return true;
			}
		}
		//check if it's a blockdestroy quest
		if(this.type.equalsIgnoreCase("blockdestroy")){
			//it's a blockdestroy mission, check the amount of the item they have and compare it to the mission reqs
			if(questTracker >= this.amountNeeded){
				//player should have destroyed enough blocks for the quest
				return true;
			}
		}
		//check if it's a blockdamage quest
		if(this.type.equalsIgnoreCase("blockdamage")){
			//it's a blockdamage mission, check the amount of the item they have and compare it to the mission reqs
			if(questTracker >= this.amountNeeded){
				//player should have damaged enough blocks for the quest
				return true;
			}
		}
		//check if it's a blockplace quest
		if(this.type.equalsIgnoreCase("blockplace")){
			//it's a blockplace mission, check the amount of the item they have and compare it to the mission reqs
			if(questTracker >= this.amountNeeded){
				//player should have placed enough blocks for the quest
				return true;
			}
		}
		//check if it's a kill quest
		if(this.type.equalsIgnoreCase("kill")){
			//it's a blockplace mission, check the amount of the item they have and compare it to the mission reqs
			if(questTracker >= this.amountNeeded){
				//player should have placed enough blocks for the quest
				return true;
			}
		} 
		return false; //quest is not complete
	}
	
	// get the players inventory, check every slot for said item, count the
	// number in the slot, add it to total -> Return
	public int countItems(Player player, int itemID) {
		int count = 0;
		ItemStack[] allItems = player.getInventory().getContents();
		for (int i = 0; i < allItems.length; i++) {
			if (allItems[i] != null) {
				if (allItems[i].getTypeId() == itemID) {
					count += allItems[i].getAmount();
				}
			}
		}
		return count;
	}
	
	public void removeItem(Player player, int id, int amountToConsume) {
		Inventory bag = player.getInventory();
		while (amountToConsume > 0) {
			int slot = bag.first(id);
			ItemStack item = bag.getItem(slot);
			if (item.getAmount() <= amountToConsume) {
				amountToConsume -= item.getAmount();
				bag.clear(slot);
			} else {
				// more in this stack than than we need
				item.setAmount(item.getAmount() - amountToConsume);
				amountToConsume = 0;
			}
		}
	}

	//Checks to see if the location given is in give range
	/* TODO: On hold. Just realized Double may have the best solution for this!!!
	public boolean isLocationInGiveRange(Location location){
		double givenX = location.getX();
		double givenY = location.getY();
		double givenZ = location.getZ();
		
		//check x
		if(givenX)
		//check y
		
		//check z

	}*/
	
	
	
	//Generic getters and setters
	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getDisplayname() {
		return displayname;
	}


	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}


	public String getObjectiveName() {
		return objectiveName;
	}


	public void setObjectiveName(String objectiveName) {
		this.objectiveName = objectiveName;
	}


	public int getAmountNeeded() {
		return amountNeeded;
	}


	public void setAmountNeeded(int amountNeeded) {
		this.amountNeeded = amountNeeded;
	}


	public Location getLocationNeeded() {
		return locationNeeded;
	}


	public void setLocationNeeded(Location locationNeeded) {
		this.locationNeeded = locationNeeded;
	}


	public Location getLocationGiveRange() {
		return locationGiveRange;
	}


	public void setLocationGiveRange(Location locationGiveRange) {
		this.locationGiveRange = locationGiveRange;
	}
	

}
