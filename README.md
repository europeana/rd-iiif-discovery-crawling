---

# IIIF Change Discovery Crawler

A Java implementation of a crawler for harvesting of IIIF data sources. The crawler implements the <a href="https://iiif.io/api/discovery/1.0/">IIIF Change Discovery API 1.0</a> for discovering IIIF resources (e.g. IIIF Manifests), tracking when resources have been added, updated or deleted.

## Functionality

The software is meant to be used as a library for development of applications that will consume the crawled resources. It provides the following functionality:

- Fully implements the processing algorithm specified in the  <a href="https://iiif.io/api/discovery/1.0/">IIIF Change Discovery API 1.0</a>
- Users need only to implement the logic to process the crawled resources in their repository.
- Includes an utility to validate the data on a stream and extract statistics about its content. 
- Includes a example implementation of the use case of a metadata aggregator that wants to keep a local copy of all the EDM metadata present in the "seeAlso" of the IIIF Manifests included in a IIIF Change Discovery API endpoint.

## Status of this software

The software has been applied in the few IIIF endpoints implementing the <a href="https://iiif.io/api/discovery/1.0/">IIIF Change Discovery API 1.0</a>. It has not yet been tested yet in a production system. 

## Using the software as a library  

To include it in your own Java application do the following:

- Clone or download from Github.  
- Build the library and install in your local Maven repository by using the command `mvn install`.
- Add the following dependency to the pom.xml of your Maven project:
```xml
<dependency>
	<groupId>europeana.eu</groupId>
	<artifactId>iiif-discovery-crawling</artifactId>
	<version>1.0</version>
</dependency>
```
- Implement the interface `europeana.rnd.iiif.discovery.ActivityHandler`, which will receive events during the execution of the `ProcessingAlgorithm` (mainly the create/update/delete activities). 
- Instantiate the IIIF Change Discovery Processing Algorithm, passing on your ActivityHandler implementation.

```java
MyActivityHandler activityHandler=new ActivityHandler();
ProcesssingAlgorithm processsingAlgorithm = new ProcesssingAlgorithm(activityHandler);		
```
- Initiate a crawl a IIIF Change Discovery API endpoint

```java
String endpointUrl="https://example.org/change-discovery/ordered-collection";
processsingAlgorithm.processStream(endpointUrl);
```
A complete example implementation is provided in the package `europeana.rnd.iiif.discovery.demo`.

## Trying out the example implementation in a real IIIF Change Discovery endpoint

Includes an example implementation of the use case of a metadata aggregator that wants to keep a local copy of all the EDM metadata present in the "seeAlso" of the IIIF Manifests included in a IIIF Change Discovery API endpoint.
Running the demo requires Java 8 (or higher) and Maven to be installed installed.

To run the demo do the following:
- Clone or download from Github.  
- Build the demo and package it in a ZIP file by using the command `mvn assembly:assembly`.
- The application is packaged in target/iiif-discovery-crawling-standalone.zip. To install it, unzip it to a directory of your choice.
- After unziping, there will be a script to execute the demo in the installation directory. Linux users should use the file `runEdmAggregationDemonstrator.sh` while Windows users should use the `runEdmAggregationDemonstrator.bat`. The script receives the following parameters:

```
Usage information:
  -d <DIR>  : Path to the directory for storing the timestamps
  -l <FILE> : Path to a log file (optional)
  -m <DIR>  : Path to the directory for storing the EDM metadata
  -u <URL>  : URL of the IIIF Change Discovery API stream  
```
- For example, run the command as follows:
```
runEdmAggregationDemonstrator.sh -d storage/timestamps-db -m storage/metadata-repository -u https://example.org/any-iiif-change-discovery-endpoint-url -l craling.log
```
- The processing algorithm will execute and display a summary of the results of the crawl. This is an example:

```
Timestamps database loaded (0 datasets)
- Crawl result
Crawl successful: true
Latest crawled timestamp: 2023-11-13T14:23:18Z
Activities processed:
Create: 50
Update: 501
- Repository status after crawl
Total resources in repository: 512
```

- In the directory indicated in the `-m` parameter (in this example, `storage/metadata-repository`), you will find the EDM metadata that was aggregated.
- The same command may be executed in the following days, to keep the local repository of EDM metadata synchronised with the current state of the IIIF Change Discovery endpoint. As new IIIF Manifests are added or updated, the corresponding EDM metadata is updated in the local repository. The metadata of IIIF that are deleted are removed from the local repository. 


## Using the validation and statistics utility

To run the utility do the following:
- Install the software as described in the first three steps of the previous section.
- After unziping, there will be a script to execute the utility in the installation directory. Linux users should use the file `runStreamAnalyser.sh` while Windows users should use the `runStreamAnalyser.bat`. The script receives onnly one parameter, which should be the URL of the IIIF Change Discovery stream
- For example, run the command as follows:
```
runStreamAnalyser.sh https://example.org/any-iiif-change-discovery-endpoint-url
```
- The utility will execute and display the statistics and number of validation errors. This is an example:

```
Statistics on the IIIF Change Discovery API stream at https://example.org/any-iiif-change-discovery-endpoint-url
 - earliest timestamp: 2023-11-07T13:22:23Z
 - latest timestamp: 2023-11-13T14:23:18Z
 - activities on IIIF manifests:
   - Update: 519
   - Create: 504
   - Remove: 2
   - Add: 1
 - activities on IIIF collections:
   - Update: 639
   - Create: 55
   - Delete: 3
   - Remove: 1
   - Add: 1
   - Move: 1
 - validation errors on activities: 0
 - validation errors on collection pages: 0
```

## Changelog

0.9 (2023-11-15) 
- Initial version   





