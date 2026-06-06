/*proyecto2 recuperacion junio2025 ;), es mi problema */
#ifndef AGENDA_H
#define AGENDA_H
#include "contacto.h"
/*la vamos a definir como un tamaño maximo de 100*/
#define MAX_TAMANIO 100
typedef struct {
    Contacto *agenda[MAX_TAMANIO];
    int indice; /*el indice del ultimo contacto guardado*/
} Agenda;
/*los metodos de agenda */
Agenda* crear_agenda();
void aniadir_contacto(Agenda *a, Contacto *c );
void borrar_contacto(Agenda *a, Contacto *c);
int busca_contacto(Agenda *a, Contacto *c);/*implementado de manera que si esta encontrado devuelve el indice de donde esta,y si no devulve -1*/
int agendas_iguales(Agenda *a1, Agenda *a2);
Contacto *obtener(Agenda *a, int i);

/*termminamos las guardas de inclusion*/
#endif