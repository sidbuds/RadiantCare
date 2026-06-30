import java.sql.*;

public class QueryDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3307/xixin_health?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "gwtzn7k8";

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(url, user, password);

        System.out.println("=== Query all exam_task_item for task_id=17 ===");
        PreparedStatement ps1 = conn.prepareStatement("SELECT * FROM exam_task_item WHERE task_id = ?");
        ps1.setLong(1, 17);
        ResultSet rs1 = ps1.executeQuery();
        int count = 0;
        while (rs1.next()) {
            count++;
            System.out.println("ID: " + rs1.getLong("id") +
                ", task_item_no: " + rs1.getString("task_item_no") +
                ", item_code: " + rs1.getString("item_code") +
                ", is_deleted: " + rs1.getInt("is_deleted"));
        }
        System.out.println("Total records: " + count);

        System.out.println("\n=== Query exam_package_item for package_id=1003 ===");
        PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM exam_package_item WHERE package_id = ?");
        ps2.setLong(1, 1003);
        ResultSet rs2 = ps2.executeQuery();
        count = 0;
        while (rs2.next()) {
            count++;
            System.out.println("ID: " + rs2.getLong("id") +
                ", item_code: " + rs2.getString("item_code") +
                ", item_name: " + rs2.getString("item_name") +
                ", is_deleted: " + rs2.getInt("is_deleted"));
        }
        System.out.println("Total records: " + count);

        conn.close();
    }
}
