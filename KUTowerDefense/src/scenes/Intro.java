package scenes;

import dimensions.GameDimensions;
import main.Game;
import main.GameStates;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class Intro {
    private Game game;
    private BufferedImage logoImage;
    private BufferedImage loadingBackgroundImage;
    private float alpha = 0f;  // for fading the logo
    private int introState = 0;  // 0: fading in, 1: hold, 2: fading out, 3: loading screen
    private int loadingDotCount = 0;
    private long lastDotUpdateTime = 0;
    private long lastStateUpdateTime = 0;
    private static final int DOT_UPDATE_DELAY = 500;  // milliseconds between dot updates
    private static final int FADE_IN_DURATION = 2000;  // 2 seconds
    private static final int HOLD_DURATION = 1500;    // 1.5 seconds
    private static final int FADE_OUT_DURATION = 1500;  // 1.5 seconds
    private static final int LOADING_DURATION = 3000;  // 3 seconds

    private Clip introClip;
    private boolean musicStarted = false;
    private FloatControl volumeControl;
    private final float initialVolume = 0.0f;   // 0 dB is normal
    private final float minVolume = -40.0f;     // -40 dB is almost silent

    // rotation animation properties
    private double rotationAngle = 0;
    private double rotationSpeed = 0.5; // degrees per frame
    private final double MAX_ROTATION = 15.0; // maximum degrees of rotation (for oscillation)
    private boolean rotationDirection = true; // true = clockwise, false = counter-clockwise

    // logo scaling properties
    private float logoScale = 0.8f; // resizes the logo (0.5 = 50% of original size)
    private boolean pulsating = true;
    private float pulseFactor = 0.05f; // how much the logo grows/shrinks
    private float currentPulseScale = 0;
    private float pulseSpeed = 0.02f;
    private boolean growing = true;


    private Color backgroundColor = new Color(255, 242, 218);

    public Intro(Game game) {
        this.game = game;
        loadImages();
        resetIntro();
    }

    private void loadImages() {
        try {
            InputStream is = getClass().getResourceAsStream("/teamD_logo_alt.png");
            if (is != null) {
                logoImage = ImageIO.read(is);
            }

            is = getClass().getResourceAsStream("/KuTowerDefence1.jpg");
            if (is != null) {
                loadingBackgroundImage = ImageIO.read(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        // update loading dots animation
        if (introState == 3 && currentTime - lastDotUpdateTime > DOT_UPDATE_DELAY) {
            loadingDotCount = (loadingDotCount + 1) % 4;  // 0, 1, 2, 3, 0, ...
            lastDotUpdateTime = currentTime;
        }

        // update intro state based on timings
        if (currentTime - lastStateUpdateTime > getStateDuration()) {
            introState++;
            lastStateUpdateTime = currentTime;

            // reset alpha for fading transitions
            if (introState == 0) alpha = 0f;

            // the intro is over
            if (introState > 3) {
                stopMusic();
                game.changeGameState(GameStates.MENU);
            }
        }

        // update alpha for fading effects
        if (introState == 0) {
            // fade in
            float progress = (float)(currentTime - lastStateUpdateTime) / FADE_IN_DURATION;
            alpha = Math.min(1.0f, progress);
        } else if (introState == 2) {
            // fade out
            float progress = (float)(currentTime - lastStateUpdateTime) / FADE_OUT_DURATION;
            alpha = Math.max(0.0f, 1.0f - progress);
        }

        // update rotation animation
        if (rotationDirection) {
            rotationAngle += rotationSpeed;
            if (rotationAngle >= MAX_ROTATION) {
                rotationDirection = false;
            }
        } else {
            rotationAngle -= rotationSpeed;
            if (rotationAngle <= -MAX_ROTATION) {
                rotationDirection = true;
            }
        }

        // update pulsating effect (only during hold state for smoother effect)
        if (pulsating && introState == 1) {
            if (growing) {
                currentPulseScale += pulseSpeed;
                if (currentPulseScale >= pulseFactor) {
                    growing = false;
                }
            } else {
                currentPulseScale -= pulseSpeed;
                if (currentPulseScale <= -pulseFactor) {
                    growing = true;
                }
            }
        }

    }

    private int getStateDuration() {
        switch (introState) {
            case 0: return FADE_IN_DURATION;
            case 1: return HOLD_DURATION;
            case 2: return FADE_OUT_DURATION;
            case 3: return LOADING_DURATION;
            default: return 0;
        }
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        // enabling anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (!musicStarted && introState == 0) {
            startMusic();
            musicStarted = true;
        }

        if (introState <= 2) {
            g2d.setColor(backgroundColor);
            g2d.fillRect(0, 0, game.getWidth(), game.getHeight());
            // logo display with fading, rotation, and scaling
            if (logoImage != null) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                // resizing the logo
                float finalScale = logoScale + (introState == 1 ? currentPulseScale : 0);
                int scaledWidth = (int)(logoImage.getWidth() * finalScale);
                int scaledHeight = (int)(logoImage.getHeight() * finalScale);

                int centerX = game.getWidth() / 2;
                int centerY = game.getHeight() / 2;
                int logoX = centerX - scaledWidth / 4;
                int logoY = centerY - scaledHeight / 4;

                //g2d.setColor(Color.RED);
                //g2d.drawLine(x - 10, y, x + 10, y);
                //g2d.drawLine(x, y - 10, x, y + 10);

                AffineTransform oldTransform = g2d.getTransform();

                // set up rotation around the center of the logo
                AffineTransform transform = new AffineTransform();
                transform.rotate(Math.toRadians(rotationAngle),
                        logoX + scaledWidth/2.0,
                        logoY + scaledHeight/2.0);
                g2d.setTransform(transform);

                g2d.drawImage(logoImage, logoX, logoY, scaledWidth, scaledHeight, null);
                g2d.setTransform(oldTransform);
            }
            // draw the skip text last so it stays on top
            String skipText = "Press any key to skip intro";
            g2d.setFont(new Font("MV Boli", Font.ITALIC, 20));

            long blinkCycle = System.currentTimeMillis() / 600;
            g2d.setColor(blinkCycle % 2 == 0 ? new Color(0, 0, 0, 180) : new Color(0, 0, 0, 60));

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(skipText);
            int textX = (game.getWidth() - textWidth) / 2;
            int textY = game.getHeight() - 60;

            g2d.drawString(skipText, textX, textY);
        } else if (introState == 3) {
            // loading screen
            if (loadingBackgroundImage != null) {
                g2d.drawImage(loadingBackgroundImage, 0, 0, GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT, null);
            } else {
                // fallback if image not found
                g2d.setColor(backgroundColor);
                g2d.fillRect(0, 0, game.getWidth(), game.getHeight());
            }

            // draw "Loading" text with animated dots
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("MV Boli", Font.BOLD, 24));

            StringBuilder loadingText = new StringBuilder("Loading");
            for (int i = 0; i < loadingDotCount; i++) {
                loadingText.append(".");
            }

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(loadingText.toString());
            int x = (game.getWidth() - textWidth) / 2;
            int y = game.getHeight() - 50;

            g2d.drawString(loadingText.toString(), x, y);

            if (volumeControl != null){
                long timeRemaining = LOADING_DURATION - (System.currentTimeMillis() - lastStateUpdateTime);
                float progress = 1f - (float) timeRemaining / LOADING_DURATION;

                // Fade from initialVolume to minVolume over 3 seconds
                float newVolume = initialVolume + progress * (minVolume - initialVolume);
                volumeControl.setValue(newVolume);

            }

        }

        g2d.dispose();
    }

    private void startMusic() {
        try {
            InputStream audioSrc = getClass().getResourceAsStream("/Audio/intro_music.wav");
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

            introClip = AudioSystem.getClip();
            introClip.open(audioStream);

            // get volume control
            volumeControl = (FloatControl) introClip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(initialVolume);

            introClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (introClip != null && introClip.isRunning()) {
            introClip.stop();
            introClip.close();
        }
    }

    public void resetIntro() {
        introState = 0;
        alpha = 0f;
        loadingDotCount = 0;
        rotationAngle = 0;
        currentPulseScale = 0;
        growing = true;
        lastStateUpdateTime = System.currentTimeMillis();
        lastDotUpdateTime = System.currentTimeMillis();
    }
}
