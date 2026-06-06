/*proyecto1 recuperacion junio2025 ;), es mi problema */

/*aquí incluimos: las librerías del sistema siempre van entre <> y los archivos propios van con "", CUIDADO!! hay que poner la ruta relativa del archiuvo propio que queremos incluir */
#include "calculadora.h"
#include <stdio.h>
#include <stdlib.h>

/*Mensaje de error*/
#define NO_DIVIDIR_0 "Error: NO se puede dividir por 0 garrulo\n"
/*declaramos la funcion auxiliar que es "privada de este fichero"*/
static int esCero(double y);
double suma (double x, double y){
    return x+y;
}
static int esCero(double y);
double resta (double x, double y){
    return x-y;
}

double multiplica (double x, double y){
    return x*y;
}

double divide (double x, double y){
    if(esCero(y)==-1){
        fprintf(stderr, NO_DIVIDIR_0);
        return -1.0;
    }
    return x/y;
}/*recordamos que es simplemenete el -1 es un resultado simbolico apra representar error */

static int esCero(double y){
    if(y==0.0)return -1;/*si el numero00 es 0 devuelvo -1*/
    return 1;/*si el numero no es cero devuelvo 1 */
}