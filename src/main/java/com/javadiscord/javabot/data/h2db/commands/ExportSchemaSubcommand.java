package com.javadiscord.javabot.data.h2db.commands;

import com.javadiscord.javabot.Bot;
import com.javadiscord.javabot.commands.SlashCommandHandler;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * This subcommand exports the database schema to a file, and uploads that file
 * to the channel in which the command was received.
 */
public class ExportSchemaSubcommand implements SlashCommandHandler {
	private static final Path SCHEMA_FILE = Path.of("___schema.sql");

	@Override
	public ReplyAction handle(SlashCommandEvent event) {
		var includeDataOption = event.getOption("include-data");
		boolean includeData = includeDataOption != null && includeDataOption.getAsBoolean();
		Bot.asyncPool.submit(() -> {
			try {
				var con = Bot.dataSource.getConnection();
				var stmt = con.createStatement();
				boolean success = stmt.execute(String.format("SCRIPT %s TO '%s';", includeData ? "" : "NODATA", SCHEMA_FILE));
				if (!success) {
					event.getHook().sendMessage("Exporting the schema was not successful.").queue();
				} else {
					event.getHook().sendMessage("The export was successful.").queue();
					event.getChannel().sendFile(Files.newInputStream(SCHEMA_FILE), "schema.sql").complete();
					Files.delete(SCHEMA_FILE);
				}
			} catch (SQLException | IOException e) {
				e.printStackTrace();
				event.getHook().sendMessage("An error occurred, and the export could not be made: " + e.getMessage()).queue();
			}
		});
		return event.deferReply(true);
	}
}