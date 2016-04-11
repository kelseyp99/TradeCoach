package com.tradecoach.patenter.processor;

import com.datanovo.patenter.entity.patent.Patent;
import com.datanovo.patenter.exception.PatentParsingException;
import com.datanovo.patenter.io.S3Worker;
import com.datanovo.patenter.parsers.ParserFactory;
//import com.datanovo.patenter.exception.PatentParsingException;
//import com.datanovo.patenter.io.S3Worker;
//import com.datanovo.patenter.parsers.ParserFactory;
import com.datanovo.patenter.parsers.PatentParser;
import com.github.rholder.retry.RetryException;
import com.google.common.base.Stopwatch;
import com.tradecoach.patenter.entity.security.SecurityInst;
import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexander Loginov on 3/16/15.
 */
public class PatentParsingProcessor extends AbstractQueueProcessor<String, SecurityInst> {

    private static final Logger logger = LoggerFactory.getLogger(PatentParsingProcessor.class);
 //   private final S3Worker s3Worker;
    private final File root = new File("/");


    public PatentParsingProcessor(Queue<String> inputQueue, Queue<SecurityInst> outputQueue, Config config) throws ParserConfigurationException {
        super(
                inputQueue,
                outputQueue,
                newFixedThreadPoolWithQueueSize(config.getInt("n_threads"))
        );    
   /*     s3Worker = new S3Worker(
                config.getConfig("S3_worker").getString("aws_access_key"),
                config.getConfig("S3_worker").getString("aws_secret_key"),
                config.getConfig("S3_worker").getString("aws_bucket_name"),
                config.getConfig("S3_worker").getInt("retry_count"),
                config.getConfig("S3_worker").getInt("retry_timeout"));*/
    }

    @Override
    public boolean start() {
        boolean result = super.start();
        logger.info("Service started");
        return result;
    }

    @Override
    public void shutdown() {
        logger.info("Stopping {} gracefully", this.getClass().getName());
        super.shutdown();
        logger.info("Service stopped");
    }

    @Override
    public Patent execute(String xmlFileName) throws Exception {
    	//Check amount of free disk space
    	while (((float) root.getFreeSpace() / root.getTotalSpace()) < 0.05) {
    		logger.warn("Free disk space < 5%. Sleeping");
    		Thread.sleep(1000);
    	}
    	File xmlFile = this.downloadDocument(xmlFileName);
    	Patent patent = this.parseFile(xmlFile);
    	if(xmlFileName.contains("/"))
    		patent.setDocumentS3Key(xmlFileName.substring(0, xmlFileName.lastIndexOf("/") + 1) );
    	else
    		patent.setDocumentS3Key(xmlFileName.substring(0, xmlFileName.lastIndexOf("\\") + 1) );
    	    
    	return patent;
    }

	private File downloadDocument(String xmlFileName) throws ExecutionException, RetryException, IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        logger.debug("Retrieving {} from S3", xmlFileName);
        File xmlFile;
        if(false)
        	xmlFile = new File(xmlFileName);
        else
            xmlFile = s3Worker.download(xmlFileName);
        
        logger.debug("Retrieved file {} from S3. Time: {} ms", xmlFileName, stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));

        return xmlFile;
    }

    private Patent parseFile(File xmlFile) throws IOException, PatentParsingException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        logger.debug("Parsing {}", xmlFile.getAbsolutePath());

        PatentParser parser = ParserFactory.getParser(xmlFile);
        Patent result = null;

        try {
            result = parser.parse();
        } finally {
   //         Files.delete(xmlFile.toPath());//temporarily removed 
        }

        logger.debug("XML successfully parsed in {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        return result;
    }

    @Override
    public void onSuccessCallback(Patent result) {

    }

    @Override
    public void onFailureCallback(String input, Throwable t) {
        logger.error("Error during getting a patent {}", input, t);
    }

}
