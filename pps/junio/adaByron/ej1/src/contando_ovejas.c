/*AUtor: rCalzas; Nmat=240141; Practica adaByron en C*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../includes/contando_ovejas.h"  

int leer_ovejas_fichero(FILE *f, int T, char **s, char buffer[]) {
    int i;
    if (f == NULL || s == NULL) return -1;

    for (i = 0; i < T; i++) {
        if (fscanf(f, "%s", buffer) != 1) {
            printf("la linea no se ha leido correctamente\n");
            return -1;
        }
        s[i] = calloc(strlen(buffer) + 1, sizeof(char));
        if (s[i] == NULL) return -1;  
        strcpy(s[i], buffer);
    }
    return 0;  /* estaba dentro del for → salía tras la primera iteración */
}

int leer_ovejas(int T, char **s, char buffer[]) {
    int i;
    for (i = 0; i < T; i++) {
        if (scanf("%s", buffer) != 1) {
            printf("la linea no se ha leido correctamente\n");
            return -1;
        }
        s[i] = calloc(strlen(buffer) + 1, sizeof(char));
        if (s[i] == NULL) return -1;
        strcpy(s[i], buffer);
    }
    return 0;
}

/* Devuelve puntero a array estático con [nOvejas, rachaMaxima] */
int *contando_ovejas(char **s, char *ovejas, int T) {
    int cOv = 0;
    int secMax = 0;
    int secMaxP = 0;
    int i, j;

    for (i = 0; i < T; i++) {
        j = 0;
        while (j < (int)strlen(s[i])) {
            if (strchr(ovejas, s[i][j]) != NULL) {
                cOv++;
                if (secMaxP > secMax) secMax = secMaxP;
                secMaxP = 0;
            } else {
                secMaxP++;
            }
            j++;
        }
    }
    /* última racha pendiente de comparar */
    if (secMaxP > secMax) secMax = secMaxP;

    nums[0] = cOv;
    nums[1] = secMax;
    fprintf(stdout, "%d %d\n", nums[0], nums[1]);
    return nums;
}
