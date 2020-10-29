import config.TestConfig;
import org.apache.log4j.Logger;
import org.dbunit.Assertion;
import org.dbunit.DBTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.io.FileInputStream;

@RunWith(JUnit4.class)
public class SocialNetworkTest extends DBTestCase {

    public static final Logger logger = Logger.getRootLogger();

    private static final String usersTableName = "TESTUSERS";
    private static final String postsTableName = "POSTS";
    private static final String likesTableName = "LIKES";
    private static final String friendshipsTableName = "FRIENDSHIPS";
    private static final String requestedData = "RequestedData";

    private static final String usersDataSet = "src/test/resources/datasets/TESTUSERS.xml";
    private static final String postsDataSet = "src/test/resources/datasets/POSTS.xml";
    private static final String likesDataSet = "src/test/resources/datasets/LIKES.xml";
    private static final String friendshipsDataSet = "src/test/resources/datasets/FRIENDSHIPS.xml";
    private static final String requestedDataSet = "src/test/resources/datasets/RequestedData.xml";

    private static final String requestedDataSQL = "SELECT TESTUSERS.ID, TESTUSERS.NAME, TESTUSERS.SURNAME, TESTUSERS.BIRTHDATE\n" +
                        ", (SELECT COUNT(FRIENDSHIPS.USERID1) \n" +
                        "FROM FRIENDSHIPS \n" +
                        "WHERE FRIENDSHIPS.USERID1 = TESTUSERS.ID \n" +
                        "AND FRIENDSHIPS.TIMESTAMP <= '2025-03-31') \n" +
                        "AS NUMBER_OF_FRIENDSHIPS \n" +
                        ", (SELECT COUNT(POSTS.ID) \n" +
                        "FROM POSTS \n" +
                        "WHERE POSTS.USERID = TESTUSERS.ID) \n" +
                        "AS NUMBER_OF_POSTS\t\n" +
                        ", SUM(CASE WHEN LIKES.POSTID = POSTS.ID \n" +
                        "AND LIKES.TIMESTAMP <= '2025-03-31' \n" +
                        "THEN 1 ELSE 0 END) \n" +
                        "AS NUMBER_OF_LIKES\n" +
                        "FROM TESTUSERS\n" +
                        "LEFT JOIN POSTS ON TESTUSERS.ID = POSTS.USERID\n" +
                        "LEFT JOIN LIKES ON LIKES.POSTID = POSTS.ID\n" +
                        "GROUP BY TESTUSERS.ID\n" +
                        "HAVING NUMBER_OF_FRIENDSHIPS > 100 AND NUMBER_OF_LIKES > 100";

    public SocialNetworkTest() {
        super();
    }

    public static AbstractApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class);
    public static DatabaseConnection databaseConnection = (DatabaseConnection) context.getBean("databaseConnection");

    @Override
    protected IDataSet getDataSet() {
        return null;
    }

    protected DatabaseOperation getSetUpOperation() {
        return DatabaseOperation.REFRESH;
    }

    protected DatabaseOperation getTearDownOperation() {
        return DatabaseOperation.DELETE_ALL;
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
        ITable factTable = databaseConnection.createQueryTable(requestedData, requestedDataSQL);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream(requestedDataSet));
        ITable testTable = expectedDataSet.getTable(requestedData);

        Assertion.assertEquals(testTable, factTable);
    }

    public void checkTable (String tableName, String testDataset) throws Exception{

        IDataSet databaseDataSet = databaseConnection.createDataSet();
        ITable factTable = databaseDataSet.getTable(tableName);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream(testDataset));
        ITable testTable = expectedDataSet.getTable(tableName);

        Assertion.assertEquals(testTable, factTable);
    }
}
