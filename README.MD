# Hippo Unit Tester

Hippo Unit Tester is a framework that tries to take advantage of JCR mocking solutions and Hippo Mock objects to setup a mock Hippo API.
It tries to be as realistic as possible while still be highly manipulatable.
In order to enable developers to focus on designing their unit tests scenarios instead of focusing on technical details mocking process.

## Installation

1. Add dependency to pom.xml
```xml
<dependencyManagement>
    <dependency>
        <groupId>nl.openweb.hippo</groupId>
        <artifactId>hippo-unit-tester</artifactId>
        <version>2.2.0</version>
        <scope>test</scope>
    </dependency>
    ...
</dependencyManagement>
```

2. Add dependency to site pom.xml.
```xml
<dependency>
    <groupId>nl.openweb.hippo</groupId>
    <artifactId>hippo-unit-tester</artifactId>
</dependency>
```

## Usage
Within the package there are classes to extend from:

* BaseHippoTest
* SimpleHippoTest

### Importer
The importer works with JSON or XML.
It will import the file at given location.

#### XML

```xml
<sv:node sv:name="news1">
    <sv:property sv:name="jcr:primaryType" sv:type="Name">
        <sv:value>hippo:handle</sv:value>
    </sv:property>
    <sv:property sv:name="jcr:mixinTypes" sv:type="Name" sv:multiple="true">
        <sv:value>mix:referenceable</sv:value>
    </sv:property>
    <sv:property sv:name="jcr:uuid" sv:type="String">
        <sv:value>3119bd98-9361-4b78-a81b-ad8228bc8bfb</sv:value>
    </sv:property>
    <sv:node sv:name="news1">
        <sv:property sv:name="jcr:primaryType" sv:type="Name">
            <sv:value>ns:NewsPage</sv:value>
        </sv:property>
        ...
    </sv:node>
</sv:node>
```

#### JSON

```json

```

### Debug

```java
/**
* prints out the whole JCR structure
*/
printNodeStructure();

/**
* prints out the JCR structure from given path
*/
printNodeStructure("/path");
```


### Explain

```java
/**
* When you want to create you own namespace
*/
registerNodeType("ns:custom");
```

```java
/**
* eForms needs a Local to be set
*/
request.setLocale(new Locale("nl_NL"));
````

```java
/**
 * From the given file create a node containing the content of imported file
 * Location should contain the path the node should be created not where you want to import the file
**/
importer.createNodesFromXml(getResourceAsStream("/nl/openweb/hippo/demo/news.xml"), "/content/documents/mychannel/news", "hippostd:folder");
````