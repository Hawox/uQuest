package hawox.uquest;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;
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
	
	//Get the correct methods to run
	public void onCustomEvent(Event event){
		if(event instanceof TrackerAddEvent)
			onTrackerAdd((TrackerAddEvent)event);
		else if(event instanceof QuestGetEvent)
			onQuestGet((QuestGetEvent)event);
		else if(event instanceof QuestFinishEvent)
			onQuestFinish((QuestFinishEvent)event);
	}
}
