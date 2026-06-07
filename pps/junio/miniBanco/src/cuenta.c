/*proyecto3 recuperacion junio2025 ;), es mi problema */
/*empezamos incluyendo las librerias del sistema de C*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
/*librerias del proyecto*/
#include "../includes/cuenta.h"

/*declaracion de funciones auxiliares*/
static void copiar_Array(char *num1, char *num2);

static void copiar_Array(char *num1, char *num2){
    size_t i = 0;
    while(i < strlen(num1)){
        num1[i] = num2[i]; /* copia el numero 2 en el numero 1 */
        i++; 
    } 
    num1[strlen(num1)] = '\0'; 
}

static int esNums(char *n, int i);

int esNums( char *num, int i ) {
    int in =i; /*devuelde 0 si todo el array es numerico, depsués de un indice */
    while (num[in] != '\0') {
        if (!isdigit(num[in])) {
            return -1;
        }
        in++;
    }
    return 1;
}

static int iban_valido(char n[25]);

static int iban_valido(char n[25]){ 
    /*revisamos todas las condiciones para que iban sea valido, deuvelve 1(TRUE) y 0(FALSE)*/
    return strlen(n)==24 && isupper(n[0]) && isupper(n[1])  && esNums(n,2)? 1:0;
}
/*incicializacion de una cuenta */
Cuenta *cear_cuenta(char *t, char n[25], double s){
    Cuenta *c = calloc(1,sizeof(Cuenta));
    if(t==NULL || !iban_valido(n)){
        printf(MSG_NO_VAL_PARAM);
        return NULL;
    }
    /*comprobamos que se puede asignar la memoria dinamica*/
    if(c==NULL){
    perror(MSG_MEM_DIN);
    return NULL;
    }
    /*comprobamos que se puede asignar la memoria dinamica*/
    c->titular=calloc(1,strlen (t)+1);
    if(c->titular==NULL){
    perror(MSG_MEM_DIN);
    free(c);
    return NULL;
    }
    /*copiamos los valores*/
    strcpy(c->titular,t);
    /*asignamos numero de cuenta */
    copiar_Array(c->num_C, n);
    c->saldo=s;
    c->histCred=1;/*al crear la cuenta el historial crediticio es bueno siempre*/
    return c;
}
/*liberacion en cascada*/
void eliminar_cuenta(Cuenta *c){
    if(c==NULL){
        printf(MSG_NO_VAL_PARAM);
        return;
    }
    free(c->titular);
    free(c);
}
/**/
void aniadir_saldo(Cuenta *c, double s){
    if(c==NULL){
        printf(MSG_NO_VAL_PARAM);
        return;
    }
    c->saldo+=s;
}
void restar_saldo(Cuenta *c, double s ){
    if(c==NULL){
        printf(MSG_NO_VAL_PARAM);
        return;
    }
    /*compruebo tb si hay saldo sufieceinte para poder retirar*/
    if(s>c->saldo){
        printf("SALDO INSUICIENTE");
        return;
    }
    c->saldo-=s;
}
/*hace transferencia desde c a v de un saldo s */
void hacer_transferencia(Cuenta *c, Cuenta *v, double s){
    if(c==NULL || v==NULL){
        printf(MSG_NO_VAL_PARAM);
        return;
    }
    /*compruebo saldo suficiente*/
    if(s>c->saldo){
        printf("SALDO INSUICIENTE");
        return;
    }
    /*actualizamos saldos*/
    aniadir_saldo(v,s);
    restar_saldo(c,s);
    return;
}
char *cuenta_texto( Cuenta *c) {
    char *buffer = calloc(256 , sizeof(char));
    if (buffer == NULL) {
        perror(MSG_MEM_DIN);
        return NULL;
    }
    sprintf(buffer, "IBAN: %s | Titular: %s | Saldo: %.2f EUR | HistorialCrediticio: %d",
             c->num_C, c->titular, c->saldo, c->histCred);
    return buffer;
}

void imprimir_cuenta(Cuenta *c) {
    char *str = cuenta_texto(c);
    if (str == NULL) return;
    printf("%s\n", str);
    free(str);  
}
/*TRUE(1) FALSE(0)*/
int misma_cuenta(Cuenta *c, Cuenta *v){
    if((c == NULL && v != NULL) || (v == NULL && c != NULL)) return 0;
    if(c == NULL && v == NULL) return 1;
    return strcmp(c->titular, v->titular)==0 && strcmp(c->num_C, v->num_C)==0 && c->saldo==v->saldo && c->histCred==v->histCred ? 1:0;
}
int es_titular(Cuenta *c, char *t){
    if(c==NULL){
        printf(MSG_NO_VAL_PARAM);
        return 0;
    }
    return strcmp(c->titular, t)?1:0;
}
int es_buen_pagador(Cuenta *c){
    if(c==NULL){
        printf(MSG_NO_VAL_PARAM);
        return 0;
    }
    return c->histCred;
}
int es_iban_cuenta(Cuenta *c, char ib[25]){
    if(c==NULL){
        printf(MSG_NO_VAL_PARAM);
        return 0;
    }
    return strcmp(c->num_C, ib)?1:0;
}
