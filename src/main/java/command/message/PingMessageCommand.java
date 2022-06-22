package command.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;


public class PingMessageCommand implements MessageCommand {

    @Override
    public Mono<Void> execute(final MessageCreateEvent messageCreateEvent) {

        return Mono.just(messageCreateEvent)
                .filter(event -> event.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .map(MessageCreateEvent::getMessage)
                .flatMap(Message::getChannel)
                .flatMap(messageChannel -> messageChannel.createMessage("Pong!"))
                .then();
    }
}
