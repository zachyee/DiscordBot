package command.message;

import audio.provider.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@RequiredArgsConstructor
public class PlayMessageCommand implements MessageCommand {

    private final AudioPlayerManager audioPlayerManager;
    private final TrackScheduler trackScheduler;

    @Override
    public Mono<Void> execute(final MessageCreateEvent messageCreateEvent) {

        return Mono.justOrEmpty(messageCreateEvent.getMessage().getContent())
                .map(content -> Arrays.asList(content.split(" ")))
                .doOnNext(command -> {
                    final String playRequest = command.get(1);
                    System.out.println("Play: " + playRequest);
                    audioPlayerManager.loadItem(playRequest, trackScheduler);
                })
                .then();
    }
}
