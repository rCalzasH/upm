/*
* s4, bdd grupo 16;
*Autores: Raul Calzas, Gonzalo Ramirez, Aymara Collado, Nouhayla El aissaoui
*hora fin: 17:55
*/
package cursos;

import java.sql.*;
import java.util.ArrayList;
import java.math.BigDecimal;

public class ConsultaCompleja extends ConsultaConResultado<Properties> {
    
    /**
     * Realiza una consulta
     *
     * @param conn La conexion ya abierta
     * @param data un entero que indica un porcentaje
     *
     * @throws BBDDException, cuando `data` no se pueda convertir
     *         en un entero
     * @throws SQLException, cuando se produzca la misma al ejecutar
     *         los comandos sql.
     */
    
    @Override
    public void run(Connection conn, String data) throws BBDDException, SQLException {
        //en este caso no hay que strippear como en la práctica 3 ya que solo es un porcentaje 
        int dato;

        try{
            dato = Integer.parseInt(data);//revisamos como dice el enunciuado que s epeuda convertir a un int 

        }
        
        catch(Exception e){
            throw new BBDDException(e, "not int"); // Si no se puede convertir en int se lanza BBDDException "not int"

        }

        //declaramos el resultado para el properties de luego 
        resultado = new ArrayList<>();

        //peticion sql como siempre en un String para que sea mas legible    
        String sql= "SELECT p.nombre, p.apellido1, p.apellido2, i.curso_id, " +
            "       (SUM(m.horas) * 100.0 / hc.total_horas) AS porcentaje " +
            "FROM imparte i " +
            "JOIN profesor p ON i.profesor_id = p.id " +
            "JOIN modulo m ON i.curso_id = m.curso_id AND i.n_modulo = m.n_modulo " +
            "JOIN ( " +
            "    SELECT curso_id, SUM(horas) AS total_horas " +
            "    FROM modulo " +
            "    GROUP BY curso_id " +
            ") hc ON i.curso_id = hc.curso_id " +
            "GROUP BY p.id, p.nombre, p.apellido1, p.apellido2, i.curso_id, hc.total_horas " +
            "HAVING porcentaje >= ? " +
            "ORDER BY p.apellido1 ASC ";

        //generamos la preparedStatment 
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, dato);
            ResultSet rs = ps.executeQuery();
            //Bucle para rellenar los datos 
            while(rs.next()){
                String nombre = rs.getString("nombre");
                String apellido1 = rs.getString("apellido1");
                String apellido2 = rs.getString("apellido2");
                int curso = rs.getInt("curso_id");

                //Usamos bigDecimal como indica el enunciado 
                BigDecimal pct   = rs.getBigDecimal("dato");
                String extra     = "curso:" + curso + ":" + pct.toString();

                //sumamos al properties como en la practica 3 
                Properties p = new Properties(nombre, apellido1, apellido2, extra);
                resultado.add(p);

            }          
        }
    }
}