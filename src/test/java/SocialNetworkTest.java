import org.dbunit.Assertion;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@RunWith(JUnit4.class)
public class SocialNetworkTest extends DBTestCase {

    private static final String usersTableName = "USERS";
    private static final String postsTableName = "POSTS";
    private static final String likesTableName = "LIKES";
    private static final String friendshipsTableName = "FRIENDSHIPS";
    private static final String requestedData = "RequestedData";

    private static final String usersDataSet = "src/test/resources/datasets/USERS.xml";
    private static final String postsDataSet = "src/test/resources/datasets/POSTS.xml";
    private static final String likesDataSet = "src/test/resources/datasets/LIKES.xml";
    private static final String friendshipsDataSet = "src/test/resources/datasets/FRIENDSHIPS.xml";
    private static final String requestedDataSet = "src/test/resources/datasets/RequestedData.xml";

    private static final String requestedDataSQL = "SELECT USERS.ID, USERS.NAME, USERS.SURNAME, USERS.BIRTHDATE\n" +
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

    private Properties prop;


    public SocialNetworkTest() {
        super();
        prop = new Properties();
        try {
            prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("jdbc.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, prop.getProperty("jdbc.driverClassName"));
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, prop.getProperty("jdbc.url"));
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, prop.getProperty("jdbc.username"));
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, prop.getProperty("jdbc.password"));
    }

    @Override
    protected IDataSet getDataSet() {
        return null;
    }

    protected DatabaseOperation getSetUpOperation() {
        return DatabaseOperation.NONE;
    }

    protected DatabaseOperation getTearDownOperation() {
        return DatabaseOperation.NONE;
    }

    @Test
    public void checkUsers() throws Exception {
        checkTable(usersTableName, usersDataSet);
    }

    @Test
    public void checkPosts() throws Exception {
        checkTable(postsTableName, postsDataSet);
    }

    @Test
    public void checkLikes() throws Exception {
        checkTable(likesTableName, likesDataSet);
    }

    @Test
    public void checkFriendShips() throws Exception {
        checkTable(friendshipsTableName, friendshipsDataSet);
    }

    @Test
    public void checkRequestedData() throws Exception {
        ITable factTable = getConnection().createQueryTable(requestedData, requestedDataSQL);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream(requestedDataSet));
        ITable testTable = expectedDataSet.getTable(requestedData);

        Assertion.assertEquals(testTable, factTable);
    }

    public void checkTable (String tableName, String testDataset) throws Exception{
        IDataSet databaseDataSet = getConnection().createDataSet();
        ITable factTable = databaseDataSet.getTable(tableName);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream(testDataset));
        ITable testTable = expectedDataSet.getTable(tableName);

        Assertion.assertEquals(testTable, factTable);
    }
}
