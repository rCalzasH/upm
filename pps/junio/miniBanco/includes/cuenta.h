/*proyecto3 recuperacion junio2025 ;), es mi problema */
/*empezamos la guarda de inclusion*/
#ifndef CUENTA_H
#define CUENTA_H
typedef struct {
    char *titular;
    char num_C[25];/*definimos el IBAN de la cuenta donde las dos primeras posiciones son letras*/
    double saldo;
    int histCred;/*Si es bueno TRUE(1) y si es malo FALSE(0) */

}Cuenta;
/*macros de mensajes de error*/
#define MSG_MEM_DIN "No se pudo reservar/realocar la memoria dinamica"
#define MSG_NO_VAL_PARAM "Los parametros no son válidos para esta operacion"
/*operaciones de cuenta*/
Cuenta *crear_cuenta(char *t, char n[25], double *s, int h);
void eliminar_cuenta(Cuenta *c);
void aniadir_saldo(Cuenta *c, double s);
void restar_saldo(Cuenta *c, double s);
void hacer_transferecnia(Cuenta *c, Cuenta *v, double s);/*hace transferenciua de saldo de c a v */
void imprimir_cuenta(Cuenta *c);
char *cuenta_texto( Cuenta *c);
int misma_cuenta(Cuenta *c, Cuenta *v);
/*verficiar informacion*/
int es_titular(Cuenta *c, char *t);
int es_buen_pagador(Cuenta *c);
int es_iban_cuenta(Cuenta *c, char ib[25]);
/*terminamos la guarda de inclusion*/
#endif