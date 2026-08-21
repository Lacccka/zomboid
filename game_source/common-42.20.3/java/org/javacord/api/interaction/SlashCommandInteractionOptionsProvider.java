/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteractionOption;

public interface SlashCommandInteractionOptionsProvider {
    public List<SlashCommandInteractionOption> getOptions();

    public List<SlashCommandInteractionOption> getArguments();

    default public Optional<SlashCommandInteractionOption> getOptionByName(String name) {
        return this.getOptions().stream().filter(option -> option.getName().equalsIgnoreCase(name)).findAny();
    }

    default public Optional<SlashCommandInteractionOption> getArgumentByName(String name) {
        return this.getArguments().stream().filter(option -> option.getName().equalsIgnoreCase(name)).findAny();
    }

    default public Optional<String> getArgumentStringRepresentationValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::getStringRepresentationValue);
    }

    default public Optional<String> getArgumentStringValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::getStringValue);
    }

    default public Optional<Long> getArgumentLongValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::getLongValue);
    }

    default public Optional<Boolean> getArgumentBooleanValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::getBooleanValue);
    }

    default public Optional<User> getArgumentUserValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::getUserValue);
    }

    default public Optional<CompletableFuture<User>> requestArgumentUserValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::requestUserValue);
    }

    default public Optional<ServerChannel> getArgumentChannelValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::getChannelValue);
    }

    default public Optional<Role> getArgumentRoleValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::getRoleValue);
    }

    default public Optional<Mentionable> getArgumentMentionableValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::getMentionableValue);
    }

    default public Optional<CompletableFuture<Mentionable>> requestArgumentMentionableValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::requestMentionableValue);
    }

    default public Optional<Double> getArgumentDecimalValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::getDecimalValue);
    }

    default public Optional<Attachment> getArgumentAttachmentValueByName(String name) {
        return this.getArgumentByName(name).flatMap(SlashCommandInteractionOption::getAttachmentValue);
    }

    default public Optional<SlashCommandInteractionOption> getOptionByIndex(int index) {
        return this.getOptions().stream().skip(index).findFirst();
    }

    default public Optional<SlashCommandInteractionOption> getArgumentByIndex(int index) {
        return this.getArguments().stream().skip(index).findFirst();
    }

    default public Optional<String> getArgumentStringRepresentationValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::getStringRepresentationValue);
    }

    default public Optional<String> getArgumentStringValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::getStringValue);
    }

    default public Optional<Long> getArgumentLongValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::getLongValue);
    }

    default public Optional<Boolean> getArgumentBooleanValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::getBooleanValue);
    }

    default public Optional<User> getArgumentUserValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::getUserValue);
    }

    default public Optional<CompletableFuture<User>> requestArgumentUserValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::requestUserValue);
    }

    default public Optional<ServerChannel> getArgumentChannelValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::getChannelValue);
    }

    default public Optional<Role> getArgumentRoleValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::getRoleValue);
    }

    default public Optional<Mentionable> getArgumentMentionableValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::getMentionableValue);
    }

    default public Optional<CompletableFuture<Mentionable>> requestArgumentMentionableValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::requestMentionableValue);
    }

    default public Optional<Double> getArgumentDecimalValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::getDecimalValue);
    }

    default public Optional<Attachment> getArgumentAttachmentValueByIndex(int index) {
        return this.getArgumentByIndex(index).flatMap(SlashCommandInteractionOption::getAttachmentValue);
    }
}

