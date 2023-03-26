package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandTranslate extends AbstractSlashCommand {

    @Override
    protected void onExecute(SlashCommandInteractionEvent event) {
        OptionMapping langOpt = event.getOption("lang");
        OptionMapping textOpt = event.getOption("text");
        OptionMapping userOpt = event.getOption("user");
        
        if (langOpt == null) {
            event.reply("Required option `lang` missing").setEphemeral(true);
            return;
        }
        
        if (textOpt == null) {
            event.reply("Required option `text` missing").setEphemeral(true);
            return;
        }
        
        Member member = null;

        if (userOpt != null) {
            member = userOpt.getAsMember();
        }

        event.deferReply().queue();
        String translated = HifumiBot.getSelf().getChatGPT().translate(event.getUser().getId(), textOpt.getAsString(), langOpt.getAsString());
        
        if (translated != null) {
            EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(event.getMember());
            eb.setTitle("Chat GPT Translation");
            eb.setDescription(translated);
            MessageBuilder mb = new MessageBuilder();
            
            if (member != null) {
                mb.append(member.getAsMention());
            }

            mb.setEmbeds(eb.build());
            event.getHook().editOriginal(mb.build()).queue();
        } else {
            event.getHook().editOriginal("Either an error occurred, or you are being rate limited. Please try again in a few minutes.").queue();
        }
    }

    @Override
    protected CommandData defineSlashCommand() {
        return Commands.slash("translate", "Translate some text using Chat GPT")
                .addOption(OptionType.STRING, "lang", "Target language", true)
                .addOption(OptionType.STRING, "text", "Text to translate", true)
                .addOption(OptionType.USER, "user", "User to ping")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }
}
