package prog.models;

import java.io.InputStream;
import java.lang.reflect.Field;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.AccessibleAction;
import javafx.scene.control.CheckBox;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.Player;
import prog.controllers.MainScreenController;

public class AudioPlayer {

    private final static int NOTSTARTED = 0;
    private final static int PLAYING = 1;
    private final static int PAUSED = 2;
    private final static int FINISHED = 3;
    private CheckBox chkFinaliza;
    private FloatControl volControl;
    private final Player player;

    private final Object playerLock = new Object();

    private int playerStatus = NOTSTARTED;

    public int getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(int playerStatus) {
        this.playerStatus = playerStatus;
    }

    public AudioPlayer(final InputStream inputStream, CheckBox chkFinaliza) throws JavaLayerException {
        this.player = new Player(inputStream);
        this.chkFinaliza = chkFinaliza;
    }

    public AudioPlayer(final InputStream inputStream, final AudioDevice audioDevice) throws JavaLayerException {
        this.player = new Player(inputStream, audioDevice);
    }

    
    public void play() throws JavaLayerException {
        synchronized (playerLock) {
            switch (playerStatus) {
                case NOTSTARTED:
                    final Runnable r = new Runnable() {
                        public void run() {
                            playInternal();
                        }
                    };
                    final Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setPriority(Thread.MAX_PRIORITY);
                    playerStatus = PLAYING;
                    t.start();
                    break;
                case PAUSED:
                    resume();
                    break;
                default:
                    break;
            }
        }
    }

    
    public boolean pause() {
        synchronized (playerLock) {
            if (playerStatus == PLAYING) {
                playerStatus = PAUSED;
            }
            return playerStatus == PAUSED;
        }
    }

    
    public boolean resume() {
        synchronized (playerLock) {
            if (playerStatus == PAUSED) {
                playerStatus = PLAYING;
                playerLock.notifyAll();
            }
            return playerStatus == PLAYING;
        }
    }

    
    public void stop() {
        synchronized (playerLock) {
            playerStatus = FINISHED;
            playerLock.notifyAll();
        }
    }
    
    private void playInternal() {
        while (playerStatus != FINISHED) {
            try {
                if (!player.play(1)) {
                    break;
                }
            } catch (final JavaLayerException e) {
                break;
            }
            synchronized (playerLock) {
                while (playerStatus == PAUSED) {
                    try {
                        playerLock.wait();
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
            }
        }
        close();
        chkFinaliza.executeAccessibleAction(AccessibleAction.FIRE, (Object) null);
    }

    
    public void close() {
        synchronized (playerLock) {
            playerStatus = FINISHED;
        }
        try {
            player.close();
        } catch (final Exception e) {
            
        }
    }
    
}
