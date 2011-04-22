package hawox.uquest;

import hawox.uquest.commands.Cmd_reloadquestconfig;
import hawox.uquest.commands.Cmd_reloadquests;
import hawox.uquest.commands.Cmd_uquest;
import hawox.uquest.questclasses.LoadedQuest;
import hawox.uquest.questclasses.QuestConverter;
import hawox.uquest.questclasses.QuestLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import sqLiteStor.SqLiteKeyValStor;

import com.earth2me.essentials.Essentials;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;

import cosine.boseconomy.BOSEconomy;

/**
 * @author Hawox
 */
public class UQuest extends JavaPlugin {
	
    //Plugin basics
	private PluginDescriptionFile pdfFile;
	public Logger log;
    private static Server Server = null;
    
	//listeners
	private final UQuestPlayerListener playerListener = new UQuestPlayerListener(this);
	private final UQuestBlockListener blockListener = new UQuestBlockListener(this);
	private final UQuestEntityListener entityListener = new UQuestEntityListener(this);
    private static PluginListener PluginListener = new PluginListener();
    private final QuestListener uQuestListener = new QuestListener();
    
    //Plugin support
    private String Money_Plugin = "none";
    private static iConomy iConomy = null;
    private static BOSEconomy BOSEconomy = null;
    private static Essentials Essentials = null;
	private static PermissionHandler Permissions = null;
    
    //Lists
	protected HashSet<Quester> theQuesterList = new HashSet<Quester>();						//Loaded players
	protected ArrayList<Quester>  theQuestersRanked = new ArrayList<Quester>();				//Loaded players sorted by rank
	protected ArrayList<LoadedQuest> theQuests = new ArrayList<LoadedQuest>();				//Loaded Quests
	protected HashSet<String> canNotDrop = new HashSet<String>();							//Players on quest drop cool down
	protected ArrayList<String> canNotDropRemoveTimer = new ArrayList<String>();			//Players on quest drop cool down
//	protected ArrayList<String> mobsKilled = new ArrayList<String>();						//Mob ID's counted as dead
	protected HashMap<Integer,String> mobsTagged = new HashMap<Integer,String>();						//Mob ID's tagged by players
	protected HashSet<String> playersLoggedInSinceBoot = new HashSet<String>();				//Names of players that have logged on since the server booted
	
	//Timers
	private ScheduledThreadPoolExecutor mobList_Timer = new ScheduledThreadPoolExecutor(50); //Resets mob/player id's so they can be killed again

	//Player storage
	protected iProperty questPlayerStorage;
	// stored player info will look like: PlayerName = QuestId:QuestsCompleted:MoneyEarnedFromQuests:questtracker
		// (-1 quest id means no active quest!)
	private String questDefaultPlayer = "-1:0:0:0,0";
	
	//Sqlite player storage
	private SqLiteKeyValStor<Quester> DB;

	//config file defaults
	private boolean hideQuestRewards = false;
	private boolean scaleQuestLevels = true;
	private boolean broadcastSaving = true;
	private boolean useiConomy = true;
	private boolean usePermissions = true;
	private boolean useSQLite = false;
	private boolean useDefaultUQuest = true;
	private boolean useBOSEconomy = false;
	private boolean useEssentials = false;
	private int SaveQuestersInfoIntervalInMinutes = 30;
	private int questAnnounceInterval = 5;
	private int questRewardInterval = 10;
	private int questLevelInterval = 50;
	private int dropQuestInterval = 60;
	private int dropQuestCharge = 5000;
	private int pluginTimerCheck = 5;
    private String moneyName = "Monies";
    private String questRewardsDefault = "87,Netherrack Blocks,10~88,Soul Sand Blocks,10~89,Glowstone Blocks,10~18,Leaf Blocks,10~344,Eggs,10~348,Glowstone Dust,10";
	private String[] questRewards = { "87,Netherrack Blocks,10",
			  "88,Soul Sand Blocks,10",
			  "89,Glowstone Blocks,10",
			  "18,Leaf Blocks,10",
			  "344,Eggs,10"};
	
	//create our general quest interaction methods (Try to have most of the API here)
	QuestInteraction questInteraction = new QuestInteraction(this);

	//stops bad things from happening when someone reloads the plugin
	boolean firstLoad = true;
	
	
	

	public void onDisable() {
		//save flat file db
		if(!(this.isUseSQLite()))
			saveQuesterListToFile();
		System.out.println(pluginNameBracket() + " disabled!");
	}

