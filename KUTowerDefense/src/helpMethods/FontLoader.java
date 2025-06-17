package helpMethods;

import java.awt.*;
import java.io.InputStream;

public class FontLoader {
    public static Font loadMedodicaFont(float size) {
        try {
            InputStream is = FontLoader.class.getResourceAsStream("/fonts/MedodicaRegular.otf");
            if (is == null) {
                throw new RuntimeException("Font not found in resources/fonts/MedodicaRegular.otf");
            }

            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(size);
        } catch (FontFormatException | java.io.IOException e) {
            e.printStackTrace();
            return new Font("Monospaced", Font.PLAIN, (int) size); // fallback
        }
    }

}
