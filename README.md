# MongoDB: findOneAndUpdate / findAndModify issue

After upgrading mongodb from version 4.4.x to 5.0.14 in a company I'm working for, we have found thousands of errors in our logs. All of them refers to the same issue `Plan executor error during findAndModify`.

In our case, we use `findAndModify` in a spring-boot application to take first pending job from a queue. This operation is executed concurrently on many app instances (5-10 threads each).

I decided to create sample project to verify how it behaves on different versions of mongodb.
In `src/test/java` you can find **JobRepositoryTest** which executes the same test case on `mongo:4.4.19`, `mongo:5.0.15` and `mongo:6.0.4`. The last step of this test case verifies whether logs contain phrase `Plan executor error during findAndModify`.

Everything seems to work on mongodb 4, but mongodb 5 and 6 logs many write conflicts as the example below:
```
{"t":{"$date":"2023-03-02T11:47:14.567+00:00"},"s":"W",  "c":"COMMAND",  "id":23802,   "ctx":"conn13","msg":"Plan executor error during findAndModify","attr":{"error":{"code":112,"codeName":"WriteConflict","errmsg":"WriteConflict error: this operation conflicted with another operation. Please retry your operation or multi-document transaction."},"stats":{"stage":"UPDATE","nReturned":0,"executionTimeMillisEstimate":0,"works":104,"advanced":0,"needTime":102,"needYield":1,"saveState":1,"restoreState":1,"failed":true,"isEOF":0,"nMatched":0,"nWouldModify":0,"nWouldUpsert":0,"inputStage":{"stage":"SORT","nReturned":1,"executionTimeMillisEstimate":0,"works":103,"advanced":1,"needTime":102,"needYield":0,"saveState":2,"restoreState":1,"isEOF":0,"sortPattern":{"creationTime":1},"memLimit":104857600,"limitAmount":1,"type":"default","totalDataSizeSorted":0,"usedDisk":false,"inputStage":{"stage":"COLLSCAN","filter":{"status":{"$eq":"NEW"}},"nReturned":53,"executionTimeMillisEstimate":0,"works":102,"advanced":53,"needTime":48,"needYield":0,"saveState":2,"restoreState":1,"isEOF":1,"direction":"forward","docsExamined":100}}},"cmd":{"findAndModify":"jobs","query":{"status":"NEW"},"sort":{"creationTime":1},"update":{"$set":{"status":"PROCESSING"}},"new":false}}}
```

## Running tests

### Linux / MacOS

```bash
$ ./gradlew clean build --info
```

### Windows

```batch
> .\gradlew.bat clean build --info
```

### Test results
Test results can be found in the following location:
```
$ ./build/reports/tests/test/index.html
```