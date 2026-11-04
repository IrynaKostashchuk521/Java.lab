
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SqlInjectionDemo {

    // Безпечний варіант: використання PreparedStatement
    public static void safeSearch(String name) {

        String sql = "SELECT * FROM vehicles WHERE name = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {

                System.out.println("Безпечний запит:");

                while (resultSet.next()) {
                    System.out.println(resultSet.getString("name"));
                }
            }

        } catch (Exception e) {
            System.out.println("Помилка: " + e.getMessage());
        }
    }
}
