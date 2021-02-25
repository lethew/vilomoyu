package studio.greeks.vilomoyu;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.JoranException;

import java.text.ParseException;
import java.util.Map;

/**
 * TODO:
 */
public class Test {
    public static void main(String[] args) throws ParseException, JoranException {
        LoggerContext context = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure("D:\\Projects\\CD_FY_PRD_ZNBQ\\com-thunisoft-znbq\\src\\main\\resources\\logback-spring.xml");
        Map<String, String> copyOfPropertyMap = configurator.getContext().getCopyOfPropertyMap();
        System.out.println(copyOfPropertyMap);
        System.out.println(context.getCopyOfListenerList());
//        String src = "2021-01-27 01:18:43:515 [ERROR] ";

//        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS , GMT+8").parse(src));
    }
}
