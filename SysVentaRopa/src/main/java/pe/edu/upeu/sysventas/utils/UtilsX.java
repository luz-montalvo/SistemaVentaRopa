package pe.edu.upeu.sysventas.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class UtilsX {

    /**
     * Carga el archivo de idioma desde resources/language (idiomas-es, -en, -fr).
     * Al estar dentro del proyecto funciona sin importar desde qué carpeta se ejecute.
     */
    public Properties detectLanguage(String idioma) {
        Properties props = new Properties();
        String ruta = "/language/idiomas-" + idioma + ".properties";
        try (InputStream in = UtilsX.class.getResourceAsStream(ruta)) {
            if (in != null) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return props;
    }
}
