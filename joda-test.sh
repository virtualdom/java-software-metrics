echo START
date "+%H:%M:%S   %d/%m/%y"

ls target/classes/org/joda/time/*.class target/classes/org/joda/time/*/*.class | xargs -n 1 java -jar Software-Metrics.jar > output.json 2> error.txt

echo END
date "+%H:%M:%S   %d/%m/%y"
