// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.dynamic;

import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Getter
@Setter
public class DynamicChoice {

  protected String name;
  protected String description;
  protected String title;
  protected String body;
  protected String imageURL;

  public DynamicChoice(
      String name, String description, String title, String body, String imageURL) {
    this.name = name;
    this.description = description;
    this.title = title;
    this.body = body;
    this.imageURL = imageURL;
  }

  public void execute(SlashCommandInteractionEvent event) {
    execute(event, null);
  }

  public void execute(SlashCommandInteractionEvent event, Member pingMember) {
    MessageBuilder mb = new MessageBuilder();

    if (pingMember != null) {
      mb.setContent(pingMember.getAsMention());
    }

    EmbedBuilder eb = EmbedUtil.newFootedEmbedBuilder(event.getMember());
    eb.setTitle(title);
    eb.setDescription(body);
    eb.setImage(imageURL);

    mb.setEmbeds(eb.build());
    event.reply(mb.build()).queue();
  }
}
