/*
* s3, bdd grupo 16;
*Autores: Raul Calzas, Gonzalo Ramirez, Aymara Collado, Nouhayla El aissaoui
*hora fin: 16:24
*/
package cursos;

import java.sql.*;
import java.util.*;

public class ConsultaConFiltro extends ConsultaConResultado<Properties> {
    /**
     * Obtiene los profesores que imparten un modulo cuyo titulo
     * contiene la cadena dada.
     *
     * @throws BBDDException, cuando data este vacia. Se debe fijar
     *         when a "filtro vacio"
     * @throws SQLException, cuando se produzca la misma al ejecutar
     *         modificar la tabla.
     */
    @Override
    public void run(Connection conn, String data) throws BBDDException, SQLException {
            //comprobamos data
            if(data==null|| data.isEmpty()) throw new BBDDException(null, "filtro vacio");
            resultado= new ArrayList<>();
            //para mejorar la claridad de la petición de datos crearemos un string sql con lo que queremos obtener 
            String sql = "SELECT p.nombre, p.apellido1, p.apellido2,i.curso_id, m.titulo " + 
                        "FROM profesor p " +
                        "JOIN imparte i ON p.id=i.profesor_id "+
                        "JOIN modulo m ON i.curso_id=m.curso_id AND i.n_modulo=m.n_modulo "+
                        "WHERE m.titulo LIKE ? " +
                        "ORDER BY p.apellido1 ASC "; //esto sería toda la petición necesaria para hacer 
            //creamos el statemetn 
            try(PreparedStatement st = conn.prepareStatement(sql)){
                //asignamos el valor a st
                st.setString(1,  "%" + data + "%");
                //crear rs
                ResultSet rs = st.executeQuery();
                while(rs.next()){
                    //asignamos los parametros 
                    String nombre = rs.getString("nombre");
                    String apellido1 = rs.getString("apellido1");
                    String apellido2 = rs.getString("apellido2");
                    String datas =rs.getInt("curso_id")+ "-" + rs.getString("titulo");
                    Properties p = new Properties(nombre, apellido1, apellido2, datas);
                    resultado.add(p);
                }
            }
    }
}