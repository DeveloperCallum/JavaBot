package net.javadiscord.javabot.systems.qotw;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.javadiscord.javabot.Bot;
import net.javadiscord.javabot.data.config.guild.QOTWConfig;
import net.javadiscord.javabot.systems.qotw.submissions.SubmissionControlsManager;
import net.javadiscord.javabot.systems.qotw.submissions.dao.QOTWSubmissionRepository;
import net.javadiscord.javabot.tasks.jobs.DiscordApiJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.SQLException;

/**
 * Job which disables the Submission button.
 */
@Slf4j
public class QOTWCloseSubmissionsJob extends DiscordApiJob {
	@Override
	protected void execute(JobExecutionContext context, JDA jda) throws JobExecutionException {
		for (var guild : jda.getGuilds()) {
			// Disable 'Submit your Answer' button on latest QOTW
			var config = Bot.config.get(guild);
			var qotwConfig = config.getQotw();
			if (config.getModeration().getLogChannel() == null) continue;
			if (qotwConfig.getSubmissionChannel() == null || qotwConfig.getQuestionChannel() == null) continue;
			var message = getLatestQOTWMessage(qotwConfig.getQuestionChannel(), qotwConfig, jda);
			if (message == null) continue;
			message.editMessageComponents(ActionRow.of(Button.secondary("qotw-submission:closed", "Submissions closed").asDisabled())).queue();
			for (var thread : qotwConfig.getSubmissionChannel().getThreadChannels()) {
				try (var con = Bot.dataSource.getConnection()) {
					var repo = new QOTWSubmissionRepository(con);
					var optionalSubmission = repo.getSubmissionByThreadId(thread.getIdLong());
					if (optionalSubmission.isEmpty()) continue;
					new SubmissionControlsManager(thread.getGuild(), qotwConfig, optionalSubmission.get()).sendSubmissionControls();
				} catch (SQLException e) {
					throw new JobExecutionException(e);
				}
			}
		}
	}

	private Message getLatestQOTWMessage(MessageChannel channel, QOTWConfig config, JDA jda) {
		var history = channel.getHistory();
		Message message = null;
		while (message == null) {
			var messages = history.retrievePast(100).complete();
			for (var m : messages) {
				if (m.getAuthor().equals(jda.getSelfUser()) && m.getContentRaw().equals(config.getQOTWRole().getAsMention())) {
					message = m;
					break;
				}
			}
		}
		return message;
	}
}
