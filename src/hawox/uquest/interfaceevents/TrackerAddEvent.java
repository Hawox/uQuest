package hawox.uquest.interfaceevents;

import org.bukkit.entity.Player;

@SuppressWarnings("serial")
public class TrackerAddEvent extends UQuestEvent{
	Player player;
	String theTracker;
	int amountIncremented;
	
	public TrackerAddEvent(Player p, String t, int a){
		super("TrackerAddEvent");
		this.player = p;
		this.theTracker = t;
		this.amountIncremented = a;
	}
	
	
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	public String getTheTracker() {
		return theTracker;
	}
	public void setTheTracker(String theTracker) {
		this.theTracker = theTracker;
	}
	public int getAmountIncremented() {
		return amountIncremented;
	}
	public void setAmountIncremented(int amountIncremented) {
		this.amountIncremented = amountIncremented;
	}	
}