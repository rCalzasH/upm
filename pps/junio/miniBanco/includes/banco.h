/*proyecto3 recuperacion junio2025 ;), es mi problema */
/*empezamos la guarda de inclusion*/
#ifndef BANCO_H
#define BANCO_H
/*MACRO tamnio maximo del numCUentas*/
#define MAX_TAMANIO 100 
/*libs sistema */
#include <stdlib.h>
#include <stdio.h>
#include "cuenta.h"
typedef struct{
    Cuenta *cuentas[MAX_TAMANIO];
    char *direccionSu;
    int indice;

}Banco;

/*funciones de banco */
Banco *crear_Banco(char* s);
void eliminar_banco(Banco *b);
void aniadir_cuenta(Banco *b,Cuenta *c );
void eliminar_cuenta_B(Banco *b,Cuenta *c );
void traspasar_cuenta(Banco *b, Banco *v, Cuenta *c);
int copiar_banco(Banco *b, Banco *v);
int esta_cuenta(Banco *b, Cuenta *c);/*devuele -1 si FALSE y >=0 si true */
void imprimir_banco(FILE *f,Banco *b );
void muestra_banco(Banco *b);
int mismo_banco(Banco *b, Banco *v);/*TRUE(1) FLASE (0)*/

#endif
