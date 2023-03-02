package pl.thinkandcode.samples.mongo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.bson.Document;

public class Job {
    private String id;
    private Instant creationTime;
    private String status;

    public Job() {
    }

    public Job(String id, Instant creationTime, String status) {
        this.id = id;
        this.creationTime = creationTime;
        this.status = status;
    }

    public static Job createFrom(Document document) {
        var date = document.getDate("creationTime");
        return new Job(
                document.getString("_id"),
                Instant.ofEpochMilli(date.getTime()),
                document.getString("status")
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Document toDocument() {
        var date = new Date(creationTime.truncatedTo(ChronoUnit.MILLIS).toEpochMilli());
        var document = new Document();
        document.put("_id", id);
        document.put("creationTime", date);
        document.put("status", status);
        return document;
    }
}
