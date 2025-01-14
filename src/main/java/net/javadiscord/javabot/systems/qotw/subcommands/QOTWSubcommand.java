package net.javadiscord.javabot.systems.qotw.subcommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.javadiscord.javabot.Bot;
import net.javadiscord.javabot.command.Responses;
import net.javadiscord.javabot.command.interfaces.ISlashCommand;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract parent class for all QOTW subcommands, which handles the standard
 * behavior of preparing a connection and obtaining the guild id; these two
 * things are required for all QOTW subcommands.
 */
public abstract class QOTWSubcommand implements ISlashCommand {
	@Override
	public ReplyCallbackAction handleSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.getGuild() == null) {
			return Responses.warning(event, "This command can only be used in the context of a guild.");
		}
		try (Connection con = Bot.dataSource.getConnection()) {
			con.setAutoCommit(false);
			var reply = this.handleCommand(event, con, event.getGuild().getIdLong());
			con.commit();
			return reply;
		} catch (SQLException e) {
			e.printStackTrace();
			return Responses.error(event, "An error occurred: " + e.getMessage());
		}
	}

	protected abstract ReplyCallbackAction handleCommand(SlashCommandInteractionEvent event, Connection con, long guildId) throws SQLException;
}
