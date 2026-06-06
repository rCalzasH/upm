/*proyecto2 recuperacion junio2025 ;), es mi problema */
#ifndef CONTACTO_H
#define CONTACTO_H
/*definimos la estructura de datos contactos, el equivalente a la clase de contacto.java*/
/*vamos a usar los mismos mensajes de error para ser consistentes */
#define MSG_MEM_DIN "No se pudo reservar/realocar la memoria dinamica"
#define MSG_NO_VAL_PARAM "Los parametros no son válidos para esta operacion"
typedef struct {
    char* nombre;/*tamaño varibale así que mejor un puntero*/
    char numero[9]; /*sin el prefijo*/
    char* email;/*tamaño varibale así que mejor un puntero*/
} Contacto; /*como he definido el tipo con typedef puedo declarar variables de ese mismo tipo sin que sean Struct */ 

Contacto *crear_contacto(char *n, char num[9], char *em);/*incializa todos los campos del struct con los parametros de entrada*/
void contacto_imprimir(Contacto *c);
void eliminar_contacto(Contacto *c);/*libera uno a uno los campos del struct, y posteriormente el puntero */
Contacto *copiar_contacto(Contacto *dest, Contacto *or);/*algoritmo de copia profunda para el correcto manejo de memoria con punteros, copiando or en dest*/
int mismo_contacto(Contacto *c, Contacto *v);/*el equivalente a un equals*/
/*acabamos la guarda de inclusion*/
#endif
