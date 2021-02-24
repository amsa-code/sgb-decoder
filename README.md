# sgb-decoder
<a href="https://travis-ci.com/amsa-code/sgb-decoder"><img src="https://travis-ci.com/amsa-code/sgb-decoder.svg"/></a><br/>
[![codecov](https://codecov.io/gh/amsa-code/sgb-decoder/branch/master/graph/badge.svg)](https://codecov.io/gh/amsa-code/sgb-decoder)<br/>

Java library that decodes [Cospas-Sarsat](https://en.wikipedia.org/wiki/International_Cospas-Sarsat_Programme) Second Generation Beacon (SGB) detection messages and is compliant with the C/S T.018 Rev. 6 [specification](https://vnmcc.vishipel.vn/images/uploads/attach/T018-MAY-2020.pdf).

**Features**

* Decodes Second Generation Beacon (SGB) detection messages (202 bits) to Java objects
* Provides a JSON form of the SGB detection message ([JSON Schema](src/main/resources/detection-schema.json))
* Extracts Beacon 23 Hex Id from an SGB detection message
* Decodes a Beacon 23 Hex Id to Java objects
* Provides a JSON form of the Beacon 23 Hex Id ([JSON Schema](src/main/resources/beacon-23-hex-id-schema.json))
* 100% unit test coverage (enforced)
* *spotbugs* maximum effort static analysis (enforced) 
* *pmd* static analysis (enforced) 

Status: *pre-alpha* (in development, **NOT** production ready yet)

## How to build

```bash
mvn clean install
```
The library jar will be in `target` directory.

## Getting started

Add this dependency to your pom.xml:

```xml
<dependency>
  <groupId>au.gov.amsa</groupId>
  <artifactId>sgb-decoder</artifactId>
  <version>VERSION_HERE</version>
</dependency>
```
Note that this library is not stand-alone and has a number of runtime dependencies. Use Maven (or Gradle etc)! 

## Usage

### Decoding an SGB detection

The most likely form that consumers will encounter a beacon detection message is in the hex encoded Cospas-Sarsat Ground Segment Representation (202 bits hex-encoded to 51 chars using left padded zero bits) Here's an example:

```java
import au.gov.amsa.sgb.decoder.Detection;

// Use the hex detection message example from specification B-1
String hex = "0039823D32618658622811F0000000000003FFF004030680258";
Detection d = Detection.fromHexGroundSegmentRepresentation(hex);
``` 
At that point you can browse the object representation of the message or dump a JSON text representation to stdout.

```java
System.out.println(d.toJson());
```
Output is [here](src/test/resources/compliance-kit/detection-specification-example.json).

The JSON Schema for the above is [here](src/main/resources/detection-schema.json).

You can also decode the raw bits (as a bit string) using `Detection.fromBitString("1010000..")`.

Note that a unit [test](src/test/java/au/gov/amsa/sgb/decoder/internal/json/JsonSchemaTest.java) ensures that the abovementioned sample json complies with the JSON Schema.

### Decoding a Beacon 23 Hex Id
```java
import au.gov.amsa.sgb.decoder.Beacon23HexId;

Beacon23HexId b = Beacon23HexId.fromHex("9934039823d000000000000");
System.out.println(d.toJson());
```
Output is [here](src/test/resources/compliance-kit/beacon-23-hex-id-sample.json).

The JSON Schema for the above is [here](src/main/resources/beacon-23-hex-id-schema.json).

## BCH Error Correction
A beacon transmits the 202 bit SGB detection message followed by a 48 bit BCH error correction code. You can calculate the code expected from the 202 bit SGB detection message like this:

```java
Bits bch = detection.calculateBchErrorCorrectionCode();
System.out.println(bch.toBitString());
```
Application of error corrections (when required) is presumed to happen upstream of consumers so this library does not support it (if you need it raise an issue).

## Performance
Quick and dirty performance testing (without JMH) indicates that the the library can decode about 140,000 beacon detection messages a second. If you need faster performance than this raise an issue.

## Semantic Versioning
This project follows [Semantic Versioning 2.0](https://semver.org/).

## SGB Compliance Kit (SGBCK) 
With the arrival of second generation Beacons on the market sometime from July 2021, many of the National Rescue Coordination Centres (RCCs) throughout the world will want to be able to decode SGB hex detection messages into a human readable form. This might be simply a web page that performs the decode but could equally be a programming library that developers might use to customize their use of the hex detection message.

Producing a programming library that decodes an SGB detection message is a non-trivial task that has one important risk: **correctness**. As a developer how do I automatically confirm that my code correctly decodes all variations of SGB detection messages? Writing unit tests still has the risk that my *interpretation* of the specification might not match the *intent* of the specification. To reduce risk (beacon alerts is a life-and-death matter after all) a developer should seek a high level of code coverage via appropriately granular unit tests of the decoder on its own but to have some certainty about what the software will do come the first real detection it's desirable to have some officially validated decode tests too.

A suggestion is that the beacons community (particularly devs) provide and maintain an SGB Compliance Kit (SGBCK) which is a list of beacon detection messages in hex form together with the corresponding decoded human and machine readable version of the detection message in some *canonical form*. If this were the case then no matter what language a decoder was written in a decent level of test coverage of that decoder would provided by consuming the (comprehensive) Compliance Kit test data. 

The authors of this project are willing to coordinate and host the SGBCK on github. If some official examples can be provided to this project with a human-readable decode then the authors of this project can compare the calculated *canonical format* with the supplied decode and if verified those examples can be added to the SGBCK. Alternatively the community could construct beacon detection messages and submit the human-readable decode to spec authors to verify correctness at which point they can be added to the SGBCK.

Dave Moten proposes that the SGBCK should look like the contents of this [folder](src/test/resources/compliance-kit). The folder contains:
* [`tests.csv`](src/test/resources/compliance-kit/tests.csv) file with columns *TYPE*, *TITLE*, *HEX*, *JSON*
* JSON files referenced by `tests.csv`

A consumer of the Compliance Kit would decode the given hex and generate the JSON canonical form string and compare it to the given JSON file (using JSON equivalence rathen exact string equality).

Note that there is also obvious benefit here for an encoder too with no change to the SGCK as described.

Clearly one test in the kit does not cut it. There are many variations on field values, some are derived from special binary codes, some field values are optional.

### Which message fields are non-trivial to parse?
The beacon detection message fields that are non-trivial to parse are:

* 3.7.1g Encoded GNSS location
* 3.7.1h Vessel ID (5 Variants)
* 4.3.1a Elapsed Time since activation
* 4.3.1b Time from last encoded location
* 4.3.1c Altitude Encoded location

In addition it would be helpful to have full coverage of:
* the Modified Baudot Codes (to check for typos in the lookup table). There are 39 characters in the Modified Baudot Codes which could be covered fully (7 characters at a time) using 6 messages all with a Radio Call Sign (Vessel ID field).
* all Rotating Field types

A minimal SGBCK would include coverage of these non-trivial fields.

Let's look at the tests cases in detail:

* 3.7.1g Encoded GNSS location
  * North lat, E long, both with decimal parts to n(?) decimal places
  * South lat, W long, both with decimal parts to n(?) decimal places
  * No Capability
* 3.7.1h Vessel ID (5 Variants)
    *  MMSI
        * MMSI + EPIRB MMSI present
        * MMSI not present, EPIRB MMSI present
        * MMSI present, EPIRB MMSI not present
        * neither present
    * Radio Call Sign
        * Not present
        * Left Justified Short (say 3 characters)
        * Right Justified Short (defensive, whitespace trimming the found value should do the job)
        * Uses all 7 characters
    * Aircraft Registration Marking
        * Not present
        * Right Justified Short (say 3 characters)
        * Left Justified Short (defensive, whitespace trimming the found value should do the job)
        * Uses all 7 characters
    * Aviation 24 Bit Addresss
        * No Bit Address, no Aircraft Operator Designator
        * Both present
    * Aircraft Operator and Serial Number
        * Both present 
* 4.3.1a Elapsed Time since activation
    * Present, non-zero
* 4.3.1b Time from last encoded location
    * Present, non-zero
    * Not present 
* 4.3.1c Altitude Encoded location
    * -400m
    * 0m
    * 15592m
* Rotating Field Types
    * Objective Requirements
    * In-Flight Emergency
    * RLS
    * National Use
    * Other  

On the above basis the minimal set of messages in the SGBCK would be 29 messages.

### Why use JSON as the *canonical format*

Given that a service implementation of the decoder would probably serialize the decoded structure into JSON or XML, it makes sense to use one of those text formats to hold the canonical decoded form so that the implementer can reuse the canonical form work.

If JSON or XML was used for the canonical form then it should also be described by a schema document (JSON Schema or XSD). This library provides a JSON form and a JSON Schema document and the author suggests that **JSON format is used for the *canonical form***. 

Note that the canonical form in JSON would not have to be exactly matched as a string during a test for compliance. We don't care about whitespace outside of expressions (new lines, indents) and even field order so the match would be based on JSON equality. Every major programming language has support for this sort of equality match (either in an open-source library or in the base platform).

## TODO
* create a set of test messages for the Compliance Kit
* discuss Compliance Kit with the specification authors
* ~report error in example in specification to authors~ (fixed in rev. 7 due post March 2021)
* how to handle invalid message bit sequences (might only affect one field). How to represent this in canonical form (or just ignore)?
* how to communicate the existence of this library to other parties (especially RCCs worldwide) to save them a bit of time and effort (and to leverage their review and contributions)? 
