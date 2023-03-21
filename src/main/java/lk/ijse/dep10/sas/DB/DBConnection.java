package lk.ijse.dep10.sas.DB;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final DBConnection instance =new DBConnection();

    private final Connection connection;

    private DBConnection(){
        try {
            File file = new File("application.properties");
            Properties properties = new Properties();
            FileReader fr = new FileReader(file);
            properties.load(fr);
            fr.close();

            String host=properties.getProperty("mysql.host","loacalhost");
            String port=properties.getProperty("mysql.port","3306");
            String database=properties.getProperty("mysql.database","dep10_jdbc2");
            String username=properties.getProperty("mysql.username","root");
            String password=properties.getProperty("mysql.password","");

            String url="jdbc:mysql://"+host+":"+port+"/"+database+"?creatDatabaseIfNotExist=true&allowMultiQueries=true";
            connection = DriverManager.getConnection(url, username, password);

        } catch (FileNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Configuration file does not exist").showAndWait();
            e.printStackTrace();
            throw new RuntimeException(e);
        }catch (IOException e){
            new Alert(Alert.AlertType.ERROR,"Failed to read configurations").showAndWait();
            System.exit(1);
            throw new RuntimeException(e);
        }catch (SQLException e){
            new Alert(Alert.AlertType.ERROR,"Failed to establish the connection,try again. IF the problem persist please contact the technical team").showAndWait();
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }
    public static DBConnection getInstance(){
        return instance;
    }
    public Connection getConnection(){
        return connection;
    }
}
