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
    	/* 处理SQL,执行SQL */
        try {
        	conn = getConnection(); // 得到数据库连接
            pstmt = conn.prepareStatement(preparedSql); // 得到PreparedStatement对象
            if (param != null) {
                for (int i = 0; i < param.length; i++) {
                    pstmt.setObject(i + 1, param[i]); // 为预编译sql设置参数
                }
            }
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace(); // 处理SQLException异常
        }
        return null;
    }
    
    public static <T> List<T> executeSQL(String preparedSql, Object[] param, Class<T> clazz) throws ClassNotFoundException {
    	Connection conn = null;
        PreparedStatement pstmt = null;
        
        List<T> list = new ArrayList<>();
        T t = null;
        
    	/* 处理SQL,执行SQL */
        try {
        	conn = getConnection(); // 得到数据库连接
            pstmt = conn.prepareStatement(preparedSql); // 得到PreparedStatement对象
            if (param != null) {
                for (int i = 0; i < param.length; i++) {
                    pstmt.setObject(i + 1, param[i]); // 为预编译sql设置参数
                }
            }
            ResultSet rs = pstmt.executeQuery();
            //  获取结果集元数据
            ResultSetMetaData rsmd = rs.getMetaData();
            // ---> 获取列的个数
            int columnCount = rsmd.getColumnCount();
            //  遍历rs
            while (rs.next()) {
                // 要封装的对象
                t = clazz.newInstance();

                // 7. 遍历每一行的每一列, 封装数据
                for (int i=0; i<columnCount; i++) {
                    // 获取每一列的列名称
                    String columnName = rsmd.getColumnName(i + 1);
                                        // 获取每一列的列名称, 对应的值
                    Object value = rs.getObject(columnName);
                    
                    String obName = columnName.replaceAll("_", "");
                    // 封装： 设置到t对象的属性中  【BeanUtils组件】
//                    BeanUtils.copyProperty(t, columnName, value);               
                }

                // 把封装完毕的对象，添加到list集合中
                list.add(t);
            }
            return list; 
        } catch (SQLException e) {
            e.printStackTrace(); // 处理SQLException异常
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
        /* 处理SQL,执行SQL */
        try {
            conn = getConnection(); // 得到数据库连接
            pstmt = conn.prepareStatement(preparedSql); // 得到PreparedStatement对象
            if (param != null) {
                for (int i = 0; i < param.length; i++) {
                    pstmt.setObject(i + 1, param[i]); // 为预编译sql设置参数
                }
            }
            return pstmt.executeUpdate(); // 执行SQL语句
        } catch (SQLException e) {
            e.printStackTrace(); // 处理SQLException异常
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
