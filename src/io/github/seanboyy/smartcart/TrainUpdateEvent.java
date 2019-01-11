package io.github.seanboyy.smartcart;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TrainUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private SmartCartTrain train;

    public TrainUpdateEvent(SmartCartTrain train){
        this.train = train;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public final SmartCartTrain getTrain(){
        return train;
    }
}
