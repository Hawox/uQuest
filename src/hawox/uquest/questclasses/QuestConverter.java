package hawox.uquest.questclasses;

import hawox.uquest.iProperty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.yaml.snakeyaml.JavaBeanDumper;

//Yay CRUD code! T.T
public class QuestConverter {
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

	public QuestConverter(){
		System.out.println("\n\n\n\n     [Hawox's uQuest]Converting old Quests...");
		iProperty questStorage = new iProperty("plugins/uQuest/uQuest_Quests.txt");
		//HashMap<Object,Object> masterOutput = new HashMap<Object,Object>();
		
		JavaBeanDumper y = new JavaBeanDumper();
		y.setUseGlobalTag(false);
	        
		try {
			// Create file 
		    FileWriter fstream = new FileWriter("plugins/uQuest/converted.Quests.yml");
		        BufferedWriter out = new BufferedWriter(fstream);
		        
			for (int i = 0; true; i++) {
				// see if there is a quest # for the current i, if there is keep
				// going, otherwise bail out
				if (questStorage.keyExists("Quest_" + Integer.toString(i))) {
					//masterOutput.put(i, questv1ToQuestv2(questStorage.getString("Quest_" + Integer.toString(i)).split(":")));
					//theQuests.add(i,new LoadedQuest(this, ,i));
					String dumpMe = y.dump(questv1ToQuestv2(questStorage.getString("Quest_" + Integer.toString(i)).split(":")));
					
					//remove class name
					dumpMe = dumpMe.replace("!!hawox.uquest.questclasses.QuestConverter$StorageQuestV2", "");
					//Replace object name with the quest number
					dumpMe = dumpMe.replace("questNumber:", Integer.toString(i)+":");
					//Don't know where this is comming from so just delete it
					dumpMe = dumpMe.replace("questNumber: *id001", "");
					//Get rid of all the '
					dumpMe = dumpMe.replace("'", "");
					//Change all the quest types to propernames
						/*#gather
						#blockdestroy
						#blockdamage
						#blockplace
						#kill*/
						dumpMe = dumpMe.replace("blockdestroy", "Block_Destroy");
						dumpMe = dumpMe.replace("blockdamage", "Block_Damage");
						dumpMe = dumpMe.replace("blockplace", "Block_Place");
						dumpMe = dumpMe.replace("kill", "Kill");

					
					
					
					//Now dump this object into the config file! =D
					out.append(dumpMe);
				} else {
					// okay we're at the end break out!
					break;
				}
			}			

			//Close the output stream
			out.close();
			renameTheFile();
			System.out.println("\n\n[Hawox's uQuest]Done converting!!!\n\n\n\n\n");
				
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			System.err.println("[Hawox's uQuest]There was a problem converting the # of quests.\n\n\n");
		} catch (Exception e){//Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		}
	}
	
	//private String quest0Default = "Get Wood:gather:Go gather me 10 wood please!:Thank you very much!:17:10:10:0:Wood:0:0";
	/*
#:
  Name: Get Wood
  Start_Info: Go gather me 10 wood please!
  Finish_Info: Thank you very much!
  Rewards: 
    Money: 0
  Objectives:
    0:
      Type: Gather
      Display_Name: Wood
      Objective_ID: 17
      Amount: 10
      
      
      
  Name: Array<0>
  Start_Info: Array<2>
  Finish_Info: Array<3>
  Rewards: 
    Money: Array<7>
  Objectives:
    0:
      Type: Array<1>
      Display_Name: Array<8>
      Objective_ID: Array<4>
      Amount: Array<5>
      */
	/*
questInfo: &id001
  Name: Get Wood
  Objectives:
    0:
      Amount: '40'
      Display_Name: '0'
      Type: gather
      Objective_ID: '5'
  Finish_Info: Thank you very much!
  Rewards:
    Money: '20'
  Start_Info: Go gather me a few planks please!
	 */
	
	public StorageQuestV2 questv1ToQuestv2(String[] questv1){
		StorageQuestV2 q = new StorageQuestV2();
		HashMap<String,String> rewards = new HashMap<String,String>();
		HashMap<Integer,Object> objectives = new HashMap<Integer,Object>();
		LinkedHashMap<String,Object> objective0 = new LinkedHashMap<String,Object>();
		
		//objective | looks different for gather quests
		if(!(questv1[1].equalsIgnoreCase("gather"))){
				objective0.put("Type", questv1[1]);
				objective0.put("Display_Name", questv1[8]);
				objective0.put("Objective_ID", questv1[4]);
				objective0.put("Amount", questv1[5]);
		}else{
			//gather mission
			LinkedHashMap<String,String> item = new LinkedHashMap<String,String>();
				
				item.put("Item_ID", questv1[4]);
				item.put("Display_Name", questv1[8]);
				item.put("Amount", questv1[5]);
				item.put("Durability", "0");

				
				objective0.put("Type", "Gather");
				objective0.put("Item", item);
			//gather quests
			/*
	1:
      Type: Gather
      Item:
        Item_ID: 58
        Display_Name: Crafting Table
        Amount: 1
        Durability: 0
			 */
		}
			objectives.put(0, objective0);
		//rewards
			rewards.put("Money", questv1[7]);
		//All info
		q.getQuestNumber().put("Name", questv1[0]);
		q.getQuestNumber().put("Start_Info", questv1[2]);
		q.getQuestNumber().put("Finish_Info", questv1[3]);
		q.getQuestNumber().put("Rewards", rewards);
		q.getQuestNumber().put("Objectives", objectives);

		//q.getQuestNumber().put(number, q.getQuestInfo());
		return q;
	}
	
	public void renameTheFile(){
		File file = new File("plugins/uQuest/uQuest_Quests.txt");
		
		file.renameTo(new File("plugins/uQuest/converted.uQuest_Quests.txt"));
	}
	
	public class StorageQuestV2{
		//public HashMap<Integer,Object> questNumber = new HashMap<Integer,Object>();
		public LinkedHashMap<String,Object> questNumber = new LinkedHashMap<String,Object>();
		//private HashMap<String,String> rewards = new HashMap<String,String>();
		//private HashMap<Integer,Object> objectives = new HashMap<Integer,Object>();
		//private HashMap<String,Object> objective0 = new HashMap<String,Object>();

		public LinkedHashMap<String, Object> getQuestNumber() {
			return questNumber;
		}

		public void setQuestNumber(LinkedHashMap<String, Object> questNumber) {
			this.questNumber = questNumber;
		}



/*		public HashMap<String, String> getRewards() {
			return rewards;
		}
		public void setRewards(HashMap<String, String> rewards) {
			this.rewards = rewards;
		}
		public HashMap<Integer, Object> getObjectives() {
			return objectives;
		}
		public void setObjectives(HashMap<Integer, Object> objectives) {
			this.objectives = objectives;
		}
		public HashMap<String, Object> getObjective0() {
			return objective0;
		}
		public void setObjective0(HashMap<String, Object> objective0) {
			this.objective0 = objective0;
		}*/
	}
}
