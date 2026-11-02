package pe.edu.upeu.sysventas.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * INVESTIGACIÓN ADICIONAL (Punto 6): Encriptación de claves con SHA-256 + "sal".
 *
 * <p>Antes la clave se guardaba tal cual (ej. "123456") y cualquiera que abriera la base
 * de datos podía leerla. Ahora se guarda un HASH: un código que no se puede revertir.
 *
 * <ul>
 *   <li><b>Hash SHA-256</b>: convierte la clave en un código de 32 bytes. Es de una sola vía:
 *       del hash no se puede volver a la clave.</li>
 *   <li><b>Sal (salt)</b>: 16 bytes aleatorios distintos para cada usuario. Así, dos usuarios con
 *       la misma clave "123456" tienen hashes diferentes, y no sirven las tablas de hashes ya calculados.</li>
 * </ul>
 *
 * <p>Formato guardado en la columna {@code Usuario.clave}: {@code salBase64:hashBase64}
 * (69 caracteres, entra en VARCHAR(100)).
 *
 * <p>Para el login NO se "desencripta": se vuelve a calcular el hash de lo que escribió
 * el usuario con la misma sal y se compara con el guardado.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TAM_SAL = 16;
    private static final String SEPARADOR = ":";

    private PasswordUtil() {
    }

    /** Genera sal aleatoria + hash. Devuelve "sal:hash" listo para guardar en la BD. */
    public static String encriptar(String clave) {
        byte[] sal = new byte[TAM_SAL];
        RANDOM.nextBytes(sal);
        byte[] hash = sha256(sal, clave);
        Base64.Encoder b64 = Base64.getEncoder();
        return b64.encodeToString(sal) + SEPARADOR + b64.encodeToString(hash);
    }

    /** Compara la clave escrita en el login con la guardada en la BD. */
    public static boolean verificar(String claveIngresada, String claveGuardada) {
        if (claveIngresada == null || !estaEncriptada(claveGuardada)) {
            return false;
        }
        String[] partes = claveGuardada.split(SEPARADOR);
        byte[] sal = Base64.getDecoder().decode(partes[0]);
        byte[] hashGuardado = Base64.getDecoder().decode(partes[1]);
        byte[] hashIngresado = sha256(sal, claveIngresada);
        // Comparación en tiempo constante (no da pistas por el tiempo que tarda)
        return MessageDigest.isEqual(hashGuardado, hashIngresado);
    }

    /** true si el texto ya tiene el formato "sal:hash" (sirve para migrar claves antiguas). */
    public static boolean estaEncriptada(String valor) {
        if (valor == null) return false;
        String[] partes = valor.split(SEPARADOR);
        if (partes.length != 2) return false;
        try {
            return Base64.getDecoder().decode(partes[0]).length == TAM_SAL
                    && Base64.getDecoder().decode(partes[1]).length == 32;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] sha256(byte[] sal, String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(sal);
            return md.digest(clave.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
