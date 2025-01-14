package net.javadiscord.javabot.systems.qotw.subcommands.qotw_points;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.javadiscord.javabot.Bot;
import net.javadiscord.javabot.command.Responses;
import net.javadiscord.javabot.command.interfaces.ISlashCommand;
import net.javadiscord.javabot.systems.qotw.dao.QuestionPointsRepository;

import java.sql.SQLException;

/**
 * Subcommand that allows staff-members to clear a user's QOTW-Account.
 */
public class ClearSubcommand implements ISlashCommand {
	@Override
	public ReplyCallbackAction handleSlashCommandInteraction(SlashCommandInteractionEvent event) {
		var memberOption = event.getOption("user");
		if (memberOption == null) {
			return Responses.error(event, "Missing required arguments.");
		}
		var member = memberOption.getAsMember();
		var memberId = member.getIdLong();
		try (var con = Bot.dataSource.getConnection()) {
			var repo = new QuestionPointsRepository(con);
			repo.update(memberId, 0);
			return Responses.success(event,
					"Cleared QOTW-Points",
					"Successfully cleared all QOTW-Points from user " + member.getUser().getAsMention());
		} catch (SQLException e) {
			e.printStackTrace();
			return Responses.error(event, "An Error occurred.");
		}

	}
}


