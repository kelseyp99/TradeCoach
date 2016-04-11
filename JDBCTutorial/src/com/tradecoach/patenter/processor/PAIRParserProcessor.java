package com.tradecoach.patenter.processor;

import com.datanovo.patenter.entity.patent.Patent;
import com.datanovo.patenter.entity.tasks.PAIRPatent;
import com.datanovo.patenter.io.S3Worker;
import com.datanovo.patenter.parsers.pair.PAIRParser;
import com.github.rholder.retry.RetryException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.typesafe.config.Config;
import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

/**
 * Created by Alexander Loginov on 3/16/15.
 */
public class PAIRParserProcessor extends AbstractQueueProcessor<PAIRPatent, Patent> {

    private static final Logger logger = LoggerFactory.getLogger(PAIRParserProcessor.class);
    private final S3Worker s3Worker;


    public PAIRParserProcessor(Queue<PAIRPatent> inputQueue, Queue<Patent> outputQueue, Config config) {
        super(
                inputQueue,
                outputQueue,
                newFixedThreadPoolWithQueueSize(config.getInt("n_threads"))
        );

        s3Worker = new S3Worker(
                config.getConfig("S3_worker").getString("aws_access_key"),
                config.getConfig("S3_worker").getString("aws_secret_key"),
                config.getConfig("S3_worker").getString("aws_bucket_name"),
                config.getConfig("S3_worker").getInt("retry_count"),
                config.getConfig("S3_worker").getInt("retry_timeout"));
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
    public Patent execute(PAIRPatent pairPatent) throws Exception {
        Patent result = pairPatent.getPatent();

        if (pairPatent.getPairFile() == null || !pairPatent.getPairFile().exists()) {
            return result;
        }

        try {
            PAIRParser parser = new PAIRParser(pairPatent.getPatent(), new ZipFile(pairPatent.getPairFile()));
            result = parser.parse();
            logger.debug("PAIR for {} application number successfully parsed", pairPatent.getPatent().getApplicationNumber());
            uploadDocumentToS3(result);
        } finally {
            if (pairPatent.getPairFile() != null) {
                FileUtils.forceDelete(pairPatent.getPairFile());
            }
        }
        return result;
    }

    private String uploadDocumentToS3(Patent patent) throws IOException, RetryException, ExecutionException {
        //Get JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(patent);
        //Save to file
        Path path = Files.createTempFile(patent.getPatentNumber(), ".tmp");
        Files.write(path, json.getBytes());
        //generate key
        String desc_key = patent.getDocumentS3Key() + "document.json";
        //upload
        s3Worker.uploadAndDelete(desc_key, path.toFile());

        return desc_key;
    }


    @Override
    public void onSuccessCallback(Patent result) {

    }

    @Override
    public void onFailureCallback(PAIRPatent pairPatent, Throwable t) {
        logger.debug("Failed to parse PAIR file: {}", t.getMessage());
    }
}
