package com.unobank.orchestrator_service.domain_logic;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unobank.orchestrator_service.payload.response.NotificationResponse;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

@Service
@Slf4j
public class TransactionsStorageManager {
    @Autowired
    private ObjectMapper objectMapper;

    private final Dotenv dotenv = Dotenv
            .configure()
            .directory("./")
            .load();

    private final AWSCredentials credentials = new BasicAWSCredentials(
            Objects.requireNonNull(dotenv.get("S3_ACCESS_KEY")),
            Objects.requireNonNull(dotenv.get("S3_SECRET_KEY"))
    );

    private final String BUCKET_NAME =  dotenv.get("BUCKET_NAME");

    private final AmazonS3 s3Client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.EU_CENTRAL_1)
            .build();

    public NotificationResponse getNotifications(String userCardId, String lastTransactionId) {
        String date = LocalDate.now().toString();
        System.out.println("date: " + date);

        String prefix = String.format("%s/%s/", userCardId, date);
        System.out.println("prefix " + prefix);
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(BUCKET_NAME).withPrefix(prefix).withDelimiter("/");
        ListObjectsV2Result listing = s3Client.listObjectsV2(req);
        if (listing.getObjectSummaries().size() == 0)
            return null;

        // Sort objects in the folder in reverse order based on LastModified
        listing.getObjectSummaries().sort(
                (o1, o2) -> o2.getLastModified().compareTo(o1.getLastModified()));

        ArrayList<JSONObject> newTransactions = new ArrayList<>();
        for (S3ObjectSummary summary: listing.getObjectSummaries()) {
            log.info("Object Key {} and LastModified {}", summary.getKey(), summary.getLastModified());
            String currentTransactionId = Utils.getFilenameFromPath(summary.getKey());
            System.out.println("Current transaction id: " + currentTransactionId);
            // TODO: maybe place it at the end of this for-cycle scope
//            if (summary.getKey().equals(lastTransactionId))
//                break;

            if (currentTransactionId.equals(lastTransactionId))
                break;

            S3Object s3Object = s3Client.getObject(BUCKET_NAME, summary.getKey());
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject)jsonParser.parse(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                System.out.println("jsonObject: " + jsonObject);
                newTransactions.add(jsonObject);
            } catch (IOException | ParseException err){
                log.error("Error: {}", err.toString());
            }
        }
        String realLastTransactionId = Utils.getFilenameFromPath(listing.getObjectSummaries().get(0).getKey());
        return new NotificationResponse(realLastTransactionId, newTransactions);
    }
}
