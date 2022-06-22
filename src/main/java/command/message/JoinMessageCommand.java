package command.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.voice.AudioProvider;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class JoinMessageCommand implements MessageCommand {

    private final AudioProvider audioProvider;

    @Override
    public Mono<Void> execute(final MessageCreateEvent messageCreateEvent) {
        return Mono.justOrEmpty(messageCreateEvent.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                // join returns a VoiceConnection which would be required if we were
                // adding disconnection features, but for now we are just ignoring it.
                .flatMap(channel -> channel.join(spec -> spec.setProvider(audioProvider)))
                .then();
    }
}
