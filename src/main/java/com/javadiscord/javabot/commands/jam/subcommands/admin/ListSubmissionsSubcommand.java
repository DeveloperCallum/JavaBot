package com.javadiscord.javabot.commands.jam.subcommands.admin;

import com.javadiscord.javabot.Bot;
import com.javadiscord.javabot.commands.jam.dao.JamSubmissionRepository;
import com.javadiscord.javabot.commands.jam.model.Jam;
import com.javadiscord.javabot.commands.jam.model.JamSubmission;
import com.javadiscord.javabot.commands.jam.subcommands.ActiveJamSubcommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListSubmissionsSubcommand extends ActiveJamSubcommand {
	public ListSubmissionsSubcommand() {
		super(true);
	}

	@Override
	protected void handleJamCommand(SlashCommandEvent event, Jam activeJam, Connection con) throws Exception {
		OptionMapping pageOption = event.getOption("page");
		OptionMapping userOption = event.getOption("user");
		int page = 1;
		if (pageOption != null) {
			page = (int) pageOption.getAsLong();
		}
		Long userId = null;
		if (userOption != null) {
			userId = userOption.getAsUser().getIdLong();
		}
		List<JamSubmission> submissions = new JamSubmissionRepository(con).getSubmissions(activeJam, page, userId);

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle("Submissions")
				.setColor(Color.decode(Bot.getProperty("jamEmbedColor")));
		for (JamSubmission sub : submissions) {
			User user = event.getJDA().getUserById(sub.getUserId());
			String userName = user == null ? "Unknown user" : user.getAsTag();
			String timestamp = sub.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss 'UTC'"));
			embedBuilder.addField(
					String.format("`%d` %s at %s", sub.getId(), userName, timestamp),
					"Link: *" + sub.getSourceLink() + "*\n> " + sub.getDescription(),
					false
			);
		}
		embedBuilder.setFooter("Page " + page);
		event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
	}
}
