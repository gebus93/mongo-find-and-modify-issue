package pl.thinkandcode.samples.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class contains tests comparing changed behaviour between mongo 4, 5 and 6.
 * Each test starts specific version of mongodb inside docker container using testcontainers.
 *
 * @see <a href="https://www.testcontainers.org/modules/databases/mongodb/">testcontainers - MongoDB</a>
 */
class JobRepositoryTest {
    @Test
    @DisplayName("test mongo 4.4.19")
    void testMongo4() throws Exception {
        try (var mongoDBContainer = createAndStartMongoContainer("4.4.19");
             var mongoClient = MongoClients.create(mongoDBContainer.getConnectionString())) {
            testFindOneAndUpdate(mongoDBContainer, mongoClient);
        }
    }

    @Test
    @DisplayName("test mongo 5.0.15")
    void testMongo5() throws Exception {
        try (var mongoDBContainer = createAndStartMongoContainer("5.0.15");
             var mongoClient = MongoClients.create(mongoDBContainer.getConnectionString())) {
            testFindOneAndUpdate(mongoDBContainer, mongoClient);
        }
    }

    @Test
    @DisplayName("test mongo 6.0.4")
    void testMongo6() throws Exception {
        try (var mongoDBContainer = createAndStartMongoContainer("6.0.4");
             var mongoClient = MongoClients.create(mongoDBContainer.getConnectionString())) {
            testFindOneAndUpdate(mongoDBContainer, mongoClient);
        }
    }

    private static MongoDBContainer createAndStartMongoContainer(String mongoVersion) {
        var container = new MongoDBContainer(DockerImageName.parse("mongo:" + mongoVersion));
        container.start();
        return container;
    }

    private void testFindOneAndUpdate(MongoDBContainer mongoDBContainer, MongoClient mongoClient) throws Exception {
        // setup logs collector
        var logsBuilder = new StringBuilder();
        mongoDBContainer.followOutput(log -> logsBuilder.append(log.getUtf8String()));

        // initialize repository and insert data set
        var repository = new JobRepository("jobs", "test", mongoClient);
        insertDataSet(repository, 100);

        // prepare asynchronous tasks
        int concurrentThreads = 50;
        var executorService = Executors.newFixedThreadPool(concurrentThreads);

        var asyncTasks = IntStream.range(0, concurrentThreads)
                .mapToObj(i -> (Callable<Job>) repository::takeFirst)
                .toList();

        // invoke asynchronous tasks prepared previously and count distinct job ids
        var uniqueIds = executorService.invokeAll(asyncTasks)
                .stream()
                .map(f -> {
                    try {
                        return f.get(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        throw new IllegalStateException("Could not get result", e);
                    }
                })
                .filter(Objects::nonNull)
                .map(Job::getId)
                .distinct()
                .count();

        // verify unique ids count
        assertThat(uniqueIds).isEqualTo(concurrentThreads);

        // lookup for plan executor errors
        var logs = logsBuilder.toString();
        assertThat(logs).doesNotContain("Plan executor error during findAndModify");
    }

    private void insertDataSet(JobRepository repository, int jobsCount) {
        var jobs = IntStream.range(0, jobsCount)
                .mapToObj(id -> new Job(String.valueOf(id), Instant.ofEpochMilli(id), "NEW"))
                .toList();

        repository.insert(jobs);
    }
}