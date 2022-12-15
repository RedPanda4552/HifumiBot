// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.slash;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.AbstractSlashCommand;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandPFP extends AbstractSlashCommand {

  private void setAvatar(String imageUrl) throws IOException, MalformedURLException {
    URL url = new URL(imageUrl);
    BufferedImage bImage = ImageIO.read(url);
    ByteArrayOutputStream oStream = new ByteArrayOutputStream();
    ImageIO.write(bImage, "png", oStream);
    HifumiBot.getSelf()
        .getJda()
        .getSelfUser()
        .getManager()
        .setAvatar(Icon.from(oStream.toByteArray()))
        .complete();
  }

  @Override
  protected void onExecute(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    try {
      String imageUrl = event.getOption("image-url").getAsString();
      setAvatar(imageUrl);
      EmbedBuilder eb = new EmbedBuilder();
      eb.setTitle("Avatar set!");
      eb.setDescription(imageUrl);
      eb.setImage(imageUrl);
      event.getHook().sendMessageEmbeds(eb.build()).queue();
    } catch (Exception e) {
      event.getHook().sendMessage("An error occurred while setting the avatar.").queue();
      Messaging.logException("CommandPFP", "onExecute", e);
    }
  }

  @Override
  protected CommandData defineSlashCommand() {
    return Commands.slash("pfp", "Set the bot's avatar")
        .addOption(OptionType.STRING, "image-url", "URL pointing to the new avatar image", true)
        .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
  }
}
