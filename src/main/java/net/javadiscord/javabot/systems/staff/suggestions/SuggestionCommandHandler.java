package net.javadiscord.javabot.systems.staff.suggestions;

import net.javadiscord.javabot.command.DelegatingCommandHandler;
import net.javadiscord.javabot.systems.staff.suggestions.subcommands.AcceptSuggestionSubcommand;
import net.javadiscord.javabot.systems.staff.suggestions.subcommands.ClearSuggestionSubcommand;
import net.javadiscord.javabot.systems.staff.suggestions.subcommands.DeclineSuggestionSubcommand;

/**
 * Handler class for all suggestion related commands.
 */
public class SuggestionCommandHandler extends DelegatingCommandHandler {
	/**
	 * Adds all subcommands {@link DelegatingCommandHandler}.
	 */
	public SuggestionCommandHandler() {
		addSubcommand("accept", new AcceptSuggestionSubcommand());
		addSubcommand("decline", new DeclineSuggestionSubcommand());
		addSubcommand("clear", new ClearSuggestionSubcommand());
	}
}
