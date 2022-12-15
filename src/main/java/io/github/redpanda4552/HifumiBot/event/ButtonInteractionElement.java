// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.event;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Getter
public class ButtonInteractionElement extends AbstractInteractionElement {

  public enum ButtonType {
    PRIMARY,
    SECONDARY,
    SUCCESS,
    DANGER
  }

  private final String label;
  private final ButtonType buttonType;
  private Button button;

  public ButtonInteractionElement(
      String userId, String commandName, String label, ButtonType buttonType) {
    super(userId, commandName);
    this.label = label;
    this.buttonType = buttonType;

    switch (buttonType) {
      case PRIMARY -> this.button = Button.primary(uuid.toString(), label);
      case SECONDARY -> this.button = Button.secondary(uuid.toString(), label);
      case SUCCESS -> this.button = Button.success(uuid.toString(), label);
      case DANGER -> this.button = Button.danger(uuid.toString(), label);
    }
  }
}
