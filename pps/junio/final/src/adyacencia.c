#include <stdio.h>
#include <stdlib.h>
#include "../includes/adyacencia.h"
/*empezamos a definir las funciones*/

size_t num_nodos(arco_t * e, int size){
  
  if(e==NULL){
    printf(MSG_NO_VAL_PARAM);
    return -1;/*devolvemos un numero negativo para simbolizar el fallo*/
  }
  int i=0;
  size_t N=0;/*siguiendo la nomenclatura del enunciado y simplemente calcando lo que dice */
  while(i<size){
    N = N<e[i]->u ? e[i]->u:N;
    /*comprobamos que el nodo que tenemos guardado es realmente el menor de entre los origenes y destinos*/
    N = N<e[i]->v ? e[i]->v:N;
    i++;
  }
  return N+1;
} 
/****************************************************************************************************/ 


unsigned char** adyacencia(arco_t* e, int size, size_t* pN) {
  if(e==NULL || pN==NULL){
    printf(MSG_NO_VAL_PARAM);
    return NULL;
  }
  unsigned char** matriz;
  int i;
  size_t s;
  *pN = num_nodos(e, *size);
  matriz = calloc(*pN, sizeof(unsigned char*));
  if(matriz==NULL){
    perror(MSG_NO_ME_DIN);
    return NULL;
  }
  for (s = 0; s < *pN; ++s) {
    matriz[s] = calloc(*pN, sizeof(unsigned char));
    if(matriz[s]==NULL){
      for(i=0;i<s;i++){
        free(matriz[i]);
      }
      free(matriz);
      perror(MSG_NO_MEM_DIN);
      return NULL;
    }
  }
  for (i = 0; i < size; ++i) {
    matriz[e[i].u][e[i].v] = 1;
  }
  return matriz;
}

int es_bidireccional(unsigned char ** A, size_t N){
  /*comprobar que A esta corrrectamente incializada*/
  int i,j;
  if(A==NULL){
    printf(MSG_NO_VAL_PARAM);
    return -1;
  }
  sim=1;
  for(i=0,j=0;i<N && sim==1 ;i++){
    while(j<N&&sim==1){
      if(!A[i][j]== A[N-i][N-j])sim=0;
      j++;
    }
  }
    return sim;
}

void liberar(unsigned char ** A, size_t N){
  int i,j;
  for(i=0,j=0;i<N;i++){
    while(j<N){
      free(A[i][j])
      j++;
    }
    free(A[i]);
  }
  free(A);
  return;
} 

