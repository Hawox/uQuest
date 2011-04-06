package hawox.uquest.commands;


import hawox.uquest.UQuest;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_reloadquests implements CommandExecutor{
	private final UQuest plugin;
	
	public Cmd_reloadquests(UQuest plugin){
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
				if(UQuest.Permissions.has(player, "uQuest.CanReloadQuests")){
					process = true;
				}
			}//Ops can use it too! Just incase we;re not unsing permissions.
			if(player.isOp()){
				process = true;
			}
		}
		
		//Actual command studd starts here
		if(process == true){
			int questsBefore = plugin.getQuestInteraction().getQuestTotal();
			plugin.theQuestsLoadAllIntoArray();
			int questsAfter = plugin.getQuestInteraction().getQuestTotal();
			
			sender.sendMessage("[" + plugin.getPdfFile().getName() + "] uQuest's quest list has been reloaded.");
			sender.sendMessage("[" + plugin.getPdfFile().getName() + "] Total before: " + Integer.toString(questsBefore) + " | Total After: " + Integer.toString(questsAfter) );
			
			if(player != null){
				System.out.println("[" + plugin.getPdfFile().getName() + "] " + player.getName() + " reloaded uQuest's quests.");
				System.out.println("[" + plugin.getPdfFile().getName() + "] Total before: " + Integer.toString(questsBefore) + " | Total After: " + Integer.toString(questsAfter) );
			}
	    }else{
	    	player.sendMessage(ChatColor.RED + "You don't have permission to use that!");
	    }
		return true;
	}
}