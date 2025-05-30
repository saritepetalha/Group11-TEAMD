module KUTowerDefense {
	requires java.desktop;
	requires com.google.gson;

	exports managers;           // <-- lets JUnit test code see GameStateManager
	exports config;          // uncomment if tests use config.GameOptions, etc.

	// -- keep reflection access for Gson --------------------------------------
	opens config      to com.google.gson;
	opens helpMethods to com.google.gson;
	opens stats       to com.google.gson;
	opens managers    to com.google.gson;
}