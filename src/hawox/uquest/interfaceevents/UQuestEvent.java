package hawox.uquest.interfaceevents;

import org.bukkit.event.Event;

@SuppressWarnings("serial")
public abstract class UQuestEvent extends Event{

	protected UQuestEvent(String name) {
		super(name);
	}
}
