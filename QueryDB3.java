import java.sql.*;

public class QueryDB3 {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3307/xixin_health?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "gwtzn7k8";

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(url, user, password);

        // Check doctor_department_rel for center C003
        System.out.println("=== doctor_department_rel for center C003 ===");
        PreparedStatement ps1 = conn.prepareStatement(
            "SELECT d.*, s.display_name, s.status as staff_status, s.is_deleted as staff_deleted " +
            "FROM doctor_department_rel d " +
            "LEFT JOIN staff_account s ON d.doctor_id = s.id " +
            "WHERE d.center_code = 'C003' AND d.is_deleted = 0");
        ResultSet rs1 = ps1.executeQuery();
        int count = 0;
        while (rs1.next()) {
            count++;
            System.out.println("Doctor ID: " + rs1.getLong("doctor_id") +
                ", Department: " + rs1.getString("department_code") +
                ", Name: " + rs1.getString("display_name") +
                ", Staff Status: " + rs1.getInt("staff_status") +
                ", Staff Deleted: " + rs1.getInt("staff_deleted"));
        }
        System.out.println("Total: " + count);

        // Check staff_role_rel for DOCTOR role
        System.out.println("\n=== staff_role_rel for DOCTOR role ===");
        PreparedStatement ps2 = conn.prepareStatement(
            "SELECT r.*, s.display_name " +
            "FROM staff_role_rel r " +
            "LEFT JOIN staff_account s ON r.staff_account_id = s.id " +
            "WHERE r.role_code = 'DOCTOR' AND r.is_deleted = 0");
        ResultSet rs2 = ps2.executeQuery();
        count = 0;
        while (rs2.next()) {
            count++;
            System.out.println("Staff ID: " + rs2.getLong("staff_account_id") +
                ", Name: " + rs2.getString("display_name"));
        }
        System.out.println("Total: " + count);

        conn.close();
    }
}
