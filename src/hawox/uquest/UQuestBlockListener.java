package hawox.uquest;

import hawox.uquest.questclasses.LoadedQuest;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Sample block listener
 */
public class UQuestBlockListener extends BlockListener {
    private final UQuest plugin;

    public UQuestBlockListener(final UQuest plugin) {
        this.plugin = plugin;
    }

    @Override
	public void onBlockBreak(BlockBreakEvent event){
    	if(plugin.isEnabled() == true){
    	Block block = event.getBlock();
    	Player player = event.getPlayer();
		if(!event.isCancelled()){
			blockCheckQuest(player, block, "blockdestroy", 1);
		}
    	}
    }
    
    @Override
    public void onBlockDamage(BlockDamageEvent event) {
    	if(plugin.isEnabled() == true){
    	Block block = event.getBlock();
    	Player player = event.getPlayer();
		    //Block Destroyed Event
			if(!event.isCancelled()){
				blockCheckQuest(player, block, "blockdamage", 1);
				}
    	}
    }
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
    	if(plugin.isEnabled() == true){
    	Block block = event.getBlock();
    	Player player = event.getPlayer();
    	blockCheckQuest(player, block, "blockplace", 1);
    	}
    }
    
    
    public void blockCheckQuest(Player player, Block block, String type, int incressBy){
    	if(plugin.isEnabled() == true){
    	//get our quester
    	Quester quester = plugin.getQuestInteraction().getQuester(player);
		//get the players current quest as well if they have one
		if(quester.getQuestID() != -1){
			//check if the block they (did 'type' to) is the one they need
			LoadedQuest loadedQuest = plugin.theQuests.get(quester.getQuestID());
			String objectiveName = Integer.toString(block.getTypeId());
			if(loadedQuest.checkObjective(plugin, player.getLocation(), type, objectiveName)){
				//Awesome! Increase their broken blocks!
				quester.addToTracker(plugin, objectiveName, 1);
			}
		}
		if(plugin.isUseSQLite() == true){
			plugin.getDB().put(player.getName(), quester);
		}
    	}
    }
    
 
    
    
}