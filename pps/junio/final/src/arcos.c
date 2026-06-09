/*final de enero2025 para repasar: 
 * Autor: rCalzas;)
*/
#include <stdio.h>
#include <stdlib.h>
#include "../includes/arcos.h"
/*empezamos a definir funciones*/

void leer_arcos(arco_t e[], int * psize){
  /*leemos desde la entrada y mandamos los arcos a e*/
  if(e==NULL){
    printf(MSG_NO_VAL_PARAM);
    return;
  }
  psize=0;
  while(scanf(%d, %d, e[*psize]->u,e[*psize]->v)==2){
    if(e[pszie]->u<=0 || e[psize]->v<=0)return;
    psize++;
  }
  return;
} 

arco_t* d_leer_arcos(const char * filename, int * psize){
  /*intentamos abrir el archivo en modo escritura*/
  FILE *f fopen(filename, "r");
  if(f==NULL){
    printf(MSG_NO_VAL_PARAM);
    return NULL;
  }
  /*incializamos el array de arcos*/
  int noNeg=1;/*FLASE(0), TRUE(1)*/
  int tam=8;/*tamanio que dice el enunciado como inicial*/
  arco_t *arcos;
  arcos = calloc(tam,sizeof(arco_t));
  if(arcos==NULL){
    perror(MSG_NO_MEM_DIN);
    return NULL;
  }
  /**/
  while(fscanf(f,%d, %d, arcos[*psize]->u,arcos[*psize]->v)==2 && noNeg==1){
    if(arcos[*pszie]->u<=0 || arcos[*pszie]->v<=0){
      noNeg=1;/*comprobamos que ningun campo es negativo, y si lo hay, nos slaimos*/
      free(arcos);
      return NULL;
    }
    psize;
    if(psize==tam){
      tam*=2;
      if(realloc(arcos,sizeof(arcos)*tam)==NULL);
      perror(MSG_NO_MEM_DIN);  
      free(arcos);
      return NULL;
    }
  }
  fclose(f);
  return arcos;
} 
