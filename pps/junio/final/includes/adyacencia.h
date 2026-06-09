/*final de enero2025 para repasar: 
 * Autor: rCalzas;)
*/
#ifndef ADYACENCIA_H
#define ADYACENCIA_H
#include "arcos.h"
size_t num_nodos(arco_t * e, int size); 
/*(1 Puntos) Calcula y retorna el número de nodos, N , en un grafo dado por el
conjunto de arcos en el array e. Siendo size el número de arcos.
1. Los nodos del grafo se numeran en el intervalo 0..N − 1, siendo N el número de nodos.
2. El número de nodos del grafo se puede calcular encontrando el máximo valor de todos los nodos origen y destino (en el
array e). Es decir, N = max(i ⊂ V ) + 1.*/

unsigned char** adyacencia(arco_t * e, int size, size_t * pN); 
/*(1.5 Puntos) Genera una matriz dinámica que guar-
da la matriz de adyacencia de un grafo dado por el conjunto de arcos en el array e. Siendo size el número de arcos. Guarda en
el entero apuntado por pN el número de nodos del grafo.
1. Su matriz de adyacencia de un grafo es una matriz A de tamaño N × N , siendo N el número de nodos, donde A[u][v] = 1
si existe el arco entre los nodos u y v (arco u → v) y 0 en caso contrario.
2. La matriz dinámica debe formarse como un array de punteros, cada uno de ellos apuntando a una fila de la matriz.
3. Se recuerda que el tipo unsigned char se puede usar como un entero sin signo cuyo tamaño es un byte, se usa como
número no como letra.*/

int es_bidireccional(unsigned char ** A, size_t N);
/*
(1 Puntos) Retorna 1 (verdadero) si se cumple que el grafo dado
por la matriz de adyacencia A y número de nodos N, es bidireccional. Retorna 0 (falso) en caso contrario. Un grafo es bidireccional
si se cumple que para cualquier arco u → v existe el arco v → u. Es decir, la matriz de adyacencia es simétrica.*/

void liberar(unsigned char ** A, size_t N); 
/*(0.5 Punto) Libera la memoria dinámica reservada para una matriz de
adyacencia que se haya generado usando la función adyacencia. Donde A es la matriz dinámica y N el número de nodos.
*/
/*terminamos la guarda de inclusion*/
#endif
