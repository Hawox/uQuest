package hawox.uquest;

import hawox.uquest.commands.Cmd_reloadquests;
import hawox.uquest.commands.Cmd_uquest;
import hawox.uquest.questclasses.LoadedQuest;
import hawox.uquest.questclasses.QuestConverter;
import hawox.uquest.questclasses.QuestLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import sqLiteStor.SqLiteKeyValStor;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import cosine.boseconomy.BOSEconomy;

/**
 * Plugin Template
 * 
 * @author Hawox
 */
public class UQuest extends JavaPlugin {
	
	private final UQuestPlayerListener playerListener = new UQuestPlayerListener(this);
	private final UQuestBlockListener blockListener = new UQuestBlockListener(this);
	private final UQuestEntityListener entityListener = new UQuestEntityListener(this);
	private PluginDescriptionFile pdfFile;
	public Logger log;
    private static PluginListener PluginListener = null;
    private static iConomy iConomy = null;
    private static Server Server = null;

	protected ArrayList<Quester> theQuesterList = new ArrayList<Quester>();
	protected ArrayList<LoadedQuest> theQuests = new ArrayList<LoadedQuest>();

	// default quest0 that will be set into the file if it dosn't exist
	//private String quest0Default = "Get Wood:gather:Go gather me 10 wood please!:Thank you very much!:17:10:10:0:Wood:0:0";

	// player storage
	protected iProperty questPlayerStorage;
	// stored player info will look like: PlayerName =
	// QuestId:QuestsCompleted:MoneyEarnedFromQuests:questtracker
	// (-1 quest id means no active quest!)
	private String questDefaultPlayer = "-1:0:0:0,0";

	// config

	protected boolean scaleQuestLevels = true;
	protected boolean broadcastSaving = true;
	protected boolean useiConomy = true;
	private boolean usePermissions = true;
	private boolean useSQLite = false;
	private boolean useDefaultUQuest = true;
	private boolean useBOSEconomy = false;
	protected int SaveQuestersInfoIntervalInMinutes = 30;
	protected int questAnnounceInterval = 5;
	protected int questRewardInterval = 10;
	protected int questLevelInterval = 50;
	protected int dropQuestInterval = 60;
	protected int dropQuestCharge = 5000;
	protected int pluginTimerCheck = 5;
    private String moneyName = "Monies";
    private String questRewardsDefault = "87,Netherrack Blocks,10~88,Soul Sand Blocks,10~89,Glowstone Blocks,10~18,Leaf Blocks,10~344,Eggs,10~348,Glowstone Dust,10";
	private String[] questRewards = { "87,Netherrack Blocks,10",
			  "88,Soul Sand Blocks,10",
			  "89,Glowstone Blocks,10",
			  "18,Leaf Blocks,10",
			  "344,Eggs,10"};

	
	private SqLiteKeyValStor<Quester> DB;

	
	//Has .get .update .put
	
	// methods only used in this plugin
    
    //TODO Get rid of iProp and use this
	 //new Configuration(new File());
	
	Reader readerQuests = null;
//	private QuestLoader questloader;

	boolean firstLoad = true;
	
	HashSet<String> canNotDrop = new HashSet<String>();
	
	ArrayList<String> canNotDropRemoveTimer = new ArrayList<String>();
	ArrayList<String> mobsKilled = new ArrayList<String>();
	
	//create our general quest interaction methods
	QuestInteraction questInteraction = new QuestInteraction(this);


	public void onDisable() {
		// Runs when plugin is disabled. Events are auto disabled so do not
		// worry about them
		if(!(this.isUseSQLite()))
			saveQuesterListToFile();
		System.out.println("[" + this.getPdfFile().getName() + "] disabled!");
		// System.out.println( pdfFile.getName() + " version " +
		// pdfFile.getVersion() + " is disabled!" );
	}

