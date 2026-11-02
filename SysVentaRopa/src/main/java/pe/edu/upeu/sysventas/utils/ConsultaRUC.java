package pe.edu.upeu.sysventas.utils;

import pe.edu.upeu.sysventas.dto.PersonaDto;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consulta de RUC (SUNAT).
 *
 * <ul>
 *   <li><b>RUC que empieza con 10</b> (persona natural con negocio): los dígitos 3 al 10 son su DNI,
 *       así que se buscan los nombres con {@link ConsultaDNI}. No necesita token.</li>
 *   <li><b>RUC que empieza con 20</b> (empresa): se consulta la API de Decolecta
 *       (https://api.decolecta.com/v1/sunat/ruc). Necesita un token gratuito que se obtiene
 *       registrándose en https://decolecta.com y se pone en application.properties: {@code api.ruc.token=...}</li>
 * </ul>
 */
public class ConsultaRUC {

    /** Resultado de la consulta: razón social y dirección (puede venir vacía). */
    public record EmpresaDto(String razonSocial, String direccion) {
    }

    private static final String URL_API = "https://api.decolecta.com/v1/sunat/ruc?numero=";

    private final ConsultaDNI consultaDNI;
    private final String token;
    private HttpClient http;   // se crea recién cuando se necesita

    public ConsultaRUC(ConsultaDNI consultaDNI) {
        this.consultaDNI = consultaDNI;
        this.token = leerToken();
    }

    /** Devuelve los datos o null si no se encontró (o no hay internet / token). */
    public EmpresaDto consultarRUC(String ruc) {
        if (ruc == null || !ruc.matches("\\d{11}")) return null;

        // RUC 10: persona natural -> sus nombres salen del DNI incluido en el RUC
        if (ruc.startsWith("10")) {
            PersonaDto p = consultaDNI.consultarDNI(ruc.substring(2, 10));
            if (p != null && p.getNombre() != null) {
                return new EmpresaDto(p.getNombre() + " " + p.getApellidoPaterno() + " " + p.getApellidoMaterno(), "");
            }
        }
        // RUC 20 (u otro): API SUNAT con token
        return consultarApi(ruc);
    }

    public boolean tieneToken() {
        return token != null && !token.isBlank();
    }

    private EmpresaDto consultarApi(String ruc) {
        if (!tieneToken()) return null;
        try {
            if (http == null) {
                http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
            }
            HttpRequest req = HttpRequest.newBuilder(URI.create(URL_API + ruc))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return null;
            String razon = campoJson(resp.body(), "razon_social");
            if (razon == null || razon.isBlank()) return null;
            String dir = campoJson(resp.body(), "direccion");
            return new EmpresaDto(razon, dir == null ? "" : dir);
        } catch (Exception e) {
            return null;
        }
    }

    /** Lee un campo de texto simple del JSON, ej. "razon_social":"MI EMPRESA SAC". */
    private static String campoJson(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        return m.find() ? m.group(1).replace("\\\"", "\"").trim() : null;
    }

    private static String leerToken() {
        try (InputStream in = ConsultaRUC.class.getResourceAsStream("/application.properties")) {
            if (in == null) return "";
            Properties p = new Properties();
            p.load(in);
            return p.getProperty("api.ruc.token", "").trim();
        } catch (Exception e) {
            return "";
        }
    }
}
