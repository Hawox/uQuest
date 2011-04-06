package hawox.uquest;

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
    				if( (creature instanceof Skeleton) && (loadedQuest.checkObjective("kill","skeleton"))){
    					quester.addToTracker(plugin, "skeleton", 1);
    				}
    				if( (creature instanceof Pig) && (loadedQuest.checkObjective("kill","pig"))){
    					quester.addToTracker(plugin, "pig", 1);
    					//system.out.println("Pig killed!");
    				}
    				if( (creature instanceof Sheep) && (loadedQuest.checkObjective("kill","sheep"))){
    					quester.addToTracker(plugin, "sheep", 1);
    				}
    				if( (creature instanceof Cow) && (loadedQuest.checkObjective("kill","cow"))){
    					quester.addToTracker(plugin, "cow", 1);
    				}
    				if( (creature instanceof Chicken) && (loadedQuest.checkObjective("kill","chicken"))){
    					quester.addToTracker(plugin, "chicken", 1);
    				}
    				if( (creature instanceof Squid) && (loadedQuest.checkObjective("kill","squid"))){
    					quester.addToTracker(plugin, "squid", 1);
    				}
    				if( (creature instanceof Spider) && (loadedQuest.checkObjective("kill","spider"))){
    					quester.addToTracker(plugin, "spider", 1);
    				}
    				if( (creature instanceof Zombie) && (loadedQuest.checkObjective("kill","zombie"))){
    					quester.addToTracker(plugin, "zombie", 1);
    				}
    				if( (creature instanceof Creeper) && (loadedQuest.checkObjective("kill","creeper"))){
    					quester.addToTracker(plugin, "creeper", 1);
    				}
    				if( (creature instanceof Slime) && (loadedQuest.checkObjective("kill","slime"))){
    					quester.addToTracker(plugin, "slime", 1);
    				}
    				if( (creature instanceof Ghast) && (loadedQuest.checkObjective("kill","ghast"))){
    					quester.addToTracker(plugin, "ghast", 1);
    				}
    				if( (creature instanceof Giant) && (loadedQuest.checkObjective("kill","giant"))){
    					quester.addToTracker(plugin, "giant", 1);
    				}
    				if( (creature instanceof PigZombie) && (loadedQuest.checkObjective("kill","pigzombie"))){
    					quester.addToTracker(plugin, "pigzombie", 1);
    				}
//    			}
    			if(plugin.isUseSQLite() == true){
        			plugin.getDB().put(player.getName(), quester);
        		}
    		}
        }
    }
}