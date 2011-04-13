package hawox.uquest.commands;


import hawox.uquest.Quester;
import hawox.uquest.UQuest;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;

public class Cmd_uquest implements CommandExecutor{
	private final UQuest plugin;
	
	public Cmd_uquest(UQuest plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		if(sender instanceof Player){
			player = (Player)sender;
			}else{
				sender.sendMessage(plugin.pluginNameBracket() + " This is not ment for console use silly!");
				return true;
			}
		//should make this a place at this point
		
		/*boolean processQuest = false;
		
		try{
		if(plugin.isUsePermissions() == true){
			if(UQuest.Permissions.has(player, "uQuest.CanQuest")){
				processQuest = true;
			}
		}else{
			//no permission support so we let everyone use it!
			processQuest = true;
		}
		}catch(NoClassDefFoundError ncdfe){
			processQuest = true;
			//they don't have permissions so disable it plugin wide
			plugin.setUsePermissions(false);
			System.err.println(plugin.pluginNameBracket() + " Failed to access Permissions plugin. Disabling support for it.");
		}*/
		
		//This is now a permissions communication check as well as an update notice.
		try{
			if(plugin.isUsePermissions() == true){
				if(UQuest.Permissions.has(player, "uQuest.CanQuest")){
					System.err.println(plugin.pluginNameBracket() + " The node 'uQuest.CanQuest' is no longer used. Update this!");
				}
			}
		}catch(NoClassDefFoundError ncdfe){
				//they don't have permissions so disable it plugin wide
				plugin.setUsePermissions(false);
				System.err.println(plugin.pluginNameBracket() + " Failed to access Permissions plugin. Disabling support for it.");
		}
		
//		if(processQuest == true){
						
			try{
				//because we'll be using it alot, lets just nab the users saved info here so the code looks a bit neater
				Quester quester = plugin.getQuestInteraction().getQuester(player);
				
				if( (  (args[0].equalsIgnoreCase("?")) ||  (args[0].equalsIgnoreCase("help")) ) ){
					displayCommands(player);
				}
				
				/* FIXME Ranking Stuff goes here */
					
				if( (args[0].equalsIgnoreCase("give")) ){
					if( (plugin.isUsePermissions() == false) || ( (plugin.isUsePermissions()) && (UQuest.Permissions.has(player, "uQuest.CanQuest.give")) ) ){
						//We want the first quest to be easy. Always give quest id 1 first!
						if(quester.getQuestsCompleted() < 1){
							plugin.getQuestInteraction().giveQuest(0, player);
						}else{
							plugin.getQuestInteraction().giveQuestRandom(player);
						}
					}else{
						player.sendMessage(ChatColor.RED + "You don't have permission to drop quests!");
						return true;					}
				}
							
								
				if( (args[0].equalsIgnoreCase("info")) ){
					if( (plugin.isUsePermissions() == false) || ( (plugin.isUsePermissions()) && (UQuest.Permissions.has(player, "uQuest.CanQuest.info")) ) ){
						//make sure the player has a quest then simply read out the info of that quest eZ
						if(quester.getQuestID() == -1){
							player.sendMessage("You don't have an active quest!");
						} else{
							//player has a quest so...
							plugin.getQuestInteraction().getCurrentQuest(player,plugin.getQuestInteraction().isScaleQuestLevels()).printInfo(this.plugin, player);
						}
					}else{
						player.sendMessage(ChatColor.RED + "You don't have permission to drop quests!");
						return true;					}
				}
							
				if( (args[0].equalsIgnoreCase("stats")) ){
					if( (plugin.isUsePermissions() == false) || ( (plugin.isUsePermissions()) && (UQuest.Permissions.has(player, "uQuest.CanQuest.stats")) ) ){
						plugin.getQuestInteraction().showQuestersInfo(player);
					}else{
						player.sendMessage(ChatColor.RED + "You don't have permission to drop quests!");
						return true;					}
				}
				
				if( (args[0].equalsIgnoreCase("amount")) ){
					if( (plugin.isUsePermissions() == false) || ( (plugin.isUsePermissions()) && (UQuest.Permissions.has(player, "uQuest.CanQuest.amount")) ) ){
						//Tell the player the # of quests in the system
						player.sendMessage("There are currently " + ChatColor.GOLD + Integer.toString(plugin.getQuestInteraction().getQuestTotal()) + ChatColor.WHITE + " quests loaded!");
					}else{
						player.sendMessage(ChatColor.RED + "You don't have permission to drop quests!");
						return true;					}
				}
				
							
				if( (args[0].equalsIgnoreCase("done")) ){
					if( (plugin.isUsePermissions() == false) || ( (plugin.isUsePermissions()) && (UQuest.Permissions.has(player, "uQuest.CanQuest.done")) ) ){
						if(quester.getQuestID() == -1){
							player.sendMessage(ChatColor.RED + "You don't have an active quest!");
						} else{
							if(plugin.getQuestInteraction().questTurnInAttempt(player) == true){
							
							}else{
								//quest is not done!
								player.sendMessage(ChatColor.RED + "Your quest isn't done! Type: /uQuest info");
							}
						}
					}else{
						player.sendMessage(ChatColor.RED + "You don't have permission to drop quests!");
						return true;					}
				}
				
				if( (args[0].equalsIgnoreCase("drop")) ){
/*					boolean processDrop = false;
					
					if(plugin.isUsePermissions() == true){
						if(UQuest.Permissions.has(player, "uQuest.CanDropQuest")){
							processDrop = true;
						}
					}else{
						//no permission support so we let everyone use it!
						processDrop = true;
					}
					if(processDrop == true){*/
					if( (plugin.isUsePermissions() == false) || ( (plugin.isUsePermissions()) && (UQuest.Permissions.has(player, "uQuest.CanDropQuest")) ) ){
						//do they even have a quest?
						if(quester.getQuestID() != -1){
							if(plugin.getQuestInteraction().isPlayerOnDropQuestList(player.getName()) == true){
								player.sendMessage(ChatColor.RED + "You've already dropped a quest! You need to wait before you can drop another one!");
							}else{
								//can drop quest check money
								boolean canDropQuest = true;
								if(plugin.getQuestInteraction().isUseiConomy() || plugin.getQuestInteraction().isUseBOSEconomy() ){
									if(plugin.getQuestInteraction().getMoney(player) >= plugin.getQuestInteraction().getDropQuestCharge()){
										//has enough money so take it away here
										plugin.getQuestInteraction().addMoney(player, -plugin.getQuestInteraction().getDropQuestCharge());
										canDropQuest = true;
									}else{
										if(plugin.getQuestInteraction().isUseiConomy()){
											UQuest.getiConomy();
											player.sendMessage(ChatColor.RED + "You don't have enough money to drop a quest! You need " + plugin.getQuestInteraction().getDropQuestCharge() + " " + iConomy.getBank().getCurrency() + "!");
											canDropQuest = false;
											return true;
										}else if(plugin.getQuestInteraction().isUseBOSEconomy()){
											player.sendMessage(ChatColor.RED + "You don't have enough money to drop a quest! You need " + plugin.getTheBOSEconomy().getMoneyNamePlural() + "!");
											canDropQuest = false;
											return true;
										}
									}
								}
								if(canDropQuest == true){
									//player can drop the quest!!!
									plugin.getQuestInteraction().questDrop(player);
									player.sendMessage(ChatColor.GREEN + "Quest dropped!");
									if(plugin.getQuestInteraction().getDropQuestInterval() > 0){
										plugin.getQuestInteraction().addPlayerToDropQuestList(player.getName());
										plugin.getQuestInteraction().removePlayerFromDropQuestListWithTimer(player.getName(), plugin.getQuestInteraction().getDropQuestInterval());
										player.sendMessage(ChatColor.GRAY + "You can not drop a quest for " + plugin.getQuestInteraction().getDropQuestInterval() + " minutes!");
									}
								}else{
									player.sendMessage(ChatColor.RED + "Quest dropping failed for an unknown reason!");
								}
							}
						}else{
							player.sendMessage(ChatColor.RED + "You don't have a quest to drop!");
							return true;
						}
					}else{
						player.sendMessage(ChatColor.RED + "You don't have permission to drop quests!");
						return true;
					}
				}
			}
			catch(ArrayIndexOutOfBoundsException aiobe){
				displayCommands(player);
				}	
			//}
			//end of /questme prefix
//	    }else{
//	    	player.sendMessage(ChatColor.RED + "You don't have permission to use that!");
//	    }
		return true;
	}
	
