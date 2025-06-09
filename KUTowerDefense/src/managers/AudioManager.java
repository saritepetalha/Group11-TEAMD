package managers;

import helpMethods.LoadSave;

import javax.sound.sampled.*;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Handles sound/music effects for the game.
 * Uses WAV files which are natively supported by Java Sound API.
 */
public class AudioManager {
    // Audio categories
    private static final String MUSIC_PATH = "/Audio/Music/";
    private static final String SFX_PATH = "/Audio/SFX/";
    private static final String GAME_MUSIC_PATH = "/GameMusic/";

    // Volume control (0.0f to 1.0f)
    private float musicVolume = 0.8f;
    private float soundVolume = 0.8f;
    private boolean musicMuted = false;
    private boolean soundMuted = false;

    // Loaded clips
    private Map<String, Clip> musicClips = new HashMap<>();
    private Map<String, Clip> soundClips = new HashMap<>();

    // Track currently playing spawn sounds to stop them when needed
    private Map<String, Clip> currentlyPlayingSpawnSounds = new HashMap<>();

    // Currently playing music
    private String currentMusic = "";

    // Random generator for variety in sound effects
    private final Random random = new Random();

    // Sound constants
    public static final String GOBLIN_DEATH_1 = "GoblinDeath1";
    public static final String GOBLIN_DEATH_2 = "GoblinDeath2";
    public static final String GOBLIN_DEATH_3 = "GoblinDeath3";
    public static final String KNIGHT_DEATH = "KnightDeath";
    public static final String TROLL_DEATH = "TrollDeath";

    // Victory/Lose sound constants
    public static final String WIN_1 = "win1";
    public static final String WIN_2 = "win2";
    public static final String WIN_3 = "win3";
    public static final String WIN_4 = "win4";
    public static final String WIN_5 = "win5";
    public static final String LOSE_1 = "lose1";
    public static final String LOSE_2 = "lose2";
    public static final String LOSE_3 = "lose3";
    public static final String LOSE_4 = "lose4";
    public static final String LOSE_5 = "lose5";

    // Singleton instance
    private static AudioManager instance;

