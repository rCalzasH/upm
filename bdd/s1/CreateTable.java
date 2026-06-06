/*
*primera parte s1, bdd grupo 16;
*Autores: Raul Calzas, Gonzalo Ramirez, Aymara Collado, Nouhayla El aissaoui
*hora fin: 16:24
*/
package cursos;
import java.sql.*;

public class CreateTable implements DataBaseTask {

    @Override
    public void run(Connection conn, String data) throws BBDDException, SQLException {
        Statement st = null;
        try{
            st= conn.createStatement();
            st.execute("DROP TABLE IF EXISTS imparte");
            st.execute(
                "CREATE TABLE imparte(" + "profesor_id INT NOT NULL," +
                "curso_id INT NOT NULL," + "n_modulo INT NOT NULL," +
                 "aula_id INT NOT NULL," + "fecha DATE NOT NULL," +
                 "PRIMARY KEY (profesor_id, curso_id, n_modulo, fecha)," + "FOREIGN KEY (profesor_id) REFERENCES profesor(id) ON DELETE CASCADE," +
                 "FOREIGN KEY (curso_id, n_modulo) REFERENCES modulo(curso_id, n_modulo) ON DELETE CASCADE," +
                 "FOREIGN KEY (aula_id) REFERENCES aula(id) ON DELETE CASCADE" + ")"
            );
            
        }catch(Exception e){
            if (e instanceof SQLException) throw (SQLException) e;
            throw new BBDDException(e, "CreateTable");
        }
    }
}
