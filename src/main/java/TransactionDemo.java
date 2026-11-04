import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionDemo {

    public static void demonstrateCommit() {

        String sql = "INSERT IGNORE INTO categories (name) VALUES (?)";

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, "Тестова категорія commit");
                statement.executeUpdate();

                connection.commit();

                System.out.println("Виконано commit. Зміни збережено.");

            } catch (Exception e) {

                connection.rollback();

                System.out.println("Помилка: " + e.getMessage());
                System.out.println("Виконано rollback.");

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            System.out.println("Помилка транзакції: " + e.getMessage());
        }

        // Видаляємо тестову категорію після перевірки commit
        String deleteSql = "DELETE FROM categories WHERE name = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(deleteSql)) {

            statement.setString(1, "Тестова категорія commit");
            statement.executeUpdate();

        } catch (Exception e) {
            System.out.println("Помилка очищення тестових даних: " + e.getMessage());
        }
    }

    public static void demonstrateRollback() {

        String categorySql =
                "INSERT INTO categories (name) VALUES (?)";

        String vehicleSql =
                "INSERT INTO vehicles " +
                "(name, brand, model, year, daily_rate, status, " +
                "vehicle_type, body_type, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int categoryId;

                // Додаємо нову категорію
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     categorySql,
                                     PreparedStatement.RETURN_GENERATED_KEYS
                             )) {

                    statement.setString(1, "Тестова категорія rollback");
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        keys.next();
                        categoryId = keys.getInt(1);
                    }
                }

                // Додаємо транспорт у нову категорію
                try (PreparedStatement statement =
                             connection.prepareStatement(vehicleSql)) {

                    statement.setString(1, "Test Car");
                    statement.setString(2, "Test");
                    statement.setString(3, "Rollback");
                    statement.setInt(4, 2025);
                    statement.setBigDecimal(
                            5, new java.math.BigDecimal("1000.00")
                    );
                    statement.setString(6, "Доступний");
                    statement.setString(7, "Автомобіль");
                    statement.setString(8, "Тестовий");
                    statement.setInt(9, categoryId);

                    statement.executeUpdate();
                }

                // Навмисно викликаємо помилку
                throw new SQLException("Тестова помилка для rollback");

            } catch (Exception e) {

                connection.rollback();

                System.out.println("Помилка: " + e.getMessage());
                System.out.println("Виконано rollback. Зміни скасовано.");

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Помилка транзакції: " + e.getMessage());
        }
    }
}