    private AudioManager() {
        loadAudio();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private void loadAudio() {
        // load background music tracks
        loadMusic("intro", "intro_music.wav");
        loadMusic("lonelyhood", "Lonelyhood.wav");

        // load game music from GameMusic folder
        //loadMusicFromGameMusicFolder("9 A.M.", "9 A.M. - Animal Crossing Wild World.wav");
        //loadMusicFromGameMusicFolder("accumula_town", "Accumula Town.wav");
        //loadMusicFromGameMusicFolder("bounce beanstalk", "Bounce Beanstalk Walks.wav");
        //loadMusicFromGameMusicFolder("memories","Memories - Dark Cloud.wav");
        //loadMusicFromGameMusicFolder("dreams hopes", "Dreams and Hopes .wav");
        loadMusicFromGameMusicFolder("dirtmouth", "Hollow Knight OST - Dirtmouth.wav");
        //loadMusicFromGameMusicFolder("forest interlude", "Forest Interlude - Donkey Kong 2.wav");
        //loadMusicFromGameMusicFolder("frappe snowland", "Frappe Snowland.wav");
        //loadMusicFromGameMusicFolder("grape garden", "Grape Garden.wav");
        //loadMusicFromGameMusicFolder("hailfire peaks", "Hailfire Peaks.wav");
        //loadMusicFromGameMusicFolder("hateno village", "Hateno Village (Day) - Breath Of The Wild Music.wav");
        //loadMusicFromGameMusicFolder("inside the castle walls", "Inside the Castle Walls.wav");
        //loadMusicFromGameMusicFolder("interstellar", "Interstellar Main Theme.wav");
        loadMusicFromGameMusicFolder("intro bayonetta origins", "Intro - Bayonetta Origins.wav");
        //loadMusicFromGameMusicFolder("lady of the lake", "Nimue - Lady of the Lake.wav");
        //loadMusicFromGameMusicFolder("maple treeway", "Maple Treeway.wav");
        //loadMusicFromGameMusicFolder("missing you", "Elinia - Missing You.wav");
        //loadMusicFromGameMusicFolder("norune village", "Norune Village.wav");
        //loadMusicFromGameMusicFolder("refugee camp", "In The Refugee Camp.wav");
        //loadMusicFromGameMusicFolder("river of life", "River of Life.wav");
        //loadMusicFromGameMusicFolder("spring", "Rune Factory Frontier - Spring.wav");
        //loadMusicFromGameMusicFolder("spring 2","Spring 2 - Story of Seasons.wav");
        //loadMusicFromGameMusicFolder("soaring the sky", "Soaring The Sky (Night).wav");
        //loadMusicFromGameMusicFolder("summer fields", "Summer Fields.wav");
        //loadMusicFromGameMusicFolder("temple of time", "Temple of Time.wav");
        //loadMusicFromGameMusicFolder("the museum", "The Museum - Animal Crossing The Movie.wav");
        loadMusicFromGameMusicFolder("white palace", "White Palace.wav");
        //loadMusicFromGameMusicFolder("wistful wild", "Wistful Wild.wav");
        //loadMusicFromGameMusicFolder("you have power", "You have power... like mine.wav");

        // load sound effects
        //loadSound("tower_build", "tower_build.wav");
        //loadSound("tower_shoot", "tower_shoot.wav");
        loadSound("button_click", "button_click.wav");
        loadSound(GOBLIN_DEATH_1, "GoblinDeath1.wav");
        loadSound(GOBLIN_DEATH_2, "GoblinDeath2.wav");
        loadSound(GOBLIN_DEATH_3, "GoblinDeath3.wav");
        loadSound(KNIGHT_DEATH, "KnightDeath.wav");
        loadSound(TROLL_DEATH, "TrollDeath.wav");
        //loadSound("wave_start", "wave_start.wav");
        // Load victory and lose sounds
        loadSound(WIN_1, WIN_1 + ".wav");
        loadSound(WIN_2, WIN_2 + ".wav");
        loadSound(WIN_3, WIN_3 + ".wav");
        loadSound(WIN_4, WIN_4 + ".wav");
        loadSound(WIN_5, WIN_5 + ".wav");
        loadSound(LOSE_1, LOSE_1 + ".wav");
        loadSound(LOSE_2, LOSE_2 + ".wav");
        loadSound(LOSE_3, LOSE_3 + ".wav");
        loadSound(LOSE_4, LOSE_4 + ".wav");
        loadSound(LOSE_5, LOSE_5 + ".wav");

        loadSound("earthquake", "earthquake_audio.wav");
        loadSound("lightning", "lightning_audio.wav");
        loadSound("coin_drop", "coin_drop.wav");
        loadSound("explosion_tnt", "explosionTNT.wav");

        // Warrior spawn sounds
        loadSound("archer_spawn", "archerSpawn.wav");
        loadSound("wizard_spawn", "wizardSpawn.wav");
        loadSound("tnt_spawn", "tntSpawn.wav");

        // Tower and warrior attack sounds
        loadSound("arrow_shot", "arrowShot.wav");
        loadSound("spell_shot", "spellShot.wav");
        loadSound("bomb_shot", "bombShot.wav");

        loadWeatherSound("rain", "rain.wav");
        loadWeatherSound("snow", "snow.wav");
        loadWeatherSound("wind", "wind.wav");
    }

    private void loadMusic(String name, String filename) {
        try {
            Clip clip = loadClip(MUSIC_PATH + filename);
            if (clip != null) {
                musicClips.put(name, clip);
                System.out.println("Loaded music: " + name);
            }
        } catch (Exception e) {
            System.err.println("Failed to load music: " + name + " - " + e.getMessage());
        }
    }

    private void loadMusicFromGameMusicFolder(String name, String filename) {
        try {
            Clip clip = loadClip(GAME_MUSIC_PATH + filename);
            if (clip != null) {
                musicClips.put(name, clip);
                System.out.println("Loaded game music: " + name);
            }
        } catch (Exception e) {
            System.err.println("Failed to load game music: " + name + " - " + e.getMessage());
        }
    }

    private void loadSound(String name, String filename) {
        try {
            String fullPath = SFX_PATH + filename;

            InputStream is = getClass().getResourceAsStream(fullPath);
            if (is == null) throw new IllegalArgumentException("Sound not found: " + fullPath);

            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream originalStream = AudioSystem.getAudioInputStream(bis);

            // Check if we need to convert the audio format
            AudioFormat originalFormat = originalStream.getFormat();
            AudioFormat targetFormat = null;

            // Convert problematic formats to PCM_SIGNED 16-bit which is better supported
            if (originalFormat.getEncoding() == AudioFormat.Encoding.PCM_FLOAT ||
                    originalFormat.getSampleSizeInBits() == 24 ||
                    originalFormat.getSampleSizeInBits() > 16) {

                System.out.println("Converting audio format for: " + name + " from " + originalFormat);

                targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        originalFormat.getSampleRate(),
                        16, // Convert to 16-bit
                        originalFormat.getChannels(),
                        originalFormat.getChannels() * 2, // 2 bytes per sample
                        originalFormat.getSampleRate(),
                        false // little endian
                );
            }

            AudioInputStream audioStream;
            if (targetFormat != null && AudioSystem.isConversionSupported(targetFormat, originalFormat)) {
                audioStream = AudioSystem.getAudioInputStream(targetFormat, originalStream);
                System.out.println("Converted audio format for: " + name + " from " + originalFormat.getEncoding() + " to " + targetFormat.getEncoding());
            } else {
                audioStream = originalStream;
            }

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            soundClips.put(name, clip);
            System.out.println("Loaded sound: " + name);

        } catch (Exception e) {
            System.err.println("Failed to load sound: " + name + " - " + e.getMessage());

            // Try alternative approach for problematic files
            if (e.getMessage().contains("not supported") ||
                    e.getMessage().contains("PCM_FLOAT") ||
                    e.getMessage().contains("24 bit") ||
                    e.getMessage().contains("3 bytes/frame")) {
                System.out.println("Attempting alternative load method for: " + name);
                loadSoundAlternative(name, filename);
            }
        }
    }

