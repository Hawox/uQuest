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
		this.name = theQuest.getName();
		this.startInfo = theQuest.getStartInfo();
		this.finishInfo = theQuest.getFinishInfo();
		for(Reward reward : theQuest.getRewards()){
			this.rewards.add(new Reward(reward));
		}
		for(Objective objective : theQuest.getObjectives()){
			this.objectives.add(new Objective(objective));
		}
		this.changeQuestLevel(level);
	}
	
	public void changeQuestLevel(int level){
		//to account for starting at level 0
		level += 1;
		for(Reward reward : this.rewards){
			reward.scaleMoneyReward(level);
		}
		for(Objective objective : this.objectives){
			objective.scaleToLevel(level);
		}
	}

}
