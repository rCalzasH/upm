/*proyecto3 recuperacion junio2025 ;), es mi problema */
/*libs sistema */
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
/*librerias de proyacto*/
#include "../includes/banco.h"

static void copiar_Array(Cuenta **cuentas, Cuenta **cuentas2);

static void copiar_Array(Cuenta **cuentas, Cuenta **cuentas2){
    int i = 0;
    while(cuentas2[i]!=NULL){
        cuentas[i] = cuentas2[i]; /* copia el numero 2 en el numero 1 */
        i++; 
    } 
    return; 
}
static void reorganizar_cuentas(Banco *b, int i);

/*asumiendo que como siempre la voy a llamar en cuanto termino de eliminar el contacto, nunca va se me va a colar un NULL, sabiendo que además lo compruebo en el aniadir_contacto()*/
static void reorganizar_cuentas(Banco *b, int i ){
    while(i < b->indice-1){
        b->cuentas[i]=b->cuentas[i+1];
        i++;
    }
    b->indice--;
}
/*inciializador de banco*/
Banco *crear_Banco(char* s){
    if(s==NULL){
        printf(MSG_NO_VAL_PARAM);
        return NULL;
    }
    Banco *b = calloc(1, sizeof(Banco));
    if(b==NULL){
        perror(MSG_MEM_DIN);
        return NULL;
    }
    b->direccionSu=calloc(1,strlen(s));
    if(b->direccionSu==NULL){
        perror(MSG_MEM_DIN);
        free(b);
        return NULL;
    }   
    
    strcpy(b->direccionSu,s);
    b->indice=0;
    return b;
}
void eliminar_banco(Banco *b){ 
    int i=0;
    if(b==NULL){
        printf(MSG_NO_VAL_PARAM);
        return;
    }
    while(b->cuentas[i]!=NULL){
        eliminar_cuenta(b->cuentas[i]);
        i++;
    }
    free(b);
    return;
}

void aniadir_cuenta(Banco *b,Cuenta *c ){ 
    if(b==NULL || c==NULL){
        printf(MSG_NO_VAL_PARAM);
        return;
    }
    if(esta_cuenta(b,c)==-1)return;
    b->cuentas[b->indice]=c;
    b->indice++;
}
void eliminar_cuenta_B(Banco *b,Cuenta *c ){
    int iA;
    if(b==NULL || c==NULL){
        printf(MSG_NO_VAL_PARAM);
        return;
    }
    /*cuidado*/
    iA=esta_cuenta(b,c);
    if(iA==-1){
        printf(MSG_NO_VAL_PARAM);
        return;
    }/*la cuenta no estaba en el banco */
    eliminar_cuenta(b->cuentas[iA]);
    reorganizar_cuentas(b, iA);
    return;
}
/*traspasa la cuenta de b a v */
void traspasar_cuenta(Banco *b, Banco *v, Cuenta *c){
    int i;
    if(b==NULL || v==NULL || c==NULL){
        printf(MSG_NO_VAL_PARAM);
        return;
    }
    i=esta_cuenta(b,c);
    if(i== -1){
        printf("La cuenta C, no esta en el banco B");
        return;
    }
    eliminar_cuenta_B(b,c);
    aniadir_cuenta(v,c);
    return;
}
int copiar_banco(Banco *b, Banco *v){
     if(b==NULL || v==NULL){
        printf(MSG_NO_VAL_PARAM);
        return -1;
    }
    /*comprobamos que no son el mismo ya */
    if(mismo_banco(b,v)==1) return 1;
    /*copiando el array*/
    copiar_Array(b->cuentas, v->cuentas);
    strcpy(b->direccionSu, v->direccionSu);
    b->indice=v->indice;
    return  1;
}
/*TRUE(1) FALSE(0)*/
int mismo_banco(Banco *b, Banco *v){
    int boolIg=1;
    int i;  
    if(b==NULL || v==NULL){
        printf(MSG_NO_VAL_PARAM);
        return -1;
    }
    if(b->indice!=v->indice || strcmp(b->direccionSu, v->direccionSu)!=0)return 0;
    while(i<b->indice && boolIg!=0){
        boolIg=misma_cuenta(b->cuentas[i],v->cuentas[i]);
        i++;
    }
    return boolIg;


}
int esta_cuenta(Banco *b, Cuenta *c){
    
}/*devuele -1 si FALSE y >=0 si true */
char* imprimir_banco(FILE *f,Banco *b );/*DUUUUURO*/
void muestra_banco(Banco *b);
