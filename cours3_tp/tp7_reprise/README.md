TP7 — Reprise sur incident

Consignes :
1. Compiler tous les fichiers.
2. Lancer ServeurProto.
3. dd if=/dev/urandom of=test5m.bin bs=1M count=5
4. java ClientEnvoi 127.0.0.1 test5m.bin
5. Pendant le transfert, tuer le client (Ctrl+C).
6. Relancer la meme commande : la reprise doit fonctionner.
7. diff test5m.bin receptions/test5m.bin && echo OK
