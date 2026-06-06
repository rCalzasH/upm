/*proyecto2 recuperacion junio2025 ;), es mi problema */
/*incluimos todas las librerias estandar de C que vamos a usar*/
#include <stdio.h>
#include <stdlib.h>
/*incluimos aqui las librerias de proyecto*/
#include "../includes/agenda.h"
/*cabecera y defincion de funcion*/
static void reorganizar_agenda(Agenda *a, int i);

/*asumiendo que como siempre la voy a llamar en cuanto termino de eliminar el contacto, nunca va se me va a colar un NULL, sabiendo que además lo compruebo en el aniadir_contacto()*/
static void reorganizar_agenda(Agenda *a, int i ){
    /*TODO actualizar el ultimo indice real del ultimo elemento*/
    while(i < a->indice-1){
        a->agenda[i]=a->agenda[i+1];
        i++;
    }
    a->indice--;
}
/*al crear la genda esta vacia */
Agenda* crear_agenda(){
    Agenda *a;
    a = calloc(MAX_TAMANIO, sizeof(Contacto *));/*hacemos un calloc para asegurarnos de que el heap en el que vamos a reservar esta vacio*/
    if(a==NULL){
        perror(MSG_MEM_DIN);
        return NULL;
    }
    a->indice=0;
    return a; 
}
/*el correcto funcionamiento de este metodo realmente depende de reorganizar_agenda*/
void aniadir_contacto(Agenda *a, Contacto *c){
    if(a==NULL || c==NULL){
        fprintf(stdout, "%s\n", MSG_NO_VAL_PARAM);
        return;
    }
    /*control de como de llena esta la agenda */
    if(a->indice +1 >= MAX_TAMANIO){
        fprintf(stdout, "%s\n", MSG_NO_VAL_PARAM);
        printf("La agenda A esta llena");
        return;
    }
    a->agenda[a->indice]=c;
    a->indice++;
    return;
}
  void borrar_contacto(Agenda *a, Contacto *c){
    int indAux;
    if(a==NULL || c==NULL){
        fprintf(stdout, "%s\n", MSG_NO_VAL_PARAM);
        return;
    }
    indAux=busca_contacto(a,c);
    if(indAux==-1){
        fprintf(stdout, "%s\n", MSG_NO_VAL_PARAM);
        printf("El contacto C no esta en la agenda A");
        return;
    }
    a->agenda[indAux]=NULL;
    reorganizar_agenda(a, indAux);
    return;
}
int busca_contacto(Agenda *a, Contacto *c){
    int iAux=0;
    int encontrado=-1;/*booleano econtrado TRUE(0), FALSE(-1)*/
    if(a==NULL || c==NULL){
        fprintf(stdout, "%s\n", MSG_NO_VAL_PARAM);
        return -1;
    }
    while(iAux<a->indice && encontrado!=0){
        encontrado=mismo_contacto(a->agenda[iAux],c);
        iAux++;
    }
    return encontrado;
}
int agendas_iguales(Agenda *a1, Agenda *a2){
    int i=0;
    int iguales=0;
    if(a1->indice != a2->indice) return -1;
    while(i<a1->indice && iguales==0){
        iguales=mismo_contacto(a1->agenda[i], a2->agenda[i]);
        i++;
    }
    return iguales;
}
Contacto *obtener(Agenda *a, int i){
      if(a==NULL || i<0){
        fprintf(stdout, "%s\n", MSG_NO_VAL_PARAM);
        return NULL;
      }
      return a->agenda[i];
}