/*
 * Decompiled with CFR 0.152.
 */
package zombie.core.skinnedmodel.advancedanimation.events;

import zombie.core.skinnedmodel.advancedanimation.AnimEvent;

public enum LocalAnimEvent {
    ActiveAnimLoopedEvent("ActiveAnimLooped", 1.0f),
    ActiveNonLoopedAnimFadeOutEvent("NonLoopedAnimFadeOut", 1.0f),
    ActiveAnimFinishingEvent("ActiveAnimFinishing", AnimEvent.AnimEventTime.END),
    ActiveNonLoopedAnimFinishedEvent("ActiveAnimFinished", AnimEvent.AnimEventTime.END),
    NoAnimConditionsPass("NoAnimConditionsPass", AnimEvent.AnimEventTime.END);

    private final AnimEvent animEvent = new AnimEvent();

    private LocalAnimEvent(String eventName, float timePc) {
        this.animEvent.timePc = timePc;
        this.animEvent.eventName = eventName;
    }

    private LocalAnimEvent(String eventName, AnimEvent.AnimEventTime time) {
        this.animEvent.time = time;
        this.animEvent.eventName = eventName;
    }

    public AnimEvent getAnimEvent() {
        return this.animEvent;
    }
}

