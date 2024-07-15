package io.github.redpanda4552.HifumiBot.event;

import io.github.redpanda4552.HifumiBot.modal.BanHandler;
import io.github.redpanda4552.HifumiBot.modal.DyncmdHandler;
import io.github.redpanda4552.HifumiBot.modal.PromptHandler;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ModalEventListener extends ListenerAdapter {

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();

        switch (modalId) {
            case "prompt": {
                PromptHandler.handle(event);
                break;
            }
            case "dyncmd": {
                DyncmdHandler.handle(event);
                break;
            }
            case "ban": {
                BanHandler.handle(event);
                break;
            }
            default: {
                Messaging.logInfo("ModalEventListener", "onModalInteraction", "Unexpected modal interaction event from user " + event.getUser().getAsMention() + " - are they a bot trying to mess with things?");
                break;
            }
        }
    }
}
