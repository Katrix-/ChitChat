/**
 * This file is part of ChitChat, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.katrix.chitchat.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.ImmutableList;

import io.github.katrix.chitchat.ChitChat;
import io.github.katrix.chitchat.chat.channels.Channel;
import io.github.katrix.chitchat.chat.channels.ChannelRoot;
import io.github.katrix.chitchat.io.ConfigSettings;
import io.github.katrix.chitchat.lib.LibKeys;

@SuppressWarnings("WeakerAccess")
public abstract class CommandBase implements CommandExecutor {

	/**
	 * Some names that don't behave to well.
	 */
	private static final ImmutableList<String> BADNAMES = ImmutableList.of("prefix", "description", "name");
	@Nullable
	private final CommandBase parent;

	protected CommandBase(@Nullable CommandBase parent) {
		this.parent = parent;
	}

	public abstract CommandSpec getCommand();

	public abstract String[] getAliases();

	public Optional<CommandBase> getParent() {
		return Optional.ofNullable(parent);
	}

	public ImmutableList<CommandBase> getChildren() {
		return ImmutableList.of();
	}

	public void registerHelp() {
		CmdHelp.registerCommandHelp(this, getCommandFamily(this));
		getChildren().forEach(CommandBase::registerHelp);
	}

	private List<CommandBase> getCommandFamily(CommandBase command) {
		List<CommandBase> list = new ArrayList<>();
		CommandBase relative = command;
		while(relative != null) {
			list.add(relative);
			relative = relative.parent;
		}
		return list;
	}

	protected void registerSubcommands(CommandSpec.Builder builder) {
		for(CommandBase command : getChildren()) {
			builder.child(command.getCommand(), command.getAliases());
		}
	}

	/**
	 * Checks that a channel name is unused and that it is not a bad name. If either check fails, it sends an errorto
	 * the player.
	 */
	protected boolean channelNameNotUsed(String channel, Player player) {
		if(BADNAMES.contains(channel)) {
			player.sendMessage(Text.of(TextColors.RED, channel + " is not an allow channel name"));
			return false;
		}

		if(!getChannelUser(player).isNameUnused(channel)) {
			player.sendMessage(Text.of(TextColors.RED, channel + " already exist"));
			return false;
		}

		return true;
	}

	/**
	 * Checks that the source is a player, and sends an error message if the source is not a player.
	 */
	protected boolean sourceIsPlayer(CommandSource src) {
		if(!(src instanceof Player)) {
			src.sendMessage(Text.of(TextColors.RED, "Only players can use this command"));
			return false;
		}
		return true;
	}

	/**
	 * Checks that a player has the permission to do something with a given channel, and sends an error message to the
	 * player if they don't have permission for the given channel.
	 */
	protected boolean permissionChannel(DataQuery channelQueryName, CommandSource subject, String basePerm) {
		if(!subject.hasPermission(basePerm + "." + channelQueryName.asString('.'))) {
			subject.sendMessage(Text.of(TextColors.RED, "You don't have the permissions to do that for this channel"));
			return false;
		}
		return true;
	}

	/**
	 * Checks that a channel exists, and sends an error message to the player if it doesn't.
	 */
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	protected boolean channelExists(CommandSource src, Optional<Channel> channel) {
		if(!channel.isPresent()) {
			src.sendMessage(Text.of(TextColors.RED, "This channel does not exist"));
			return false;
		}
		return true;
	}

	protected Channel getChannelUser(User user) {
		return user.get(LibKeys.USER_CHANNEL)
				.flatMap(q -> ChannelRoot.getRoot().getChannel(q))
				.orElse(ChannelRoot.getRoot());
	}

	protected ConfigSettings getCfg() {
		return ChitChat.getConfig();
	}
}
