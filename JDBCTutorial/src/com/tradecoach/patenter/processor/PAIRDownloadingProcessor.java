package com.tradecoach.patenter.processor;

import com.datanovo.patenter.entity.patent.Patent;
import com.datanovo.patenter.entity.tasks.PAIRPatent;
import com.datanovo.patenter.io.GooglePAIRDownloader;
import com.datanovo.patenter.io.S3Worker;
import com.github.rholder.retry.RetryException;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alexander Loginov on 3/16/15.
 */
public class PAIRDownloadingProcessor extends AbstractQueueProcessor<Patent, PAIRPatent> {

    private static final Logger logger = LoggerFactory.getLogger(PAIRDownloadingProcessor.class);
    private final Queue<Patent> patentQueue;
    private final S3Worker s3Worker;

    public PAIRDownloadingProcessor(Queue<Patent> inputQueue, Queue<PAIRPatent> outputQueue, Queue<Patent> patentQueue, Config config) {
        super(
                inputQueue,
                outputQueue,
                newFixedThreadPoolWithQueueSize(config.getInt("n_threads"))
        );
        this.patentQueue = patentQueue;
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
    public PAIRPatent execute(Patent patent) throws Exception {
        try {
            logger.debug("Retrieving {} from Google USPTO DATA", patent.getApplicationNumber());
            Stopwatch stopwatch = Stopwatch.createStarted();
            File zipFile = GooglePAIRDownloader.downloadPAIR(patent.getApplicationNumber().replaceAll("/", ""));

            float time = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
            logger.debug("Retrieved file {} from Google USPTO DATA. Time: {} ms, Size: {} KB, AVG Speed: {} KB/sec",
                    patent.getApplicationNumber(),
                    time,
                    ((float) zipFile.length()) / 1024,
                    ((float) zipFile.length()) / time * 1000 / 1024);
            return new PAIRPatent(zipFile, patent);
        } catch (IOException e) {
            uploadDocumentToS3(patent);
            patentQueue.add(patent);
            throw e;
        }
    }

    @Override
    public void onSuccessCallback(PAIRPatent result) {

    }

    @Override
    public void onFailureCallback(Patent patent, Throwable t) {
        logger.error("Failed to download PAIR {}", t.getMessage());
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
}
