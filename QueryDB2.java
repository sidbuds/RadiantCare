import java.sql.*;

public class QueryDB2 {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3307/xixin_health?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "gwtzn7k8";

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(url, user, password);

        // Check if there are ANY exam_task_item records for this task
        System.out.println("=== ALL exam_task_item for task_id=17 (including deleted) ===");
        PreparedStatement ps1 = conn.prepareStatement("SELECT COUNT(*) as cnt FROM exam_task_item WHERE task_id = 17");
        ResultSet rs1 = ps1.executeQuery();
        if (rs1.next()) {
            System.out.println("Count: " + rs1.getInt("cnt"));
        }

        // Check exam_task for task_id=17
        System.out.println("\n=== exam_task for task_id=17 ===");
        PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM exam_task WHERE id = 17");
        ResultSet rs2 = ps2.executeQuery();
        if (rs2.next()) {
            System.out.println("task_no: " + rs2.getString("task_no"));
            System.out.println("package_id: " + rs2.getLong("package_id"));
            System.out.println("center_code: " + rs2.getString("center_code"));
            System.out.println("order_id: " + rs2.getLong("order_id"));
            System.out.println("task_status: " + rs2.getInt("task_status"));
            System.out.println("is_deleted: " + rs2.getInt("is_deleted"));
        }

        // Check order for order_id
        System.out.println("\n=== order for order_id=34 ===");
        PreparedStatement ps3 = conn.prepareStatement("SELECT * FROM `order` WHERE id = 34");
        ResultSet rs3 = ps3.executeQuery();
        if (rs3.next()) {
            System.out.println("order_no: " + rs3.getString("order_no"));
            System.out.println("status: " + rs3.getInt("status"));
            System.out.println("user_id: " + rs3.getLong("user_id"));
            System.out.println("package_id: " + rs3.getLong("package_id"));
        }

        conn.close();
    }
}
