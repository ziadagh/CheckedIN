package com.example.Ibrahim.myapplication.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.utils.SystemProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "userApi",
        version = "v1",
        resource = "user",
        namespace = @ApiNamespace(
                ownerDomain = "backend.myapplication.Ibrahim.example.com",
                ownerName = "backend.myapplication.Ibrahim.example.com",
                packagePath = ""
        )
)
public class UserEndpoint {
    private static final Logger logger = Logger.getLogger(UserEndpoint.class.getName());
    protected static Connection con = null;

    public static Connection beginConnection() {
        while (con == null) {
            if (SystemProperty.environment.value() ==
                    SystemProperty.Environment.Value.Production) {
                try {
                    Class.forName("com.mysql.jdbc.GoogleDriver");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                // Connecting from an external network.
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            try {
                con = DriverManager.getConnection(
                        "jdbc:google:mysql://gleaming-abacus-123320:checked-in/checkedin", "root", "123456");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return con;
    }

    /**
     * This method gets the <code>User</code> object associated with the specified <code>id</code>.
     *
     * @param id The id of the object to be returned.
     * @return The <code>User</code> associated with <code>id</code>.
     */

    @ApiMethod(name = "getUser", path = "get_user")
    public User getUser(@Named("id") Long id) throws SQLException {
        if (con == null)
            con = beginConnection();
        ResultSet rs = con.createStatement().executeQuery("Select* from `checkedin`.`users` where id=" + id + ";");
        logger.info("Calling getUser method");
        while (rs.next()) {
            String username = rs.getString("username");
            String fname = rs.getString("fname");
            String lname = rs.getString("lname");
            String email = rs.getString("email");
            String dob = rs.getString("dob");
            String bio = rs.getString("bio");
            String password = rs.getString("password");
            String sex = rs.getString("sex");
            String status = rs.getString("status");
            String city = rs.getString("currentcity");
            User user = new User();
            user.setId(id);
            user.setUsername(username);
            user.setPassword(password);
            user.setFname(fname);
            user.setLname(lname);
            user.setDob(dob);
            user.setSex(sex);
            user.setStatus(status);
            user.setCurrentCity(city);
            user.setEmail(email);
            user.setBio(bio);
            return user;
        }
        return null;
    }

    @ApiMethod(name = "getUserByUsername", path = "getUserByUsername")
    public User getUserByUsername(@Named("username") String username) throws SQLException {
        if (con == null)
            con = beginConnection();
        ResultSet rs = con.createStatement().executeQuery("Select* from `checkedin`.`users` where username='" + username + "';");
        logger.info("Calling getUserByUsername method");
        while (rs.next()) {
            int id = rs.getInt("id");
            username = rs.getString("username");
            String fname = rs.getString("fname");
            String lname = rs.getString("lname");
            String email = rs.getString("email");
            String dob = rs.getString("dob");
            String bio = rs.getString("bio");
            String password = rs.getString("password");
            String sex = rs.getString("sex");
            String status = rs.getString("status");
            String city = rs.getString("currentcity");
            User user = new User();
            user.setId(id);
            user.setUsername(username);
            user.setPassword(password);
            user.setFname(fname);
            user.setLname(lname);
            user.setDob(dob);
            user.setSex(sex);
            user.setStatus(status);
            user.setCurrentCity(city);
            user.setEmail(email);
            user.setBio(bio);
            return user;
        }
        return null;
    }


    /**
     * This inserts a new <code>User</code> object.
     *
     * @param user The object to be added.
     * @return The object to be added.
     */
    @ApiMethod(name = "insertUser", path = "inser_user")
    public User insertUser(User user) throws SQLException {
        if (con == null)
            con = beginConnection();
        int rs = con.createStatement().executeUpdate(
                "INSERT INTO `checkedin`.`users`(`username`,`fname`,`lname`,`email`,`sex`,`password`, `currentcity`,`dob`,`bio`)" +
                        "VALUES ('" + user.getUsername() + "', '" + user.getFname() + "','" + user.getLname() + "','" + user.getEmail() + "','" + user.getSex() + "','" + user.getPassword() + "','" + user.getCurrentCity() + "','" + user.getDob() + "','" + user.getBio() + "');");
        con.createStatement().execute(
                "Create table `" + user.getUsername() + "iliked`(userId int NOT NULL Unique,username varchar(45) NOT NULL Primary key Unique);");
        con.createStatement().execute(
                "Create table `" + user.getUsername() + "likedme`(usrId int NOT NULL Unique,usrname varchar(45) NOT NULL Primary key Unique);");
        logger.info("Calling insertUser method");
        return user;
    }

    @ApiMethod(name = "updateUser")
    public User updateUser(User user) throws SQLException {
        if (con == null)
            con = beginConnection();
        con.createStatement().executeUpdate(
                "UPDATE `checkedin`.`users` SET fname= '" + user.getFname() + "', lname = '" + user.getLname() + "', bio = '" + user.getBio() + "', currentCity= '" + user.getCurrentCity() + "',dob= '" + user.getDob() + "' WHERE username = '" + user.getUsername() + "';");
        return user;
    }

    @ApiMethod(name = "getUsersByLocation", path = "get_users_by_location")
    public ArrayList<User> getUsersByLocation(@Named("Location") String location) throws ClassNotFoundException, SQLException {
        if (con == null)
            con = beginConnection();
        ResultSet rs = con.createStatement().executeQuery("Select * FROM user_locations INNER JOIN users ON (user_locations.user_ID = users.id and user_locations.user_name=users.username) WHERE location ='" + location + "';");
        ArrayList<User> result = new ArrayList<User>();
        while (rs.next()) {
            User user = new User();
            User image=getProfilePic(rs.getString("username"));
            user.setProfilepic(image.getProfilepic());
            String fname = rs.getString("fname");
            String username = rs.getString("username");
            String currentCity = rs.getString("currentcity");
            long id = rs.getLong("id");
            String email = rs.getString("email");
            String lname = rs.getString("lname");
            String dob = rs.getString("dob");
            String gender = rs.getString("sex");
            String bio = rs.getString("bio");
            user.setUsername(username);
            user.setFname(fname);
            user.setLname(lname);
            user.setDob(dob);
            user.setEmail(email);
            user.setBio(bio);
            user.setId(id);
            user.setCurrentCity(currentCity);
            user.setSex(gender);
            result.add(user);
        }
        return result;
    }

    @ApiMethod(name = "insertUserLocation", path = "insert_user_location")
    public void insertUserLocation(User user, @Named("Location") String location) throws SQLException {
        if (con == null)
            con = beginConnection();
        user = getUserByUsername(user.getUsername());
        ArrayList<User> result = getAllUserLocation();
        ArrayList<String> usernames = new ArrayList<String>();

        for (int i = 0; i < result.size(); i++) {
            usernames.add(result.get(i).getUsername());
        }
        if (usernames.contains(user.getUsername())) {
            con.createStatement().executeUpdate(
                    "Update `checkedin`.`user_locations` set `location`='" + location + "' where user_name='" + user.getUsername() + "';");
        } else {
            con.createStatement().executeUpdate(
                    "INSERT INTO `checkedin`.`user_locations`(`user_name`,`user_ID`,`location`)" +
                            "VALUES ('" + user.getUsername() + "', '" + user.getId() + "','" + location + "');");
        }
    }

    @ApiMethod(name = "getAllUserLocation", path = "get_all_user_location")
    public ArrayList<User> getAllUserLocation() throws SQLException {
        if (con == null)
            con = beginConnection();
        ResultSet rs = con.createStatement().executeQuery("Select * FROM user_locations INNER JOIN users ON user_locations.user_ID = users.id and user_locations.user_name=users.username ;");
        ArrayList<User> result = new ArrayList<User>();
        while (rs.next()) {
            User user = new User();
            String fname = rs.getString("fname");
            String username = rs.getString("username");
            String lname = rs.getString("lname");
            String dob = rs.getString("dob");
            user.setUsername(username);
            user.setFname(fname);
            user.setLname(lname);
            user.setDob(dob);
            result.add(user);
        }
        return result;
    }

    @ApiMethod(name = "deleteUserFromLocations", path = "delete_user_locations", httpMethod = ApiMethod.HttpMethod.POST)
    public void deleteUserFromLocations(User userrm) throws SQLException {
        if (con == null)
            con = beginConnection();
        ArrayList<User> result = getAllUserLocation();
        ArrayList<String> usernames = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            usernames.add(result.get(i).getUsername());
        }
        if (usernames.contains(userrm.getUsername())) {
            con.createStatement().executeUpdate(
                    "Delete from `checkedin`.`user_locations` where user_name='" + userrm.getUsername() + "';");
        }
    }

    @ApiMethod(name = "deleteUser", path = "delete_user", httpMethod = ApiMethod.HttpMethod.POST)
    public void deleteUser(@Named("username") String username) throws SQLException {
        if (con == null)
            con = beginConnection();
        if (username != null) {
            con.createStatement().executeUpdate("drop table " + username + "iliked;");
            con.createStatement().executeUpdate("drop table " + username + "likedme;");
            con.createStatement().executeUpdate("Delete from `checkedin`.`users` where username='" + username + "';");
        }
    }

    @ApiMethod(name = "insertUserLiked", path = "insert_user_liked")
    public void insertUserLiked(@Named("username") String username, @Named("likedUser") String likedUser) throws SQLException {
        if (con == null)
            con = beginConnection();
        User user = getUserByUsername(username);
        User liked = getUserByUsername(likedUser);
        if (user != null && liked != null) {
            con.createStatement().executeUpdate("insert into `checkedin`.`" + username + "iliked`(userId,username) values ('" + liked.getId() + "','" + liked.getUsername() + "');");
            con.createStatement().executeUpdate("insert into `checkedin`.`" + likedUser + "likedme`(usrId,usrname) values ('" + user.getId() + "','" + user.getUsername() + "');");
        }
    }

    @ApiMethod(name = "getUsersILiked", path = "get_user_i_liked")
    public ArrayList<User> getUsersILiked(@Named("username") String username) throws SQLException {
        if (con == null)
            con = beginConnection();
        User user = getUserByUsername(username);
        ArrayList<User> users = new ArrayList<>();
        ResultSet rs;
        if (user != null) {
            rs = con.createStatement().executeQuery("Select username from `checkedin`.`" + username + "iliked`;");
            while (rs.next()) {
                User image=getProfilePic(rs.getString("username"));
                User usr=getUserByUsername(rs.getString("username"));
                usr.setProfilepic(image.getProfilepic());
                users.add(usr);
            }
        }
        return users;
    }

    @ApiMethod(name = "getUsersLikedMe", path = "get_user_liked_me")
    public ArrayList<User> getUsersLikedMe(@Named("username") String username) throws SQLException {
        if (con == null)
            con = beginConnection();
        User user = getUserByUsername(username);
        ArrayList<User> users = new ArrayList<>();
        ResultSet rs;
        if (user != null) {
            rs = con.createStatement().executeQuery("Select usrname from `checkedin`.`" + username + "likedme`;");
            while (rs.next()) {
                User image=getProfilePic(rs.getString("usrname"));
                User usr=getUserByUsername(rs.getString("usrname"));
                usr.setProfilepic(image.getProfilepic());
                users.add(usr);
            }
        }
        return users;
    }

    @ApiMethod(name = "getAllUserImages", path = "get_all_user_images")
    public ArrayList<User> getAllUserImages() throws SQLException {
        if (con == null)
            con = beginConnection();
        ResultSet rs;
        while (true) {
            rs = con.createStatement().executeQuery("Select username_img from `checkedin`.`images`;");
            if (rs != null) break;
        }
        ArrayList<User> result = new ArrayList<>();
        while (rs.next()) {
            String username = rs.getString("username_img");
            result.add(getUserByUsername(username));
        }
        return result;
    }

    @ApiMethod(name = "insertImage", path = "insert_image")
    public void insertImage(@Named("username") String username, @Named("profilepic") String profilepic, @Named("img2") String img2, @Named("img3") String img3) throws SQLException {
        if (con == null)
            con = beginConnection();
        boolean iscontained=false;
        User user = getUserByUsername(username);
        ArrayList<User> users = getAllUserImages();
        System.out.println(users);
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                if(users.get(i).getUsername().equals(username)){
                    iscontained=true;
                };
            }
        }
        if (user != null) {
            if (iscontained) {
                con.createStatement().executeUpdate("Update `checkedin`.`images` Set profilepic = '" + profilepic + "', image2 = '" + img2 + "', image3 = '" + img3 + "' Where username_img = '" + user.getUsername() + "';");
            } else {
                con.createStatement().executeUpdate("insert into `checkedin`.`images` ( username_img, profilepic, image2, image3) values ('" + user.getUsername() + "','" + profilepic + "', '" + img2 + "', '" + img3 + "');");
            }
        }
    }

    @ApiMethod(name = "getImage", path = "get_image")
    public User getImage(@Named("username") String username) throws SQLException {
        if(con==null)
            con = beginConnection();
        User user = getUserByUsername(username);
        User result = new User();
        ResultSet rs;
        if (user != null) {
            rs = con.createStatement().executeQuery("Select profilepic, image2, image3 from `checkedin`.`images` where `username_img` = '" + username + "';");
            while (rs.next()) {
                result.setProfilepic(rs.getString("profilepic"));
                result.setImg2(rs.getString("image2"));
                result.setImg3(rs.getString("image3"));
            }
        }
        return result;
    }

    @ApiMethod(name = "getMatches", path = "get_matches")
    public ArrayList<User> getMatches(@Named("username") String username) throws SQLException {
        if(con==null)
            con = beginConnection();
        User user = getUserByUsername(username);
        ArrayList<User> users = new ArrayList<>();
        ResultSet rs;
        if (user != null) {
            rs = con.createStatement().executeQuery("Select username from `checkedin`.`" + username + "likedme` join " + username + "iliked on " + username + "likedme.usrId=" + username + "iliked.userId where usrname=username;");
            while (rs.next()) {
                User image=getProfilePic(rs.getString("username"));
                User usr=getUserByUsername(rs.getString("username"));
                usr.setProfilepic(image.getProfilepic());

                users.add(usr);
            }
        }
        return users;
    }

    @ApiMethod(name = "getProfilePic", path = "get_profile_pic")
    public User getProfilePic(@Named("username") String username) throws SQLException {
        Connection conn = beginConnection();
        User user = getUserByUsername(username);
        User result = new User();
        ResultSet rs;
        if (user != null) {
            rs = conn.createStatement().executeQuery("Select profilepic from `checkedin`.`images` where `username_img` = '" + username + "';");
            while (rs.next()) {
                result.setProfilepic(rs.getString("profilepic"));
            }
        }
        return result;
    }
}