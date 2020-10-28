import config.AppConfig;
import dao.SocialNetworkDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;


public class SocialNetworkApp {

    public static void main(String[] args) {

        AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        SocialNetworkDao dao = context.getBean(SocialNetworkDao.class);

        dao.dropTables();
        dao.createTables();
        dao.populateData();
        dao.readData();
    }


}
