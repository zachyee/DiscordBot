package command.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public interface MessageCommand {
    Mono<Void> execute(MessageCreateEvent messageCreateEvent);
}