	public void onEnable() {
		//Basic plugin setup
		Server = getServer();
		log = Server.getLogger();
		setPdfFile(this.getDescription());

		//load up our files if they don't exist
		moveFiles();
		
		//Load our flat file player DB
		questPlayerStorage = new iProperty("plugins/uQuest/uQuest_Players.txt");
		
		//Check if we need to convert old uQuest v1 quests
		if(new File("plugins/uQuest/uQuest_Quests.txt").exists())
			new QuestConverter();
		
		//registerCommands
		if(isUseDefaultUQuest()){
			Cmd_uquest cmd_uquest = new Cmd_uquest(this);
			getCommand("uquest").setExecutor(cmd_uquest);
			getCommand("quest").setExecutor(cmd_uquest);
			getCommand("q").setExecutor(cmd_uquest);
			//getCommand("test").setExecutor(new Cmd_test(this));
		}
		
		//These commands exist even if they are not using the default uQuest
		Cmd_reloadquests cmd_reloadquests = new Cmd_reloadquests(this);
		getCommand("reloadquests").setExecutor(cmd_reloadquests);
		Cmd_reloadquestconfig cmd_reloadquestconfig = new Cmd_reloadquestconfig(this);
		getCommand("reloadquestconfig").setExecutor(cmd_reloadquestconfig);		
		
		// setup config
		readConfig();
		
		// load quests into array
		theQuestsLoadAllIntoArray();
		
		//Make sure we have quests loaded!
		if(this.theQuests.isEmpty()){
			System.err.println("\n\n\n" + pluginNameBracket() + " You have an empty quest list!\n Disabling plugin.\n\n\n");
			Server.getPluginManager().disablePlugin(this);
			return; //exit out of our enable method
		}
		
		//Get the DB fired up
		if(isUseSQLite() == true){
			this.setDB(new SqLiteKeyValStor<Quester>("questers", "plugins/uQuest/uQuestQuesters"));
			System.out.println(pluginNameBracket() + " Loaded with SQLite!");
		}
		
		// start the player saving timer
		if (firstLoad == true && isUseSQLite() == false) {
			timerSavePlayers();
			firstLoad = false;
			System.out.println(pluginNameBracket() + " Loaded with Flatfile!");
		}
		
		// Register Bukkit Hooks
		registerEvents();

		System.out.println(pluginNameBracket() + " v" + this.getPdfFile().getVersion() + " enabled! With " + this.getQuestInteraction().getQuestTotal() + " quests loaded!");
		
		//For iCon at least, it hooks in after the plugin enables. Solution: Timer!
		ScheduledThreadPoolExecutor onEnable_Timer = new ScheduledThreadPoolExecutor(1);
		onEnable_Timer.schedule(new Runnable() {
			public void run() {
				checkPluginSupport();
				}
			}, pluginTimerCheck, TimeUnit.SECONDS);
	}
	
	public void registerEvents() {
		// Register our events
		PluginManager pm = getServer().getPluginManager();

		// Used for plugin interaction
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, PluginListener, Priority.Monitor, this);
		
