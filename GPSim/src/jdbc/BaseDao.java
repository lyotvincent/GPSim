package jdbc;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BaseDao {
	private static String driver="com.mysql.jdbc.Driver";
	private static String url="jdbc:mysql://localhost:3306/DGP";
    private static String user="root";
    private static String password="123456";

        static {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);    
    }
    
    public static void closeAll(Connection conn,Statement stmt,ResultSet rs) throws SQLException {
        if(rs!=null) {
            rs.close();
        }
        if(stmt!=null) {
            stmt.close();
        }
        if(conn!=null) {
            conn.close();
        }
    }
    

    public static ResultSet executeSQL(String preparedSql, Object[] param,Connection conn,PreparedStatement pstmt) throws ClassNotFoundException {
    	/* ����SQL,ִ��SQL */
        try {
        	conn = getConnection(); // �õ����ݿ�����
            pstmt = conn.prepareStatement(preparedSql); // �õ�PreparedStatement����
            if (param != null) {
                for (int i = 0; i < param.length; i++) {
                    pstmt.setObject(i + 1, param[i]); // ΪԤ����sql���ò���
                }
            }
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace(); // ����SQLException�쳣
        }
        return null;
    }
    
    public static <T> List<T> executeSQL(String preparedSql, Object[] param, Class<T> clazz) throws ClassNotFoundException {
    	Connection conn = null;
        PreparedStatement pstmt = null;
        
        List<T> list = new ArrayList<>();
        T t = null;
        
    	/* ����SQL,ִ��SQL */
        try {
        	conn = getConnection(); // �õ����ݿ�����
            pstmt = conn.prepareStatement(preparedSql); // �õ�PreparedStatement����
            if (param != null) {
                for (int i = 0; i < param.length; i++) {
                    pstmt.setObject(i + 1, param[i]); // ΪԤ����sql���ò���
                }
            }
            ResultSet rs = pstmt.executeQuery();
            //  ��ȡ�����Ԫ����
            ResultSetMetaData rsmd = rs.getMetaData();
            // ---> ��ȡ�еĸ���
            int columnCount = rsmd.getColumnCount();
            //  ����rs
            while (rs.next()) {
                // Ҫ��װ�Ķ���
                t = clazz.newInstance();

                // 7. ����ÿһ�е�ÿһ��, ��װ����
                for (int i=0; i<columnCount; i++) {
                    // ��ȡÿһ�е�������
                    String columnName = rsmd.getColumnName(i + 1);
                                        // ��ȡÿһ�е�������, ��Ӧ��ֵ
                    Object value = rs.getObject(columnName);
                    
                    String obName = columnName.replaceAll("_", "");
                    // ��װ�� ���õ�t�����������  ��BeanUtils�����
//                    BeanUtils.copyProperty(t, columnName, value);               
                }

                // �ѷ�װ��ϵĶ�����ӵ�list������
                list.add(t);
            }
            return list; 
        } catch (SQLException e) {
            e.printStackTrace(); // ����SQLException�쳣
        } catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}finally {
            try {
                BaseDao.closeAll(conn, pstmt, null);
            } catch (SQLException e1) {    
                e1.printStackTrace();
            }
        }
        return null;
    }

	public static int executeUpdate(String preparedSql, Object[] param) throws ClassNotFoundException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        /* ����SQL,ִ��SQL */
        try {
            conn = getConnection(); // �õ����ݿ�����
            pstmt = conn.prepareStatement(preparedSql); // �õ�PreparedStatement����
            if (param != null) {
                for (int i = 0; i < param.length; i++) {
                    pstmt.setObject(i + 1, param[i]); // ΪԤ����sql���ò���
                }
            }
            return pstmt.executeUpdate(); // ִ��SQL���
        } catch (SQLException e) {
            e.printStackTrace(); // ����SQLException�쳣
        } finally {
            try {
                BaseDao.closeAll(conn, pstmt, null);
            } catch (SQLException e) {    
                e.printStackTrace();
            }
        }
        return 0;
    }
    
}
