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

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.ImmutableMap;

import io.github.katrix.chitchat.lib.LibPerm;
import io.github.katrix.chitchat.io.ConfigSettings;
import io.github.katrix.chitchat.lib.LibCommandKey;

public class CmdPM extends CommandBase {

	public static final CmdPM INSTANCE = new CmdPM();
	private final Map<CommandSource, CommandSource> conversations = new WeakHashMap<>();

	private CmdPM() {
		super(null);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<Player> optPlayer = args.getOne(LibCommandKey.PLAYER);
		if(optPlayer.isPresent()) {
			Player player = optPlayer.get();
			String message = args.<String>getOne(LibCommandKey.MESSAGE).orElse("");
			conversations.put(src, player);
			conversations.put(player, src);

			Map<String, TextElement> templateMap = ImmutableMap.of(ConfigSettings.TEMPLATE_PLAYER, Text.of(player.getName()), ConfigSettings.TEMPLATE_MESSAGE,
					Text.of(message));
			player.sendMessage(getCfg().getPmReceiverTemplate(), templateMap);
			src.sendMessage(getCfg().getPmSenderTemplate(), templateMap);

			if(getCfg().getChatPling()) {
				player.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, player.getLocation().getPosition(), 0.5D);
			}
			return CommandResult.success();
		}
		src.sendMessage(Text.of(TextColors.RED, "Player not found"));
		return CommandResult.empty();
	}

	@Override
	public CommandSpec getCommand() {
		return CommandSpec.builder()
				.description(Text.of("Send a private message to someone else"))
				.permission(LibPerm.PM)
				.arguments(GenericArguments.player(LibCommandKey.PLAYER), GenericArguments.remainingJoinedStrings(LibCommandKey.MESSAGE))
				.executor(this)
				.build();
	}

	@Override
	public String[] getAliases() {
		return new String[] {"pm", "msg", "tell", "w"};
	}

	public Optional<CommandSource> getConversationPartner(CommandSource player) {
		return Optional.ofNullable(conversations.get(player));
	}
}
