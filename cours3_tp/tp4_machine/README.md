TP4 — Machine à états

États :
- CONNECTE    : LOGIN uniquement
- AUTHENTIFIE : SEND_FILE, RESUME
- TRANSFERANT : CHUNK, END_FILE
- TERMINE     : aucun

Consignes :
1. Compiler tous les fichiers.
2. Lancer ServeurProto.
3. Lancer ClientProto.
4. Observer les réponses.

Résultat attendu :
[avant login]   ERROR id=1 LOGIN attendu
[login]         ACK   id=2
[second login]  ERROR id=3 SEND_FILE ou RESUME attendu
[send_file]     ACK   id=4
