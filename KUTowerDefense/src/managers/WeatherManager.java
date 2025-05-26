package managers;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeatherManager {
    private static final int MAX_PARTICLES = 200;
    private static final float PARTICLE_SPEED = 15.0f;
    private static final float DAY_DURATION = 30.0f;
    private static final float NIGHT_DURATION = 15.0f;
    private static final float TRANSITION_DURATION = 3.0f;
    private static final int GAME_WIDTH = 1024;
    private static final int GAME_HEIGHT = 576;

    public enum WeatherType {
        CLEAR,
        RAINY,
        SNOWY,
        WINDY
    }

    private List<WeatherParticle> weatherParticles;
    private Random random;
    private WeatherType currentWeather;
    private WeatherType lastWeather = null;
    private float dayTime;
    private float nightIntensity;
    private boolean lastNightState = false;

    public WeatherManager() {
        weatherParticles = new ArrayList<>();
        random = new Random();

        WeatherType[] weatherTypes = {WeatherType.CLEAR, WeatherType.RAINY, WeatherType.SNOWY, WeatherType.WINDY};
        currentWeather = weatherTypes[random.nextInt(weatherTypes.length)];


        dayTime = 0;
        initializeWeatherParticles();

        startWeatherSound();
    }

    public void update() {
        updateDayNightCycle();
        updateWeatherParticles();
        checkWeatherChange();
    }

    public void update(float deltaTime) {
        updateDayNightCycle(deltaTime);
        updateWeatherParticles();
    }

    private void updateDayNightCycle() {
        dayTime += 1.0f / 60.0f;
        if (dayTime >= DAY_DURATION + NIGHT_DURATION) {
            dayTime = 0;
        }

        updateNightIntensity();
    }

    private void updateDayNightCycle(float deltaTime) {
        dayTime += deltaTime;
        if (dayTime >= DAY_DURATION + NIGHT_DURATION) {
            dayTime = 0;
        }

        updateNightIntensity();
    }

    private void updateNightIntensity() {
        if (dayTime < DAY_DURATION) {
            nightIntensity = 0.0f;
        } else if (dayTime < DAY_DURATION + TRANSITION_DURATION) {
            float transitionProgress = (dayTime - DAY_DURATION) / TRANSITION_DURATION;
            nightIntensity = transitionProgress * 0.8f;
        } else if (dayTime < DAY_DURATION + NIGHT_DURATION - TRANSITION_DURATION) {
            nightIntensity = 0.8f;
        } else {
            float transitionProgress = (dayTime - (DAY_DURATION + NIGHT_DURATION - TRANSITION_DURATION)) / TRANSITION_DURATION;
            nightIntensity = 0.8f * (1.0f - transitionProgress);
        }
    }

    private void updateWeatherParticles() {
        for (int i = weatherParticles.size() - 1; i >= 0; i--) {
            WeatherParticle particle = weatherParticles.get(i);
            particle.update();

            if (particle.getY() > GAME_HEIGHT) {
                particle.reset(
                        random.nextInt(GAME_WIDTH),
                        -10,
                        PARTICLE_SPEED * (0.8f + random.nextFloat() * 0.4f),
                        0.5f + random.nextFloat() * 0.5f,
                        10.0f + random.nextFloat() * 10.0f,
                        (float)Math.PI/2 + (random.nextFloat() * 0.3f - 0.15f)
                );
            }
        }
    }

    public void draw(Graphics g) {
        if (currentWeather == WeatherType.RAINY) {
            drawRainEffect(g);
        } else if (currentWeather == WeatherType.SNOWY) {
            drawSnowEffect(g);
        } else if (currentWeather == WeatherType.WINDY) {
            drawWindEffect(g);
        }

        if (isNight()) {
            drawNightOverlay(g);
        }
    }

    private void drawRainEffect(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(100, 150, 255, 180));



        for (WeatherParticle particle : weatherParticles) {
            float length = particle.getLength();
            float angle = particle.getAngle();

            g2d.setStroke(new BasicStroke(Math.max(1.0f, particle.getThickness())));
            g2d.drawLine(
                    (int) particle.getX(),
                    (int) particle.getY(),
                    (int) (particle.getX() + length * (float)Math.cos(angle)),
                    (int) (particle.getY() + length * (float)Math.sin(angle))
            );
        }
    }

    private void drawSnowEffect(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(255, 255, 255, 220));

        for (WeatherParticle particle : weatherParticles) {
            int size = (int)(particle.getThickness() * 3);
            g2d.fillOval(
                    (int) particle.getX() - size/2,
                    (int) particle.getY() - size/2,
                    size,
                    size
            );
        }
    }

    private void drawWindEffect(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(139, 69, 19, 150));

        for (WeatherParticle particle : weatherParticles) {
            int size = (int)(particle.getThickness() * 2);
            g2d.fillOval(
                    (int) particle.getX(),
                    (int) particle.getY(),
                    size * 2,
                    size
            );
        }
    }

    private void drawNightOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, nightIntensity));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public boolean isNight() {
        boolean night = dayTime >= DAY_DURATION;
        if (night != lastNightState) {

            lastNightState = night;
        }
        return night;
    }

    public float getNightIntensity() {
        return nightIntensity;
    }

    public boolean isRaining() {
        return currentWeather == WeatherType.RAINY;
    }

    public boolean isSnowing() {
        return currentWeather == WeatherType.SNOWY;
    }

    public boolean isWindy() {
        return currentWeather == WeatherType.WINDY;
    }

    public String getCurrentTimeOfDay() {
        return isNight() ? "Night" : "Day";
    }

    public float getTowerRangeMultiplier() {
        return isRaining() ? 0.8f : 1.0f;
    }

    private void initializeWeatherParticles() {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            weatherParticles.add(new WeatherParticle(
                    random.nextInt(GAME_WIDTH),
                    random.nextInt(GAME_HEIGHT),
                    PARTICLE_SPEED * (0.8f + random.nextFloat() * 0.4f),
                    0.5f + random.nextFloat() * 0.5f,
                    10.0f + random.nextFloat() * 10.0f,
                    (float)Math.PI/2 + (random.nextFloat() * 0.3f - 0.15f)
            ));
        }
    }

    private static class WeatherParticle {
        private float x, y;
        private float speed;
        private float thickness;
        private float length;
        private float angle;

        public WeatherParticle(float x, float y, float speed, float thickness, float length, float angle) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.thickness = thickness;
            this.length = length;
            this.angle = angle;
        }

        public void update() {
            y += speed;
        }

        public void reset(float x, float y, float speed, float thickness, float length, float angle) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.thickness = thickness;
            this.length = length;
            this.angle = angle;
        }

        public float getX() { return x; }
        public float getY() { return y; }
        public float getLength() { return length; }
        public float getAngle() { return angle; }
        public float getThickness() { return thickness; }
    }

    public WeatherType getCurrentWeatherType() {
        return currentWeather;
    }

    private void checkWeatherChange() {
        if (lastWeather != currentWeather) {

            startWeatherSound();
            lastWeather = currentWeather;
        }
    }

    private void startWeatherSound() {
        try {
            AudioManager audioManager = AudioManager.getInstance();

            audioManager.stopWeatherSounds();

            switch (currentWeather) {
                case RAINY:
                    audioManager.playWeatherSound("rain");
                    break;
                case SNOWY:
                    audioManager.playWeatherSound("snow");
                    break;
                case WINDY:
                    audioManager.playWeatherSound("wind");
                    break;
                case CLEAR:
                    break;
            }
        } catch (Exception e) {
        }
    }

    public void stopAllWeatherSounds() {
        try {
            AudioManager.getInstance().stopAllWeatherAndMusic();
        } catch (Exception e) {
        }
    }
}