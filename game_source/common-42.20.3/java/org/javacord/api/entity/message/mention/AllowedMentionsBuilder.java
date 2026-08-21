/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.mention;

import java.util.Collection;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.internal.AllowedMentionsBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class AllowedMentionsBuilder {
    private final AllowedMentionsBuilderDelegate delegate = DelegateFactory.createAllowedMentionsBuilderDelegate();

    public AllowedMentionsBuilderDelegate getDelegate() {
        return this.delegate;
    }

    public AllowedMentionsBuilder setMentionRoles(boolean value) {
        this.delegate.setMentionRoles(value);
        return this;
    }

    public AllowedMentionsBuilder setMentionUsers(boolean value) {
        this.delegate.setMentionUsers(value);
        return this;
    }

    public AllowedMentionsBuilder setMentionEveryoneAndHere(boolean value) {
        this.delegate.setMentionEveryoneAndHere(value);
        return this;
    }

    public AllowedMentionsBuilder setMentionRepliedUser(boolean value) {
        this.delegate.setMentionRepliedUser(value);
        return this;
    }

    public AllowedMentionsBuilder addRole(String roleId) {
        this.delegate.addRole(roleId);
        return this;
    }

    public AllowedMentionsBuilder addRole(long roleId) {
        this.delegate.addRole(roleId);
        return this;
    }

    public AllowedMentionsBuilder addRoles(Collection<Long> roleIds) {
        this.delegate.addRoles(roleIds);
        return this;
    }

    public AllowedMentionsBuilder addUser(String userId) {
        this.delegate.addUser(userId);
        return this;
    }

    public AllowedMentionsBuilder addUser(long userId) {
        this.delegate.addUser(userId);
        return this;
    }

    public AllowedMentionsBuilder addUsers(Collection<Long> userIds) {
        this.delegate.addUsers(userIds);
        return this;
    }

    public AllowedMentionsBuilder removeRole(String roleId) {
        this.delegate.removeRole(roleId);
        return this;
    }

    public AllowedMentionsBuilder removeRole(long roleId) {
        this.delegate.removeRole(roleId);
        return this;
    }

    public AllowedMentionsBuilder removeRoles(Collection<Long> roleIds) {
        this.delegate.removeRoles(roleIds);
        return this;
    }

    public AllowedMentionsBuilder removeUser(String userId) {
        this.delegate.removeUser(userId);
        return this;
    }

    public AllowedMentionsBuilder removeUser(long userId) {
        this.delegate.removeUser(userId);
        return this;
    }

    public AllowedMentionsBuilder removeUsers(Collection<Long> userIds) {
        this.delegate.removeUsers(userIds);
        return this;
    }

    public AllowedMentions build() {
        return this.delegate.build();
    }
}

