/*
 * This file is part of PermissionBlock, licensed under the MIT License (MIT).
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
package io.github.katrix_.chitchat.io;

import java.nio.file.Path;
import java.util.Optional;

import com.google.common.reflect.TypeToken;

import io.github.katrix_.chitchat.chat.ChannelChitChat;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ConfigurateStorage extends ConfigurateBase implements IPersistentStorage {

	private static final String CHANNELS = "channel";

	public ConfigurateStorage(Path path, String name) {
		super(path, name, true);
	}

	@Override
	public Optional<ChannelChitChat.ChannelRoot> loadRootChannel() {
		try {
			return Optional.of(cfgRoot.getNode(CHANNELS).getValue(TypeToken.of(ChannelChitChat.ChannelRoot.class)));
		}
		catch(ObjectMappingException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	@Override
	public boolean saveRootChannel() {
		try {
			cfgRoot.getNode(CHANNELS).setValue(TypeToken.of(ChannelChitChat.ChannelRoot.class), ChannelChitChat.getRoot());
			saveFile();
			return true;
		}
		catch(ObjectMappingException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected void saveData() {
		//NO-OP We save and load on demand
	}
}
