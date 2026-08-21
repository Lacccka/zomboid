/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import java.util.List;
import zombie.characters.IsoGameCharacter;
import zombie.core.skinnedmodel.advancedanimation.AnimEvent;
import zombie.core.skinnedmodel.advancedanimation.AnimLayer;
import zombie.core.skinnedmodel.advancedanimation.LiveAnimNode;
import zombie.core.skinnedmodel.animation.AnimationTrack;

public final class ThumpAnimations {
    public static final String EVENT_NAME = "ThumpFrame";
    private static final int MAX_THUMP_TIMES = 10;
    private static final float[] thumpTimes = new float[10];

    public static LiveAnimNode getLiveAnimNode(IsoGameCharacter chr) {
        AnimLayer animLayer = chr.getAdvancedAnimator().getRootLayer();
        if (animLayer == null) {
            return null;
        }
        List<LiveAnimNode> liveAnimNodes = animLayer.getLiveAnimNodes();
        for (LiveAnimNode liveAnimNode : liveAnimNodes) {
            for (AnimEvent event : liveAnimNode.getSourceNode().events) {
                if (!event.eventName.equalsIgnoreCase(EVENT_NAME)) continue;
                return liveAnimNode;
            }
        }
        return null;
    }

    public static AnimationTrack getAnimationTrack(IsoGameCharacter chr) {
        LiveAnimNode liveAnimNode = ThumpAnimations.getLiveAnimNode(chr);
        return liveAnimNode == null ? null : ThumpAnimations.getAnimationTrack(liveAnimNode);
    }

    public static AnimationTrack getAnimationTrack(LiveAnimNode liveAnimNode) {
        for (int i = 0; i < liveAnimNode.getMainAnimationTracksCount(); ++i) {
            AnimationTrack track = liveAnimNode.getMainAnimationTrackAt(i);
            if (!track.isPlaying) continue;
            return track;
        }
        return null;
    }

    public static float getDuration(IsoGameCharacter chr) {
        LiveAnimNode liveAnimNode = ThumpAnimations.getLiveAnimNode(chr);
        if (liveAnimNode == null) {
            return -1.0f;
        }
        AnimationTrack track = ThumpAnimations.getAnimationTrack(liveAnimNode);
        return track == null ? -1.0f : track.getDuration();
    }

    public static int getThumpTimes(LiveAnimNode liveAnimNode, float[] times) {
        int count = 0;
        for (AnimEvent event : liveAnimNode.getSourceNode().events) {
            if (!event.eventName.equalsIgnoreCase(EVENT_NAME)) continue;
            if (event.time == AnimEvent.AnimEventTime.PERCENTAGE) {
                times[count++] = event.timePc;
                continue;
            }
            times[count++] = event.time == AnimEvent.AnimEventTime.START ? 0.0f : 1.0f;
        }
        return count;
    }

    public static int countThumpEventsBetween(IsoGameCharacter chr, float startTime, float endTime) {
        LiveAnimNode liveAnimNode = ThumpAnimations.getLiveAnimNode(chr);
        if (liveAnimNode == null) {
            return 0;
        }
        AnimationTrack track = ThumpAnimations.getAnimationTrack(liveAnimNode);
        if (track == null) {
            return 0;
        }
        int thumpTimesCount = ThumpAnimations.getThumpTimes(liveAnimNode, thumpTimes);
        if (thumpTimesCount == 0) {
            return 0;
        }
        float duration = track.getDuration();
        int result = 0;
        for (int i = 0; i < thumpTimesCount; ++i) {
            int s = (int)Math.floor((startTime - thumpTimes[i] * duration) / duration);
            int e = (int)Math.floor((endTime - thumpTimes[i] * duration) / duration);
            result += e - s;
        }
        return result;
    }
}

