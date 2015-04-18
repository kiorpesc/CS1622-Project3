sh linuxcompile.sh -O1 test/Milestone1.java milestone1.asm
sh linuxcompile.sh -O1 test/Milestone2.java milestone2.asm
sh linuxcompile.sh -O1 test/Milestone3a.java milestone3a.asm
sh linuxcompile.sh -O1 test/Milestone3b.java milestone3b.asm
sh linuxcompile.sh -O1 test/Milestone4.java milestone4.asm
sh linuxcompile.sh -O1 test/Milestone5.java milestone5.asm

java -jar lib/Mars4_5.jar milestone1.asm 
java -jar lib/Mars4_5.jar milestone2.asm 
java -jar lib/Mars4_5.jar milestone3a.asm 
java -jar lib/Mars4_5.jar milestone3b.asm 
java -jar lib/Mars4_5.jar milestone4.asm 
java -jar lib/Mars4_5.jar milestone5.asm 


