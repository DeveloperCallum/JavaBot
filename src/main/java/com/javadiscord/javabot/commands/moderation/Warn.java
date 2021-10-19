package com.javadiscord.javabot.commands.moderation;

import com.javadiscord.javabot.Bot;
import com.javadiscord.javabot.commands.Responses;
import com.javadiscord.javabot.commands.SlashCommandHandler;
import com.javadiscord.javabot.utils.Misc;
import com.javadiscord.javabot.utils.TimeUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.bson.Document;

import java.time.Instant;
import java.time.LocalDateTime;

import static com.javadiscord.javabot.service.Startup.mongoClient;
import static com.mongodb.client.model.Filters.eq;

public class Warn implements SlashCommandHandler {

	public void addToDatabase(String memID, String guildID, String reason) {
		Document doc = new Document("guild_id", guildID)
				.append("user_id", memID)
				.append("date", LocalDateTime.now().format(TimeUtils.STANDARD_FORMATTER))
				.append("reason", reason);
		mongoClient.getDatabase("userdata")
				.getCollection("warns")
				.insertOne(doc);
	}

	public void deleteAllDocs(String memID) {
		mongoClient.getDatabase("userdata")
				.getCollection("warns")
				.deleteMany(eq("user_id", memID));
	}

	public void warn(Member member, Guild guild, String reason) throws Exception {
		int warnPoints = getWarnCount(member);
		if ((warnPoints + 1) >= 3) {
			new Ban().ban(member, "3/3 warns");
		} else {
			addToDatabase(member.getId(), guild.getId(), reason);
		}
	}

	public int getWarnCount(Member member) {
		return (int) mongoClient.getDatabase("userdata")
				.getCollection("warns")
				.countDocuments(eq("user_id", member.getId()));
	}

	@Override
	public ReplyAction handle(SlashCommandEvent event) {
		var userOption = event.getOption("user");
		if (userOption == null) {
			return Responses.warning(event, "Missing required user.");
		}

		Member member = userOption.getAsMember();
		if (member == null) {
			return Responses.warning(event, "The given user is not a member of this guild.");
		}

		OptionMapping reasonOption = event.getOption("reason");
		String reason = reasonOption == null ? "None" : reasonOption.getAsString();
		int warnPoints = getWarnCount(member);
		var eb = new EmbedBuilder()
				.setColor(Bot.config.get(event.getGuild()).getSlashCommand().getWarningColor())
				.setAuthor(member.getUser().getAsTag() + " | Warn (" + (warnPoints + 1) + "/3)", null, member.getUser().getEffectiveAvatarUrl())
				.addField("Name", "```" + member.getUser().getAsTag() + "```", true)
				.addField("Moderator", "```" + event.getUser().getAsTag() + "```", true)
				.addField("ID", "```" + member.getId() + "```", false)
				.addField("Reason", "```" + reason + "```", false)
				.setFooter("ID: " + member.getId())
				.setTimestamp(Instant.now())
				.build();

		Misc.sendToLog(event.getGuild(), eb);
		member.getUser().openPrivateChannel().complete().sendMessageEmbeds(eb).queue();

		try {
			warn(member, event.getGuild(), reason);
			return event.replyEmbeds(eb);
		} catch (Exception e) {
			return Responses.error(event, e.getMessage());
		}
	}
}

