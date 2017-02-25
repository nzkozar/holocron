# Holocron [![](https://jitpack.io/v/nzkozar/holocron.svg)](https://jitpack.io/#nzkozar/holocron)
<h4>Encrypted Object Storage for Android</h4>
<blockquote>Holocrons are ancient repositories of knowledge and wisdom that can only be accessed by those skilled in the Force.</blockquote>

## Add through JitPack
<b>Step 1.:</b> Add JitPack in your root build.gradle at the end of repositories:
```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
<b>Step 2. Add the dependency
```gradle
	dependencies {
	        compile 'com.github.nzkozar:holocron:v1.0@aar'
	}
```

## Usage:
### Constructors
```java
//Init Holocron. This will take  few seconds to execute, as it must build encryption resources.
Holocron holocron = new Holocron(context);

//Init Holocron asynchronously and get notified through a HolocronResponseHandler interface when Holocron is ready.
Holocron holocron = new Holocron(context, new Holocron.HolocronResponseHandler() {
                @Override
                public void onHolocronResponse(int responseCode, HolocronResponse response) {
                    //Holocron can now be used
                }
            });
```
### Save & retrieve objects from storage
```java
//save an object of any Class that extends Object
holocron.put(object, (long)object.getId());

//retrieve a single stored object of a class using an id. Returns null if no object matches both class and id
Object object = holocron.get(object.getClass(),(long)id);

//retrieve all objects stored using the provided Class
List<Object> objects = holocron.getAll(object.getClass());

//retrieve asynchronously all objects stored using the provided Class
holocron.getAllAsync(object.class, new Holocron.HolocronResponseHandler() {
            @Override
            public void onHolocronResponse(int responseCode, HolocronResponse response) {
                List<Object> objects = data.getDataObjectList());
            }
        });
```
### Delete objects from storage
```java
//delete an object from storage
boolean deleted = holocron.remove(object.getClass(),(long)id);

//delete all objects of a provided Class from storage
boolean deleted = holocron.remove(object.getClass());

//delete asynchronously all objects of a provided Class from storage
holocron.removeAllAsync(object.class, new Holocron.HolocronResponseHandler() {
        @Override
        public void onHolocronResponse(int responseCode, HolocronResponse response) {
            //Objects deleted
        }
    });
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
This library uses Gson to convert objects, so any object convertable by Gson can be stored with Holocron.
