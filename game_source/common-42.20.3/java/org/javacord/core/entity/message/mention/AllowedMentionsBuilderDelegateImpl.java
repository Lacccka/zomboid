/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.mention;

import java.util.Collection;
import java.util.HashSet;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.internal.AllowedMentionsBuilderDelegate;
import org.javacord.core.entity.message.mention.AllowedMentionsImpl;

public class AllowedMentionsBuilderDelegateImpl
implements AllowedMentionsBuilderDelegate {
    private final HashSet<Long> allowedUserMentions = new HashSet();
    private final HashSet<Long> allowedRoleMentions = new HashSet();
    private boolean mentionAllRoles = false;
    private boolean mentionAllUsers = false;
    private boolean mentionEveryoneAndHere = false;
    private boolean mentionRepliedUser = false;

    @Override
    public void setMentionEveryoneAndHere(boolean value) {
        this.mentionEveryoneAndHere = value;
    }

    @Override
    public void setMentionRoles(boolean value) {
        this.mentionAllRoles = value;
    }

    @Override
    public void setMentionUsers(boolean value) {
        this.mentionAllUsers = value;
    }

    @Override
    public void setMentionRepliedUser(boolean value) {
        this.mentionRepliedUser = value;
    }

    @Override
    public void addUser(long userId) {
        this.allowedUserMentions.add(userId);
    }

    @Override
    public void addUser(String userId) {
        this.addUser(Long.parseLong(userId));
    }

    @Override
    public void addUsers(Collection<Long> userIds) {
        this.allowedUserMentions.addAll(userIds);
    }

    @Override
    public void addRole(long roleId) {
        this.allowedRoleMentions.add(roleId);
    }

    @Override
    public void addRole(String roleId) {
        this.addRole(Long.parseLong(roleId));
    }

    @Override
    public void addRoles(Collection<Long> roleIds) {
        this.allowedRoleMentions.addAll(roleIds);
    }

    @Override
    public void removeUser(long userId) {
        this.allowedUserMentions.remove(userId);
    }

    @Override
    public void removeUser(String userId) {
        this.removeUser(Long.parseLong(userId));
    }

    @Override
    public void removeRole(long roleId) {
        this.allowedRoleMentions.remove(roleId);
    }

    @Override
    public void removeRole(String roleId) {
        this.removeRole(Long.parseLong(roleId));
    }

    @Override
    public void removeUsers(Collection<Long> userIds) {
        this.allowedUserMentions.removeAll(userIds);
    }

    @Override
    public void removeRoles(Collection<Long> roleIds) {
        this.allowedRoleMentions.removeAll(roleIds);
    }

    @Override
    public AllowedMentions build() {
        return new AllowedMentionsImpl(this.mentionAllRoles, this.mentionAllUsers, this.mentionEveryoneAndHere, this.mentionRepliedUser, this.allowedRoleMentions, this.allowedUserMentions);
    }
}

