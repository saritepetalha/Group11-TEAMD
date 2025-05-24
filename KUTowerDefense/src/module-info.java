module KUTowerDefense {
	requires java.desktop;
	requires com.google.gson;

	// for any config-based serialization
	opens config to com.google.gson;
	// allow Gson to see private fields in helpMethods
	opens helpMethods to com.google.gson;
	opens managers to com.google.gson;
}
