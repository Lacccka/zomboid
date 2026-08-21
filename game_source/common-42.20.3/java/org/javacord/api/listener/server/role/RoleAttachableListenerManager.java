/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.role;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelChangeOverwrittenPermissionsListener;
import org.javacord.api.listener.server.role.RoleAttachableListener;
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

public interface RoleAttachableListenerManager {
    public ListenerManager<RoleChangePositionListener> addRoleChangePositionListener(RoleChangePositionListener var1);

    public List<RoleChangePositionListener> getRoleChangePositionListeners();

    public ListenerManager<RoleChangeMentionableListener> addRoleChangeMentionableListener(RoleChangeMentionableListener var1);

    public List<RoleChangeMentionableListener> getRoleChangeMentionableListeners();

    public ListenerManager<RoleChangeColorListener> addRoleChangeColorListener(RoleChangeColorListener var1);

    public List<RoleChangeColorListener> getRoleChangeColorListeners();

    public ListenerManager<RoleChangeNameListener> addRoleChangeNameListener(RoleChangeNameListener var1);

    public List<RoleChangeNameListener> getRoleChangeNameListeners();

    public ListenerManager<RoleChangeHoistListener> addRoleChangeHoistListener(RoleChangeHoistListener var1);

    public List<RoleChangeHoistListener> getRoleChangeHoistListeners();

    public ListenerManager<RoleChangePermissionsListener> addRoleChangePermissionsListener(RoleChangePermissionsListener var1);

    public List<RoleChangePermissionsListener> getRoleChangePermissionsListeners();

    public ListenerManager<UserRoleRemoveListener> addUserRoleRemoveListener(UserRoleRemoveListener var1);

    public List<UserRoleRemoveListener> getUserRoleRemoveListeners();

    public ListenerManager<UserRoleAddListener> addUserRoleAddListener(UserRoleAddListener var1);

    public List<UserRoleAddListener> getUserRoleAddListeners();

    public ListenerManager<RoleDeleteListener> addRoleDeleteListener(RoleDeleteListener var1);

    public List<RoleDeleteListener> getRoleDeleteListeners();

    public ListenerManager<ServerChannelChangeOverwrittenPermissionsListener> addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener var1);

    public List<ServerChannelChangeOverwrittenPermissionsListener> getServerChannelChangeOverwrittenPermissionsListeners();

    public <T extends RoleAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addRoleAttachableListener(T var1);

    public <T extends RoleAttachableListener & ObjectAttachableListener> void removeRoleAttachableListener(T var1);

    public <T extends RoleAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getRoleAttachableListeners();

    public <T extends RoleAttachableListener & ObjectAttachableListener> void removeListener(Class<T> var1, T var2);
}

