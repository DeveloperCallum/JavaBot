package net.javadiscord.javabot.systems.moderation.warn.subcommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.javadiscord.javabot.command.Responses;
import net.javadiscord.javabot.command.interfaces.ISlashCommand;
import net.javadiscord.javabot.systems.moderation.ModerationService;

/**
 * Subcommand that allows staff-members to discard any warn by their id.
 */
public class DiscardWarnByIdSubCommand implements ISlashCommand {
	@Override
	public ReplyCallbackAction handleSlashCommandInteraction(SlashCommandInteractionEvent event) {
		var idOption = event.getOption("id");
		if (idOption == null) {
			return Responses.error(event, "Id may not be empty!");
		}
		var id = idOption.getAsLong();
		var moderationService = new ModerationService(event.getInteraction());
		if (moderationService.discardWarnById(id, event.getUser())) {
			return Responses.success(event, "Warn Discarded", String.format("Successfully discarded Warn with id `%s`", id));
		} else {
			return Responses.error(event, String.format("Could not find Warn with id `%s`", id));
		}
	}
}