	public void displayCommands(Player player){
		player.sendMessage(ChatColor.LIGHT_PURPLE + "uQuest is a simple random quest plugin. How to use it:");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "You can also use /quest or /q instead of /uquest");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> Commands:");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest (?/help)" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows this help menu");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest stats" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows your stored info. Try it!");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest amount" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows the amount of loaded quests");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest give" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Gives you a random quest");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest done" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Attempts to turn in your current quest");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest info" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Resends you your quest info/progress");
		player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest drop" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Drops your current quest");
		//player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest top #" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows you the top 5 questers");
	}
}



/*if( (split[1].equalsIgnoreCase("top"))){
	try{
	int topAmount = Integer.parseInt(split[2]);
	ArrayList<Quester> topQuestersInOrder = null;
	ArrayList<Quester> tempQuesterList = plugin.theQuesterList;
	
	//just to advoid warnings and potentioal nulls there HAS to be a player here since one needs to be logged it
	//topQuestersInOrder.add(tempQuesterList.get(0));
	
	//this could be made better. Ask \\!
	for(int i=0; i< topAmount; i++){
		Quester currentBest = tempQuesterList.get(0);
		if(currentBest != null){
			for(int j=1; j<tempQuesterList.size(); j++){
				Quester current = tempQuesterList.get(j);
				if(current.getQuestsCompleted() > currentBest.getQuestsCompleted()){
					currentBest = current;
				}
				//if it's equal, see if they have more money earned!
				if(current.getQuestsCompleted() == currentBest.getQuestsCompleted()){
					if(current.getMoneyEarnedFromQuests() > currentBest.getMoneyEarnedFromQuests()){
						currentBest = current;
					}
				}
			}
			topQuestersInOrder.add(currentBest);
			tempQuesterList.remove(currentBest);
		}
	}
	//Got the list! (I think...)
	//display the ranks out to the user!
	player.sendMessage(ChatColor.GRAY + "I need to fix the ranking, may not display correctly.");
	player.sendMessage(ChatColor.DARK_BLUE + "******Best Online Questers******");
	for(int i=0; i<topQuestersInOrder.size(); i++){
		Quester currentQuester = topQuestersInOrder.get(i);
		if(topQuestersInOrder.get(i) != null){
			
			player.sendMessage(" *" + (i+1) + ".) " + ChatColor.DARK_GREEN + currentQuester.theQuester.getName() + ChatColor.GRAY + " | Quests:" + currentQuester.getQuestsCompleted() + " | Earnings:" + currentQuester.getMoneyEarnedFromQuests());
		} else {
			//name is empty so just show empty
			player.sendMessage(" *" + (i+1) + ".) Empty Slot");
		}
	}
	}
	catch(NumberFormatException nfe){ player.sendMessage(ChatColor.RED + "That's not a number!"); }
	catch(ArrayIndexOutOfBoundsException aiobe){ player.sendMessage(ChatColor.RED + "Please type a number after top!"); }
}*/