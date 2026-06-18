/*AUtor: rCalzas; Nmat=240141; Practica adaByron en C*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../includes/contando_ovejas"
/*emepzamos a definir la funcion */
void leer_ovejas(int T, char**s){
  char buffer[MAX];
  int i;
  for(i=0;i<T;i++){
    /*sabemos que T son las lineas que vamos a leer */
    if(scanf("%s", buffer)==NULL){
      printf("la linea no se ha leido correctamente");
      return;
    }
    s[i]=calloc(1*strlen[buffer]);
    if(s[i]==NULL)return;
    strcpy(s[i],buffer);
    return;
  } 
  
}
int []contando_ovejas(char **s, char *ovejas,int T){
  int nums[2];
  int cOv=0;/*contador de reinicio de  ovejas*/ 
  int secMax=0;
  int secMaxP=0;
  int j=0;
  int i=0;
  while(i<T){
    while(j<strlen(s[i])){
      if(strch(s[i][j],ovejas)==NULL){
        s[i][j]=ovejas[j];
        secMaxP++;
      }
      else{
        cOv++;
        secMax = secMaxP>secMax ? secMaxP : secMax;
      }
      j++;
  }
    i++;
  }
  nums[0]=cOv;
  nums[1]=secMax;
  return nums;
}
