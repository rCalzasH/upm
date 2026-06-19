/*AUtor: rCalzas; Nmat=240141; Practica adaByron en C*/
#include <stdio.h>
#include <stdlib.h>          /* stlib.h → stdlib.h */
#include <string.h>
#include "../includes/contando_ovejas.h"

int main(int argc, char *argv[]) {
    char buffer[MAX];
    int T;
    int i;
    char **s;
    char *ovejas = "baa";    /* define aquí qué caracteres son "ovejas" */

    /* VLA prohíbido en C90 → malloc */
    FILE **files = NULL;

    scanf("%d", &T);

    s = calloc(T, sizeof(char *));
    if (s == NULL) return -1;

    if (argc > 1) {
        files = calloc(argc - 1, sizeof(FILE *));
        if (files == NULL) { free(s); return -1; }

        for (i = 1; i < argc; i++) {        /* argv[0] es el ejecutable */
            files[i - 1] = fopen(argv[i], "r");   /* aargv → argv */
            if (files[i - 1] == NULL) {
                /* liberar lo ya abierto */
                int j;
                for (j = 0; j < i - 1; j++) fclose(files[j]);
                free(files);
                free(s);
                return -1;
            }
            leer_ovejas_fichero(files[i - 1], T, s, buffer);
        }

        for (i = 0; i < argc - 1; i++) fclose(files[i]);  /* flcose → fclose */
        free(files);
    } else {                               /* else suelto → ahora va con el if */
        leer_ovejas(T, s, buffer);
    }

    contando_ovejas(s, ovejas, T);

    for (i = 0; i < T; i++) free(s[i]);
    free(s);
    return 0;
}
