package io.github.redpanda4552.HifumiBot.command.slash;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.github.redpanda4552.HifumiBot.BrowsableEmbed;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.database.objects.AutoModEventObject;
import io.github.redpanda4552.HifumiBot.database.objects.MemberEventObject;
import io.github.redpanda4552.HifumiBot.database.objects.WarezEventObject;
import io.github.redpanda4552.HifumiBot.util.DateTimeUtils;
import io.github.redpanda4552.HifumiBot.util.MemberUtils;
import io.github.redpanda4552.HifumiBot.util.UserUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CommandWhois extends AbstractSlashCommand {

    @Override
    public void onExecute(SlashCommandInteractionEvent event) {
        OptionMapping userIdOpt = event.getOption("user");
        
        if (userIdOpt == null) {
            event.reply("Missing required option 'user'").setEphemeral(true).queue();
            return;
        }

        long userId = userIdOpt.getAsLong();
        Optional<User> userOpt = UserUtils.getOrRetrieveUser(userId);
        boolean userRetrievable = userOpt.isPresent();

        if (!userRetrievable) {
            event.reply("User could not be retrieved, has their account been deleted?").setEphemeral(true).queue();
            return;
        }

        event.deferReply().setEphemeral(true).queue();

        ArrayList<MessageEmbed> pages = new ArrayList<MessageEmbed>();

        // User page
        User user = userOpt.get();
        EmbedBuilder userEmbedBuilder = new EmbedBuilder();
        userEmbedBuilder.setColor(Color.BLUE);
        userEmbedBuilder.setTitle("General Info");
        userEmbedBuilder.setImage(user.getEffectiveAvatarUrl());
        userEmbedBuilder.addField("User ID", user.getId(), true);
        userEmbedBuilder.addField("Username", user.getName(), true);
        userEmbedBuilder.addField("Display Name", user.getEffectiveName(), true);
        userEmbedBuilder.addField("User Mention", user.getAsMention(), true);
        userEmbedBuilder.addBlankField(false);
        userEmbedBuilder.addField("Created Date", user.getTimeCreated().format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC", true);
        userEmbedBuilder.addField("Account Age", UserUtils.getAgeOfUserAsPrettyString(user), true);
        pages.add(userEmbedBuilder.build());

        // Member page
        Optional<Member> memberOpt = MemberUtils.forceRetrieveMember(event.getGuild(), userId);
        boolean memberRetrievable = memberOpt.isPresent();
        
        if (memberRetrievable) {
            Member member = memberOpt.get();
            EmbedBuilder memberEmbedBuilder = new EmbedBuilder();
            memberEmbedBuilder.setColor(Color.GREEN);
            memberEmbedBuilder.setTitle("Server Profile");
            memberEmbedBuilder.setImage(member.getEffectiveAvatarUrl());
            memberEmbedBuilder.addField("Server Nickname", member.getEffectiveName(), true);
            memberEmbedBuilder.addField("Server Mention", member.getAsMention(), true);
            memberEmbedBuilder.addBlankField(false);
            memberEmbedBuilder.addField("Active Client Types", member.getActiveClients().toString(), false);
            memberEmbedBuilder.addField("Member Flags", member.getFlags().toString(), false);
            
            StringBuilder rolesBuilder = new StringBuilder();
            
            for (Role role : member.getRoles()) {
                rolesBuilder.append(role.getAsMention());
                rolesBuilder.append(" ");
            }

            memberEmbedBuilder.addField("Roles", rolesBuilder.toString().trim(), false);
            memberEmbedBuilder.addField("Joined Date", member.getTimeJoined().format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC", true);
            pages.add(memberEmbedBuilder.build());
        }

        // Member event pages
        ArrayList<ArrayList<MemberEventObject>> memberEventsPaginated = Database.getAllMemberEventsPaginated(userId);
        int i = 1;
        
        for (ArrayList<MemberEventObject> memberEventList : memberEventsPaginated) {
            EmbedBuilder memberEventEmbedBuilder = new EmbedBuilder();
            memberEventEmbedBuilder.setColor(Color.YELLOW);
            memberEventEmbedBuilder.setTitle("Joins, Leaves, Bans (Page " + i++ + ")");
            memberEventEmbedBuilder.setFooter("Sorted most recent events first, 10 events per page.");

            for (MemberEventObject memberEvent : memberEventList) {
                OffsetDateTime time = DateTimeUtils.longToOffsetDateTime(memberEvent.getTimestamp());
                String formatStr = time.format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC";
                memberEventEmbedBuilder.addField(formatStr, memberEvent.getAction().toString(), false);
            }

            pages.add(memberEventEmbedBuilder.build());
        }
        
        // Warez event pages
        ArrayList<ArrayList<WarezEventObject>> warezEventsPaginated = Database.getAllWarezActionsForUserPaginated(userId);
        int j = 1;

        for (ArrayList<WarezEventObject> warezEventList : warezEventsPaginated) {
            EmbedBuilder warezEventEmbedBuilder = new EmbedBuilder();
            warezEventEmbedBuilder.setColor(Color.YELLOW);
            warezEventEmbedBuilder.setTitle("Warez History (Page " + j++ + ")");
            warezEventEmbedBuilder.setFooter("Sorted most recent events first, 10 events per page.");

            for (WarezEventObject warezEvent : warezEventList) {
                OffsetDateTime time = DateTimeUtils.longToOffsetDateTime(warezEvent.getTimestamp());
                String formatStr = time.format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC";
                warezEventEmbedBuilder.addField(formatStr, warezEvent.getAction().toString(), false);
            }

            pages.add(warezEventEmbedBuilder.build());
        }

        // Automod event pages
        ArrayList<ArrayList<AutoModEventObject>> autoModEventsPaginated = Database.getAllAutoModEventsPaginated(userId);
        int k = 1;

        for (ArrayList<AutoModEventObject> autoModEventList : autoModEventsPaginated) {
            EmbedBuilder autoModEventEmbedBuilder = new EmbedBuilder();
            autoModEventEmbedBuilder.setColor(Color.ORANGE);
            autoModEventEmbedBuilder.setTitle("AutoMod History (Page " + k++ + ")");
            autoModEventEmbedBuilder.setFooter("Sorted most recent events first, 10 events per page. Content truncated to 1000 chars.");

            for (AutoModEventObject autoModEvent : autoModEventList) {
                OffsetDateTime time = DateTimeUtils.longToOffsetDateTime(autoModEvent.getTimestamp());
                String formatStr = time.format(DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss")) + " UTC";
                String value = (autoModEvent.getMatchedContent() != null ? autoModEvent.getMatchedContent() : autoModEvent.getContent());
                autoModEventEmbedBuilder.addField(formatStr, "```\n" + StringUtils.truncate(value, 1000) + "\n```", false);
            }

            pages.add(autoModEventEmbedBuilder.build());
        }
        
        BrowsableEmbed embed = new BrowsableEmbed(this.defineSlashCommand().getName(), event.getIdLong(), event.getUser().getIdLong(), pages);
        Optional<MessageEmbed> firstPageOpt = embed.getCurrentPage();

        if (firstPageOpt.isPresent()) {
            ArrayList<Button> buttons = embed.refreshButtonOptions();
            BrowsableEmbed.embedCache.put(event.getIdLong(), embed);
            event.getHook().sendMessageEmbeds(firstPageOpt.get())
                    .addActionRow(buttons)
                    .queue();
        } else {
            event.getHook().editOriginal("Failed to generate embeds").queue();
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        return Commands.slash("whois", "Report of information on a user")
            .addOption(OptionType.USER, "user", "User to generate report on", true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    public void handleButtonEvent(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        
        String[] parts = componentId.split(":");

        if (parts.length != 4) {
            return;
        }

        String action = parts[1];
        Long eventId = null;

        try {
            eventId = Long.valueOf(parts[2]);
        } catch (Exception e) {
            event.getHook().sendMessage("Malformed event ID").setEphemeral(true).queue();
            return;
        }
        
        String userId = parts[3];

        // Test if the event that originally created the browsable embed is in memory.
        BrowsableEmbed existingBrowsableEmbed = BrowsableEmbed.embedCache.get(eventId);

        if (existingBrowsableEmbed == null) {
            event.getHook().sendMessage("This embed has expired, you should re-run the original command instead").setEphemeral(true).queue();
            return;
        }

        Optional<MessageEmbed> destinationPage = Optional.empty();

        switch (action) {
            case "prev": {
                destinationPage = existingBrowsableEmbed.prevPage();
                break;
            }
            case "next": {
                destinationPage = existingBrowsableEmbed.nextPage();
                break;
            }
            default: {
                break;
            }
        }

        if (destinationPage.isPresent()) {
            ArrayList<Button> buttons = existingBrowsableEmbed.refreshButtonOptions();
            event.getHook().editOriginalEmbeds(destinationPage.get())
                .setActionRow(buttons)
                .queue();
        }
    }
}
