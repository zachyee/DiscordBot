package command.message;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.core.spec.VoiceChannelJoinSpec;
import discord4j.discordjson.possible.Possible;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;


public class AntiCakeMessageCommand implements MessageCommand {

    private static final Random RANDOM_GENERATOR = new Random();
    private static final int STRIKE_UPPER_BOUND = 10;
    private static int STRIKE_LIMIT = RANDOM_GENERATOR.nextInt(STRIKE_UPPER_BOUND) + 1;
    private static int STRIKE_COUNTER = 0;
    private static final int TOTAL_ALLOWED_ATTEMPTS = 2;

    private static final Pattern EMOTE_PATTERN = Pattern.compile("^(<:.*:\\d+>\\s)?(<:.*:\\d+>)$");
    private static final Pattern AAA_PATTERN = Pattern.compile("^[Aa]+$");
    private static final Pattern WTF_PATTERN = Pattern.compile("^[Ww]+[Tt]+[Ff]+$");

    @Override
    public Mono<Void> execute(final MessageCreateEvent messageCreateEvent) {

        return Mono.just(messageCreateEvent)
                .filter(event -> event.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().map(user -> user.getUsername().equalsIgnoreCase("Cake")).orElse(false))
                .flatMap(message -> {
                    final String messageContent = message.getContent().trim();
                    final MessageChannel channel = message.getChannel().block(Duration.ofSeconds(30));
                    Mono<Message> botMessage = Mono.empty();
                    if (channel != null) {
                        if (EMOTE_PATTERN.matcher(messageContent).find()) {
                            STRIKE_COUNTER++;
                        }
                        else if (AAA_PATTERN.matcher(messageContent).find()) {
                            STRIKE_COUNTER++;
                        }
                        else if (WTF_PATTERN.matcher(messageContent).find()) {
                            STRIKE_COUNTER++;
                        } else {
                            STRIKE_COUNTER = 0;
                        }
                    }

                    if (STRIKE_COUNTER >= STRIKE_LIMIT) {
                        final Member member = message.getAuthorAsMember().block();
                        for (int attempt = 1; attempt <= TOTAL_ALLOWED_ATTEMPTS; attempt++) {
                            try {
                                final Member updatedMember = member.edit(GuildMemberEditSpec.builder()
                                        .communicationDisabledUntilOrNull(Instant.now().plusSeconds(5))
                                        .build()).block();
                                System.out.println(updatedMember.toString());
                                botMessage = channel.createMessage("GET CAKE OUTTA HERE");
                                STRIKE_COUNTER = 0;
                                STRIKE_LIMIT = RANDOM_GENERATOR.nextInt(STRIKE_UPPER_BOUND) + 1;
                                System.out.println("Strike limit set to: " + STRIKE_LIMIT);
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                                this.sleep();
                            }
                        }
                    }
                    return botMessage;
                })
                .then();
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
