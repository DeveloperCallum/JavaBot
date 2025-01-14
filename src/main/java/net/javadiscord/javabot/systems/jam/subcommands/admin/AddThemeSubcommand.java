package net.javadiscord.javabot.systems.jam.subcommands.admin;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.javadiscord.javabot.command.Responses;
import net.javadiscord.javabot.data.config.guild.JamConfig;
import net.javadiscord.javabot.systems.jam.dao.JamThemeRepository;
import net.javadiscord.javabot.systems.jam.model.Jam;
import net.javadiscord.javabot.systems.jam.model.JamTheme;
import net.javadiscord.javabot.systems.jam.subcommands.ActiveJamSubcommand;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Subcommand that allows jam-admins to add themes to vote on for the current JavaJam.
 */
public class AddThemeSubcommand extends ActiveJamSubcommand {
	@Override
	protected ReplyCallbackAction handleJamCommand(SlashCommandInteractionEvent event, Jam activeJam, Connection con, JamConfig config) throws SQLException {
		OptionMapping nameOption = event.getOption("name");
		OptionMapping descriptionOption = event.getOption("description");
		if (nameOption == null || descriptionOption == null) {
			return Responses.warning(event, "Invalid command arguments.");
		}

		JamThemeRepository themeRepository = new JamThemeRepository(con);

		// First check that we don't have too many themes, and make sure none of them have the same name.
		List<JamTheme> themes = themeRepository.getThemes(activeJam);
		if (themes.size() >= 9) {
			return Responses.warning(event, "Too Many Themes", "Cannot have more than 9 themes. Remove some if you want to add new ones.");
		}

		JamTheme theme = new JamTheme();
		theme.setName(nameOption.getAsString());
		theme.setDescription(descriptionOption.getAsString());

		for (JamTheme existingTheme : themes) {
			if (existingTheme.getName().equals(theme.getName())) {
				return Responses.warning(event, "Theme Already Exists", "There is already a theme with that name.");
			}
		}

		themeRepository.addTheme(activeJam, theme);
		return Responses.success(event, "Theme Added", "Added theme **" + theme.getName() + "** to the jam.");
	}
}
