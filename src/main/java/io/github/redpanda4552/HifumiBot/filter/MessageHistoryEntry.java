// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.filter;

import java.time.Instant;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;

@Getter
public class MessageHistoryEntry {

  private final String userId;
  private final String serverId;
  private final String channelId;
  private final String messageId;
  private final String messageContent;
  private final Instant instant;

  public MessageHistoryEntry(Message msg) {
    this.userId = msg.getAuthor().getId();
    this.serverId = msg.getGuild().getId();
    this.channelId = msg.getChannel().getId();
    this.messageId = msg.getId();
    this.messageContent = msg.getContentRaw();
    this.instant = msg.getTimeCreated().toInstant();
  }
}
