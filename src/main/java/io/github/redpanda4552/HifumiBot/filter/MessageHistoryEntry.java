// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.filter;

import java.time.Instant;
import net.dv8tion.jda.api.entities.Message;

public class MessageHistoryEntry {

  private String userId;
  private String serverId;
  private String channelId;
  private String messageId;
  private String messageContent;
  private Instant instant;

  public MessageHistoryEntry(Message msg) {
    this.userId = msg.getAuthor().getId();
    this.serverId = msg.getGuild().getId();
    this.channelId = msg.getChannel().getId();
    this.messageId = msg.getId();
    this.messageContent = msg.getContentRaw();
    this.instant = msg.getTimeCreated().toInstant();
  }

  public String getUserId() {
    return userId;
  }

  public String getServerId() {
    return serverId;
  }

  public String getChannelId() {
    return channelId;
  }

  public String getMessageId() {
    return messageId;
  }

  public String getMessageContent() {
    return messageContent;
  }

  public Instant getInstant() {
    return instant;
  }
}
