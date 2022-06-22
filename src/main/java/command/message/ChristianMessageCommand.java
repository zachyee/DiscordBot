package command.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;


public class ChristianMessageCommand implements MessageCommand {

    private static final List<String> SUPPORTED_EMOTES = new ArrayList<String>(){{
        add("<:4Head:514293113137922049>");
        add("<:AngryPepe:522296536445485056>");
        add("<:COGGERS:531009992271200256>");
        add("<:FeelsBadMan:522260677788958731>");
        add("<:FeelsOkayMan:531017292092407808>");
        add("<:FeelsWeirdlyComfy:531015446103851009>");
        add("<:Jebaited:514293077498658836>");
        add("<:monkaH:531008373471051777>");
        add("<:monkaHmm:531003468056494080>");
        add("<:monkaS:523295750654787614>");
        add("<:OMEGALUL:514322103638556673>");
        add("<:PepeHands:521885322515316736>");
        add("<:pepeW:591297923149725700>");
        add("<:Pog:531015982865711114>");
        add("<:PogChamp:514268576811712512>");
        add("<:POGGERS:523295841205747733>");
        add("<:Sadge:787101455739256882>");
        add("<:TriplemonkaS:604060550728515612>");
    }};


    @Override
    public Mono<Void> execute(final MessageCreateEvent messageCreateEvent) {

        return Mono.just(messageCreateEvent)
                .filter(event -> event.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .map(MessageCreateEvent::getMessage)
                .flatMap(message -> {
                    final List<String> emotes = new ArrayList<>();
                    final String messageContent = message.getContent();
                    final List<String> messageTokens = Arrays.asList(messageContent.split("\\s+"));
                    messageTokens.forEach(token -> {
                        if (SUPPORTED_EMOTES.contains(token)) {
                            emotes.add(token);
                        }
                    });
                    final String returnMessageContent = String.join(" ", emotes);
                    final MessageChannel channel = message.getChannel().block(Duration.ofSeconds(5));
                    Mono<Message> botMessage = Mono.empty();
                    if (channel != null && !returnMessageContent.isEmpty()) {
                        botMessage = channel.createMessage(returnMessageContent);
                    }
                    return botMessage;
                })
                .then();
    }
}
