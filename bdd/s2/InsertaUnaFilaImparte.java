/*
*Práctica 2 de bases grupo 16. Sesion 2
*Autores: Raul Calzas, Gonzalo Ramirez, Aymara Collado, Nouhayla el aissaoui
*/
package cursos;

import java.sql.*;
import java.time.LocalDate;

public class InsertaUnaFilaImparte implements DataBaseTask {

    @Override
    public void run(Connection conn, String data) throws BBDDException, SQLException {
    PreparedStatement ep = null;

    int profesor_id, curso_id, n_modulo, aula_id;
    Date fechar;

    try {
        
        String[] campSep = data.split(",");
        profesor_id = Integer.parseInt(campSep[0].trim());
        curso_id = Integer.parseInt(campSep[1].trim());//separamos todos los datos 
        n_modulo = Integer.parseInt(campSep[2].trim());
        aula_id = Integer.parseInt(campSep[3].trim());

        String[] fechaSep = campSep[4].trim().split("/");
        int dia = Integer.parseInt(fechaSep[0]);
        int mes = Integer.parseInt(fechaSep[1]);//separamos la fecha 
        int annio = Integer.parseInt(fechaSep[2]);
        fechar = Date.valueOf(LocalDate.of(annio, mes, dia));

    } catch (Exception e) {
        throw new BBDDException(e, "Insertando");
    }

    try {
        ep = conn.prepareStatement(
            "INSERT INTO imparte (profesor_id, curso_id, n_modulo, aula_id, fecha) VALUES (?, ?, ?, ?, ?)"
        );
        ep.setInt(1, profesor_id);
        ep.setInt(2, curso_id);
        ep.setInt(3, n_modulo);//vamos insertando los datos 
        ep.setInt(4, aula_id);
        ep.setDate(5, fechar);

        int filas = ep.executeUpdate();
        if (filas != 1) throw new SQLException("La insercion no retorna 1");//si no es una fila solamente vamos a lanzar la excepcion

    } finally {
        if (ep != null) ep.close();
    }
}
}
