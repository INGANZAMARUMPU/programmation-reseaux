TP5 — Échange de fichiers

Consignes :
1. Compiler tous les fichiers.
2. Lancer ServeurProto (port 9011).
3. Preparer un fichier de 1 Mo :
   dd if=/dev/urandom of=test.bin bs=1M count=1
4. Lancer :
   java ClientEnvoi 127.0.0.1 test.bin
5. Comparer :
   diff test.bin receptions/test.bin && echo OK
