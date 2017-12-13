# Hippo Unit Tester

Hippo Unit Tester is a framework that tries to take advantage of JCR mocking solutions and Hippo Mock objects to setup a mock Hippo API.
It tries to be as realistic as possible while still be highly manipulatable.
In order to enable developers to focus on designing their unit tests scenarios instead of focusing on technical details mocking process.

## Installation

1. Add dependency to pom.xml
```xml   
<dependency>
  <groupId>nl.openweb.hippo</groupId>
  <artifactId>hippo-unit-tester</artifactId>
  <version>2.1.2</version>
  <scope>test</scope>
</dependency>
```

2. Add dependency to site pom.xml.
```xml
<dependency>
  <groupId>nl.openweb.hippo</groupId>
  <artifactId>hippo-unit-tester</artifactId>
</dependency>
```

## Usage

```java
registerNodeType("ns:custom"); // When you want to create you own namespace
```

```java
request.setLocale(new Locale("nl_NL")); //eForms needs a Local to be set
````