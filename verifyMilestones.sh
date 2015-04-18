sh "$1" test/Milestone1.java milestone1.asm
sh "$1" test/Milestone2.java milestone2.asm
sh "$1" test/Milestone3a.java milestone3a.asm
sh "$1" test/Milestone3b.java milestone3b.asm
sh "$1" test/Milestone4.java milestone4.asm
sh "$1" test/Milestone5.java milestone5.asm
sh "$1" test/OptimizationTest.java opt.asm
sh "$1" test/ArrayTest.java array.asm
sh "$1" test/HardObjectTest.java obj.asm

java -jar lib/Mars4_5.jar milestone1.asm > results.txt
java -jar lib/Mars4_5.jar milestone2.asm >> results.txt
java -jar lib/Mars4_5.jar milestone3a.asm >> results.txt
java -jar lib/Mars4_5.jar milestone3b.asm >> results.txt
java -jar lib/Mars4_5.jar milestone4.asm >> results.txt
java -jar lib/Mars4_5.jar milestone5.asm >> results.txt
java -jar lib/Mars4_5.jar opt.asm >> results.txt
java -jar lib/Mars4_5.jar array.asm >> results.txt
java -jar lib/Mars4_5.jar obj.asm >> results.txt

diff results.txt correctMilestoneOutput.txt
