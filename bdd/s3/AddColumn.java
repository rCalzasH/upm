/*
*s3, bbdd grupo 16;
*Autores: Raul Calzas, Gonzalo Ramirez, Aymara Collado, Nouhayla El aissaoui
*hora fin: 15:15
*/


package cursos;

import java.sql.*;

public class AddColumn implements DataBaseTask {
    /**
     * Pone una nueva columna en `edificio`.
     *
     * @param conn La conexion ya abierta
     * @param data No es necesaria, se asume vacia.
     *
     * @throws BBDDException (no es necesario lanzarla)
     * @throws SQLException, cuando se produzca la misma al ejecutar
     *         la modificacion de la tabla.
     */
    @Override
    public void run(Connection conn, String data) throws BBDDException, SQLException {

        //Solo se lanzan excepciones que vienen de la ejecución SQL
        try(Statement st = conn.createStatement()){
            st.execute("ALTER TABLE edificio ADD COLUMN foto BLOB NULL");

        }
    }
}