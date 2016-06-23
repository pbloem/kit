# kit

A small toolkit of generic java utilities. Here are some examples:


```java

// Loop through integers with a foreach loop
import static nl.peterbbloem.kit.Series.series;

for(int i : series(3))
	System.out.print(i + ' ');
// prints 0 1 2 

for(int i : series(1, 5))
	System.out.print(i + ' ');
// prints 1 2 3 4 

for(int i : series(1, 2, 10))
	System.out.print(i + ' ');
// prints 1, 3, 5, 7, 9
```

```java
import nl.peterbloem.kit.FrequencyModel;

FrequencyModel<String> model = new FrequencyModel<String>();

model.add(Arrays.asList("a", "a", "b", "b", "c");
model.add("c", 2.5);

model.print(System.out);
// prints a table showing the frequencies a:2.0, b:2.0, c:3.5
```

## Installation

The simplest way to use kit is as a maven module, with 
[JitPack](https://jitpack.io/#pbloem/kit). Add the following repository:

```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

And the following dependency:

```xml
	<dependency>
	    <groupId>com.github.pbloem</groupId>
	    <artifactId>kit</artifactId>
	    <version>0.1.0</version>
	</dependency>
```