	public void onEnable() {
		setPdfFile(this.getDescription());

		//load up our files if they don't exist
		moveFiles();
		
		questPlayerStorage = new iProperty("plugins/uQuest/uQuest_Players.txt");
		
		//Check if we need to convert old uQuest v1 quests
		if(new File("plugins/uQuest/uQuest_Quests.txt").exists())
			new QuestConverter();
		
		//System.out.println(getDataFolder());
		//registerCommands
		//using default uquest
		if(isUseDefaultUQuest()){
			Cmd_uquest cmd_uquest = new Cmd_uquest(this);
			getCommand("uquest").setExecutor(cmd_uquest);
			getCommand("quest").setExecutor(cmd_uquest);
			getCommand("q").setExecutor(cmd_uquest);
			//getCommand("test").setExecutor(new Cmd_test(this));
		}
		
		//This command exists even if they are not using the default uQuest
		Cmd_reloadquests cmd_reloadquests = new Cmd_reloadquests(this);
		getCommand("reloadquests").setExecutor(cmd_reloadquests);
		
		log = getServer().getLogger();
		
		// setup config
		readConfig();
		
		// load quests into array
		theQuestsLoadAllIntoArray();
		
		//iCon stuff
		if(isUseiConomy()){
		 Server = getServer();

	        PluginListener = new PluginListener();

	        // Event Registration
	        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, PluginListener, Priority.Monitor, this);
		}//end iCon stuff
		
		// Get permissions
		setupPermissions();
		
		//Get the DB fired up
		if(isUseSQLite() == true){
			this.setDB(new SqLiteKeyValStor<Quester>("questers", "plugins/uQuest/uQuestQuesters"));
			System.out.println("[" + this.getPdfFile().getName() + "] Loaded with SQLite!");
		}
		
		// start the player saving timer
		if (firstLoad == true && isUseSQLite() == false) {
			timerSavePlayers();
			firstLoad = false;
			System.out.println("[" + this.getPdfFile().getName() + "] Loaded with Flatfile!");
		}
		
		// Runs everytime the plugin is enabled. Always register the events
		// here! (I placed them in a custom method to make it easier to read!)
		registerEvents();

		System.out.println("[" + this.getPdfFile().getName() + "] v" + this.getPdfFile().getVersion() + " enabled! With " + this.getQuestInteraction().getQuestTotal() + " quests loaded!");
		// System.out.println( pdfFile.getName() + " version " +
		// pdfFile.getVersion() + " is enabled!" );
		
