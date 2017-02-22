# Holocron
<h4>Encrypted Object Storage for Android</h4>
<blockquote>Holocrons are ancient repositories of knowledge and wisdom that can only be accessed by those skilled in the Force.</blockquote>

## Usage:

```java
//init Holocron. This will take  few seconds to execute, as it must build encryption resources.
Holocron holocron = Holocron.init(context);

//save an object of any Class that extends Object
holocron.put(object, (long)object.getId());

//retrieve a single stored object of a class using an id. Returns null if no object matches both class and id
Object object = holocron.get(object.getClass(),(long)id);

//retrieve all objects stored using the provided Class
List<Object> objects = holocron.getAll(object.getClass());

//delete an object from storage
boolean deleted = holocron.remove(object.getClass(),(long)id);

//delete all objects of a provided Class from storage
boolean deleted = holocron.remove(object.getClass());

```

## Example
The accompanying example shows the adding, displaying and removing of Checkpoint class objects.

```java
public class Checkpoint{
    private long id;
    private String name;
    private double longitude,latitude;
}
```

## Encryption:

All data is encrypted using AES encryption.

### Gson

This library uses Gson to convert objects. Gson library is included in this library package, so there is no need to import it separately.


## In development
This library is still in development. Once it is ready to use Out-Of-The-Box, import options will be listed here.
