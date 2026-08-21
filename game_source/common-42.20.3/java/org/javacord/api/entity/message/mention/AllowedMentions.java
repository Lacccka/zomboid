/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.mention;

import java.util.EnumSet;
import java.util.Set;
import org.javacord.api.entity.message.mention.AllowedMentionType;

public interface AllowedMentions {
    public Set<Long> getAllowedRoleMentions();

    public Set<Long> getAllowedUserMentions();

    public EnumSet<AllowedMentionType> getMentionTypes();

    public boolean getMentionRepliedUser();
}

