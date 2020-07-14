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
package x;
@x.X({true,true})
public class FooX {
}
```

### FooXX.java
```
package x;
@x.X({true})
public class FooXX {
}
```

### FooXXX.java
```
package x;
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
        input files: {x.Foo, x.Bar}
        annotations: [x.X]
        last round: false
Processor org.gradle.api.internal.tasks.compile.processing.TimeTrackingProcessor matches [/x.X] and returns false.
Processor org.gradle.api.internal.tasks.compile.processing.SupportedOptionsCollectingProcessor matches [/x.X] and returns false.
Round 2:
        input files: {x.FooX, x.BarX}
        annotations: [x.X]
        last round: false
Processor org.gradle.api.internal.tasks.compile.processing.TimeTrackingProcessor matches [/x.X] and returns false.
Processor org.gradle.api.internal.tasks.compile.processing.SupportedOptionsCollectingProcessor matches [/x.X] and returns false.
Round 3:
        input files: {x.FooXX, x.BarXX}
        annotations: [x.X]
        last round: false
Processor org.gradle.api.internal.tasks.compile.processing.TimeTrackingProcessor matches [/x.X] and returns false.
Processor org.gradle.api.internal.tasks.compile.processing.SupportedOptionsCollectingProcessor matches [/x.X] and returns false.
Round 4:
        input files: {x.FooXXX, x.BarXXX}
        annotations: []
        last round: false
Processor org.gradle.api.internal.tasks.compile.processing.TimeTrackingProcessor matches [] and returns false.
Processor org.gradle.api.internal.tasks.compile.processing.SupportedOptionsCollectingProcessor matches [] and returns false.
Round 5:
        input files: {}
        annotations: []
        last round: true

> Task :ecjCompile
[parsing    /home/casey/projects/ecj-apt-issue/src/main/java/x/Foo.java - #1/2]
[parsing    /home/casey/projects/ecj-apt-issue/src/main/java/x/Bar.java - #2/2]
[reading    java/lang/Object.class]
[reading    x/X.class]
Round 1:
        input files: {x.Foo,x.Bar}
        annotations: [x.X]
        last round: false
Discovered processor service x.XProcessor
  supporting [x.X]
  in jar:file:/home/casey/projects/ecj-apt-issue/processor/build/libs/processor-1.0.jar!/
Processor x.XProcessor matches [x.X] and returns false
[parsing    /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/BarX.java - #1/2]
[parsing    /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/FooX.java - #2/2]
Round 2:
        input files: {x.FooX,x.BarX}
        annotations: [x.X]
        last round: false
Processor x.XProcessor matches [x.X] and returns false
[parsing    /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/FooXX.java - #1/1]
Round 3:
        input files: {x.FooXX}
        annotations: [x.X]
        last round: false
Processor x.XProcessor matches [x.X] and returns false
[parsing    /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/FooXXX.java - #1/1]
Round 4:
        input files: {x.FooXXX}
        annotations: []
        last round: false
Processor x.XProcessor matches [] and returns false
Round 5:
        input files: {}
        annotations: []
        last round: true
[analyzing  /home/casey/projects/ecj-apt-issue/src/main/java/x/Foo.java - #1/6]
[analyzing  /home/casey/projects/ecj-apt-issue/src/main/java/x/Bar.java - #2/6]
[writing    x/Foo.class - #1]
[analyzing  /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/BarX.java - #3/6]
[completed  /home/casey/projects/ecj-apt-issue/src/main/java/x/Foo.java - #1/6]
[writing    x/Bar.class - #2]
[completed  /home/casey/projects/ecj-apt-issue/src/main/java/x/Bar.java - #2/6]
[analyzing  /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/FooX.java - #4/6]
[writing    x/BarX.class - #3]
[completed  /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/BarX.java - #3/6]
[analyzing  /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/FooXX.java - #5/6]
[writing    x/FooX.class - #4]
[completed  /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/FooX.java - #4/6]
[analyzing  /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/FooXXX.java - #6/6]
[writing    x/FooXX.class - #5]
[completed  /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/FooXX.java - #5/6]
[writing    x/FooXXX.class - #6]
[completed  /home/casey/projects/ecj-apt-issue/build/ecj/generated/x/FooXXX.java - #6/6]
[6 units compiled]
[6 .class files generated]

BUILD SUCCESSFUL in 1s
11 actionable tasks: 11 executed
```

