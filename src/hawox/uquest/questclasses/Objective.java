package hawox.uquest.questclasses;

import hawox.uquest.UQuest;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Objective{
	String type;				 //Type of quest objective this will be
	String displayname;			 //Name that will be displayed to the users
	String objectiveName;		 //this will be the itemID/monster name based on the type of quest it is.
	int amountNeeded;			 //Amount of needed. Whether it be items, monster kills, etc
	
	//So we don't have to store a plugin object here (just in case I ever need to serialize these) locations will be strings.
	String locationNeeded;	 //The point where the quest needs to be
	String locationGiveRange;  //How far from the point players can be in any direction.
	
	
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
		this.locationNeeded = point;
		this.locationGiveRange = give;
	}
	
	//Used for quests that require items
	public Objective(String type, String displayName, ItemStack itemNeeded, String point, String give){
		
		//String[] pointInfo = point.split("~");
		
		this.type = type;
		this.displayname = displayName;
		this.itemNeeded = itemNeeded;
		this.objectiveName = Integer.toString(itemNeeded.getTypeId());
		this.amountNeeded = itemNeeded.getAmount();
		this.locationNeeded = point;
		this.locationGiveRange = give;
	}
	
	//cloning constructor
	public Objective(Objective old){
		this.type = old.type;
		this.displayname = old.getDisplayname();
		this.itemNeeded = old.itemNeeded;
		this.objectiveName = old.objectiveName;
		this.amountNeeded = old.amountNeeded;
		this.locationNeeded = old.locationNeeded;
		this.locationGiveRange = old.locationGiveRange;
	}
	
	public boolean locationCheck(UQuest plugin, Location pointCheck){
		try{
			Location neededLocation = this.pointToLocation(plugin);

			//Just quit if it's not the right world!
			if(!(pointCheck.getWorld().equals(neededLocation))){
				return false;
			}
			//x,y,z
			String[] giveSplit = this.locationGiveRange.split(":");
			int[] checkX = { (Math.round(Math.round(pointCheck.getX())) - Integer.parseInt(giveSplit[0])),(Math.round(Math.round(pointCheck.getX())) + Integer.parseInt(giveSplit[0])) };
			int[] checkY = { (Math.round(Math.round(pointCheck.getY())) - Integer.parseInt(giveSplit[1])),(Math.round(Math.round(pointCheck.getY())) + Integer.parseInt(giveSplit[1])) };
			int[] checkZ = { (Math.round(Math.round(pointCheck.getZ())) - Integer.parseInt(giveSplit[2])),(Math.round(Math.round(pointCheck.getZ())) + Integer.parseInt(giveSplit[2])) };

			//check x
			if( (pointCheck.getX() >= checkX[0]) && (pointCheck.getX() <= checkX[1]) ){
				//check y
				if( (pointCheck.getY() >= checkY[0]) && (pointCheck.getY() <= checkY[1]) ){
					//check z
					if( (pointCheck.getZ() >= checkZ[0]) && (pointCheck.getZ() <= checkZ[1]) ){
						//Yay were are in the right world and within all the given bounds!!!
						return true;
					}
				}

			}
			//Not in bounds
			return false;
			
		}catch(NullPointerException npe){
			//just return true because we couldn't get a valid location.
			return true;
		}
	}
	
	public Location pointToLocation(UQuest plugin){
		try{
			// world:x:y:z
			String[] splitInfo = this.locationNeeded.split(":");
			World world = plugin.getServer().getWorld(splitInfo[0]);
			int x = Integer.parseInt(splitInfo[1]);
			int y = Integer.parseInt(splitInfo[2]);
			int z = Integer.parseInt(splitInfo[3]);

			Location returnMe = new Location(world,x,y,z);
			
			return (returnMe);
		}catch(ArrayIndexOutOfBoundsException aiobe){
			//Either there was no location or they messed it up. Either way just send null.
			return null;
		}
	}
	
	/*
	 * Basicly delete items it has for now
	 */
	public void done(Player player){
		if(this.itemNeeded != null)
			removeItem(player, this.itemNeeded.getTypeId(), this.amountNeeded);
	}

	public void scaleToLevel(int factor){
		this.setAmountNeeded(factor * this.getAmountNeeded());
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
			//location
			if(this.locationNeeded != null){
				//world:x:y:z
				String[] loc = this.locationNeeded.split(":");
				String[] give = this.locationGiveRange.split(":");
				//TODO Find a better way to output this...
				returnMe += "\n     @: " + loc[0] + " " + loc[1] + "," + loc[2] + "," + loc[3] + " Radius: " + give[0] + "," + give[1] + "," + give[2];
			}
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

	public String getLocationNeeded() {
		return locationNeeded;
	}

	public void setLocationNeeded(String locationNeeded) {
		this.locationNeeded = locationNeeded;
	}

	public String getLocationGiveRange() {
		return locationGiveRange;
	}

	public void setLocationGiveRange(String locationGiveRange) {
		this.locationGiveRange = locationGiveRange;
	}

	public ItemStack getItemNeeded() {
		return itemNeeded;
	}

	public void setItemNeeded(ItemStack itemNeeded) {
		this.itemNeeded = itemNeeded;
	}
}
