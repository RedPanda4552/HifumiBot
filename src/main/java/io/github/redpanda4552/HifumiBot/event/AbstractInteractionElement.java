// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class AbstractInteractionElement {

  protected Instant createdInstant;
  protected UUID uuid;
  protected String userId;
  protected String commandName;

  public AbstractInteractionElement(String userId, String commandName) {
    this.createdInstant = Instant.now();
    this.userId = userId;
    this.commandName = commandName;
    this.uuid = UUID.nameUUIDFromBytes(new String(userId + commandName).getBytes());
  }
}
