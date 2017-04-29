echo START
date "+%H:%M:%S   %d/%m/%y"

ls target/test-classes/org/apache/commons/dbutils/*.class target/test-classes/org/apache/commons/dbutils/*/*.class target/test-classes/org/apache/commons/dbutils/*/*/*.class | xargs -n 1 java -jar Software-Metrics.jar > output.json 2> error.txt

echo END
date "+%H:%M:%S   %d/%m/%y"
