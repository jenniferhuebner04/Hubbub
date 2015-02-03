package edu.acc.j2ee.hubbub3;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class HubbubDB implements java.lang.AutoCloseable {
    
    private final Connection CONN;
    private final PreparedStatement psADD_USER;
    private final PreparedStatement psADD_POST;
    private final PreparedStatement psGET_POSTS;
    private final PreparedStatement psGET_USER;
    private final PreparedStatement psGET_USER2;
    private final Statement STAT;
    
    public HubbubDB() throws SQLException {
        String cs = "jdbc:derby://localhost:1527/hubbub1;user=javauser;password=javauser";
        this.CONN = DriverManager.getConnection(cs);
        this.psADD_USER = CONN.prepareStatement("INSERT INTO USERS VALUES (?,?,?)");
        this.psADD_POST = CONN.prepareStatement("INSERT INTO POSTS (CONTENT,AUTHOR,POSTDATE) VALUES (?,?,?)");
        this.psGET_POSTS = CONN.prepareStatement("SELECT * FROM POSTS WHERE AUTHOR = ?");
        this.psGET_USER = CONN.prepareStatement("SELECT username,joinDate FROM USERS WHERE USERNAME = ?");
        this.psGET_USER2 = CONN.prepareStatement("SELECT username,joinDate FROM USERS WHERE USERNAME = ? AND PASSWORD = ?");
        this.STAT = CONN.createStatement();
    }
    
    public void addUser(User u) throws SQLException {
        psADD_USER.setString(1, u.getUsername());
        psADD_USER.setDate(2, new Date(u.getJoinDate().getTime()));
        psADD_USER.executeUpdate();
    }
    
    public int getUserCount() throws SQLException {
        String query = "SELECT COUNT(USERNAME) FROM USERS";
        ResultSet rs = STAT.executeQuery(query);
        int result = rs.next() ? rs.getInt(1) : 0;
        rs.close();
        return result;
    }
    
    public int getPostCount() throws SQLException {
        String query = "SELECT COUNT(ID) FROM POSTS";
        ResultSet rs = STAT.executeQuery(query);
        int result = rs.next() ? rs.getInt(1) : 0;
        rs.close();
        return result;
    }
    
    public User[] getAllUsers() throws SQLException {
        int count = this.getUserCount();
        if (count == 0) return null;
        String query = "SELECT * FROM USERS";
        ResultSet rs = STAT.executeQuery(query);
        User[] users = new User[count];
        int usrIdx = 0;
        while (rs.next()) {
            String username = rs.getString(1);
            Date joinDate = rs.getDate(2);
            User u = new User();
            u.setUsername(username);
            u.setJoinDate(joinDate);
            users[usrIdx++] = u;
        }
        rs.close();
        return users;
    }
    
    public Post[] getAllPosts() throws SQLException {
        int count = this.getPostCount();
        if (count == 0) return null;
        String query = "SELECT * FROM POSTS ORDER BY postDate DESC";
        ResultSet rs = STAT.executeQuery(query);
        Post[] posts = new Post[count];
        int postIdx = 0;
        while (rs.next()) {
            String content = rs.getString(1);
            String author = rs.getString(2);
            Date postDate = rs.getDate(3);
            Post p = new Post();
            p.setContent(content);
            User u = getUserByUsername(author);
            p.setAuthor(u);
            p.setPostDate(postDate);
            posts[postIdx] = p;
            postIdx++;
        }
        rs.close();
        return posts;
    }
    
    public void addPost(Post p) throws SQLException {
            psADD_POST.setString(1, p.getContent());
            psADD_POST.setString(2, p.getAuthor().getUsername());
            Date d = new Date(p.getPostDate().getTime());
            psADD_POST.setDate(3, d);
            psADD_POST.executeUpdate();

    }
    
    private User getUserByUsername(String username) throws SQLException {
        psGET_USER.setString(1, username);
        ResultSet rs = psGET_USER.executeQuery();
        if (!rs.next()) return null;
        User user = new User();
        user.setUsername(username);
        Date d = rs.getDate(2);
        user.setJoinDate(d);
        rs.close();
        return user;
    }
    
    public User authenticateUser(String username, String password) throws SQLException {
        psGET_USER2.setString(1,username);
        psGET_USER2.setString(2,password);
        ResultSet rs = psGET_USER2.executeQuery();
        if (!rs.next()) return null;
        User user = new User();
        user.setUsername(rs.getString(1));
        user.setJoinDate(rs.getDate(2));
        rs.close();
        return user;
    }
    
    @Override
    public void close() throws SQLException {
        this.psADD_POST.close();
        this.psGET_POSTS.close();
        this.psADD_USER.close();
        this.psGET_USER.close();
        this.CONN.close();
    }
}