package pl.thinkandcode.samples.mongo;

import static com.mongodb.client.model.Sorts.ascending;

import java.util.List;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Sorts;

public class JobRepository {
    private final MongoCollection<Document> collection;

    public JobRepository(
            String collectionName,
            String dbName,
            MongoClient client) {
        MongoDatabase database = client.getDatabase(dbName);
        this.collection = database.getCollection(collectionName);
    }

    public void insert(List<Job> jobs) {
        var documents = jobs.stream()
                .map(Job::toDocument)
                .toList();
        collection.insertMany(documents);
    }

    public Job takeFirst() {
        var query = new Document("status", "NEW");
        var update = new Document("$set", new Document("status", "PROCESSING"));
        var options = new FindOneAndUpdateOptions()
                .sort(ascending("creationTime"));

        Document document = collection.findOneAndUpdate(query, update, options);
        if (document == null) {
            return null;
        }
        return Job.createFrom(document);
    }
}
