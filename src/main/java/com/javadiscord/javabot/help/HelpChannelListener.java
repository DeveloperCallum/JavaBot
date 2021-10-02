package com.javadiscord.javabot.help;

import com.javadiscord.javabot.Bot;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * This listener is responsible for handling messages that are sent in one or
 * more designated help channels.
 */
public class HelpChannelListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;

		var config = Bot.config.get(event.getGuild()).getHelp();
		TextChannel channel = event.getChannel();

		// If a message was sent in an open text channel, reserve it.
		if (config.getOpenChannelCategory().equals(channel.getParent())) {
			try {
				new HelpChannelManager(config).reserve(channel, event.getAuthor(), event.getMessage());
			} catch (SQLException e) {
				e.printStackTrace();
				channel.sendMessage("An error occurred and this channel could not be reserved.").queue();
			}
		}
	}
}
