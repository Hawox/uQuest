package hawox.uquest;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.nijiko.coelho.iConomy.iConomy;

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
                    System.out.println("[Hawox's uQuest] Successfully linked with iConomy.");
                }
            }
        }
    }
}