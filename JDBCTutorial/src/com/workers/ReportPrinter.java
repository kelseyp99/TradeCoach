package com.workers;

import com.datanovo.patenter.entity.patent.Patent;
import com.datanovo.patenter.entity.tasks.PAIRPatent;
import com.datanovo.patenter.processor.PAIRDownloadingProcessor;
import com.datanovo.patenter.processor.PAIRParserProcessor;
import com.datanovo.patenter.processor.PatentParsingProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexander Loginov on 4/28/15.
 */
public class ReportPrinter {

    static final Logger logger = LoggerFactory.getLogger(ReportPrinter.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final PAIRDownloadingProcessor pairDownloadingProcessor;
    private final PatentParsingProcessor patentParsingProcessor;
    private final PAIRParserProcessor pairParserProcessor;

    private final Queue<Patent> pairDownloadingQueue;
    private final Queue<PAIRPatent> pairParsingQueue;
    private final Queue<String> prefixesQueue;
    private final Queue<Patent> dbQueue;

    public ReportPrinter(PAIRDownloadingProcessor pairDownloadingProcessor,
                         PatentParsingProcessor patentParsingProcessor,
                         PAIRParserProcessor pairParserProcessor,
                         Queue<Patent> pairDownloadingQueue,
                         Queue<PAIRPatent> pairParsingQueue,
                         Queue<String> prefixesQueue,
                         Queue<Patent> dbQueue) {
        this.pairDownloadingProcessor = pairDownloadingProcessor;
        this.patentParsingProcessor = patentParsingProcessor;
        this.pairParserProcessor = pairParserProcessor;
        this.pairDownloadingQueue = pairDownloadingQueue;
        this.pairParsingQueue = pairParsingQueue;
        this.prefixesQueue = prefixesQueue;
        this.dbQueue = dbQueue;
    }

    public void start() {
        startNotification();
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    void startNotification() {
        final int INTERVAL = 30;

        final Runnable reportQueues = new Runnable() {
            int previousXmlNum = 0;
            int previousPairNum = 0;

            @Override
            public void run() {
                int xmlNum = patentParsingProcessor.getFailCounter() + patentParsingProcessor.getSuccessCounter();
                int pairNum = pairDownloadingProcessor.getFailCounter() + pairParserProcessor.getFailCounter() + pairParserProcessor.getSuccessCounter();
                float xmlRate = ((float) xmlNum - previousXmlNum) / INTERVAL;
                float pairRate = ((float) pairNum - previousPairNum) / INTERVAL;

                logger.info("Queue report: Processed {} Failed {} XML and PAIR {} Failed {}. AVG speed: XML={} tasks/sec, PAIR={} tasks/sec", xmlNum,
                        patentParsingProcessor.getFailCounter(), pairNum, pairParserProcessor.getFailCounter() + pairDownloadingProcessor.getFailCounter(), xmlRate, pairRate);
                logger.info("Queue report: prefixesQueue {}", prefixesQueue.size());
                logger.info("Queue report: pairDownloadingQueue {}", pairDownloadingQueue.size());
                logger.info("Queue report: pairParsingQueue {}", pairParsingQueue.size());
                logger.info("Queue report: dbQueue {}", dbQueue.size());

                previousXmlNum = xmlNum;
                previousPairNum = pairNum;
            }
        };

        scheduler.scheduleAtFixedRate(reportQueues, INTERVAL, INTERVAL, TimeUnit.SECONDS);

    }

}
