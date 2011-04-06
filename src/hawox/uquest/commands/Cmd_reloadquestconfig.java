package hawox.uquest.commands;


import hawox.uquest.UQuest;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_reloadquestconfig implements CommandExecutor{
	private final UQuest plugin;
	
	public Cmd_reloadquestconfig(UQuest plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		boolean process = false;
		if(sender instanceof Player){
			player = (Player)sender;
			}else{
				//Console can use this command
				process = true;
			}

		if(player != null){
			if(plugin.isUsePermissions() == true){
				if(UQuest.Permissions.has(player, "uQuest.CanReloadQuestConfig")){
					process = true;
				}
			}//Ops can use it too! Just in case we're not using permissions.
			if(player.isOp()){
				process = true;
			}
		}
		
		//Actual command starts here
		if(process == true){
			plugin.readConfig();
			
			sender.sendMessage("[" + plugin.getPdfFile().getName() + "] I hope you didn't change anything under 'Database' or 'PluginSupport'!!!");
			sender.sendMessage("[" + plugin.getPdfFile().getName() + "] These will not reconfigure mid runtime and may cause UNDESIRED RESULTS!!!");
			
			sender.sendMessage("[" + plugin.getPdfFile().getName() + "] uQuest's config has been reloaded.");
			
			if(player != null){
				System.out.println("[" + plugin.getPdfFile().getName() + "] " + player.getName() + " reloaded uQuest's config.");
			}
	    }else{
	    	player.sendMessage(ChatColor.RED + "You don't have permission to use that!");
	    }
		return true;
	}
}