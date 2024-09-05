import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

public class MassInsert {

    // Configurações do banco de dados
    private static final String URL = "jdbc:postgresql://localhost:5432/EstoquedeProdutos";
    private static final String USER = "postgres";
    private static final String PASSWORD = "2004.Gabi";

    // Quantidade de registros
    private static final int NUM_RECORDS = 2000000;

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {

            // Medir o tempo para inserir na tabela Products
            Instant start = Instant.now();
            insertIntoProducts(connection);
            Instant end = Instant.now();
            System.out.println("Tempo para inserir na tabela Products: " + Duration.between(start, end).toMillis() + " ms");

            // Medir o tempo para inserir na tabela Inventory
            start = Instant.now();
            insertIntoInventory(connection);
            end = Instant.now();
            System.out.println("Tempo para inserir na tabela Inventory: " + Duration.between(start, end).toMillis() + " ms");

            // Medir o tempo para inserir na tabela Transactions
            start = Instant.now();
            insertIntoTransactions(connection);
            end = Instant.now();
            System.out.println("Tempo para inserir na tabela Transactions: " + Duration.between(start, end).toMillis() + " ms");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para inserir dados na tabela Products
    private static void insertIntoProducts(Connection connection) throws SQLException {
        String sql = "INSERT INTO Products (name, description, unit_price) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 1; i <= NUM_RECORDS; i++) {
                pstmt.setString(1, "Produto " + i);
                pstmt.setString(2, "Descrição do produto " + i);
                pstmt.setBigDecimal(3, new java.math.BigDecimal(Math.random() * 100)); // Preço aleatório entre 0 e 100
                pstmt.addBatch();

                // Execute em lotes de 1000 para melhor desempenho
                if (i % 1000 == 0) {
                    pstmt.executeBatch();
                }
            }
            pstmt.executeBatch(); // Inserir o restante
        }
    }

    // Método para inserir dados na tabela Inventory
    private static void insertIntoInventory(Connection connection) throws SQLException {
        String sql = "INSERT INTO Inventory (product_id, quantity) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 1; i <= NUM_RECORDS; i++) {
                pstmt.setInt(1, i); // O ID do produto correspondente
                pstmt.setInt(2, (int) (Math.random() * 1000)); // Quantidade aleatória entre 0 e 1000
                pstmt.addBatch();

                // Execute em lotes de 1000
                if (i % 1000 == 0) {
                    pstmt.executeBatch();
                }
            }
            pstmt.executeBatch(); // Inserir o restante
        }
    }

    // Método para inserir dados na tabela Transactions
    private static void insertIntoTransactions(Connection connection) throws SQLException {
        String sql = "INSERT INTO Transactions (product_id, transaction_type, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 1; i <= NUM_RECORDS; i++) {
                pstmt.setInt(1, i); // O ID do produto correspondente
                pstmt.setString(2, i % 2 == 0 ? "compra" : "venda"); // Alterna entre 'compra' e 'venda'
                pstmt.setInt(3, (int) (Math.random() * 499) + 1); // Quantidade aleatória entre 1 e 500
                pstmt.addBatch();

                // Execute em lotes de 1000
                if (i % 1000 == 0) {
                    pstmt.executeBatch();
                }
            }
            pstmt.executeBatch(); // Inserir o restante
        }
    }
}
