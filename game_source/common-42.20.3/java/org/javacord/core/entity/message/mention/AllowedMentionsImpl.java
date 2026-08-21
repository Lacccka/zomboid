/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.mention;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.javacord.api.entity.message.mention.AllowedMentionType;
import org.javacord.api.entity.message.mention.AllowedMentions;

public class AllowedMentionsImpl
implements AllowedMentions {
    private final Set<Long> allowedRoleMentions;
    private final Set<Long> allowedUserMentions;
    private final EnumSet<AllowedMentionType> allowedMentionTypes = EnumSet.noneOf(AllowedMentionType.class);
    private final boolean mentionRepliedUser;

    public AllowedMentionsImpl(boolean mentionAllRoles, boolean mentionAllUsers, boolean mentionEveryoneAndHere, boolean mentionRepliedUser, HashSet<Long> allowedRoleMentions, HashSet<Long> allowedUserMentions) {
        this.allowedRoleMentions = allowedRoleMentions;
        this.allowedUserMentions = allowedUserMentions;
        this.mentionRepliedUser = mentionRepliedUser;
        if (mentionAllRoles) {
            this.allowedMentionTypes.add(AllowedMentionType.ROLES);
        }
        if (mentionAllUsers) {
            this.allowedMentionTypes.add(AllowedMentionType.USERS);
        }
        if (mentionEveryoneAndHere) {
            this.allowedMentionTypes.add(AllowedMentionType.EVERYONE);
        }
    }

    @Override
    public Set<Long> getAllowedRoleMentions() {
        return this.allowedRoleMentions;
    }

    @Override
    public Set<Long> getAllowedUserMentions() {
        return this.allowedUserMentions;
    }

    @Override
    public EnumSet<AllowedMentionType> getMentionTypes() {
        return this.allowedMentionTypes;
    }

    @Override
    public boolean getMentionRepliedUser() {
        return this.mentionRepliedUser;
    }

    public ObjectNode toJsonNode() {
        ObjectNode object = JsonNodeFactory.instance.objectNode();
        return this.toJsonNode(object);
    }

    public ObjectNode toJsonNode(ObjectNode object) {
        ArrayNode parse = object.putArray("parse");
        if (this.allowedMentionTypes.contains((Object)AllowedMentionType.ROLES)) {
            parse.add("roles");
        } else {
            ArrayNode roles = object.putArray("roles");
            this.allowedRoleMentions.forEach(id -> roles.add(id.toString()));
        }
        if (this.allowedMentionTypes.contains((Object)AllowedMentionType.USERS)) {
            parse.add("users");
        } else {
            ArrayNode users = object.putArray("users");
            this.allowedUserMentions.forEach(id -> users.add(id.toString()));
        }
        if (this.allowedMentionTypes.contains((Object)AllowedMentionType.EVERYONE)) {
            parse.add("everyone");
        }
        object.put("replied_user", this.mentionRepliedUser);
        return object;
    }
}

