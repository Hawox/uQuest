package hawox.uquest;

import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;
import com.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;

import cosine.boseconomy.BOSEconomy;

/**
 * Setup plugin support
 */
public class PluginSupport{

    private final UQuest plugin;
	
	public PluginSupport(UQuest plugin) {
		this.plugin = plugin;
		
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
	
	//Makes sure all of our supported plugins are loaded and accounted for
	public void checkPluginSupport(){
		if(plugin.isUseiConomy())
			checkiCon();
		if(plugin.isUsePermissions())
			checkPerm();
		if(plugin.isUseBOSEconomy())
			checkBOSE();
		if(plugin.isUseEssentials())
			checkEssentials();
	}

	public void checkiCon(){	
		boolean test = (UQuest.getiConomy() != null);
		if (test == false) {
			plugin.log.log(Level.SEVERE, UQuest.pluginNameBracket() + " iConomy is not loaded. Turning iConomy support off.");
			plugin.setUseiConomy(false);
		}
	}
	
	public void checkPerm(){	
		boolean test = (UQuest.getPermissions() != null);
		if (test == false) {
			plugin.log.log(Level.SEVERE, UQuest.pluginNameBracket() + " Permissions is not loaded. Turning Permissions support off.");
			plugin.setUsePermissions(false);
		}
	}
	
	public void checkBOSE(){	
		boolean test = (UQuest.getBOSEconomy() != null);
		if (test == false) {
			plugin.log.log(Level.SEVERE, UQuest.pluginNameBracket() + " BOSEconomy is not loaded. Turning BOSEconomy support off.");
			plugin.setUseBOSEconomy(false);
		}
	}
	
	public void checkEssentials(){	
		boolean test = (UQuest.getEssentials() != null);
		if (test == false) {
			plugin.log.log(Level.SEVERE, UQuest.pluginNameBracket() + " Essentials is not loaded. Turning Essentials support off.");
			plugin.setUseEssentials(false);
		}
	}
}