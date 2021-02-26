package studio.greeks.vilomoyu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import studio.greeks.vilomoyu.repository.*;
import studio.greeks.vilomoyu.util.LogFileParser;

/**
 * TODO:
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>吴昭</a>
 */
@Service
public class Init implements ApplicationRunner {

    @Autowired private LogItemRepository logItemRepository;
    @Autowired private StackTraceRepository stackTraceRepository;
    @Autowired private ThrowableClassRepository throwableClassRepository;
    @Autowired private ThrowableMessageRepository throwableMessageRepository;
    @Autowired private ThrowableStackTraceMappingRepository throwableStackTraceMappingRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LogFileParser parser = new LogFileParser("D:\\Users\\Zhao\\Documents\\唐正强\\RIZHI\\新建文件夹\\znbq_stderr_127.0.0.1_8080.2021-01-26.0.log");
        parser.reorganize("znbq", "广西", "测试");
        logItemRepository.saveAll(parser.getLogItemList());
        stackTraceRepository.saveAll(parser.getStackTraces());
        throwableClassRepository.saveAll(parser.getThrowableClassList());
        throwableMessageRepository.saveAll(parser.getThrowableMessageList());
        throwableStackTraceMappingRepository.saveAll(parser.getThrowableStackTraceMappingList());
    }
}
