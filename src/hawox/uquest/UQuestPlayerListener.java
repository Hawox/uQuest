package hawox.uquest;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;


/**
 * Handle events for all Player related events
 */
public class UQuestPlayerListener extends PlayerListener {
    private final UQuest plugin;

    public UQuestPlayerListener(UQuest instance) {
        plugin = instance;
    }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event){
    	Player player = event.getPlayer();
    	//Keep track of all the players that have logged in
    	if(!(plugin.getPlayersLoggedInSinceBoot().contains(player.getName())))
    		plugin.getPlayersLoggedInSinceBoot().add(player.getName());
    	
    	// check if the player is registered in the questme's file
    	if(plugin.isUseSQLite()){
    		if(plugin.getDB().get(player.getName()) == null){
    			//They are not in the DB so add them as a quester!
    			plugin.getDB().put(player.getName(), new Quester(plugin.getQuestDefaultPlayer().split(":"), player));
    			System.out.println(plugin.pluginNameBracket() + " Player added to SQLite DB! : " + player.getName());
    		}
    		//we still need to add them to the list for ranking means
    		Quester q = plugin.getDB().get(player.getName());
    		if(!(plugin.getTheQuesterList().contains(q)))
    			plugin.getTheQuesterList().add(q);
    		plugin.placePlayerIntoRankedList(q);
    	}else{
    		plugin.placePlayerIntoList(player);
    	}
    }
}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
/*
    
	@Override
    public void onPlayerCommand(PlayerChatEvent event){
		Player player = event.getPlayer();
    	String[] split = event.getMessage().split(" ");
    	if(plugin.isEnabled() == true){
		if(UQuest.Permissions.has(player, "Hawox.CanQuest")){
		
		//Quest_# = Name:Type:Info:FinishInfo:ItemIDNeeded:AmountNeeded:AmountConsume:RubixReward:KitReward:ItemIDName
		
		try{
		if(  (split[0].equals("/questme"))  ||  (split[0].equals("/quest"))  ||  (split[0].equals("/uquest"))  ||  (split[0].equals("/q"))  ||  (split[0].equals("/uq"))  ||  (split[0].equals("/uquest")) ){
			//because we'll be using it alot, lets just nab the users saved info here so the code looks a bit neater
			Quester quester = plugin.getQuester(player);
			//get the players current quest as well if they have one
			CurrentQuest currentQuest = null;
			if(quester.getQuestID() != -1){
				if(plugin.isScaleQuestLevels()){
					currentQuest = new CurrentQuest(plugin, plugin.theQuests.get(quester.getQuestID()), plugin.getQuestLevel(quester));
				}else{
					currentQuest = new CurrentQuest(plugin, plugin.theQuests.get(quester.getQuestID()), 0);
				}
			}
			
			
			
			if( (  (split[1].equals("?")) ||  (split[1].equals("help")) )){
				player.sendMessage(ChatColor.LIGHT_PURPLE + "uQuest is a simple random quest plugin. How to use it:");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "You can also use /quest or /q instead of /uquest");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "-> Commands:");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest (?/help)" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows this help menu");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest stats" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows your stored info. Try it!");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest amount" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows the amount of loaded quests");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest give" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Gives you a random quest");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest done" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Attempts to turn in your current quest");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest info" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Resends you your quest info/progress");
				//player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest top #" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows you the top 5 questers");
			}
			
			/* FIXME Ranking Stuff goes here */
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
			}
				
			if( (split[1].equals("give"))){
				//check if the player already has an active quest or not (-1 is no active quest)
				if( quester.getQuestID() == -1 ){
					//We want the first quest to be easy. Always give quest id 1 first!
					if(quester.getQuestsCompleted() < 1){
						quester.setQuestID(0);
					}else{
						//player can get a quest! Assign them a quest ID
						Random numberGen = new Random();
						//We have the random go two deep because I'm seeing issues with people having the same 4 quests
						Random numberGenDeep = new Random(numberGen.nextLong());
						quester.setQuestID(numberGenDeep.nextInt(plugin.getQuestAmount()));
					}
					//get their quest info again to output stuffs
					if(plugin.isScaleQuestLevels()){
						currentQuest = new CurrentQuest(plugin, plugin.theQuests.get(quester.getQuestID()), plugin.getQuestLevel(quester));
					}else{
						currentQuest = new CurrentQuest(plugin, plugin.theQuests.get(quester.getQuestID()), 0);
					}
					currentQuest.printInfo(player);
				} else {
					//player dosn't have a quest
					player.sendMessage(ChatColor.RED + "You already have an active quest!");
				}
			}
						
							
			if( (split[1].equals("info"))){
				//make sure the player has a quest then simply read out the info of that quest eZ
				if(quester.getQuestID() == -1){
					player.sendMessage("You don't have an active quest!");
				} else{
					//player has a quest so...
					currentQuest.printInfo(player);
				}
			}
						
			if( (split[1].equals("stats") ){
				//Tell the player their active quest name
				player.sendMessage(ChatColor.GOLD + "Stats for: " + player.getName());
				String tempQuestNameStorage = "You have no active quests!";
				
				if( quester.getQuestID() != -1 ){
					//get the players active quest name
					tempQuestNameStorage = currentQuest.getName();
				}
				player.sendMessage("Quest Level: " + plugin.getQuestLevel(quester));
				player.sendMessage("Active quest: " + tempQuestNameStorage);
				//tell the player their # of completed quests
				player.sendMessage("Quests completed: " + quester.getQuestsCompleted());
				//tell the player the amount of money they have earned from quests
				if(plugin.useiConomy == true){
					player.sendMessage("Total " + iConomy.currency + " received: " + quester.getMoneyEarnedFromQuests());
				}
			}
			
			if( (split[1].equals("amount")) ){
				//Tell the player the # of quests in the system
				player.sendMessage("There are currently " + ChatColor.GOLD + Integer.toString(plugin.getQuestAmount()) + ChatColor.WHITE + " quests loaded!");
			}
			
						
			if( (split[1].equals("done")) ){
				//make sure the player has a quest then simply check if it's done like in progress!
				if(quester.getQuestID() == -1){
					player.sendMessage(ChatColor.RED + "You don't have an active quest!");
				} else{
					//player has a quest so check if it's done
					if(currentQuest.questDoneCheck(player) == true){
						//Quest_# = Name:Type:Info:FinishInfo:ItemIDNeeded:AmountNeeded:AmountConsume:RubixReward:KitReward:ItemIDName
						/*When a quest is done we need to:
						 * Show finish info
						 * Consume the amount used
						 * Give them a rubix award
						 * Give them the kit reward
						 * incress the players completed quests + 1
						 * get rid of their quest
						*
						//finish info
						//give the rubix award
						plugin.addMoney(player, currentQuest.getMoneyReward());
						player.sendMessage(ChatColor.BLUE + "**** Quest finished! ****");
						player.sendMessage(ChatColor.GREEN + "   *" + ChatColor.GOLD + currentQuest.name + ": " + ChatColor.GREEN + currentQuest.getFinishInfo());
						//Consume the number of items used
						if(!(currentQuest.getType().equalsIgnoreCase("kill"))){
							plugin.removeItem(player, currentQuest.getItemIDNeeded(), currentQuest.getAmountToConsume());
						}
						//Increase player's finished quests meter thing
						player.sendMessage(ChatColor.DARK_GREEN + "   *Your total completed quests has increased by 1!");
						quester.setQuestsCompleted(quester.getQuestsCompleted() + 1);
						
						//Tell the server for every x quests someone completes
						if( (quester.getQuestsCompleted() % plugin.questAnnounceInterval ) == 0){
							plugin.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + " has completed " + ChatColor.DARK_PURPLE + quester.getQuestsCompleted() + ChatColor.YELLOW + " quests! [Quest Level " + ChatColor.AQUA + plugin.getQuestLevel(quester) + ChatColor.YELLOW + "]");
						}
						
						//for every 10 quests give them a random 10 blocks!
						if( ( quester.getQuestsCompleted() % plugin.questRewardInterval ) == 0){
							Random numberGen = new Random();
							int itemNumberatInterValReward = numberGen.nextInt( plugin.getQuestRewards().length );
							try{
								player.getInventory().addItem(new ItemStack(Integer.parseInt(plugin.getQuestRewards()[itemNumberatInterValReward][0]), Integer.parseInt(plugin.getQuestRewards()[itemNumberatInterValReward][2])));
								plugin.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + " got a reward of " + ChatColor.DARK_PURPLE + plugin.getQuestRewards()[itemNumberatInterValReward][2] + " " + plugin.getQuestRewards()[itemNumberatInterValReward][1] + ChatColor.YELLOW + "!");
							}catch(NumberFormatException nfe){
									System.out.println("[" + plugin.pdfFile.getName() + "] Invalid quest reward item ID! Giving them dirt by default!");
									plugin.getServer().broadcastMessage(ChatColor.RED + "There was an invalid item ID in the quest rewards config! so you get 10 dirt!");
									player.getInventory().addItem(new ItemStack(Material.DIRT, 10));
									plugin.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + " got a reward of " + ChatColor.DARK_PURPLE + "10 Dirt" + ChatColor.YELLOW + "!");
								}
						}
						
						//Tell the server for every x quests that the difficulty increased!
						if( (quester.getQuestsCompleted() % plugin.questLevelInterval ) == 0){
							//plugin.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + " has completed " + ChatColor.DARK_PURPLE + quester.getQuestsCompleted() + ChatColor.YELLOW + " quests!");
							plugin.getServer().broadcastMessage(ChatColor.GOLD + player.getName() + ChatColor.RED + " is now on quest level " + ChatColor.DARK_RED + plugin.getQuestLevel(quester));
						}
						
						//set them to having no active quest
						quester.setQuestID(-1);
						//set them to having no quest progress thingy stored
						quester.setQuestTracker(0);
						//save them to file
						plugin.saveQuesterToFile(quester);
					} else {
						//the quest isn't done
						player.sendMessage(ChatColor.RED + "Your quest isn't done! Type: /uQuest info");
					}
				}
			}
			if(plugin.isUseSQLite() == true){
				plugin.getDB().put(player.getName(), quester);
			}
		}
		}
		catch(ArrayIndexOutOfBoundsException aiobe){
			/*TODO permissionsif(player.canUseCommand("/questme")){*
			player.sendMessage(ChatColor.LIGHT_PURPLE + "uQuest is a simple random quest plugin. How to use it:");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "You can also use /quest or /q instead of /uQuest");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "-> Commands:");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest (?/help)" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows this help menu");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest stats" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows your stored info. Try it!");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest amount" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows the amount of loaded quests");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest give" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Gives you a random quest");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest done" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Attempts to turn in your current quest");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest info" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Resends you your quest info/progress");
			//player.sendMessage(ChatColor.LIGHT_PURPLE + "   ->" + ChatColor.GREEN + "/uquest top #" + ChatColor.WHITE + "  |  " + ChatColor.BLUE + "Shows you the top 5 questers");
			}	
		//}
		//end of /questme prefix
    }else{
    	player.sendMessage(ChatColor.RED + "You need to register on the forums to use that!");
    }
	}
	}
}*/