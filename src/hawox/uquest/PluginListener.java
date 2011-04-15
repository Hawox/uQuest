package hawox.uquest;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;

import cosine.boseconomy.BOSEconomy;

/**
 * Checks for plugins whenever one is enabled
 */
public class PluginListener extends ServerListener {
    public PluginListener() { }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if(UQuest.getiConomy() == null) {
            Plugin iConomy = UQuest.getBukkitServer().getPluginManager().getPlugin("iConomy");

            if (iConomy != null) {
                if(iConomy.isEnabled()) {
                    UQuest.setiConomy((iConomy)iConomy);
                    System.out.println("[uQuest] Successfully linked with iConomy.");
                }
            }
        }
        
        if(UQuest.getPermissions() == null){
    		Plugin Permissions = UQuest.getBukkitServer().getPluginManager().getPlugin("Permissions");

    		if (Permissions != null) {
    			if(Permissions.isEnabled()){
    				UQuest.setPermissions(((Permissions) Permissions).getHandler());
                    System.out.println("[uQuest] Successfully linked with Permissions.");
    			}
    		}
        }
        
        if(UQuest.getBOSEconomy() == null){
    	    Plugin BOSEconomy = UQuest.getBukkitServer().getPluginManager().getPlugin("BOSEconomy");

    	    if (BOSEconomy != null) {
    			if(BOSEconomy.isEnabled()){
    				UQuest.setBOSEconomy((BOSEconomy) BOSEconomy);
                    System.out.println("[uQuest] Successfully linked with BOSEconomy.");
    			}
    		}
        }
        
        if(UQuest.getEssentials() == null){
    	    Plugin Essentials = UQuest.getBukkitServer().getPluginManager().getPlugin("Essentials");

    	    if (Essentials != null) {
    			if(Essentials.isEnabled()){
    				UQuest.setEssentials((Essentials) Essentials);
                    System.out.println("[uQuest] Successfully linked with Essentials.");
    			}
    		}
        }
        
    }
}