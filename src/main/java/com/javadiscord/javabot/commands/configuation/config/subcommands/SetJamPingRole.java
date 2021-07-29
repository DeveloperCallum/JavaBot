package com.javadiscord.javabot.commands.configuation.config.subcommands;

import com.javadiscord.javabot.commands.configuation.config.ConfigCommandHandler;
import com.javadiscord.javabot.other.Database;
import com.javadiscord.javabot.other.Embeds;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class SetJamPingRole implements ConfigCommandHandler {

    @Override
    public void handle(SlashCommandEvent event) {

        Role role = event.getOption("role").getAsRole();
        new Database().queryConfig(event.getGuild().getId(), "roles.jam_ping_rid", role.getId());
        event.replyEmbeds(Embeds.configEmbed(event, "Jam Ping Role", "Jam Ping Role successfully changed to", null, role.getId(), true, false, true)).queue();

    }
}