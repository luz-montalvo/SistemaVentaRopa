package pe.edu.upeu.sysventas.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pe.edu.upeu.sysventas.dto.PersonaDto;

import java.io.IOException;


public class ConsultaDNI {

    // Consulta los nombres de un DNI en la página eldni.com usando Jsoup (lee el HTML)
    public PersonaDto consultarDNI(String dni){

        PersonaDto personaDto=new PersonaDto();
        String url = "https://eldni.com/pe/buscar-datos-por-dni";
        try {

            // Primero: hacer GET para obtener el _token
            // 1) Abre la página para obtener el _token de seguridad del formulario
            Connection.Response getResponse = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .execute();

            Document doc = getResponse.parse();
            String token = doc.select("input[name=_token]").attr("value");

            // Ahora hacer POST con el DNI y el token
            // 2) Envía el formulario con el DNI, como si lo escribiéramos en la página
            Connection.Response postResponse = Jsoup.connect(url)
                    .cookies(getResponse.cookies()) // mantener la sesión
                    .data("_token", token)
                    .data("dni", dni)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();
            // Paso 3: parsear respuesta
            // 3) Lee la tabla de resultados: DNI, nombres, apellido paterno y materno
            Document resultDoc = Jsoup.parse(postResponse.body());
            Element fila = resultDoc.select("table tbody tr").first();
            if (fila != null) {
                Elements celdas = fila.select("td");
                personaDto.setDni(celdas.get(0).text());
                personaDto.setNombre(celdas.get(1).text());
                personaDto.setApellidoPaterno(celdas.get(2).text());
                personaDto.setApellidoMaterno(celdas.get(3).text());
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return personaDto;
    }

    public static void main(String[] args) {
        PersonaDto p=new ConsultaDNI().consultarDNI("43631919");
        if (p!=null)
            System.out.println(p.getDni()+"  "+p.getNombre()+"  "+p.getApellidoPaterno());
    }

}
