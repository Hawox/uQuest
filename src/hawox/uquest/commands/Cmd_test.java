package hawox.uquest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Cmd_test implements CommandExecutor{
//	private final UQuest plugin;
	
//	public Cmd_test(UQuest plugin){
//		this.plugin = plugin;
//	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//		Player player = null;
//		if(sender instanceof Player){
//			player = (Player)sender;
//			}//else{
				//sender.sendMessage("[Hawox's uQuest] This is not ment for console use silly!");
				//return true;
			//}

		/*
09:12:33 [INFO] Below me.
09:12:34 [INFO] !!hawox.uquest.questclasses.LoadedQuest
finishInfo: Quest finshed SuperrawrFace
name: TheQuest Name
objectives:
- amountNeeded: 3
  amountToConsume: 0
  displayname: Pigs Killed
  itemID: 0
  locationGiveRange:
    pitch: 0.0
    world: &id001 !!org.bukkit.craftbukkit.CraftWorld {fullTime: 26615, time: 26
15}
    x: 5.0
    y: 8.0
    yaw: 0.0
    z: 10.0
  locationNeeded:
    pitch: 13.366815
    world: *id001
    x: 26.30000001192093
    y: 64.0
    yaw: 5.9858456
    z: -40.61428283192512
  monsterTypeID: pig
  objectiveName: pig
  type: kill
rewards:
- {howMuch: 20, item: null, type: money}
- howMuch: 10
  item: {amount: 10, data: null, durability: 0, type: LOG, typeId: 17}
  type: item
startInfo: Start intfo rawr
types: null

09:12:34 [INFO] Above me.
		 */
		/*
		LoadedQuest quest = new LoadedQuest(plugin);
		quest.setStartInfo("Start intfo rawr");
		quest.setName("TheQuest Name");
		quest.setFinishInfo("Quest finshed SuperrawrFace");
			Reward reward1 = new Reward(plugin, "money", "CoinThings", 20);
			Reward reward2 = new Reward(plugin, "item", "Log", 17, 10);
		quest.setrewards(new Reward[]{ reward1, reward2});

			Objective ob1 = new Objective("kill", "Pigs Killed", "pig", 3, 0, player.getLocation(), new Location(player.getWorld(),5,8,10));
		quest.setObjectives(new Objective[]{ ob1 });*/
		
		
		
		
		/*System.out.println("Below me.");
		System.out.println(plugin.getYaml().dump(quest));
		System.out.println("Above me.");
		System.out.println(plugin.getYaml().dump(reward1));
		
		
		System.out.println("\n\n\n\n\n\n\n\n");
		System.out.println(plugin.getYaml().load(plugin.getReaderQuests()));
				System.out.println("\n\n\n\n\n\n\n\n");

		System.out.println("Below me.");
		quest = (LoadedQuest) plugin.getYaml().load(plugin.getReaderQuests());
		System.out.println(plugin.getYaml().dump(quest));
		System.out.println("Above me.");*/
		
		/*HashMap<String,Object> testMap = new HashMap<String,Object>();
		
		testMap.put("The Objective", ob1);

		System.out.println("Below me.");
		System.out.println(plugin.getYaml().dump(testMap));
		System.out.println("Above me.");*/
		
	//	plugin.getQuestloader().test();
		
		
		
		return true;
		/*
		 * finishInfo: Quest finished SuperrawrFace
name: TheQuest Name
objectives:
- amountNeeded: 3
  amountToConsume: 0
  displayname: Pigs Killed
  itemID: 0
  locationGiveRange:
    pitch: 0.0
    world: &id001 !!org.bukkit.craftbukkit.CraftWorld {fullTime: 65470, time: 17
470}
    x: 5.0
    y: 8.0
    yaw: 0.0
    z: 10.0
  locationNeeded:
    pitch: 55.41725
    world: *id001
    x: 24.580162461770122
    y: 66.0
    yaw: -46.14427
    z: -45.20019545566997
  monsterTypeID: pig
  objectiveName: pig
  type: kill
rewards:
- {howMuch: 20, item: null, type: money}
- howMuch: 10
  item: {amount: 10, data: null, durability: 0, type: LOG, typeId: 17}
  type: item
startInfo: Start intfo rawr
types: null
		 */
	}

}