    /**
     * Alternative loading method for problematic audio files
     */
    private void loadSoundAlternative(String name, String filename) {
        try {
            String fullPath = SFX_PATH + filename;
            InputStream is = getClass().getResourceAsStream(fullPath);
            if (is == null) {
                System.err.println("Alternative load failed - file not found: " + fullPath);
                return;
            }

            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream originalStream = AudioSystem.getAudioInputStream(bis);

            // Force conversion to a widely supported format
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    22050, // Standard sample rate
                    16,    // 16-bit
                    1,     // Mono
                    2,     // 2 bytes per sample
                    22050, // Frame rate same as sample rate
                    false  // Little endian
            );

            if (AudioSystem.isConversionSupported(targetFormat, originalStream.getFormat())) {
                AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, originalStream);
                Clip clip = AudioSystem.getClip();
                clip.open(convertedStream);
                soundClips.put(name, clip);
                System.out.println("Alternative load successful for: " + name);
            } else {
                System.err.println("Audio conversion not supported for: " + name);
            }

        } catch (Exception e) {
            System.err.println("Alternative load failed for " + name + ": " + e.getMessage());
        }
    }

    private void loadWeatherSound(String name, String filename) {
        try {
            String fullPath = SFX_PATH + filename;

            InputStream is = getClass().getResourceAsStream(fullPath);
            if (is == null) throw new IllegalArgumentException("Weather sound not found: " + fullPath);

            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            musicClips.put(name, clip);
            System.out.println("Loaded weather sound: " + name);

        } catch (Exception e) {
            System.err.println("Failed to load weather sound: " + name + " - " + e.getMessage());
        }
    }



    private Clip loadClip(String path) {
        try (InputStream is = LoadSave.class.getResourceAsStream(path);
             BufferedInputStream bis = new BufferedInputStream(is)) {

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading audio file: " + path + " - " + e.getMessage());
            return null;
        }
    }

    public void playMusic(String name) {
        if (musicMuted) return;

        stopMusic();

        Clip clip = musicClips.get(name);
        if (clip != null) {
            setClipVolume(clip, musicVolume);
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            currentMusic = name;
        }
    }

    public void stopMusic() {
        if (!currentMusic.isEmpty()) {
            Clip clip = musicClips.get(currentMusic);
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
            currentMusic = "";
        }
    }

    public void playSound(String name) {
        if (soundMuted) return;

        Clip clip = soundClips.get(name);
        if (clip != null) {
            setClipVolume(clip, soundVolume);
            clip.setFramePosition(0);
            clip.start();
        }
    }

    /**
     * Play a sound that can overlap with itself (multiple instances can play simultaneously)
     * This creates a new Clip instance each time to allow overlapping playback
     */
    public void playOverlappingSound(String name) {
        playOverlappingSound(name, 1.0f); // Full volume by default
    }

    /**
     * Play a sound that can overlap with itself with custom volume
     * @param name The sound name
     * @param volumeMultiplier Volume multiplier (1.0f = full volume, 0.5f = half volume, etc.)
     */
    public void playOverlappingSound(String name, float volumeMultiplier) {
        if (soundMuted) return;

        Clip originalClip = soundClips.get(name);
        if (originalClip != null) {
            try {
                // Create a new clip instance from the original
                Clip newClip = AudioSystem.getClip();

                // We need to get the audio data from the original clip
                // Since we can't easily extract it, we'll load it fresh from resources
                loadAndPlayFreshClip(name, volumeMultiplier);

            } catch (Exception e) {
                // Fallback to regular playSound if something goes wrong
                playSound(name);
            }
        }
    }

    /**
     * Load and play a fresh clip instance for overlapping sounds
     */
    private void loadAndPlayFreshClip(String name) {
        loadAndPlayFreshClip(name, 1.0f); // Default full volume
    }

    /**
     * Load and play a fresh clip instance for overlapping sounds with custom volume
     */
    private void loadAndPlayFreshClip(String name, float volumeMultiplier) {
        try {
            String filename = getSoundFilename(name);
            if (filename == null) return;

            String fullPath = SFX_PATH + filename;
            InputStream is = getClass().getResourceAsStream(fullPath);
            if (is == null) return;

            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream originalStream = AudioSystem.getAudioInputStream(bis);

            // Apply same format conversion as in loadSound method
            AudioFormat originalFormat = originalStream.getFormat();
            AudioInputStream audioStream = originalStream;

            if (originalFormat.getEncoding() == AudioFormat.Encoding.PCM_FLOAT ||
                    originalFormat.getSampleSizeInBits() == 24 ||
                    originalFormat.getSampleSizeInBits() > 16) {

                AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        originalFormat.getSampleRate(),
                        16,
                        originalFormat.getChannels(),
                        originalFormat.getChannels() * 2,
                        originalFormat.getSampleRate(),
                        false
                );

                if (AudioSystem.isConversionSupported(targetFormat, originalFormat)) {
                    audioStream = AudioSystem.getAudioInputStream(targetFormat, originalStream);
                }
            }

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            setClipVolume(clip, soundVolume * volumeMultiplier);

            // Auto-close the clip when it finishes playing
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            clip.start();

        } catch (Exception e) {
            // Silent fallback - just don't play the sound
        }
    }

    /**
     * Get the filename for a sound name (reverse lookup)
     */
    private String getSoundFilename(String name) {
        switch (name) {
            case "arrow_shot": return "arrowShot.wav";
            case "spell_shot": return "spellShot.wav";
            case "bomb_shot": return "bombShot.wav";
            case "archer_spawn": return "archerSpawn.wav";
            case "wizard_spawn": return "wizardSpawn.wav";
            case "tnt_spawn": return "tntSpawn.wav";
            case "explosion_tnt": return "explosionTNT.wav";
            case "button_click": return "button.wav";
            default: return null;
        }
    }

    // enemy-specific sound methods from SoundManager
    public void playRandomGoblinDeathSound() {
        if (soundMuted) return;

        if (random.nextDouble() < 0.50) {
            playSound(GOBLIN_DEATH_1);
        } else if (0.5 < random.nextDouble() && random.nextDouble() < 0.75) {
            playSound(GOBLIN_DEATH_2);
        }else
            playSound(GOBLIN_DEATH_3);
    }

    public void playKnightDeathSound() {
        if (soundMuted) return;

        playSound(KNIGHT_DEATH);
    }

    public void playTrollDeathSound() {
        if (soundMuted) return;

        playSound(TROLL_DEATH);
    }

    private void setClipVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            // convert linear volume (0.0 to 1.0) to decibels
            float dB = (float) (Math.log10(Math.max(0.0001, volume)) * 20.0);
            // ensure we're within the allowed range
            dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
            gainControl.setValue(dB);
        }
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));

        // update currently playing music volume
        if (!currentMusic.isEmpty()) {
            Clip clip = musicClips.get(currentMusic);
            if (clip != null) {
                setClipVolume(clip, musicVolume);
            }
        }
    }

    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0.0f, Math.min(1.0f, volume));

        String[] weatherSounds = {"rain", "snow", "wind"};
        for (String weather : weatherSounds) {
            Clip clip = musicClips.get(weather);
            if (clip != null && clip.isRunning()) {
                setClipVolume(clip, soundVolume * 0.8f);
            }
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public void setMusicMuted(boolean muted) {
        this.musicMuted = muted;
        if (muted) {
            stopMusic();
        } else if (!currentMusic.isEmpty()) {
            // restart the music if we unmute
            playMusic(currentMusic);
        }
    }

    public void setSoundMuted(boolean muted) {
        this.soundMuted = muted;
    }

    public boolean isMusicMuted() {
        return musicMuted;
    }

    public boolean isSoundMuted() {
        return soundMuted;
    }

    public void stopAllSounds() {
        for (Clip clip : soundClips.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
    }

    // Add a method to get all available music names for UI purposes
    public String[] getAvailableMusicTracks() {
        return musicClips.keySet().toArray(new String[0]);
    }


    public void playRandomGameMusic() {
        String[] gameMusic = {
                "dirtmouth", "intro bayonetta origins", "white palace"
        };

        int index = (int)(Math.random() * gameMusic.length);
        playMusic(gameMusic[index]);
    }

    public void playRandomVictorySound() {
        if (soundMuted) return;

        int random = this.random.nextInt(5) + 1; // Random number from 1 to 5
        String soundName = "win" + random;
        playSound(soundName);
    }

    public void playRandomLoseSound() {
        if (soundMuted) return;

        int random = this.random.nextInt(5) + 1; // Random number from 1 to 5
        String soundName = "lose" + random;
        playSound(soundName);
    }


    public void toggleMusicMute() {
        musicMuted = !musicMuted;
        if (musicMuted) {
            pauseMusic();
        } else {
            resumeMusic();
        }
    }

    private void pauseMusic() {
        if (!currentMusic.isEmpty()) {
            Clip clip = musicClips.get(currentMusic);
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
    }

    private void resumeMusic() {
        if (!currentMusic.isEmpty()) {
            Clip clip = musicClips.get(currentMusic);
            if (clip != null && !clip.isRunning()) {
                setClipVolume(clip, musicVolume);
                clip.start();
            } else if (clip == null || clip.getFramePosition() == 0) {
                playMusic(currentMusic);
            }
        } else {
            playMusic("lonelyhood");
        }
    }

    public void playButtonClickSound() {
        playSound("button_click");
    }

    public void playTNTExplosionSound() {
        // Stop any TNT spawn sound that might still be playing
        stopSpawnSound("tnt_spawn");
        playSound("explosion_tnt");
    }

    public void playArcherSpawnSound() {
        playSpawnSound("archer_spawn");
    }

    public void playWizardSpawnSound() {
        playSpawnSound("wizard_spawn");
    }

    public void playTNTSpawnSound() {
        playSpawnSound("tnt_spawn");
    }

    public void playArrowShotSound() {
        playOverlappingSound("arrow_shot");
    }

    public void playSpellShotSound() {
        playOverlappingSound("spell_shot");
    }

    public void playBombShotSound() {
        playOverlappingSound("bomb_shot", 0.35f); // Slightly reduced volume
    }

    public void playWeatherSound(String weatherType) {
        if (soundMuted) return;

        stopWeatherSounds();

        Clip clip = musicClips.get(weatherType);
        if (clip != null) {
            setClipVolume(clip, soundVolume * 0.8f);
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
        }
    }

    public void stopWeatherSounds() {
        String[] weatherSounds = {"rain", "snow", "wind"};
        for (String weather : weatherSounds) {
            Clip clip = musicClips.get(weather);
            if (clip != null && clip.isRunning()) {
                clip.stop();

            }
        }
    }

    public void stopAllWeatherAndMusic() {
        stopMusic();
        stopWeatherSounds();
    }

    /**
     * Play a spawn sound and track it so it can be stopped later
     */
    private void playSpawnSound(String name) {
        if (soundMuted) return;

        Clip clip = soundClips.get(name);
        if (clip != null) {
            // Stop any currently playing spawn sound of this type
            stopSpawnSound(name);

            setClipVolume(clip, soundVolume);
            clip.setFramePosition(0);
            clip.start();

            // Track this as a currently playing spawn sound
            currentlyPlayingSpawnSounds.put(name, clip);
        }
    }

    /**
     * Stop a specific spawn sound if it's currently playing
     */
    public void stopSpawnSound(String name) {
        Clip clip = currentlyPlayingSpawnSounds.get(name);
        if (clip != null && clip.isRunning()) {
            clip.stop();
            currentlyPlayingSpawnSounds.remove(name);
        }
    }

    /**
     * Stop all currently playing spawn sounds
     */
    public void stopAllSpawnSounds() {
        for (Map.Entry<String, Clip> entry : currentlyPlayingSpawnSounds.entrySet()) {
            Clip clip = entry.getValue();
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
        currentlyPlayingSpawnSounds.clear();
    }
}