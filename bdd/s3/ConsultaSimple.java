/*
*s3, bdd grupo 16;
*Autores: Raul Calzas, Gonzalo Ramirez, Aymara Collado, Nouhayla El aissaoui
*hora fin: 15:41
*/

package cursos;

import java.sql.*;

import java.util.*;

public class ConsultaSimple extends ConsultaConResultado<Properties> {

    /**
     * Obtiene los profesores ordenados por apellido1
     *
     * @param conn La conexion ya abierta
     * @param data o bien ASC o bien DESC (debe ser case-insentive)
     *
     * @throws BBDDException, cuando `data` sea distinto de ACS y DESC.
     * @throws SQLException, cuando se produzca la misma al ejecutar
     *         modificar la tabla.
     */

    @Override
    public void run(Connection conn, String data) throws BBDDException, SQLException {
        //Si data no es ASC ni DESC lanzamos una BBDDException

        if(!data.equalsIgnoreCase("ASC") && !data.equalsIgnoreCase("DESC")) throw new BBDDException(null, "ordenando"); 

        //Hacemos un Arraylist al que le añadiremos cada profesor con nombre y apellidos, ordenandolos alfabeticamente por apellido1
        resultado = new ArrayList<>();

        try (Statement st = conn.createStatement()){

            ResultSet rs = st.executeQuery("SELECT nombre, apellido1, apellido2 FROM profesor ORDER BY apellido1 "+ data);

            //Consultamos en el ResultSet (res) los nombres y apellidos de cada profesor y los añadimos los nombres al arraylist (rs) 
            while(rs.next()){

                String nombre = rs.getString("nombre");
                String apellido1 = rs.getString("apellido1");
                String apellido2 = rs.getString("apellido2");
                resultado.add(new Properties(nombre, apellido1, apellido2));

            }
        }
    }
}