package command.message;

import audio.provider.TrackScheduler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoopMessageCommand implements MessageCommand{

    private final TrackScheduler trackScheduler;

    @Override
    public Mono<Void> execute(final MessageCreateEvent messageCreateEvent) {

        trackScheduler.toggleLoop();
        return Mono.empty();
    }
}
