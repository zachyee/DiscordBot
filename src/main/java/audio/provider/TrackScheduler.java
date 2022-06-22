package audio.provider;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.Queue;

@RequiredArgsConstructor
public final class TrackScheduler extends AudioEventAdapter implements AudioLoadResultHandler {

    private final AudioPlayer audioPlayer;
    private final AudioPlayerManager audioPlayerManager;
    private final Queue<AudioTrack> audioTracks = new LinkedList<>();
    private Boolean loop = false;

    @Override
    public void trackLoaded(AudioTrack track) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(track);
        } else {
            this.enqueue(track);
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        for (AudioTrack track : playlist.getTracks()) {
            this.trackLoaded(track);
        }
    }

    @Override
    public void noMatches() {
        // Notify the user that we've got nothing
        System.out.println("No matches");
    }

    @Override
    public void loadFailed(FriendlyException throwable) {
        // Notify the user that everything exploded
        throwable.printStackTrace();
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        // Player was paused
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        // Player was resumed
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        // A track started playing
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (loop) {
                audioPlayerManager.loadItem(track.getIdentifier(), this);
            }
            if (!audioTracks.isEmpty()) {
                final AudioTrack nextTrack = audioTracks.remove();
                audioPlayer.playTrack(nextTrack);
            }
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        // An already playing track threw an exception (track end event will still be received separately)
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Audio track has been unable to provide us any audio, might want to just start a new track
    }

    public void enqueue(final AudioTrack audioTrack) {
        audioTracks.add(audioTrack);
    }

    public void toggleLoop() {
        loop = !loop;
        System.out.println("Toggled loop to " + loop.toString());
    }

    public void skip() {
        if (!audioTracks.isEmpty()) {
            final AudioTrack nextTrack = audioTracks.remove();
            audioPlayer.playTrack(nextTrack);
        } else {
            audioPlayer.stopTrack();
        }
    }
}
