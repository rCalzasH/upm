package cursos;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class InsertaImparteDesdeCSV implements DataBaseTask {
    
    @Override
    public void run(Connection conn, String data) throws BBDDException, SQLException {

        
        String sql= "INSERT INTO imparte(profesor_id, curso_id, n_modulo, aula_id, fecha) VALUES (?,?,?,?,?)";
        //Creamos el preparedStatement (ps), para poder insertar datos en la base de datos
        //Tambien un FileInputStream (fis) y un scanner (sc) que lee fis
        try(PreparedStatement ps = conn.prepareStatement(sql); FileInputStream fis = new FileInputStream(data); Scanner sc = new Scanner(fis);)
        {
            //Leemos el archivo linea a linea, y separamos cada linea por comas para obtener los datos de cada campo
            while(sc.hasNextLine()){
                String linea = sc.nextLine().trim();
                if (linea.isEmpty()) continue;
                

                //Separamos cada linea por comas para obtener los datos de cada campo
                String [] cp = linea.split(","); 
                int profesor_id = Integer.parseInt(cp[0].trim());
                int curso_id = Integer.parseInt(cp[1].trim());
                int n_modulo = Integer.parseInt(cp[2].trim());
                int aula_id = Integer.parseInt(cp[3].trim());
                Date fecha = Date.valueOf(LocalDate.parse(cp[4].trim()));
                
                //Insertamos los datos en la base de datos
                ps.setInt(1, profesor_id);
                ps.setInt(2, curso_id);
                ps.setInt(3, n_modulo);
                ps.setInt(4, aula_id);
                ps.setDate(5, fecha);  

             //Si no se inserta una fila, lanzamos una excepcion
                if (ps.executeUpdate() != 1){
                    throw new SQLException("No se retorna 1 al insertar");
                }
            }

        }
        
        catch(SQLException e){
            throw e;
        }

        catch(Exception e){
            throw new BBDDException(e, "Error al insertar");
        }


    }
}
