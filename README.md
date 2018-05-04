# Rumble-4-Android
r4a abstracts away the annoying Android Vibrator API which suffers from a number of issues:

 - Different versions of android have **deprecated** or **added** different functions and you are expected to know which ones to use depending on the device version.
 - You have to make a **new object** for every single vibration you want to run.
 - Patterns are tedious and **not intuitive**.

Instead...

## Setup *(easy!)*

 1. Download ```Rumble.java``` and place it anywhere in the **source directory** of your project.
 2. Add the line ```<uses-permission android:name="android.permission.VIBRATE" />``` inside the ```manifest``` tag of your project's ```AndroidManifest.xml```.
 3. Call ```Rumble.init(applicationContext)``` and pass it your application's **Context**. For example, you can place the following code in your activity:
 >MainActivity.java

```java
@Override
protected void onCreate(Bundle savedInstanceState)  {
    super.onCreate(savedInstanceState);
    Rumble.init(getApplicationContext());
}
```

## Use *(fluent!)*
**One-time** device vibration
```java Rumble.once(500); // Vibrate for 500 milliseconds.```

**Patterns**
```java
Rumble.makePattern()  
    .beat(300)  
    .rest(250)  
    .beat(720)  
    .playPattern();
```
**Repeating patterns**
```java
Rumble.makePattern()  
    .rest(200)
    .beat(30) 
    .beat(holdDuration) // Automatically adds to previous beat.
    .playPattern(4);      // Play 4 times in a row.
```
**Save patterns for later**
```java
RumblePattern pattern = Rumble.makePattern()
    .beat(30).rest(150).beat(40).rest(40);
	
pattern.rest(80).beat(700); // Add to a pattern later.
pattern.playPattern();      // Play it whenever you like.
```

**Lock patterns to prevent mutation**
```java
pattern.lock();
pattern.playPattern(3);     // Works just fine.
pattern.beat(500).rest(250) // Throws IllegalStateException.
```
