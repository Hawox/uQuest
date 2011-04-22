package hawox.uquest;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Listener;

import hawox.uquest.interfaceevents.QuestFinishEvent;
import hawox.uquest.interfaceevents.QuestGetEvent;
import hawox.uquest.interfaceevents.TrackerAddEvent;

public class QuestListener extends CustomEventListener
 implements Listener{
	
	public void onTrackerAdd(TrackerAddEvent event){	
	}
	
	public void onQuestGet(QuestGetEvent event){
	}
	
	public void onQuestFinish(QuestFinishEvent event){	
	}
	
/*	public void onQuestDrop(QuestDropEvent event){
		
	}*/
}