		// Player Stuff
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener,Priority.Normal, this);

		// Block Stuff
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener,Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener,Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener,Priority.Normal, this);
		
		// Entity Stuff
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, this.entityListener, Event.Priority.Normal, this);
		
		//custom!
		pm.registerEvent(Event.Type.CUSTOM_EVENT, this.uQuestListener, Event.Priority.Normal, this);
	}
	
	public void readConfig() {
		/* private boolean useDefaultUQuest = true;
		 * 
		 * Database:
		 * 		private boolean useSQLite = false;
		 * 		protected boolean broadcastSaving = true;
		 * 		protected int SaveQuestersInfoIntervalInMinutes = 30;
		 * 
		 * PluginSupport:
		 * 		private String MoneyPlugin = "none";
		 * 		>>>protected boolean useiConomy = true;
		 * 		private boolean usePermissions = true;
		 * 		>>>private boolean useEssentialsEco = false;
		 * 		>>>private boolean useBOSEconomy = false;
		 *      private String moneyName = "Monies";
		 * 
		 * QuestLevels:
		 * 		protected int questLevelInterval = 50;
		 * 		protected boolean scaleQuestLevels = true;
		 * 
		 * Announcements:
		 * 		protected int questAnnounceInterval = 5;
		 * 		protected int questRewardInterval = 10;
		 * 
		 * QuestDropping:
		 * 		protected int dropQuestInterval = 60;
		 * 		protected int dropQuestCharge = 5000;
		 * 
		 */
		Configuration config = new Configuration(new File(getDataFolder(), "config.yml"));
		config.load();
		useDefaultUQuest = config.getBoolean("etc.useDefaultUQuest", useDefaultUQuest);
		hideQuestRewards = config.getBoolean("etc.hideQuestRewards", hideQuestRewards);
			try{
				String[] questRewardsFromFile = config.getString("etc.questRewards", questRewardsDefault).split("~");
				setQuestRewards(questRewardsFromFile);
				//test every reward threw a meaningless loop to try and get the catch to run
				for(int i=0; i<questRewardsFromFile.length; i++){
					String rewards[] = questRewardsFromFile[i].split(",");
					rewards[0] = rewards[0];
					rewards[1] = rewards[1];
					rewards[2] = rewards[2];
				}
			} catch (ArrayIndexOutOfBoundsException aiobe) {
				log.log(Level.SEVERE, pluginNameBracket() + " Error setting up quest rewards! Fix the config file!");
				log.log(Level.SEVERE, pluginNameBracket() + " Quest item rewards are loaded as defaults!");
				setQuestRewards(questRewards);
			}

		useSQLite = config.getBoolean("Database.useSQLite", useSQLite);
		broadcastSaving = config.getBoolean("Database.broadcastSaving", broadcastSaving);
		SaveQuestersInfoIntervalInMinutes = config.getInt("Database.SaveQuestersInfoIntervalInMinutes", SaveQuestersInfoIntervalInMinutes);

		pluginTimerCheck = config.getInt("PluginSupport.pluginTimerCheck", pluginTimerCheck);
		usePermissions = config.getBoolean("PluginSupport.usePermissions", usePermissions);
		moneyName = config.getString("PluginSupport.moneyName", moneyName);
		
		//*** money plugin stuff ***//
		Money_Plugin = config.getString("PluginSupport.MoneyPlugin", "none");
		if(Money_Plugin.equalsIgnoreCase("iConomy"))
			this.useiConomy = true;
		if(Money_Plugin.equalsIgnoreCase("BOSEconomy"))
			this.useBOSEconomy = true;
		if(Money_Plugin.equalsIgnoreCase("Essentials"))
			this.useEssentials = true;

		questLevelInterval = config.getInt("QuestLevels.questLevelInterval", questLevelInterval);
		scaleQuestLevels = config.getBoolean("QuestLevels.scaleQuestLevels", scaleQuestLevels);
		
		questAnnounceInterval = config.getInt("Announcements.questAnnounceInterval", questAnnounceInterval);
		questRewardInterval = config.getInt("Announcements.questRewardInterval", questRewardInterval);
		
		dropQuestInterval = config.getInt("QuestDropping.dropQuestInterval", dropQuestInterval);
		dropQuestCharge = config.getInt("QuestDropping.dropQuestCharge", dropQuestCharge);

	}
	
	public void moveFiles(){
		getDataFolder().mkdir();
		getDataFolder().setWritable(true);
	    getDataFolder().setExecutable(true);
		extractFile("config.yml");
		extractFile("Quests.yml");
		extractFile("uQuest_Players.txt");
		extractFile("uQuestQuesters");
	}
	
	//Taken and modified from iCon
	private void extractFile(String name) {
		File actual = new File(getDataFolder(), name);
		if (!actual.exists()) {
			InputStream input = getClass().getResourceAsStream("/Default_Files/" + name);
			if (input != null) {
				FileOutputStream output = null;
				try
		        {
		          output = new FileOutputStream(actual);
		          byte[] buf = new byte[8192];
		          int length = 0;

		          while ((length = input.read(buf)) > 0) {
		            output.write(buf, 0, length);
		          }

		          System.out.println(pluginNameBracket() + " Default file written: " + name);
		        } catch (Exception e) {
		          e.printStackTrace();
		        } finally {
		          try {
		            if (input != null)
		              input.close();
		          }
		          catch (Exception e) {
		          }
		          try {
		            if (output != null)
		              output.close();
		          }
		          catch (Exception e)
		          {
		          }
		        }
			}
		}
	}
	  
	//Makes sure all of our supported plugins are loaded and accounted for
	public void checkPluginSupport(){
		if(this.useiConomy)
			checkiCon();
		if(this.usePermissions)
			checkPerm();
		if(this.useBOSEconomy)
			checkBOSE();
		if(this.useEssentials)
			checkEssentials();
	}

	public void checkiCon(){	
		boolean test = this.useiConomy = (iConomy != null);
		if (test == false) {
			log.log(Level.SEVERE, pluginNameBracket() + " iConomy is not loaded. Turning iConomy support off.");
			log.log(Level.SEVERE, pluginNameBracket() + " If this is not correct, change the config 'pluginTimerCheck' to a higher value.");
			this.useiConomy = false;
		}
	}
	
	public void checkPerm(){	
		boolean test = this.usePermissions = (Permissions != null);
		if (test == false) {
			log.log(Level.SEVERE, pluginNameBracket() + " Permissions is not loaded. Turning Permissions support off.");
			log.log(Level.SEVERE, pluginNameBracket() + " If this is not correct, change the config 'pluginTimerCheck' to a higher value.");
			this.usePermissions = false;
		}
	}
	
	public void checkBOSE(){	
		boolean test = this.useBOSEconomy = (BOSEconomy != null);
		if (test == false) {
			log.log(Level.SEVERE, pluginNameBracket() + " BOSEconomy is not loaded. Turning BOSEconomy support off.");
			log.log(Level.SEVERE, pluginNameBracket() + " If this is not correct, change the config 'pluginTimerCheck' to a higher value.");
			this.useBOSEconomy = false;
		}
	}
	
	public void checkEssentials(){	
		boolean test = this.useEssentials = (Essentials != null);
		if (test == false) {
			log.log(Level.SEVERE, pluginNameBracket() + " Essentials is not loaded. Turning Essentials support off.");
			this.useEssentials = false;
		}
	}



	public void timerSavePlayers() {
		ScheduledThreadPoolExecutor saveQuestersToFile_timer = new ScheduledThreadPoolExecutor(
				20);
		saveQuestersToFile_timer.schedule(new Runnable() {
			public void run() {
				saveQuesterListToFile();
				timerSavePlayers();
			}
		}, SaveQuestersInfoIntervalInMinutes, TimeUnit.MINUTES);
	}

	public void theQuestsLoadAllIntoArray() {
		this.theQuests = new QuestLoader(this).loadAllQuests();
	}
	
	public void saveQuesterToFile(Quester quester){
		questPlayerStorage.setString(quester.theQuestersName, quester.toString());
	}

	public boolean saveQuesterListToFile() {
		if (broadcastSaving == true) {
			getServer().broadcastMessage(
					"[Hawox uQuest] Saving all players quests to file...");
		}
		System.out.println("[Hawox uQuest] Saving all players quests to file...");
		for(Quester q : theQuesterList)
			questPlayerStorage.setString(q.theQuestersName, q.toString());
		if (broadcastSaving == true) {
			getServer().broadcastMessage("[Hawox uQuest] Done saving.");
		}
		System.out.println("[Hawox uQuest] Done saving.");
		return true;
	}

	public void placePlayerIntoList(Player player) {
		// check if player is already in the quester list
		boolean wasInList = false;
		for (Quester quester : theQuesterList) {
			if (quester.theQuestersName.equalsIgnoreCase(player.getName())) {
				wasInList = true;
			}
		}
		if (wasInList == false) { // needs to be added
			Quester q = null;
			if (!(questPlayerStorage.keyExists(player.getName()))) {
				questPlayerStorage.setString(player.getName(),questDefaultPlayer);
				q = new Quester(questDefaultPlayer.split(":"), player);
			}
			q = new Quester(questPlayerStorage.getString(player.getName()).split(":"), player);
			theQuesterList.add(q);
			placePlayerIntoRankedList(q);
		}
	}
	
	public boolean placePlayerIntoRankedList(Quester q) {
		if (!(theQuestersRanked.contains(q))) {
			theQuestersRanked.add(q);
			return true;
		}
		return false;
	}

	public String arrayToString(String[] stringToChange) {
		// take each part of the array and separate it with ':'s
		String stringToReturn = stringToChange[0];
		try {
			// start with the first index on the array
			for (int i = 1; i < stringToChange.length; i++) {
				stringToReturn = stringToReturn.concat(":");
				stringToReturn = stringToReturn.concat(stringToChange[i]);
			}
			return stringToReturn;
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			getServer().broadcastMessage("There was a problem converting an array to a string.");
			log.log(Level.SEVERE, "There was a problem converting an array to a string.");
			return null;
		}
	}

	public String pluginNameBracket(){
		return ("[" + this.getPdfFile().getName() + "]");
	}
	

	//Plugin support getters and setters
    public static iConomy getiConomy() {
        return iConomy;
    }
    
    public static boolean setiConomy(iConomy plugin) {
        if (iConomy == null) {
            iConomy = plugin;
        } else {
            return false;
        }
        return true;
    }
    
	public static void setPermissions(PermissionHandler permissions) {
		Permissions = permissions;
	}

	public static PermissionHandler getPermissions() {
		return Permissions;
	}
    
	public static BOSEconomy getBOSEconomy() {
		return BOSEconomy;
	}

	public static void setBOSEconomy(BOSEconomy bOSEconomy) {
		BOSEconomy = bOSEconomy;
	}

	
	public static Essentials getEssentials() {
		return Essentials;
	}

	public static void setEssentials(Essentials essentials) {
		Essentials = essentials;
	}

	//Getters and Setters
	public ArrayList<LoadedQuest> getTheQuests() {
		return theQuests;
	}

	public HashSet<Quester> getTheQuesterList() {
		return theQuesterList;
	}

	public void setTheQuesterList(HashSet<Quester> theQuesterList) {
		this.theQuesterList = theQuesterList;
	}

	public void setTheQuests(ArrayList<LoadedQuest> theQuests) {
		this.theQuests = theQuests;
	}

	public iProperty getQuestPlayerStorage() {
		return questPlayerStorage;
	}

	public void setQuestPlayerStorage(iProperty questPlayerStorage) {
		this.questPlayerStorage = questPlayerStorage;
	}

	public boolean isScaleQuestLevels() {
		return scaleQuestLevels;
	}

	public void setScaleQuestLevels(boolean scaleQuestLevels) {
		this.scaleQuestLevels = scaleQuestLevels;
	}

	public boolean isBroadcastSaving() {
		return broadcastSaving;
	}

	public void setBroadcastSaving(boolean broadcastSaving) {
		this.broadcastSaving = broadcastSaving;
	}

	public boolean isUseiConomy() {
		return useiConomy;
	}

	public void setUseiConomy(boolean useiConomy) {
		this.useiConomy = useiConomy;
	}

	public int getSaveQuestersInfoIntervalInMinutes() {
		return SaveQuestersInfoIntervalInMinutes;
	}

	public void setSaveQuestersInfoIntervalInMinutes(
			int saveQuestersInfoIntervalInMinutes) {
		SaveQuestersInfoIntervalInMinutes = saveQuestersInfoIntervalInMinutes;
	}

	public int getQuestAnnounceInterval() {
		return questAnnounceInterval;
	}

	public void setQuestAnnounceInterval(int questAnnounceInterval) {
		this.questAnnounceInterval = questAnnounceInterval;
	}

	public int getQuestRewardInterval() {
		return questRewardInterval;
	}

	public void setQuestRewardInterval(int questRewardInterval) {
		this.questRewardInterval = questRewardInterval;
	}

	public int getQuestLevelInterval() {
		return questLevelInterval;
	}

	public void setQuestLevelInterval(int questLevelInterval) {
		this.questLevelInterval = questLevelInterval;
	}

	public String getQuestDefaultPlayer() {
		return questDefaultPlayer;
	}

	public void setQuestDefaultPlayer(String questDefaultPlayer) {
		this.questDefaultPlayer = questDefaultPlayer;
	}

	public void setUseSQLite(boolean useSQLite) {
		this.useSQLite = useSQLite;
	}

	public boolean isUseSQLite() {
		return useSQLite;
	}

	public void setDB(SqLiteKeyValStor<Quester> dB) {
		DB = dB;
	}

	public SqLiteKeyValStor<Quester> getDB() {
		return DB;
	}

	public void setQuestRewards(String[] loadedInfo) {
		this.questRewards = loadedInfo;
	}

	public String[] getQuestRewards() {
		return questRewards;
	}

	public void setUsePermissions(boolean usePermissions) {
		this.usePermissions = usePermissions;
	}

	public boolean isUsePermissions() {
		return usePermissions;
	}
	
	public static Server getBukkitServer() {
        return Server;
    }

	public QuestInteraction getQuestInteraction() {
		return questInteraction;
	}

	public boolean isUseDefaultUQuest() {
		return useDefaultUQuest;
	}

	public void setUseDefaultUQuest(boolean useDefaultUQuest) {
		this.useDefaultUQuest = useDefaultUQuest;
	}

	public void setQuestInteraction(QuestInteraction questInteraction) {
		this.questInteraction = questInteraction;
	}

	public boolean isUseBOSEconomy() {
		return useBOSEconomy;
	}

	public void setUseBOSEconomy(boolean useBOSEconomy) {
		this.useBOSEconomy = useBOSEconomy;
	}

	public int getDropQuestInterval() {
		return dropQuestInterval;
	}

	public void setDropQuestInterval(int dropQuestInterval) {
		this.dropQuestInterval = dropQuestInterval;
	}

	public int getDropQuestCharge() {
		return dropQuestCharge;
	}

	public void setDropQuestCharge(int dropQuestCharge) {
		this.dropQuestCharge = dropQuestCharge;
	}

	public HashSet<String> getCanNotDrop() {
		return canNotDrop;
	}

	public void setCanNotDrop(HashSet<String> canNotDrop) {
		this.canNotDrop = canNotDrop;
	}

	public String getMoneyName() {
		return moneyName;
	}

	public void setMoneyName(String moneyName) {
		this.moneyName = moneyName;
	}

	public ArrayList<String> getCanNotDropRemoveTimer() {
		return canNotDropRemoveTimer;
	}

	public void setCanNotDropRemoveTimer(ArrayList<String> canNotDropRemoveTimer) {
		this.canNotDropRemoveTimer = canNotDropRemoveTimer;
	}

