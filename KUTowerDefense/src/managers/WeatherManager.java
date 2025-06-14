package managers;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import objects.Tower;
import objects.LightDecorator;

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
    private TowerManager towerManager;
    private boolean isLoadingFromSave = false; // Flag to prevent random weather during loading

    public WeatherManager() {
        weatherParticles = new ArrayList<>();
        random = new Random();

        WeatherType[] weatherTypes = {WeatherType.CLEAR, WeatherType.RAINY, WeatherType.SNOWY, WeatherType.WINDY};
        currentWeather = weatherTypes[random.nextInt(weatherTypes.length)];


        dayTime = 0;
        initializeWeatherParticles();

        startWeatherSound();
    }

    /**
     * Constructor with initial weather state (for loading saved games)
     * @param initialWeatherState The weather state to restore
     */
    public WeatherManager(java.util.Map<String, Object> initialWeatherState) {
        weatherParticles = new ArrayList<>();
        random = new Random();

        // Initialize with default values first
        dayTime = 0;
        nightIntensity = 0.0f;
        lastNightState = false;
        currentWeather = WeatherType.CLEAR; // Default fallback

        // Restore the weather state if provided
        if (initialWeatherState != null) {
            restoreWeatherState(initialWeatherState);
        } else {
            // Fallback to random weather if no state provided
            WeatherType[] weatherTypes = {WeatherType.CLEAR, WeatherType.RAINY, WeatherType.SNOWY, WeatherType.WINDY};
            currentWeather = weatherTypes[random.nextInt(weatherTypes.length)];
            initializeWeatherParticles();
            startWeatherSound();
        }
    }

    /**
     * Prepare WeatherManager for loading saved state (prevents random weather initialization)
     */
    public void prepareForLoading() {
        isLoadingFromSave = true;
        // Set to a neutral state while waiting for saved state to be loaded
        currentWeather = WeatherType.CLEAR;
        dayTime = 0;
        nightIntensity = 0.0f;
        lastNightState = false;
        lastWeather = null;

        // Initialize particles but don't start weather sounds yet
        weatherParticles.clear();
        initializeWeatherParticles();

        System.out.println("WeatherManager prepared for loading saved state");
    }

    /**
     * Complete the loading process after weather state has been restored
     */
    public void completeLoading() {
        isLoadingFromSave = false;
        System.out.println("WeatherManager loading completed with weather: " + currentWeather);
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

        BufferedImage nightOverlay = new BufferedImage(GAME_WIDTH, GAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D overlayG2d = nightOverlay.createGraphics();
        overlayG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        overlayG2d.setColor(new Color(0, 0, 0, (int)(nightIntensity * 255)));
        overlayG2d.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        if (towerManager != null) {
            overlayG2d.setComposite(AlphaComposite.DstOut);

            for (Tower tower : towerManager.getTowers()) {
                if (tower instanceof LightDecorator && !tower.isDestroyed()) {
                    LightDecorator lightTower = (LightDecorator) tower;
                    float lightRadius = lightTower.getLightRadius();

                    int centerX = tower.getX() + 32;
                    int centerY = tower.getY() + 32;

                    RadialGradientPaint lightGradient = new RadialGradientPaint(
                            centerX, centerY, lightRadius,
                            new float[]{0.0f, 0.3f, 0.7f, 1.0f},
                            new Color[]{
                                    new Color(255, 255, 255, (int)(nightIntensity * 255 * 0.85f)),
                                    new Color(255, 255, 255, (int)(nightIntensity * 255 * 0.65f)),
                                    new Color(255, 255, 255, (int)(nightIntensity * 255 * 0.25f)),
                                    new Color(255, 255, 255, 0)
                            }
                    );

                    overlayG2d.setPaint(lightGradient);
                    overlayG2d.fillOval(
                            (int)(centerX - lightRadius),
                            (int)(centerY - lightRadius),
                            (int)(lightRadius * 2),
                            (int)(lightRadius * 2)
                    );
                }
            }
        }

        overlayG2d.dispose();

        g2d.drawImage(nightOverlay, 0, 0, null);
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



    /**
     * Returns the enemy speed multiplier based on weather
     * Snowy weather slows down enemies by 25%
     */
    public float getEnemySpeedMultiplier() {
        return isSnowing() ? 0.75f : 1.0f;
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
            System.err.println("Hava durumu sesi çalarken hata: " + e.getMessage());
        }
    }

    public void stopAllWeatherSounds() {
        try {
            AudioManager.getInstance().stopAllWeatherAndMusic();
        } catch (Exception e) {
            System.err.println("Hava durumu sesleri durdurulurken hata: " + e.getMessage());
        }
    }

    public void setTowerManager(TowerManager towerManager) {
        this.towerManager = towerManager;
    }

    /**
     * Reset weather manager state for game restart
     */
    public void reset() {
        stopAllWeatherSounds();

        dayTime = 0;
        nightIntensity = 0.0f;
        lastNightState = false;

        WeatherType[] weatherTypes = {WeatherType.CLEAR, WeatherType.RAINY, WeatherType.SNOWY, WeatherType.WINDY};
        currentWeather = weatherTypes[random.nextInt(weatherTypes.length)];
        lastWeather = null;

        weatherParticles.clear();
        initializeWeatherParticles();

        startWeatherSound();

        System.out.println("WeatherManager reset: weather=" + currentWeather + ", day/night cycle reset");
    }

    /**
     * Get weather state for saving
     * @return A map containing all the weather state information
     */
    public java.util.Map<String, Object> getWeatherState() {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put("currentWeather", currentWeather.toString());
        state.put("dayTime", dayTime);
        state.put("nightIntensity", nightIntensity);
        state.put("lastNightState", lastNightState);
        return state;
    }

    /**
     * Restore weather state from saved data
     * @param weatherState The saved weather state data
     */
    @SuppressWarnings("unchecked")
    public void restoreWeatherState(java.util.Map<String, Object> weatherState) {
        try {
            // Stop current weather sounds before changing weather
            stopAllWeatherSounds();

            // Restore weather type
            String weatherTypeStr = (String) weatherState.get("currentWeather");
            if (weatherTypeStr != null) {
                currentWeather = WeatherType.valueOf(weatherTypeStr);
            }

            // Restore day/night cycle
            if (weatherState.containsKey("dayTime")) {
                dayTime = ((Number) weatherState.get("dayTime")).floatValue();
            }
            if (weatherState.containsKey("nightIntensity")) {
                nightIntensity = ((Number) weatherState.get("nightIntensity")).floatValue();
            }
            if (weatherState.containsKey("lastNightState")) {
                lastNightState = (Boolean) weatherState.get("lastNightState");
            }

            // Reset lastWeather to trigger weather sound
            lastWeather = null;

            // Reinitialize particles for new weather type
            weatherParticles.clear();
            initializeWeatherParticles();

            // Start weather sound for restored weather
            startWeatherSound();

            // Complete loading process if we were in loading mode
            if (isLoadingFromSave) {
                completeLoading();
            }

            System.out.println("Weather state restored: weather=" + currentWeather +
                    ", dayTime=" + dayTime + ", isNight=" + isNight());

        } catch (Exception e) {
            System.err.println("Failed to restore weather state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get current day time for saving
     * @return current day time value
     */
    public float getDayTime() {
        return dayTime;
    }

    /**
     * Set day time (used for restoration)
     * @param dayTime the day time to set
     */
    public void setDayTime(float dayTime) {
        this.dayTime = dayTime;
        updateNightIntensity(); // Recalculate night intensity based on new day time
    }

    /**
     * Set current weather type (used for restoration)
     * @param weatherType the weather type to set
     */
    public void setCurrentWeather(WeatherType weatherType) {
        WeatherType oldWeather = this.currentWeather;
        this.currentWeather = weatherType;

        // If weather changed, restart weather sounds and particles
        if (oldWeather != weatherType) {
            stopAllWeatherSounds();
            weatherParticles.clear();
            initializeWeatherParticles();
            startWeatherSound();
            lastWeather = null;
        }
    }
}