# DMS (Document Management System) API

Welcome to the wonderful world of DMS, your one-stop shop in the world of document processing!

A very simple client to test this api can be found [here](https://github.com/mudderman/dms-api-client)

## Setup

To set up the DMS on your system, or for development, you will need the following:

- GIT
- Java 11
- Maven
- Docker (optional, but recommended)

To run the API on your system using Docker:

1. Clone the repo
2. Build using Maven: `mvn clean package`, optionally, you can skip the tests by adding `-DskipTests=true` to the
   command
3. Build Docker image: `docker build -t dms-api .`
4. Run Docker image: `docker run -p 8080:8080 -t dms-api`

Don't have docker? No problem! You can still run the DMS API using Java in your terminal:

1. Build with maven: `mvn clean package`, optionally, you can skip the tests by adding `-DskipTests=true` to the
   command
2. `cd target`
3. `java -jar dms-api.jar`

## Development

To set up for development please do the following:

1. Clone the repo
2. Build with maven: `mvn clean install -e -B -U`, optionally, you can skip the tests by adding `-DskipTests=true` to the
   command
3. Import the project into you favorite IDE or editor, we recommend IntelliJ.
4. Start hacking!

## Implementation notes

As I run this on my Mac M1, I had to add the `--platform=linux/amd64` in the Dockerfile, otherwise, Docker wouldn't 
build the image.

The concurrency limit of the document processing can be configured in the application.yml file.

Just to log some errors, I do a "coin flip" in the queued task to determine whether it will succeed or fail.

## Edument QA

***Q: What would it entail to support cancelling operations?***

As the current implementation of the DocumentProcessor doesn't keep a reference to the queued tasks, it would need some 
refactoring to either return the `Future<Documentation>` or to keep a reference of it, that way it could easily be 
cancelled. The ApiController could also implement the DELETE option of an operation to make it available for the clients 
of the API.

***Q: How would you assign different priorities to API clients, i.e. selecting operations to execute by also factoring in 
whether an API client is a premium / non-premium customer?***

By changing from LinkedBlockingQueue to PriorityBlockingQueue, tasks that are submitted to the  ExecutorService could 
easily be prioritized. With this approach, and some more refactoring, it would be fairly easy to determine which client 
queued the operation and make sure it is higher/lower prioritized based on their "subscription".

***Q: How would you implement rate limiting (= a maximum number of API invocations per  unit of time) for individual API 
clients?***

By using a framework called Bucket4J, this can easily be implemented.

***Q: Can you think of an alternative scheme in which API clients can be notified of an operation's completion?***

By implementing the api to also use websockets, the API could supply an interface so that clients of the clients of the 
API don't have to poll for the status, but instead automatically receive status updates as soon as they change.
