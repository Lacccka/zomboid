/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.listener.server.role;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeOverwrittenPermissionsListener;
import org.javacord.api.listener.server.role.RoleAttachableListener;
import org.javacord.api.listener.server.role.RoleAttachableListenerManager;
import org.javacord.api.listener.server.role.RoleChangeColorListener;
import org.javacord.api.listener.server.role.RoleChangeHoistListener;
import org.javacord.api.listener.server.role.RoleChangeMentionableListener;
import org.javacord.api.listener.server.role.RoleChangeNameListener;
import org.javacord.api.listener.server.role.RoleChangePermissionsListener;
import org.javacord.api.listener.server.role.RoleChangePositionListener;
import org.javacord.api.listener.server.role.RoleDeleteListener;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.ClassHelper;

public interface InternalRoleAttachableListenerManager
extends RoleAttachableListenerManager {
    public DiscordApi getApi();

    public long getId();

    @Override
    default public ListenerManager<RoleChangePositionListener> addRoleChangePositionListener(RoleChangePositionListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Role.class, this.getId(), RoleChangePositionListener.class, listener);
    }

    @Override
    default public List<RoleChangePositionListener> getRoleChangePositionListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Role.class, this.getId(), RoleChangePositionListener.class);
    }

    @Override
    default public ListenerManager<RoleChangeMentionableListener> addRoleChangeMentionableListener(RoleChangeMentionableListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Role.class, this.getId(), RoleChangeMentionableListener.class, listener);
    }

    @Override
    default public List<RoleChangeMentionableListener> getRoleChangeMentionableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Role.class, this.getId(), RoleChangeMentionableListener.class);
    }

    @Override
    default public ListenerManager<RoleChangeColorListener> addRoleChangeColorListener(RoleChangeColorListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Role.class, this.getId(), RoleChangeColorListener.class, listener);
    }

    @Override
    default public List<RoleChangeColorListener> getRoleChangeColorListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Role.class, this.getId(), RoleChangeColorListener.class);
    }

    @Override
    default public ListenerManager<RoleChangeNameListener> addRoleChangeNameListener(RoleChangeNameListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Role.class, this.getId(), RoleChangeNameListener.class, listener);
    }

    @Override
    default public List<RoleChangeNameListener> getRoleChangeNameListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Role.class, this.getId(), RoleChangeNameListener.class);
    }

    @Override
    default public ListenerManager<RoleChangeHoistListener> addRoleChangeHoistListener(RoleChangeHoistListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Role.class, this.getId(), RoleChangeHoistListener.class, listener);
    }

    @Override
    default public List<RoleChangeHoistListener> getRoleChangeHoistListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Role.class, this.getId(), RoleChangeHoistListener.class);
    }

    @Override
    default public ListenerManager<RoleChangePermissionsListener> addRoleChangePermissionsListener(RoleChangePermissionsListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Role.class, this.getId(), RoleChangePermissionsListener.class, listener);
    }

    @Override
    default public List<RoleChangePermissionsListener> getRoleChangePermissionsListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Role.class, this.getId(), RoleChangePermissionsListener.class);
    }

    @Override
    default public ListenerManager<UserRoleRemoveListener> addUserRoleRemoveListener(UserRoleRemoveListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Role.class, this.getId(), UserRoleRemoveListener.class, listener);
    }

    @Override
    default public List<UserRoleRemoveListener> getUserRoleRemoveListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Role.class, this.getId(), UserRoleRemoveListener.class);
    }

    @Override
    default public ListenerManager<UserRoleAddListener> addUserRoleAddListener(UserRoleAddListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Role.class, this.getId(), UserRoleAddListener.class, listener);
    }

    @Override
    default public List<UserRoleAddListener> getUserRoleAddListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Role.class, this.getId(), UserRoleAddListener.class);
    }

    @Override
    default public ListenerManager<RoleDeleteListener> addRoleDeleteListener(RoleDeleteListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Role.class, this.getId(), RoleDeleteListener.class, listener);
    }

    @Override
    default public List<RoleDeleteListener> getRoleDeleteListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Role.class, this.getId(), RoleDeleteListener.class);
    }

    @Override
    default public ListenerManager<ServerChannelChangeOverwrittenPermissionsListener> addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener listener) {
        return ((DiscordApiImpl)this.getApi()).addObjectListener(Role.class, this.getId(), ServerChannelChangeOverwrittenPermissionsListener.class, listener);
    }

    @Override
    default public List<ServerChannelChangeOverwrittenPermissionsListener> getServerChannelChangeOverwrittenPermissionsListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Role.class, this.getId(), ServerChannelChangeOverwrittenPermissionsListener.class);
    }

    @Override
    default public <T extends RoleAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addRoleAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass()).filter(RoleAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).map(listenerClass -> ((DiscordApiImpl)this.getApi()).addObjectListener(Role.class, this.getId(), listenerClass, listener)).collect(Collectors.toList());
    }

    @Override
    default public <T extends RoleAttachableListener & ObjectAttachableListener> void removeRoleAttachableListener(T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass()).filter(RoleAttachableListener.class::isAssignableFrom).filter(ObjectAttachableListener.class::isAssignableFrom).map(listenerClass -> listenerClass).forEach(listenerClass -> ((DiscordApiImpl)this.getApi()).removeObjectListener(Role.class, this.getId(), listenerClass, listener));
    }

    @Override
    default public <T extends RoleAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getRoleAttachableListeners() {
        return ((DiscordApiImpl)this.getApi()).getObjectListeners(Role.class, this.getId());
    }

    @Override
    default public <T extends RoleAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        ((DiscordApiImpl)this.getApi()).removeObjectListener(Role.class, this.getId(), listenerClass, listener);
    }
}

