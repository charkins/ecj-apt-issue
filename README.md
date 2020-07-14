The eclipse compiler appears to miss execution of annotation processors for
annotations on classes generated directly in previous processing rounds.
This project is intended to demonstrate this issue.

https://bugs.eclipse.org/bugs/show_bug.cgi?id=565214

This project is structured as a main project and two subprojects. The main
project build.gradle has an ```ecjCompile``` task which will execute the ecj
compiler on the main project source. Both the ecj compiler and the standard
javac compiler on the main project are configured to print annotation
processing details (**-XprintProcessorInfo -XprintRounds**). The annotation
subproject provides the X annotation and the processor subproject provides
the annotation processor.

The X annotation takes a boolean array as it's value. If this array is
empty, nothing will be generated for the annotated class. If the first value
in this array is true, a java source file will be generated otherwise a class
file will be generated directly. The generated class name will be the
annotated class name with an "X" appended. If the array of booleans has more
than one element, the remaining elements will be used in an X annotation on
the generated class. This should trigger multiple rounds of annotation
processing.

## Foo.java
The first example from the main project is annotated
```@X({true,true,true})```. This should generate **FooX.java**,
**FooXX.java** and **FooXXX.java** in subsequent annotation
processing rounds. This works as expected with both the javac compiler and
the eclipse compiler.

### FooX.java
```
@X({true,true})
public class FooX {
}
```

### FooXX.java
```
@X({true})
public class FooXX {
}
```

### FooXXX.java
```
public class FooXXX {
}
```

## Bar.java
The second example from the main project is annotated
```@X({true,false,true})```. This should generate **BarX.java**,
**BarXX.class** and **BarXXX.java** in subsequent annotation
processing rounds. This works as expected with the javac compiler, but fails
with the eclipse compiler. The eclipse compiler is generating **BarX.java**
and **BarXX.class** as expected, but the annotation processor is not being
executed to process the X annotation on the generated **BarXX.class**.


### Output of compileJava and ecjCompile
```
#> ./gradlew clean ecjCompile
> Task :compileJava
Round 1:
        input files: {Bar, Foo}
        annotations: [X]
        last round: false
Processor org.gradle.api.internal.tasks.compile.processing.TimeTrackingProcessor matches [/X] and returns false.
Processor org.gradle.api.internal.tasks.compile.processing.SupportedOptionsCollectingProcessor matches [/X] and returns false.
Round 2:
        input files: {BarX, FooX}
        annotations: [X]
        last round: false
Processor org.gradle.api.internal.tasks.compile.processing.TimeTrackingProcessor matches [/X] and returns false.
Processor org.gradle.api.internal.tasks.compile.processing.SupportedOptionsCollectingProcessor matches [/X] and returns false.
Round 3:
        input files: {FooXX, BarXX}
        annotations: [X]
        last round: false
Processor org.gradle.api.internal.tasks.compile.processing.TimeTrackingProcessor matches [/X] and returns false.
Processor org.gradle.api.internal.tasks.compile.processing.SupportedOptionsCollectingProcessor matches [/X] and returns false.
Round 4:
        input files: {FooXXX, BarXXX}
        annotations: []
        last round: false
Processor org.gradle.api.internal.tasks.compile.processing.TimeTrackingProcessor matches [] and returns false.
Processor org.gradle.api.internal.tasks.compile.processing.SupportedOptionsCollectingProcessor matches [] and returns false.
Round 5:
        input files: {}
        annotations: []
        last round: true

> Task :ecjCompile
[parsing    /tmp/ecj-apt-issue/src/main/java/Bar.java - #1/2]
[parsing    /tmp/ecj-apt-issue/src/main/java/Foo.java - #2/2]
[reading    java/lang/Object.class]
[reading    X.class]
Round 1:
        input files: {Foo,Bar}
        annotations: [X]
        last round: false
Discovered processor service XProcessor
  supporting [X]
  in jar:file:/tmp/ecj-apt-issue/processor/build/libs/processor-1.0.jar!/
Processor XProcessor matches [X] and returns false
[parsing    /tmp/ecj-apt-issue/build/ecj/generated/BarX.java - #1/2]
[parsing    /tmp/ecj-apt-issue/build/ecj/generated/FooX.java - #2/2]
Round 2:
        input files: {FooX,BarX}
        annotations: [X]
        last round: false
Processor XProcessor matches [X] and returns false
[parsing    /tmp/ecj-apt-issue/build/ecj/generated/FooXX.java - #1/1]
Round 3:
        input files: {FooXX}
        annotations: [X]
        last round: false
Processor XProcessor matches [X] and returns false
[parsing    /tmp/ecj-apt-issue/build/ecj/generated/FooXXX.java - #1/1]
Round 4:
        input files: {FooXXX}
        annotations: []
        last round: false
Processor XProcessor matches [] and returns false
Round 5:
        input files: {}
        annotations: []
        last round: true
[analyzing  /tmp/ecj-apt-issue/src/main/java/Bar.java - #1/6]
[analyzing  /tmp/ecj-apt-issue/src/main/java/Foo.java - #2/6]
[writing    Bar.class - #1]
[completed  /tmp/ecj-apt-issue/src/main/java/Bar.java - #1/6]
[analyzing  /tmp/ecj-apt-issue/build/ecj/generated/BarX.java - #3/6]
[writing    Foo.class - #2]
[completed  /tmp/ecj-apt-issue/src/main/java/Foo.java - #2/6]
[analyzing  /tmp/ecj-apt-issue/build/ecj/generated/FooX.java - #4/6]
[writing    BarX.class - #3]
[completed  /tmp/ecj-apt-issue/build/ecj/generated/BarX.java - #3/6]
[analyzing  /tmp/ecj-apt-issue/build/ecj/generated/FooXX.java - #5/6]
[writing    FooX.class - #4]
[completed  /tmp/ecj-apt-issue/build/ecj/generated/FooX.java - #4/6]
[analyzing  /tmp/ecj-apt-issue/build/ecj/generated/FooXXX.java - #6/6]
[writing    FooXX.class - #5]
[completed  /tmp/ecj-apt-issue/build/ecj/generated/FooXX.java - #5/6]
[writing    FooXXX.class - #6]
[completed  /tmp/ecj-apt-issue/build/ecj/generated/FooXXX.java - #6/6]
[6 units compiled]
[6 .class files generated]

BUILD SUCCESSFUL in 2s
11 actionable tasks: 11 executed
```