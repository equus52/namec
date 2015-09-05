# namec [![Build Status](https://travis-ci.org/equus52/namec.svg)](https://travis-ci.org/equus52/namec)

Annotation processing to generate String fields of field name, method name, property name, class name, and package name.

## How to use

Add the @GenerateName annotation to your class, and @GenerateName will automatically generate an interface that has static String fields of source class descriptions.

### In Example.java
```java
package com.test;

import equus.namec.GenerateName;

@GenerateName
public class Example {

  private final String field1 = "test";

  public String getField1(){
    return field1;
  }

  public boolean isEmpty(){
    return false;
  }
}
```

### In ExampleName.java (generated)
```java
package com.test;

import javax.annotation.Generated;

@Generated("equus.namec.processor.GenerateNameProcessor")
public interface ExampleName {

  static String CLASS_CANONICAL = "com.test.Example";
  static String CLASS_SIMPLE = "Example";

  static String PACKAGE = "com.test";

  static String FIELD_field1 = "field1";

  static String METHOD_getField1 = "getField1";
  static String METHOD_isEmpty = "isEmpty";

  static String PROPERTY_field1 = "field1";
  static String PROPERTY_empty = "empty";

}
```

You can change generated class name by assigning suffix, prefix, or full class name.  
See @GenerateName.

## Build

```
gradlew build
```


## Requirements

* JDK 8 +

## Dependencies

* nothing

### License

namec is released under the [MIT License](http://www.opensource.org/licenses/MIT).


#### Donations

Your donation is great appreciated.  
PayPal: stepdesign81@gmail.com
