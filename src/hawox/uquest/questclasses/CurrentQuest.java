package hawox.uquest.questclasses;

import hawox.uquest.UQuest;


/*    #:
 *       Name
 *       Start Info
 *       Finish Info
 *       Rewards
 *          money - 
 *          item -
 *          item -
 *          etc~
 *       Objectives
 */
public class CurrentQuest extends LoadedQuest{

	public CurrentQuest(UQuest plugin, LoadedQuest theQuest, int level) {
		//currentQuest = new CurrentQuest(plugin, plugin.getTheQuests().get(quester.getQuestID()), this.getQuestLevel(player));
		//TODO Leveling
		super();
		this.name = theQuest.getName();
		this.startInfo = theQuest.getStartInfo();
		this.finishInfo = theQuest.getFinishInfo();
		this.rewards = theQuest.getRewards();
		this.objectives = theQuest.getObjectives();
		// TODO Auto-generated constructor stub
	}


	
	/*public CurrentQuest(final UQuest plugin, LoadedQuest theQuest, int level) {
		this.plugin = plugin;
		
		this.name = theQuest.name;
		this.finishInfo = theQuest.finishInfo;

    }*/
	

	
	/* TODO
	public void changeQuestLevel(int level){
			//to account for starting at level 0
			level += 1;
			this.amountNeeded *= level;
			this.amountToConsume *= level;
			this.moneyReward *= level;
			this.expToGive *= level;
			this.classToGiveItTo *= level;
	}*/
	
	
	//Getters and Setters
	
}
