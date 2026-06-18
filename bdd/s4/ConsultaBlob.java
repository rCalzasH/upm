/*
* s4, bdd grupo 16;
*Autores: Raul Calzas, Gonzalo Ramirez, Aymara Collado, Nouhayla El aissaoui
*hora fin: 17:46
*/

package cursos;

import java.sql.*;
import java.io.*;
import java.util.*;

public class ConsultaBlob implements DataBaseTask {
    
    /**
     * Realiza una consulta que recupera una imagen desde una bbdd.
     *
     * @param conn La conexion ya abierta
     * @param data el nombre del edificio (exacto)
     *    
     * @throws SQLException, cuando se produzca la misma al ejecutar
     *         los comandos sql.
     * @throws BBDDException, cuando se produzca una IOException,
     *         fija `when` a "error archivo"
     * @throws IllegalArgumentException, cuando el edificio no exista
     *         en la base de datos.
     */

    @Override
    public void run(Connection conn, String data) throws BBDDException, SQLException {
        String a = "SELECT foto FROM edificio WHERE nombre = ? ORDER BY id ASC LIMIT 1";
        try(PreparedStatement st = conn.prepareStatement(a)){
            st.setString(1,data);
            ResultSet res= st.executeQuery();
            //si no hay resultados se lanza IllegalArgumentException "no existe"
            if(!res.next())
                throw new IllegalArgumentException("no existe");
            //obtenemos el blob y sus bytes (posicion inicial 1)
            Blob bloby= res.getBlob(1);
            byte[] bt = bloby.getBytes(1,(int)bloby.length()); //obtenemos los bytes del blob
            //guardamos el contenido en un archivo llamado data + ".jpg"
            try(FileOutputStream archivo= new FileOutputStream(data + ".jpg")){
                archivo.write(bt);//escribimos los bytes en el archivo
            }catch (IOException e) {
                throw new BBDDException(e, "error archivo");
            }
        }
    }
}