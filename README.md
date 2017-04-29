# Java Software Metrics Project

## Directions for our TA/grader
The following commands will handle downloading our project, `commons-dbutils`, and `joda-time`, and will ensure they are on the correct branch. Please execute all commands to ensure proper execution.

```sh
# Clone our project and the two projects we will test on
git clone git@github.com:virtualdom/java-software-metrics.git
git clone https://github.com/apache/commons-dbutils.git
git clone git@github.com:JodaOrg/joda-time.git

# Checkout the correct branch in commons-dbutils
cd commons-dbutils
git checkout 633749db5b0fd25b9a3ca133e7496a353de4fd5d
mvn package
cd ..

# Checkout the correct branch in joda-time
cd joda-time
git checkout acff94148b2110b95f7aeae6a1bdcafb756061f0
mvn package
cd ..

# Build our tool to produce a JAR file
cd java-software-metrics/
javac -cp .:./asm-all-5.0.3.jar -g:vars,lines -parameters *.java
jar cvfm Software-Metrics.jar manifest.txt ClassParseVisitor.class MethodTransformVisitor.class ParseClassFile.class asm-all-5.0.3.jar

# Copy the necessary dependencies to the test locations
cp Software-Metrics.jar ../commons-dbutils/
cp Software-Metrics.jar ../joda-time/

cp asm-all-5.0.3.jar ../commons-dbutils/
cp asm-all-5.0.3.jar ../joda-time/

cp dbutils-test.sh ../commons-dbutils/
cp joda-test.sh ../joda-time/

# Execute the JAR on commons-dbutils
cd ../commons-dbutils/
./dbutils-test.sh
# Results are stored in output.json.
# Errors are stored in errors.txt.

# Execute the JAR on joda-time
cd ../joda-time/
./joda-test.sh
# Results are stored in output.json.
# Errors are stored in errors.txt.

```

## How to compile
```
javac -cp .:./asm-all-5.0.3.jar -g:vars,lines -parameters *.java
java -cp .:./asm-all-5.0.3.jar ParseClassFile TestMe.class
```

## How to produce and execute JAR
```
jar cvfm Software-Metrics.jar manifest.txt ClassParseVisitor.class MethodTransformVisitor.class ParseClassFile.class asm-all-5.0.3.jar

# Now move Software-Metrics.jar and asm-all-5.0.3.jar to where you'd like to execute it

ls *class | xargs -n 1 java -jar Software-Metrics.jar
```
