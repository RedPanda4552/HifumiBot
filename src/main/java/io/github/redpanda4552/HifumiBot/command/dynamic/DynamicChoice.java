// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.command.dynamic;

import io.github.redpanda4552.HifumiBot.util.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DynamicChoice {

  protected String name, description, title, body, imageURL;

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

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBody() {
    return this.body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getImageURL() {
    return this.imageURL;
  }

  public void setImageURL(String imageURL) {
    this.imageURL = imageURL;
  }
}
