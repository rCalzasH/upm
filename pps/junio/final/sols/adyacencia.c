
#include "adyacencia.h"
#include <stdio.h>
#include <stdlib.h>

size_t num_nodos(arco_t* e, int size) {
  int i;
  int N = 0;
  for (i = 0; i < size; ++i) {
    if (e[i].u > N) {
      N = e[i].u;
    }
    if (e[i].v > N) {
      N = e[i].v;
    }
  }
  return N + 1;
}

unsigned char** adyacencia(arco_t* e, int size, size_t* pN) {
  unsigned char** A;
  int i;
  size_t s;
  *pN = num_nodos(e, size);
  A   = calloc(*pN, sizeof(unsigned char*));
  for (s = 0; s < *pN; ++s) {
    A[s] = calloc(*pN, sizeof(unsigned char));
  }
  for (i = 0; i < size; ++i) {
    A[e[i].u][e[i].v] = 1;
  }
  return A;
}

int es_bidireccional(unsigned char** A, size_t N) {
  size_t i, j;
  int res = 1;
  for (i = 0; i < N && res; ++i) {
    for (j = i + 1; j < N && res; ++j) {
      res = A[i][j] == A[j][i];
    }
  }
  return res;
}

void liberar(unsigned char** A, size_t N) {
  size_t s;
  for (s = 0; s < N; ++s) {
    free(A[s]);
  }
  free(A);
}
