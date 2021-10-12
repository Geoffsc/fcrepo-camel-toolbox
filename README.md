# Fedora Messaging Application Toolbox

A collection of ready-to-use messaging applications for use
with [Fedora](http://fcrepo.org). These applications use
[Apache Camel](https://camel.apache.org).

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.fcrepo.camel/toolbox-features/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.fcrepo.camel/toolbox-features/)

Additional background information is available on the Fedora Wiki on the
[Integration Services page](https://wiki.duraspace.org/display/FEDORA4x/Integration+Services).

## Prerequisites
* Java 11
* Solr
* Fedora 6

NOTE:  This is project is currently in a state of flux as we are in the process of upgrading it to support Java 11 and Camel 3.9.x
Currently the Solr, ActiveMQ, Reindexing, and LDPath microservices are available. Triplestore indexing and Fixity are coming soon.


## Running the toolbox

Before starting the camel toolbox fire up a Fedora 6.x instance, Solr 8.x, and Fuseki (triple store).

Fedora
```
docker run -p8080:8080 --rm -p61616:61616  -p8181:8181 --name=my_fcrepo6  fcrepo/fcrepo:6.0.0
```

Solr
```
docker run  --rm -p 8983:8983 --name my_solr solr:8
```

Create the default Solr Core
```
docker exec -it my_solr solr create_core -c collection1
```

Fuseki 
```
docker run --rm -p 3030:3030 --name my_fuseki atomgraph/fuseki --mem /test
```


```
mvn clean install
java -jar fcrepo-camel-toolbox/fcrepo-camel-toolbox-app/target/fcrepo-camel-toolbox-app-<verion>-driver.jar -c /path/to/configuration.properties
``` 

where your `configuration.properties `file is a standard java properties file containing key value pairs. To run with the above solr and fuseki docker containers  set the following properties

```
triplestore.indexer.enabled=true
solr.indexer.enabled=true
audit.enabled=true
fixity.enabled=true
triplestore.baseUrl=http://localhost:3030/test
solr.baseUrl=http://localhost:8983/solr/
```

## Note

Please note: the RDF representation of Fedora Resources is sensitive to the `Host` header
supplied by any client. This can lead to potentially surprising effects from the perspective
of the applications in this Messaging Toolbox.

For example, if the `fcrepo-indexing-triplestore` connects to Fedora at `http://localhost:8080`
but another client modifies Fedora resources at `http://repository.example.edu`, you may
end up with incorrect and/or duplicate data in downstream applications. It is far better to
force clients to connect to Fedora over a non-`localhost` network interface.
Depending on your deployment needs, you may also consider setting a static `Host` header in a proxy.
For instance, with `nginx`, to proxy Fedora over a standard web port, this configuration may suffice:
```
    location /fcrepo {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host repository.example.edu;
    }
```
Any such reverse proxy will work. Then, if port 8080 is inaccessible from outside the
deployment server, and all clients (including these messaging toolbox applications) access Fedora
with the `baseUrl` set to something like: `http://repository.example.edu/fcrepo/rest`,
then the asynchonous integrations will be less prone to configuration errors.

### Global Properties
| Name      | Description| Default Value | Values |
| :---      | :---| :----   | --- |
| fcrepo.baseUrl | The base url endpoint for your Fedora installation.  | http://localhost:8080/fcrepo/rest | Any valid fcrepo url
| fcrepo.authUsername | A valid username      | fcrepoAdmin | | 
| fcrepo.authPassword | A valid password      | fcrepoAdmin | | 
| fcrepo.authHostName |       | localhost | | 
| fcrepo.authPort |       | 8080 | | 

### Repository Audit Service (Triplestore)

This application listens to Fedora's event stream, and stores
audit-related events in an external triplestore. Both
[Jena Fuseki](http://jena.apache.org/documentation/serving_data/)
and [Open RDF Sesame](http://rdf4j.org/) are supported.

More information about the
[audit service](https://wiki.duraspace.org/display/FF/Design+-+Audit+Service)
is available on the Fedora wiki.

#### Properties
| Name      | Description| Default Value | Values |
| :---      | :---| :----   | --- |
| audit.enabled | Enables/disables audit triplestore service  | false |
| audit.input.stream | Audit Service jms message stream | broker:topic:fedora |
| audit.event.baseUri | The baseUri to use for event URIs in the triplestore. A `UUID` will be appended to this value, forming, for instance: `http://example.com/event/{UUID}` | http://example.com/event |
| audit.triplestore.baseUrl| The base url for the external triplestore service | http://localhost:3030/fuseki/test/update |
| audit.filter.containers |  A comma-delimited list of URIs to be filtered (ignored) by the audit service | http://localhost:8080/fcrepo/rest/audit | 


### Repository Indexer (Solr)

This application listens to Fedora's event stream and
indexes objects into an external Solr server.

#### Properties
| Name      | Description| Default Value | Values |
| :---      | :---| :----   | --- |
| solr.indexer.enabled | Enables/disables the SOLR indexing service. Disabled by default | false | 
| error.maxRedeliveries |       | 10 | | 
| fcrepo.checkHasIndexingTransformation |       | true | | 
| fcrepo.defaultTransform |   ?    | null | | 
| input.stream |   The JMS topic or queue serving as the message source    | broker:topic:fedora | | 
| solr.reindex.stream |   The JMS topic or queue serving as the reindex message source    | broker:queue:solr.reindex | | 
| solr.commitWithin |   Milliseconds within which commits should occur    | 10000 | | 
| indexing.predicate |   ?    | false | | 
| ldpath.service.baseUrl |   The LDPath service base url    | http://localhost:9085/ldpath | | 
| filter.containers |   A comma-separate list of containers that should be ignored by the indexer  | http://localhost:8080/fcrepo/rest/audit | | 

### Repository Indexer (Triplestore)

This application listens to Fedora's event stream and
indexes objects into an external triplestore.

#### Properties
| Name      | Description| Default Value | Values |
| :---      | :---| :----   | --- |
| triplestore.indexer.enabled | Enables the triplestore indexing service. Disabled by default | false | 
| triplestore.baseUrl | Base URL for the triplestore | http://localhost:8080/fuseki/test/update | 
| triplestore.input.stream |   The JMS topic or queue serving as the message source    | broker:topic:fedora | | 
| triplestore.reindex.stream |   The JMS topic or queue serving as the reindex message source    | broker:queue:solr.reindex | | 
| triplestore.indexing.predicate |   ?    | false | | 
| triplestore.filter.containers |   A comma-separate list of containers that should be ignored by the indexer  | http://localhost:8080/fcrepo/rest/audit | | 
| triplestore.namedGraph |  ?  | null | | 
| triplestore.prefer.include |  ?  | null | | 
| triplestore.prefer.omit |  ?  | http://www.w3.org/ns/ldp#PreferContainment | | 

### LDPath Service

This application implements an LDPath service on repository
resources. This allows users to dereference and follow URI
links to arbitrary lengths. Retrieved triples are cached locally
for a specified period of time.

More information about LDPath can be found at the [Marmotta website](http://marmotta.apache.org/ldpath/language.html).

Note: The LDPath service requires an LDCache backend, such as `fcrepo-service-ldcache-file`.

#### Usage
The LDPath service responds to `GET` and `POST` requests using any accessible resources as a context.

For example, a request to
`http://localhost:9086/ldpath/?context=http://localhost/rest/path/to/fedora/object`
will apply the appropriate ldpath program to the specified resource. Note: it is possible to
identify non-Fedora resources in the context parameter.

A `GET` request can include a `ldpath` parameter, pointing to the URL location of an LDPath program:

    `curl http://localhost:9086/ldpath/?context=http://localhost/rest/path/to/fedora/object&ldpath=http://example.org/ldpath`

Otherwise, it will use a simple default ldpath program.

A `POST` request can also be accepted by this endpoint. The body of a `POST` request should contain
the entire `LDPath` program. The `Content-Type` of the request should be either `text/plain` or
`application/ldpath`.

    `curl -XPOST -H"Content-Type: application/ldpath" -d @program.txt http://localhost:9086/ldpath/?context=http://localhost/rest/path/to/fedora/object

#### Properties
| Name      | Description| Default Value | Values |
| :---      | :---| :----   | --- |
| fcrepo.cache.timeout | The timeout in seconds for the ldpath cache | 0 | | 
| rest.prefix | The LDPath rest endpoint prefix |  no | /ldpath| |
| rest.port| The LDPath rest endpoint port |  no | 9085 |
| rest.host| The LDPath rest endpoint host |  no | localhost |
| cache.timeout | LDCache ?  timeout in seconds  |  no | 86400  | |
| ldcache.directory | LDCache directory  |  no | ldcache/  | |
| ldpath.transform.path | The LDPath transform file path | classpath:org/fcrepo/camel/ldpath/default.ldpath | For local file paths use `file:` prefix. e.g `file:/path/to/your/transform.txt` |

### Reindexing Service

This application implements a reindexing service so that
any node hierarchy in fedora (e.g. the entire repository
or some subset thereof) can be reindexed by a set of external
services.

#### Properties
| Name      | Description| Default Value | Values |
| :---      | :---| :----   | --- |
| reindexing.enabled | Enables/disables the reindexing component. Enabled by default | true | 
| reindexing.error.maxRedeliveries | Maximum redelivery attempts | 10 | 
| reindexing.stream | Reindexing jms message stream | broker:queue:reindexing | 
| reindexing.host | Reindexing service host | localhost | 
| reindexing.port | Reindexing service port | 9080 |
| reindexing.rest | Reindexing rest URI prefix | /reindexing | 

### ActiveMQ Service

This implements a connector to an ActiveMQ broker.

#### Properties
| Name      | Description| Default Value | Values |
| :---      | :---| :----   | --- |
| jms.brokerUrl | JMS Broker endpoint | tcp://localhost:61616 |
| jms.username | JMS username | null |
| jms.password | JMS password | null |
| jms.connections | The JMS connection count | 10 |
| jms.consumers | The JMS consumer count | 1 |

### Fixity Checking Service (not currently available)

This application can be used in conjunction with the Repository
Re-Indexer to verify the checksums for all Binary resources in
the repository.

#### Properties
| Name      | Description| Default Value | Values |
| :---      | :---| :----   | --- |
| fixity.enabled | Enables/disabless fixity service  | false |
| fixity.input.stream | Fixity Service jms  message stream | broker:queue:fixity |
| fixity.delay | A delay in milliseconds between each fixity check to reduce load on server | 0 |
| fixity.success|  It is also possible to trigger an action on success. By default, this is a no-op. The value should be a camel route action.  To log it to a file use something like this:  file:/tmp/?fileName=fixity-succes.log&fileExist=Append | null |
| fixity.failure |  Most importantly, it is possible to configure what should happen when a fixity check fails. In the default example below, the fixity output is written to a file in `/tmp/fixityErrors.log`. But this can be changed to send a message to an email address (`fixity.failure=smtp:admin@example.org?subject=Fixity`) or use just about any other camel component.| file:/tmp/?fileName=fixity-errors.log&fileExist=Append |

### Repository Serializer

This application automatically serializes Fedora resources to a filesystem
location. Using the re-indexer, one can optionally serialize particular
segments of the repository to a location on the filesystem.

### Repository Re-Indexer

This application allows a user to initiate a re-indexing process
from any location within the Fedora node hierarchy, sending
re-indexing requests to a specified list of external applications
(e.g. fcrepo-indexing-solr, fcrepo-indexing-triplestore, and/or
fcrepo-fixity).

One can specify which applications/endpoints to send these 
reindexing events, by POSTing a JSON array to the re-indexing
service:

    curl -XPOST localhost:9080/reindexing/fedora/path -H"Content-Type: application/json" \
        -d '["broker:queue:solr.reindex","broker:queue:fixity","broker:queue:triplestore.reindex"]'

## Building

To build these projects use this command

    MAVEN_OPTS="-Xmx1024m" mvn clean install

## Maintainers

Current maintainers:

* [Danny Bernstein](https://github.com/dbernstein)
