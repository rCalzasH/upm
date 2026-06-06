/*incluimos las librerias estandar de c que vamos a usar */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
/*de aqui se incluyen despues los ficheros relativos al proyecto o las cabeceras de terceros*/
#include "../includes/contacto.h"



static void copiar_Array(char num1[9], char num2[9]);

static void copiar_Array(char num1[9], char num2[9]){
    int i = 0;
    while(i < 8){
        num1[i] = num2[i]; /* copia el numero 2 en el numero 1 */
        i++; 
    } 
    num1[8] = '\0'; 
}

Contacto *crear_contacto(char *n, char num[9], char *em){
    Contacto *c;/*declaracion de cariabkes arriba ya que no se puede mezclar con el códgio*/
    /* Validar parámetros de entrada */
    if (n == NULL || num == NULL || em == NULL) return NULL;

    /* reservamos memoria para el puntero al nuevo contacto */
    c = malloc(sizeof(Contacto));
    if(c == NULL){
        perror(MSG_MEM_DIN);
        return NULL;
    }
    /*+1 para reservar memoria ya que tenemos en cuenta el caracater nulo del cierre de cadena*/
    c->nombre = malloc(strlen(n) + 1);
    if(c->nombre == NULL){
        perror(MSG_MEM_DIN);
        free(c); 
        return NULL;
    }
    strcpy(c->nombre, n); 
    
    /* copiamos el numero */
    copiar_Array(c->numero, num);
    
    c->email = malloc(strlen(em) + 1);
    if(c->email == NULL){
        perror(MSG_MEM_DIN);
        free(c->nombre);
        free(c);
        return NULL;
    }
    strcpy(c->email, em); 
    /*devolvemos el puntero creado */
    return c; 
}

void contacto_imprimir(Contacto *c){
    if(c == NULL){
        fprintf(stdout, "%s\n", MSG_NO_VAL_PARAM);
        return;
    }
    fprintf(stdout, "la informacion del contacto es: Nombre: %s; Numero: %s; email: %s \n", c->nombre, c->numero, c->email);
}

/* metodo para eliminar un contacto */
void eliminar_contacto(Contacto *c){
    if(c == NULL){
        fprintf(stdout, "%s\n", MSG_NO_VAL_PARAM);
        return;
    }
    free(c->nombre); /* c->numero NO se libera con free() porque es un array estático dentro del struct (char numero[9]), vive y muere con el struct*/
    free(c);
}

/* copiamos or en dest */
Contacto* copiar_contacto(Contacto *dest, Contacto *or){
    if(dest == NULL || or == NULL){
        fprintf(stdout, "%s\n", MSG_NO_VAL_PARAM);
        return NULL;
    }
    /*TODO revisar gestion de memoria cuando hago las copias, ua qie puede ser que tengan tamaños muy distintos*/
    strcpy(dest->nombre, or->nombre);
    strcpy(dest->email, or->email);
    copiar_Array(dest->numero, or->numero); 

    return dest;
}

/* nuestro método para poder comparar contactos */
int mismo_contacto(Contacto *c1, Contacto *c2){
    if((c1 == NULL && c2 != NULL) || (c2 == NULL && c1 != NULL)) return -1;
    if(c1 == NULL && c2 == NULL) return 0;
    return (strcmp(c1->nombre, c2->nombre) == 0 &&  strcmp(c1->email, c2->email) == 0 && strcmp(c1->numero, c2->numero) == 0) ? 0 : -1;
}