		//For iCon at least, it hooks in after the plugin enables. Solution: Timer!
		ScheduledThreadPoolExecutor onEnable_Timer = new ScheduledThreadPoolExecutor(1);
		onEnable_Timer.schedule(new Runnable() {
			public void run() {
				if(isUseiConomy())
					checkiCon();
				if(isUseBOSEconomy())
					checkBOSE();
				}
			}, pluginTimerCheck, TimeUnit.SECONDS);
	}

	/**
	 * Adds Permission plugin support
	 */
	public static PermissionHandler Permissions = null;

	public void setupPermissions() {
		Plugin test = this.getServer().getPluginManager()
				.getPlugin("Permissions");

		if (Permissions == null) {
			if (test != null) {
				Permissions = ((Permissions) test).getHandler();
			} else {
				System.out.println("[" + this.getPdfFile().getName() + "] Permission system not enabled. Disabling permission support.");
				setUsePermissions(false);
			}
		}
	}

	/**
	 * End of Permissions plugin thing
	 */
	
	/**
	 * Disables iConomy function if it's not here
	 */
	public void checkiCon(){
		
		 //Plugin test = getServer().getPluginManager().getPlugin("iConomy");
		  boolean test = checkiConomy();
		      if (test != false) {
		        //iConomy iC = (iConomy)test;
		        //currency = iConomy.currency;
		      } else {
		        log.log(Level.SEVERE, "[" + this.getPdfFile().getName() + "] iConomy is not loaded. Turning iConomy support off.");
		        useiConomy = false;
		      }
	    
	}
	
	public boolean checkiConomy() {
        this.useiConomy = (iConomy != null);
        return this.useiConomy;
    }
    
    /**
	 * Disables BOSE function if it's not here
	 */
    BOSEconomy theBOSEconomy = null;

	public void checkBOSE(){
	    Plugin temp = this.getServer().getPluginManager().getPlugin("BOSEconomy");
		  
		      if (temp != null) {
		    	  theBOSEconomy = (BOSEconomy)temp;
		      } else {
		        log.log(Level.SEVERE, "[" + this.getPdfFile().getName() + "] iConomy is not loaded. Turning iConomy support off.");
		        useiConomy = false;
		      }
	}
	

	public void registerEvents() {
		// Register our events
		PluginManager pm = getServer().getPluginManager();

		// player Stuff, handled by the player listener
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener,Priority.Normal, this);
		// TODO pm.registerEvent(Event.Type.ENTITY_DEATH, playerListener,Priority.Normal, this);

		// Block Stuff, handled by the block listener
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener,Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener,Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener,Priority.Normal, this);
		
		//entity
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Normal, this);
	}
	
	public void moveFiles(){
		getDataFolder().mkdir();
		getDataFolder().setWritable(true);
	    getDataFolder().setExecutable(true);
		extractFile("config.yml");
		extractFile("new.Quests.yml");
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

		          System.out.println("[" + this.getPdfFile().getName() + "] Default file written: " + name);
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

	public void readConfig() {
		/* private boolean useDefaultUQuest = true;
		 * 
		 * Database:
		 * 		private boolean useSQLite = false;
		 * 		protected boolean broadcastSaving = true;
		 * 		protected int SaveQuestersInfoIntervalInMinutes = 30;
		 * 
		 * PluginSupport:
		 * 		protected boolean useiConomy = true;
		 * 		private boolean usePermissions = true;
		 * 		private boolean useEssentialsEco = false;
		 * 		private boolean useBOSEconomy = false;
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
				log.log(Level.SEVERE, "[" + this.getPdfFile().getName() + "] Error setting up quest rewards! Fix the config file!");
				log.log(Level.SEVERE, "[" + this.getPdfFile().getName() + "] Quest item rewards are loaded as defaults!");
				setQuestRewards(questRewards);
			}

		useSQLite = config.getBoolean("Database.useSQLite", useSQLite);
		broadcastSaving = config.getBoolean("Database.broadcastSaving", broadcastSaving);
		SaveQuestersInfoIntervalInMinutes = config.getInt("Database.SaveQuestersInfoIntervalInMinutes", SaveQuestersInfoIntervalInMinutes);

		pluginTimerCheck = config.getInt("PluginSupport.pluginTimerCheck", pluginTimerCheck);
		useiConomy = config.getBoolean("PluginSupport.useiConomy", useiConomy);
		usePermissions = config.getBoolean("PluginSupport.usePermissions", usePermissions);
		//useEssentialsEco = config.getBoolean("PluginSupport.useEssentialsEco", useEssentialsEco);
		useBOSEconomy = config.getBoolean("PluginSupport.useBOSEconomy", useBOSEconomy);
		moneyName = config.getString("PluginSupport.moneyName", moneyName);
		

		questLevelInterval = config.getInt("QuestLevels.questLevelInterval", questLevelInterval);
		scaleQuestLevels = config.getBoolean("QuestLevels.scaleQuestLevels", scaleQuestLevels);
		
		questAnnounceInterval = config.getInt("Announcements.questAnnounceInterval", questAnnounceInterval);
		questRewardInterval = config.getInt("Announcements.questRewardInterval", questRewardInterval);
		
		dropQuestInterval = config.getInt("QuestDropping.dropQuestInterval", dropQuestInterval);
		dropQuestCharge = config.getInt("QuestDropping.dropQuestCharge", dropQuestCharge);

		/*
		if ((config.keyExists("scaleQuestLevels"))) {
			scaleQuestLevels = config.getBoolean("scaleQuestLevels");
		} else {
			config.setBoolean("scaleQuestLevels", scaleQuestLevels);
		}

		if ((config.keyExists("broadcastFlatFileSaving"))) {
			broadcastSaving = config.getBoolean("broadcastFlatFileSaving");
		} else {
			config.setBoolean("broadcastFlatFileSaving", broadcastSaving);
		}
		
		if ((config.keyExists("useiConomy"))) {
			useiConomy = config.getBoolean("useiConomy");
		} else {
			config.setBoolean("useiConomy", useiConomy);
		}
		
		if ((config.keyExists("useSQLite"))) {
			setUseSQLite(config.getBoolean("useSQLite"));
		} else {
			config.setBoolean("useSQLite", isUseSQLite());
		}
		
		if ((config.keyExists("usePermissions"))) {
			setUsePermissions((config.getBoolean("usePermissions")));
		} else {
			config.setBoolean("usePermissions", isUsePermissions());
		}
		
		if ((config.keyExists("useDefaultUQuest"))) {
			setUseDefaultUQuest((config.getBoolean("useDefaultUQuest")));
		} else {
			config.setBoolean("useDefaultUQuest", isUseDefaultUQuest());
		}
		
		if ((config.keyExists("useEssentialsEco"))) {
			setUseEssentialsEco((config.getBoolean("useEssentialsEco")));
		} else {
			config.setBoolean("useEssentialsEco", isUseEssentialsEco());
		}
		
		if ((config.keyExists("useBOSEconomy"))) {
			setUseBOSEconomy((config.getBoolean("useBOSEconomy")));
		} else {
			config.setBoolean("useBOSEconomy", isUseBOSEconomy());
		}
		
		if ((config.keyExists("SaveFlatFileQuestersInfoIntervalInMinutes"))) {
			SaveQuestersInfoIntervalInMinutes = config.getInt("SaveFlatFileQuestersInfoIntervalInMinutes");
		} else {
			config.setInt("SaveFlatFileQuestersInfoIntervalInMinutes", SaveQuestersInfoIntervalInMinutes);
		}

		if ((config.keyExists("questAnnounceInterval"))) {
			questAnnounceInterval = config.getInt("questAnnounceInterval");
		} else {
			config.setInt("questAnnounceInterval", questAnnounceInterval);
		}
		
		if ((config.keyExists("dropQuestInterval"))) {
			dropQuestInterval = config.getInt("dropQuestInterval");
		} else {
			config.setInt("dropQuestInterval", dropQuestInterval);
		}
		
		if ((config.keyExists("dropQuestCharge"))) {
			dropQuestCharge = config.getInt("dropQuestCharge");
		} else {
			config.setInt("dropQuestCharge", dropQuestCharge);
		}

		if ((config.keyExists("questRewardInterval"))) {
			questRewardInterval = config.getInt("questRewardInterval");
		} else {
			config.setInt("questRewardInterval", questRewardInterval);
		}
		
		if ((config.keyExists("questLevelInterval"))) {
			questLevelInterval = config.getInt("questLevelInterval");
		} else {
			config.setInt("questLevelInterval", questLevelInterval);
		}

		// This one is different because we want it to be a String[][]
		if ((config.keyExists("questRewards"))) {
			try {
				String[] loadedInfo = config.getString("questRewards").split("~");
				setQuestRewards(loadedInfo);
				//test every reward threw a meaningless loop to try and get the catch to run
				for(int i=0; i<loadedInfo.length; i++){
					String rewards[] = loadedInfo[i].split(",");
					rewards[0] = rewards[0];
					rewards[1] = rewards[1];
					rewards[2] = rewards[2];
				}
			} catch (ArrayIndexOutOfBoundsException aiobe) {
				log.log(Level.SEVERE, "[" + this.pdfFile.getName() + "] Error setting up quest rewards! Fix the config file!");
				log.log(Level.SEVERE, "[" + this.pdfFile.getName() + "] Quest item rewards may give undesired results!");
			}
		} else {
			config.setString("questRewards","87,Netherrack Blocks,10~88,Soul Sand Blocks,10~89,Glowstone Blocks,10~18,Leaf Blocks,10~344,Eggs,10~348,Glowstone Dust,10");
		}

		
		
		// check if theres a questStoreage file. If not make one with the
		// default quest
		if (!(questStorage.keyExists("Quest_0"))) {
			questStorage.setString("Quest_0", quest0Default);
		}*/

	}

	// blocks that will be awarded every x quests
	// int[] randomBlockRewards = {87,88,89,18,344,348};
	// String[] randomBlockRewardsNames =
	// {"Netherstone Blocks","Slow Sand Blocks","Lightstone Blocks","Leaf Blocks","Eggs","Lightstone Dust"};

	// [Number][itemID, name, amount to give]
	/*
	private String[][] questRewards = { { "87", "Netherrack Blocks", "10" },
			{ "88", "Soul Sand Blocks", "10" },
			{ "89", "Glowstone Blocks", "10" },
			{ "18", "Leaf Blocks", "10" },
			{ "344", "Eggs", "10" }, { "348", "Glowstone Dust", "10" } };*/
	
	// [itemID, name, amount to give]


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

	/*
	 * Storage should look like: #_name = Get Wood #_type = gather #_info = Go
	 * gather me 10 wood please! #_finishInfo = Thank you very much!
	 * #_itemIDNeeded = 17 #_amountNeeded = 10 #_amountConsume = 10
	 * #_rubixReward = 0 #_kitReward = quest0kit #_mixxitExpToGive = 0
	 * #_mixxitExpNeeded = 0
	 */

	public void theQuestsLoadAllIntoArray() {
		this.theQuests = new QuestLoader(this).loadAllQuests();
	}

	
	
	/* OLD
	public void theQuestsLoadAllIntoArray() {
		try {
			for (int i = 0; true; i++) {
				// see if there is a quest # for the current i, if there is keep
				// going, otherwise bail out
				if (questStorage.keyExists("Quest_" + Integer.toString(i))) {
					theQuests.add(
							i,
							new LoadedQuest(this, questStorage.getString(
									"Quest_" + Integer.toString(i)).split(":"),
									i));
				} else {
					// okay we're at the end break out!
					break;
				}
			}
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			getServer().broadcastMessage(
					"There was a problem loading the # of quests.");
		}
	}*/
	

	
	
	public void saveQuesterToFile(Quester quester){
		questPlayerStorage.setString(quester.theQuestersName, quester.toString());
	}

	public boolean saveQuesterListToFile() {
		if (broadcastSaving == true) {
			getServer().broadcastMessage(
					"[Hawox uQuest] Saving all players quests to file...");
		}
		System.out.println("[Hawox uQuest] Saving all players quests to file...");
		for (int i = 0; i < theQuesterList.size(); i++) {
			questPlayerStorage.setString(theQuesterList.get(i).theQuestersName, theQuesterList.get(i).toString());
		}
		if (broadcastSaving == true) {
			getServer().broadcastMessage("[Hawox uQuest] Done saving.");
		}
		System.out.println("[Hawox uQuest] Done saving.");
		return true;
	}

	public boolean placePlayerIntoList(Player player) {
		// check if player is already in the quester list
		boolean wasInList = false;
		for (Quester quester : theQuesterList) {
			if (quester.theQuestersName.equalsIgnoreCase(player.getName())) {
				wasInList = true;
			}
		}
		if (wasInList == false) { // needs to be added
			if (!(questPlayerStorage.keyExists(player.getName()))) {
				questPlayerStorage.setString(player.getName(),questDefaultPlayer);
				theQuesterList.add(new Quester(questDefaultPlayer.split(":"), player));
				return true;
			}
			theQuesterList.add(new Quester(questPlayerStorage.getString(player.getName()).split(":"), player));
			return true;
		}
		return false;
	}

	

	

	public ArrayList<Quester> getTheQuesterList() {
		return theQuesterList;
	}

	public void setTheQuesterList(ArrayList<Quester> theQuesterList) {
		this.theQuesterList = theQuesterList;
	}

	public ArrayList<LoadedQuest> getTheQuests() {
		return theQuests;
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

	public BOSEconomy getTheBOSEconomy() {
		return theBOSEconomy;
	}

	public void setTheBOSEconomy(BOSEconomy theBOSEconomy) {
		this.theBOSEconomy = theBOSEconomy;
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

	public Reader getReaderQuests() {
		return readerQuests;
	}

	public void setReaderQuests(Reader readerQuests) {
		this.readerQuests = readerQuests;
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

	public ArrayList<String> getMobsKilled() {
		return mobsKilled;
	}

	public void setMobsKilled(ArrayList<String> mobsKilled) {
		this.mobsKilled = mobsKilled;
	}

	public void setPdfFile(PluginDescriptionFile pdfFile) {
		this.pdfFile = pdfFile;
	}

	public PluginDescriptionFile getPdfFile() {
		return pdfFile;
	}
    
    
	
}