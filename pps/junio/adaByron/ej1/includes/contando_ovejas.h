/*AUtor: rCalzas; Nmat=240141; Practica adaByron en C*/
#ifndef CONTANDO_OVEJAS_H
#define CONTANDO_OVEJAS_H

#define MAX 126

int leer_ovejas_fichero(FILE *f, int T, char **s, char buffer[]);
int leer_ovejas(int T, char **s, char buffer[]);
int *contando_ovejas(char **s, char *ovejas, int T);

#endif
