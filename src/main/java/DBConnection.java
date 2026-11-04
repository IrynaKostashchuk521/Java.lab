
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.InputStream;

public class DBConnection {

    private static final DBConnection INSTANCE = new DBConnection();

    private String url;
    private String username;
    private String password;

    private DBConnection() {
        try {
            Properties properties = new Properties();

            InputStream input = DBConnection.class
                    .getClassLoader()
                    .getResourceAsStream("application.properties");

            if (input == null) {
                throw new RuntimeException(
                        "Файл application.properties не знайдено"
                );
            }

            properties.load(input);
            input.close();

            url = properties.getProperty("db.url");
            username = properties.getProperty("db.username");
            password = properties.getProperty("db.password");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Помилка завантаження налаштувань БД", e
            );
        }
    }

    public static Connection getConnection() {
        try {
            return INSTANCE.createConnection();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Помилка підключення до MySQL: " + e.getMessage(), e
            );
        }
    }

    private Connection createConnection() throws Exception {
        return DriverManager.getConnection(url, username, password);
    }
}
