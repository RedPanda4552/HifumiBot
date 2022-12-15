// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.event;

import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ButtonInteractionElement extends AbstractInteractionElement {

  public enum ButtonType {
    PRIMARY,
    SECONDARY,
    SUCCESS,
    DANGER
  }

  private String label;
  private ButtonType buttonType;
  private Button button;

  public ButtonInteractionElement(
      String userId, String commandName, String label, ButtonType buttonType) {
    super(userId, commandName);
    this.label = label;
    this.buttonType = buttonType;

    switch (buttonType) {
      case PRIMARY:
        this.button = Button.primary(uuid.toString(), label);
        break;
      case SECONDARY:
        this.button = Button.secondary(uuid.toString(), label);
        break;
      case SUCCESS:
        this.button = Button.success(uuid.toString(), label);
        break;
      case DANGER:
        this.button = Button.danger(uuid.toString(), label);
        break;
    }
  }

  public String getLabel() {
    return label;
  }

  public ButtonType getButtonType() {
    return buttonType;
  }

  public Button getButton() {
    return button;
  }
}
