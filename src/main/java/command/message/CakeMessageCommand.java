package command.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;


public class CakeMessageCommand implements MessageCommand {

    private static final Map<String, String> SUPPORTED_RESPONSE_BY_CAKE_EMOTE = new HashMap<String, String>() {{
       put("<:FeelsBadMan:803360118153412678>", "<:FeelsOkayMan:803360072268513281>");
       put("<:FeelsOkayMan:803360072268513281>", "<:FeelsBadMan:803360118153412678>");
       put("<:POGGERS:803362150780633135>", "<:COGGERS:803362162205524013>");
       put("<:COGGERS:803362162205524013>", "<:POGGERS:803362150780633135>");
       put("<:Bedge:917950582038356018>", "<:Wokege:917950603987124244>");
       put("<:Wokege:917950603987124244>", "<:Bedge:917950582038356018>");
       put("<:Hmm:917950673969086504>", "<:monkaHmm:799067594241081365>");
       put("<:monkaHmm:799067594241081365>", "<:Hmm:917950673969086504>");
       put("<:PepeHands:803360239066808321>", "<:FeelsAmazingMan:941885898566553671>");
       put("<:FeelsAmazingMan:941885898566553671>", "<:PepeHands:803360239066808321>");
       put("<:AquaCry:917967577781788702>", "<:AquaScream:917967853708279818>");
       put("<:AquaScream:917967853708279818>", "<:AquaCry:917967577781788702>");
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
                        if (SUPPORTED_RESPONSE_BY_CAKE_EMOTE.containsKey(token)) {
                            emotes.add(SUPPORTED_RESPONSE_BY_CAKE_EMOTE.get(token));
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
