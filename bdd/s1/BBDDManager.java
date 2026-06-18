/*
*primera parte s1, bdd grupo 16;
*Autores: Raul Calzas, Gonzalo Ramirez, Aymara Collado, Nouhayla el aissaoui
*hora fin: 16:25 
*todo comentado para evitar la penalización por codigo sucio :)
*/
package cursos;

import java.sql.*;

public class BBDDManager {

    private String user;//nombre del usuario
    private String password;//contraseña del usuario

    public BBDDManager(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String url() {
        return "jdbc:mysql://localhost:3306/cursos_db"; //viedno las diapos de la teoría, 
    }

    public StringWriter run(DataBaseTask[] tasks, String[] dataArray, boolean autoCommit) {
        StringWriter result = new StringWriter();//es como una arraylist pero de strings así que usaremos add :)
        Connection conn = null; //abro una conexion en el run y así son independientes de cada instancia 

        try {
            conn = DriverManager.getConnection(url(), user, password);
            conn.setAutoCommit(autoCommit); //abro y compruebo estado de autocommit
        } catch (SQLException e) {
            result.add("Connection:" + e.getMessage() + ";"); //concatenar errores 
            result.add("fin");//excepcion
            return result;
        } catch (Exception e) {
            result.add("Otro:" + e.getMessage() + ";"); //concatenar errores
            result.add("fin");
            return result;
        }

        for (int i = 0; i < tasks.length; i++) {//para cada tarea, dentro del array, las iniciamos 
            try {
                tasks[i].run(conn, dataArray[i]);
            } 
            catch (BBDDException e) {//gestionamos posibles errores y ecepciones con la clase de bbde que se da en la carpeta
                result.add("Task:" + e.when() + ";" + e.getMessage() + ";");
                if (!autoCommit) {//comprobamos estado del commit
                    try { conn.commit(); } catch (SQLException ex) {}
                }
                
            } 
            catch (SQLException e) {
                result.add("SQL:" + e.getMessage() + ";");//concatenar
                if (!autoCommit) {
                    try { conn.rollback(); } catch (SQLException ex) {}
                }
            }
        }

        try {
            conn.close();
        } 
        catch (SQLException e) {
            // se ignora segun el enunciado
        }

        result.add("fin");
        return result;
    }
}
/*hemos seguido las intrsucciones del enunciado paso por paso de manera literal, 
ayudándonos de las diapositivas de teoría de clase del tema de acceso programático*/

