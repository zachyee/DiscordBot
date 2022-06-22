package main;

import audio.provider.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import command.message.*;
import audio.provider.LavaPlayerAudioProvider;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class Bot {

    private static final String BOT_COMMAND_PREFIX = "!";

    public static void main(String[] args) {

        // Creates AudioPlayer instances and translates URLs to AudioTrack instances
        final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
        // This is an optimization strategy that Discord4J can utilize. It is not important to understand
        audioPlayerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        // Create an AudioPlayer so Discord4J can receive audio data
        final AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
        // We will be creating LavaPlayerAudioProvider in the next step
        final AudioProvider audioProvider = new LavaPlayerAudioProvider(audioPlayer);
        final TrackScheduler trackScheduler = new TrackScheduler(audioPlayer, audioPlayerManager);
        audioPlayer.addListener(trackScheduler);

        final String token = args[0];
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();

        gateway.on(ReadyEvent.class)
                .subscribe(event -> {
                    final User self = event.getSelf();
                    System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
                });


        final Map<String, MessageCommand> commandsByKey = new HashMap<>();
        commandsByKey.put("ping", new PingMessageCommand());
        commandsByKey.put("join", new JoinMessageCommand(audioProvider));
        commandsByKey.put("loop", new LoopMessageCommand(trackScheduler));
        commandsByKey.put("play", new PlayMessageCommand(audioPlayerManager, trackScheduler));
        commandsByKey.put("skip", new SkipMessageCommand(trackScheduler));


        gateway.on(MessageCreateEvent.class)
                .flatMap(event -> Mono.justOrEmpty(event.getMessage().getContent())
                        .flatMap(content -> Flux.fromIterable(commandsByKey.entrySet())
                                .filter(entry -> content.startsWith(BOT_COMMAND_PREFIX + entry.getKey()))
                                .flatMap(entry -> entry.getValue().execute(event))
                                .next()))
                .subscribe();

        gateway.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> new AntiCakeMessageCommand().execute(event))
                .next()
                .subscribe();

        gateway.onDisconnect().block();
    }
}
