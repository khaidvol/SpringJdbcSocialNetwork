package dao;

import entities.Friendship;
import entities.Like;
import entities.Post;
import entities.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SocialNetworkDao {

    private static final Logger logger = Logger.getRootLogger();

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert insertTemplateUser;
    private SimpleJdbcInsert insertTemplatePost;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public static final String DROP_USERS = "DROP TABLE IF EXISTS USERS;";
    public static final String DROP_FRIENDSHIPS = "DROP TABLE IF EXISTS FRIENDSHIPS;";
    public static final String DROP_POSTS = "DROP TABLE IF EXISTS POSTS;";
    public static final String DROP_LIKES = "DROP TABLE IF EXISTS LIKES;";

    public static final String CREATE_USERS = "CREATE TABLE USERS (\n" +
            "\t   ID INT NOT NULL AUTO_INCREMENT\n" +
            "     , NAME VARCHAR(30) NOT NULL\n" +
            "     , SURNAME VARCHAR(30) NOT NULL\n" +
            "     , BIRTHDATE DATE\n" +
            "     , UNIQUE UQ_USERS_1 (ID)\n" +
            "     , PRIMARY KEY (ID)\n" +
            ");";

    public static final String CREATE_FRIENDSHIPS = "CREATE TABLE FRIENDSHIPS (\n" +
            "       USERID1 INT NOT NULL\n" +
            "     , USERID2 INT NOT NULL\n" +
            "     , TIMESTAMP DATE\n" +
            "     , CONSTRAINT FK_USERID1 FOREIGN KEY (USERID1) REFERENCES USERS (ID)\n" +
            "\t , CONSTRAINT FK_USERID2 FOREIGN KEY (USERID2) REFERENCES USERS (ID)\n" +
            "\t , CONSTRAINT UQ_FRIENDSHIPS UNIQUE (USERID1, USERID2) \n" +
            ");";

    public static final String CREATE_POSTS = "CREATE TABLE POSTS (\n" +
            "       ID INT NOT NULL AUTO_INCREMENT\n" +
            "     , USERID INT NOT NULL\n" +
            "     , TEXT VARCHAR(500) NOT NULL\n" +
            "     , TIMESTAMP DATE\n" +
            "     , PRIMARY KEY (ID)\n" +
            "     , CONSTRAINT FK_POSTS FOREIGN KEY (USERID) REFERENCES USERS (ID)\n" +
            ");";

    public static final String CREATE_LIKES = "CREATE TABLE LIKES (\n" +
            "       POSTID INT NOT NULL\n" +
            "     , USERID INT NOT NULL\n" +
            "     , TIMESTAMP DATE\n" +
            "     , CONSTRAINT FK_POSTID FOREIGN KEY (POSTID) REFERENCES POSTS (ID)\n" +
            "\t , CONSTRAINT FK_USERID FOREIGN KEY (USERID) REFERENCES USERS (ID)\n" +
            "\t , CONSTRAINT UQ_LIKES UNIQUE (POSTID, USERID)\n" +
            ");";


    private static final String READ_DATA =
            "SELECT USERS.ID, USERS.NAME, USERS.SURNAME, USERS.BIRTHDATE\n" +
                    ", (SELECT COUNT(FRIENDSHIPS.USERID1) \n" +
                    "FROM FRIENDSHIPS \n" +
                    "WHERE FRIENDSHIPS.USERID1 = USERS.ID \n" +
                    "AND FRIENDSHIPS.TIMESTAMP <= '2025-03-31') \n" +
                    "AS NUMBER_OF_FRIENDSHIPS \n" +
                    ", (SELECT COUNT(POSTS.ID) \n" +
                    "FROM POSTS \n" +
                    "WHERE POSTS.USERID = USERS.ID) \n" +
                    "AS NUMBER_OF_POSTS\t\n" +
                    ", SUM(CASE WHEN LIKES.POSTID = POSTS.ID \n" +
                    "AND LIKES.TIMESTAMP <= '2025-03-31' \n" +
                    "THEN 1 ELSE 0 END) \n" +
                    "AS NUMBER_OF_LIKES\n" +
                    "FROM USERS\n" +
                    "LEFT JOIN POSTS ON USERS.ID = POSTS.USERID\n" +
                    "LEFT JOIN LIKES ON LIKES.POSTID = POSTS.ID\n" +
                    "GROUP BY USERS.ID\n" +
                    "HAVING NUMBER_OF_FRIENDSHIPS > 100 AND NUMBER_OF_LIKES > 100";


    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.insertTemplateUser = new SimpleJdbcInsert(dataSource).withTableName("USERS");
        this.insertTemplatePost = new SimpleJdbcInsert(dataSource).withTableName("POSTS");
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

    }

    //create all tables
    public void createTables() {
        logger.info("Creating tables...");
        this.jdbcTemplate.execute(CREATE_USERS);
        this.jdbcTemplate.execute(CREATE_FRIENDSHIPS);
        this.jdbcTemplate.execute(CREATE_POSTS);
        this.jdbcTemplate.execute(CREATE_LIKES);
        logger.info("Success");
    }

    //drop all tables if exist
    public void  dropTables() {
        logger.info("Dropping tables...");
        this.jdbcTemplate.execute(DROP_LIKES);
        this.jdbcTemplate.execute(DROP_POSTS);
        this.jdbcTemplate.execute(DROP_FRIENDSHIPS);
        this.jdbcTemplate.execute(DROP_USERS);
        logger.info("Success");
    }

    // read data according to predefined SQL statement
    public void readData() {
        SqlRowSet rs = jdbcTemplate.queryForRowSet(READ_DATA);
        while(rs.next()) {
            logger.info(
                    "User - Id: " + rs.getInt(1) +
                            ", Name: " + rs.getString(2) +
                            ", Surname: " + rs.getString(3) +
                            ", Birthday: " + rs.getDate(4) +
                            ", Friendships: " + rs.getInt(5) +
                            ", Posts: " + rs.getInt(6) +
                            ", Likes: " + rs.getInt(7)
            );
        }
    }

    //Add Users to the database (one by one)
    private void insertUser(User user) {
        Map<String, Object> userParameters = new HashMap<>();
        userParameters.put("NAME", user.getName());
        userParameters.put("SURNAME", user.getSurname());
        userParameters.put("BIRTHDATE", user.getBirthdate());
        insertTemplateUser.execute(userParameters);
    }

    //Add Posts to the database  (one by one)
    private void insertPost(Post post) {
        Map<String, Object> userParameters = new HashMap<>();
        userParameters.put("USERID", post.getUserId());
        userParameters.put("TEXT", post.getText());
        userParameters.put("TIMESTAMP", post.getTimestamp());
        insertTemplatePost.execute(userParameters);
    }

    //Add Friendships to the database (1000-entries list by one)
    private void insertFriendshipList(List<Friendship> friendships) {
        String sql = "insert into FRIENDSHIPS (USERID1, USERID2, TIMESTAMP) values (:userId1, :userId2, :timestamp)";
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(friendships.toArray());
        namedParameterJdbcTemplate.batchUpdate(sql, batch);

    }

    //Add Likes to the database (1000-entries list by one)
    private void insertLikesList(List<Like> likes) {
        String sql = "insert into LIKES (POSTID, USERID, TIMESTAMP) values (:postId, :userId, :timestamp)";
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(likes.toArray());
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
    }

    //Add all data to the database
    public void populateData() {
        List<User> users = DataGenerator.generateUsersList();
        logger.info("Inserting generated users to the database...");
        users.forEach(this::insertUser);
        logger.info((long) users.size() + " users inserted successfully");

        List<Post> posts = DataGenerator.generatePostsList();
        logger.info("Inserting generated posts to the database");
        posts.forEach(this::insertPost);
        logger.info((long) posts.size() + " posts inserted successfully");


        // load 110_000 friendships in 110 batches
        List<Friendship> friendships;
        int startFriendships = 1;
        int endFriendships = 1000;

        for(int i = 0; i < 110; i++){
            logger.info("Friendships: batch #" + i);
            friendships = DataGenerator.generateFriendshipsList(startFriendships, endFriendships);
            logger.info("Inserting generated friendships to the database");
            insertFriendshipList(friendships);
            logger.info(endFriendships + " friendships inserted successfully");

            startFriendships = endFriendships + 1;
            endFriendships += 1000;

        }

        // load 310_000 likes in 310 batches
        List<Like> likes;
        int startLikes = 1;
        int endLikes = 1000;

        for(int i = 0; i < 310; i++){
            logger.info("Likes: batch #" + i);
            likes = DataGenerator.generateLikesList(startLikes, endLikes);
            logger.info("Inserting generated likes to the database");
            insertLikesList(likes);
            logger.info(endLikes + " likes inserted successfully");
            startLikes = endLikes + 1;
            endLikes += 1000;
        }
    }

}
