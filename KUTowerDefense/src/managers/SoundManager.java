package managers;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Handles sound effects for the game.
 * Uses WAV files which are natively supported by Java Sound API.
 */
public class SoundManager {
    private static SoundManager instance;
    private Map<String, Clip> soundClips = new HashMap<>();
    private final Random random = new Random();
    private float volume = 1.0f; // Default full volume
    private boolean soundEnabled = true;

    // Sound file names - using .wav extension
    public static final String GOBLIN_DEATH_1 = "Goblin_Death1";
    public static final String GOBLIN_DEATH_2 = "Goblin_Death2";
    public static final String WARRIOR_DEATH = "Warrior_Death";

    private SoundManager() {
        loadSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadSounds() {
        loadSound(GOBLIN_DEATH_1, "/Audio/" + GOBLIN_DEATH_1 + ".wav");
        loadSound(GOBLIN_DEATH_2, "/Audio/" + GOBLIN_DEATH_2 + ".wav");
        loadSound(WARRIOR_DEATH, "/Audio/" + WARRIOR_DEATH + ".wav");
    }

    private void loadSound(String soundName, String filePath) {
        try {
            InputStream soundStream = getClass().getResourceAsStream(filePath);
            if (soundStream == null) {
                System.err.println("Could not find sound file: " + filePath);
                return;
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            soundClips.put(soundName, clip);
            System.out.println("Loaded sound: " + soundName);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound " + soundName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void playSound(String soundName) {
        if (!soundEnabled) return;

        Clip clip = soundClips.get(soundName);
        if (clip == null) {
            System.err.println("Sound not found: " + soundName);
            return;
        }

        // Stop the clip if it's already playing
        clip.stop();
        clip.setFramePosition(0);

        // Set volume
        setClipVolume(clip, volume);

        // Play the sound
        clip.start();
    }

    public void playRandomGoblinDeathSound() {
        if (random.nextDouble() < 0.75) {
            playSound(GOBLIN_DEATH_1);
        } else {
            playSound(GOBLIN_DEATH_2);
        }
    }

    public void playWarriorDeathSound() {
        playSound(WARRIOR_DEATH);
    }

    private void setClipVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
            gainControl.setValue(dB);
        }
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public void enableSound(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void stopAllSounds() {
        for (Clip clip : soundClips.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
    }
}