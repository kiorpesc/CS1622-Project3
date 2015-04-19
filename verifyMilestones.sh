sh "$1" test/Milestone1.java milestone1.asm
sh "$1" test/Milestone2.java milestone2.asm
sh "$1" test/Milestone3.java milestone3.asm
sh "$1" test/Milestone4.java milestone4.asm
sh "$1" test/Milestone5.java milestone5.asm
sh "$1" test/Milestone9.java obj.asm
sh "$1" test/Milestone10.java opt.asm

java -jar lib/Mars4_5.jar milestone1.asm > results.txt
java -jar lib/Mars4_5.jar milestone2.asm >> results.txt
java -jar lib/Mars4_5.jar milestone3.asm >> results.txt
java -jar lib/Mars4_5.jar milestone4.asm >> results.txt
java -jar lib/Mars4_5.jar milestone5.asm >> results.txt
java -jar lib/Mars4_5.jar obj.asm >> results.txt
java -jar lib/Mars4_5.jar opt.asm >> results.txt

diff results.txt correctMilestoneOutput.txt

