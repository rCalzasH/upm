/*final de enero2025 para repasar: 
 * Autor: rCalzas;)
*/
#ifndef ARCOS_H
#define ARCOS_H 
/*definimos las macros de mensajes de error por comodidad*/
#define MSG_NO_VAL_PARAM "parametro/s de entrada no valido/s"
#define MSG_NO_MEM_DIN "no se gestiono la memoria de manera adecuada (error en alocacion o realocacion)"

/*definitmos arcos_t*/
typdef struct{i
  int u;/*nodo de origen */
  int v;/*nodo de destino*/
}arco_t;/*nodo de destino*/


void leer_arcos(arco_t e[], int * psize); 
/*(2 Puntos) Lee un conjunto de arcos de la entrada estándar y lo guarda en
el array e. El número de arcos se guarda en el entero apuntado por psize.
1. En la entrada estándar, para cada arco, se escriben el nodo origen, un espacio y el correspondiente nodo destino, seguido
por un salto de línea.
2. La lectura termina cuando alguno de los nodos es negativo, es decir, u ó v son negativos.*/

arco_t* d_leer_arcos(const char * filename, int * psize); 
/*(2 Puntos) Lee un conjunto de arcos y lo guarda en un
array dinámico que se retorna como resultado. Los datos se leen desde un archivo cuya ruta es filename. El número de arcos se
guarda en el entero apuntado por psize.
1. La lectura de los arcos debe acabar si alguno de los nodos es negativo, si se produce algún error en el formato de entrada
o se alcanza el final de archivo. El formato es igual que en la función anterior.
2. El tamaño inicial del array dinámico se fijará en 8 arcos, cada vez que el número de arcos supere la capacidad del array
se duplicará la capacidad del mismo (con realloc, estrategia de duplicación).*/
#endif