/*	public ArrayList<String> getMobsKilled() {
		return mobsKilled;
	}

	public void setMobsKilled(ArrayList<String> mobsKilled) {
		this.mobsKilled = mobsKilled;
	}*/

	public void setPdfFile(PluginDescriptionFile pdfFile) {
		this.pdfFile = pdfFile;
	}

	public PluginDescriptionFile getPdfFile() {
		return pdfFile;
	}

	public ScheduledThreadPoolExecutor getMobList_Timer() {
		return mobList_Timer;
	}

	public void setMobList_Timer(ScheduledThreadPoolExecutor mobList_Timer) {
		this.mobList_Timer = mobList_Timer;
	}

	public Logger getLog() {
		return log;
	}

	public void setLog(Logger log) {
		this.log = log;
	}

	public static PluginListener getPluginListener() {
		return PluginListener;
	}

	public static void setPluginListener(PluginListener pluginListener) {
		PluginListener = pluginListener;
	}

	public String getMoney_Plugin() {
		return Money_Plugin;
	}

	public void setMoney_Plugin(String money_Plugin) {
		Money_Plugin = money_Plugin;
	}

	public boolean isUseEssentials() {
		return useEssentials;
	}

	public void setUseEssentials(boolean useEssentials) {
		this.useEssentials = useEssentials;
	}

	public int getPluginTimerCheck() {
		return pluginTimerCheck;
	}

	public void setPluginTimerCheck(int pluginTimerCheck) {
		this.pluginTimerCheck = pluginTimerCheck;
	}

	public String getQuestRewardsDefault() {
		return questRewardsDefault;
	}

	public void setQuestRewardsDefault(String questRewardsDefault) {
		this.questRewardsDefault = questRewardsDefault;
	}

	public boolean isFirstLoad() {
		return firstLoad;
	}

	public void setFirstLoad(boolean firstLoad) {
		this.firstLoad = firstLoad;
	}

	public UQuestPlayerListener getPlayerListener() {
		return playerListener;
	}

	public UQuestBlockListener getBlockListener() {
		return blockListener;
	}

	public boolean isHideQuestRewards() {
		return hideQuestRewards;
	}

	public void setHideQuestRewards(boolean hideQuestRewards) {
		this.hideQuestRewards = hideQuestRewards;
	}

	public UQuestEntityListener getEntityListener() {
		return entityListener;
	}

	public HashSet<String> getPlayersLoggedInSinceBoot() {
		return playersLoggedInSinceBoot;
	}

	public void setPlayersLoggedInSinceBoot(HashSet<String> playersLoggedInSinceBoot) {
		this.playersLoggedInSinceBoot = playersLoggedInSinceBoot;
	}

	public ArrayList<Quester> getTheQuestersRanked() {
		return theQuestersRanked;
	}

	public void setTheQuestersRanked(ArrayList<Quester> theQuestersRanked) {
		this.theQuestersRanked = theQuestersRanked;
	}

	public HashMap<Integer, String> getMobsTagged() {
		return mobsTagged;
	}

	public void setMobsTagged(HashMap<Integer, String> mobsTagged) {
		this.mobsTagged = mobsTagged;
	}

	public QuestListener getuQuestListener() {
		return uQuestListener;
	}
    
	
}