
import java.sql.Connection;
import java.sql.Statement;
import java.io.InputStream;
import java.util.Scanner;

public class DatabaseInitializer {

    public static void initialize() {

        try (
                Connection connection = DBConnection.getConnection();
                InputStream input = DatabaseInitializer.class
                        .getClassLoader()
                        .getResourceAsStream("init.sql")
        ) {

            if (input == null) {
                throw new RuntimeException("Файл init.sql не знайдено");
            }

            Scanner scanner = new Scanner(input);
            scanner.useDelimiter(";");

            try (Statement statement = connection.createStatement()) {

                while (scanner.hasNext()) {

                    String sql = scanner.next().trim();

                    if (!sql.isEmpty()) {
                        statement.execute(sql);
                    }
                }
            }

            System.out.println("Таблиці бази даних перевірено.");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Помилка ініціалізації бази даних", e
            );
        }
    }
}
