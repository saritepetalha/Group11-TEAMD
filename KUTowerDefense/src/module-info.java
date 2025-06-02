module KUTowerDefense {
	requires java.desktop;
	requires com.google.gson;

	// Gson-reflection access
	opens config      to com.google.gson;
	opens helpMethods to com.google.gson;
	opens stats       to com.google.gson;
}

