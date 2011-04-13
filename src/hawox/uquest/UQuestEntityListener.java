package hawox.uquest;

import java.util.concurrent.TimeUnit;

import hawox.uquest.questclasses.LoadedQuest;

import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

/**
 * Handle events for all Player related events
 */
public class UQuestEntityListener extends EntityListener {
    private final UQuest plugin;

    public UQuestEntityListener(UQuest instance) {
        plugin = instance;
    }
    
    
    @Override
    public void onEntityDamage(EntityDamageEvent event){
    	if(event.isCancelled())
    		return;
    	Entity damager = null;
    	Creature creature = null;
    	Player damagedPlayer = null;
    	Player player = null;
    	if (event instanceof EntityDamageByEntityEvent)
          damager = ((EntityDamageByEntityEvent)event).getDamager();
    	if(event.getEntity() instanceof Creature)
    		creature = (Creature)event.getEntity();
    	if( (creature != null) && (damager != null)){
    		if(damager instanceof Player)
        		player = (Player) damager;
    		if(player != null){
    			//At this point a player has damaged a monster
    			if(event.getDamage() >= creature.getHealth()){
    				//for some reason mob deaths count multiple times if killed the right way. Added this list so we only count it once!
    				String id = Integer.toString(creature.getEntityId());
    				if(!(plugin.mobsKilled.contains(id))){
    					plugin.mobsKilled.add(id);
        				playerKilledCreature(player, creature);
    				}
    			}
    		}
    	}
    	//See if it's a player killed
    	if(event.getEntity() instanceof Player)
    		damagedPlayer = (Player)event.getEntity();
    	if( (damagedPlayer != null) && (damager != null)){
    		if(damager instanceof Player)
        		player = (Player) damager;
    		if(player != null){
    			//At this point a player has damaged a player
    			if(event.getDamage() >= damagedPlayer.getHealth()){
    				//for some reason mob deaths count multiple times if killed the right way. Added this list so we only count it once!
    				String id = Integer.toString(damagedPlayer.getEntityId());
    				if(!(plugin.mobsKilled.contains(id))){
    					addToMobList(id);
        				playerKilledPlayer(player, damagedPlayer);
    				}
    			}
    		}
    	}
    }
    //skeleton, pig, sheep, cow, chicken, squid, spider, zombie, creeper, slime, ghast, giant, zombie pigman 
    public void playerKilledCreature(Player player, Creature creature){
    	if(plugin.isEnabled() == true){
        	//get our quester
        	Quester quester = plugin.getQuestInteraction().getQuester(player);
    		//get the players current quest as well if they have one
    		if(quester.getQuestID() != -1){  
    			LoadedQuest loadedQuest = plugin.theQuests.get(quester.getQuestID());
//    			if(loadedQuest.checkType("kill")){
    				//check if the monster they killed in the one they needed
    				if( (creature instanceof Skeleton) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","skeleton"))){
    					quester.addToTracker(plugin, "skeleton", 1);
    				}
    				if( (creature instanceof Pig) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","pig"))){
    					quester.addToTracker(plugin, "pig", 1);
    					//system.out.println("Pig killed!");
    				}
    				if( (creature instanceof Sheep) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","sheep"))){
    					quester.addToTracker(plugin, "sheep", 1);
    				}
    				if( (creature instanceof Cow) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","cow"))){
    					quester.addToTracker(plugin, "cow", 1);
    				}
    				if( (creature instanceof Chicken) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","chicken"))){
    					quester.addToTracker(plugin, "chicken", 1);
    				}
    				if( (creature instanceof Squid) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","squid"))){
    					quester.addToTracker(plugin, "squid", 1);
    				}
    				if( (creature instanceof Spider) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","spider"))){
    					quester.addToTracker(plugin, "spider", 1);
    				}
    				if( (creature instanceof Zombie) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","zombie"))){
    					quester.addToTracker(plugin, "zombie", 1);
    				}
    				if( (creature instanceof Creeper) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","creeper"))){
    					quester.addToTracker(plugin, "creeper", 1);
    				}
    				if( (creature instanceof Slime) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","slime"))){
    					quester.addToTracker(plugin, "slime", 1);
    				}
    				if( (creature instanceof Ghast) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","ghast"))){
    					quester.addToTracker(plugin, "ghast", 1);
    				}
    				if( (creature instanceof Giant) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","giant"))){
    					quester.addToTracker(plugin, "giant", 1);
    				}
    				if( (creature instanceof PigZombie) && (loadedQuest.checkObjective(plugin, player.getLocation(),"kill","pigzombie"))){
    					quester.addToTracker(plugin, "pigzombie", 1);
    				}
//    			}
    			if(plugin.isUseSQLite() == true){
        			plugin.getDB().put(player.getName(), quester);
        		}
    		}
        }
    }
    
    public void playerKilledPlayer(Player player, Player damagedPlayer){
    	if(plugin.isEnabled() == true){
        	//get our quester
        	Quester quester = plugin.getQuestInteraction().getQuester(player);
    		//get the players current quest as well if they have one
    		if(quester.getQuestID() != -1){  
    			LoadedQuest loadedQuest = plugin.theQuests.get(quester.getQuestID());
//    			if(loadedQuest.checkType("kill")){
    				//check if the monster they killed in the one they needed
    				if(loadedQuest.checkObjective(plugin, player.getLocation(),"kill","player")){
    					quester.addToTracker(plugin, "player", 1);
    				}
//    			}
    			if(plugin.isUseSQLite() == true){
        			plugin.getDB().put(player.getName(), quester);
        		}
    		}
        }
    }
    
    
    
    
    //Add's ID to the list and then removes it after a set time
    public void addToMobList(String id){
		if(!(plugin.mobsKilled.contains(id))){
			plugin.mobsKilled.add(id);
			plugin.getMobList_Timer().schedule(new Runnable() {
				public void run() {
					if(!(plugin.getMobsKilled().isEmpty())){
						//remove it from the list
						plugin.getMobsKilled().remove(0);
					}
				}
			}, 1, TimeUnit.MINUTES);
		}
    	
    }
}