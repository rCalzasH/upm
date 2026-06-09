
#include "arcos.h"
#include <stdio.h>
#include <stdlib.h>

void leer_arcos(arco_t e[], int* psize) {
  int u, v;
  *psize = 0;
  scanf("%d %d", &u, &v);
  while (u >= 0 && v >= 0) {
    e[*psize].u = u;
    e[*psize].v = v;
    *psize += 1;
    scanf("%d %d", &u, &v);
  }
}

arco_t* d_leer_arcos(const char* filename, int* psize) {
  int u, v, ok;
  int capacidad = 8;
  arco_t* e;
  FILE* f = fopen(filename, "r");
  *psize  = 0;
  if (f == NULL) {
    fprintf(stderr, "No se puede abrir el archivo\n");
    return NULL;
  }
  fprintf(stderr, "Se abre '%s'\n", filename);
  e  = calloc(capacidad, sizeof(arco_t));
  ok = fscanf(f, "%d %d", &u, &v);
  while (ok == 2 && u >= 0 && v >= 0) {
    if (*psize + 1 > capacidad) {
      capacidad *= 2;
      e = realloc(e, capacidad * sizeof(arco_t));
    }
    e[*psize].u = u;
    e[*psize].v = v;
    *psize += 1;
    ok = fscanf(f, "%d %d", &u, &v);
  }
  fclose(f);
  return e;
}