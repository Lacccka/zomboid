/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.mention.internal;

import java.util.Collection;
import org.javacord.api.entity.message.mention.AllowedMentions;

public interface AllowedMentionsBuilderDelegate {
    public void setMentionEveryoneAndHere(boolean var1);

    public void setMentionRoles(boolean var1);

    public void setMentionUsers(boolean var1);

    public void setMentionRepliedUser(boolean var1);

    public void addUser(long var1);

    public void addUser(String var1);

    public void addUsers(Collection<Long> var1);

    public void addRole(long var1);

    public void addRole(String var1);

    public void addRoles(Collection<Long> var1);

    public void removeUser(long var1);

    public void removeUser(String var1);

    public void removeRole(long var1);

    public void removeRole(String var1);

    public void removeUsers(Collection<Long> var1);

    public void removeRoles(Collection<Long> var1);

    public AllowedMentions build();
}

