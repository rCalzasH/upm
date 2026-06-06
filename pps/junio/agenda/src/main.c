/*proyecto2 recuperacion junio2025 ;), es mi problema */
#include <stdio.h>
#include <stdlib.h>
#include "../includes/agenda.h"  /* agenda.h ya incluye contacto.h */

/* Separador visual para que los prints sean legibles */
static void separador(const char *titulo) {
    printf("\n========================================\n");
    printf("  %s\n", titulo);
    printf("========================================\n");
}

int main(void) {
    /* C90: TODAS las variables declaradas aqui arriba, antes de cualquier instruccion */
    Contacto *c1;
    Contacto *c2;
    Contacto *c3;
    Contacto *c4;
    Contacto *c1_copia;
    Contacto *c_noexiste;
    Contacto *obtenido;
    Agenda   *a1;
    Agenda   *a2;
    Agenda   *a3;
    int       idx;
    int       i;

    /* -------------------------------------------------- */
    separador("1. CREAR CONTACTOS");
    /* -------------------------------------------------- */

    c1 = crear_contacto("Luis Garcia",   "600123456", "luis@mail.com");
    c2 = crear_contacto("Ana Martinez",  "611234567", "ana@mail.com");
    c3 = crear_contacto("Pedro Sanchez", "622345678", "pedro@mail.com");
    c4 = crear_contacto("Maria Lopez",   "633456789", "maria@mail.com");

    printf("Contactos creados:\n");
    contacto_imprimir(c1);
    contacto_imprimir(c2);
    contacto_imprimir(c3);
    contacto_imprimir(c4);

    /* -------------------------------------------------- */
    separador("2. MISMO CONTACTO (equals)");
    /* -------------------------------------------------- */

    printf("c1 == c1 ? %s\n", mismo_contacto(c1, c1) == 0 ? "SI" : "NO");
    printf("c1 == c2 ? %s\n", mismo_contacto(c1, c2) == 0 ? "SI" : "NO");

    /* -------------------------------------------------- */
    separador("3. COPIAR CONTACTO (copia profunda)");
    /* -------------------------------------------------- */

    c1_copia = crear_contacto("", "000000000", "");
    copiar_contacto(c1_copia, c1);
    printf("Original:  "); contacto_imprimir(c1);
    printf("Copia:     "); contacto_imprimir(c1_copia);
    printf("Son el mismo contacto? %s\n",
           mismo_contacto(c1, c1_copia) == 0 ? "SI" : "NO");

    /* -------------------------------------------------- */
    separador("4. CREAR AGENDA Y ANIADIR CONTACTOS");
    /* -------------------------------------------------- */

    a1 = crear_agenda();
    printf("Agenda creada. Indice inicial: 0\n\n");

    aniadir_contacto(a1, c1);
    printf("Aniadido: "); contacto_imprimir(c1);

    aniadir_contacto(a1, c2);
    printf("Aniadido: "); contacto_imprimir(c2);

    aniadir_contacto(a1, c3);
    printf("Aniadido: "); contacto_imprimir(c3);

    aniadir_contacto(a1, c4);
    printf("Aniadido: "); contacto_imprimir(c4);

    printf("\nAgenda tras aniadir 4 contactos (indice=%d):\n", a1->indice);

    /* -------------------------------------------------- */
    separador("5. OBTENER CONTACTO POR INDICE");
    /* -------------------------------------------------- */

    obtenido = obtener(a1, 2);
    printf("Contacto en indice 2: ");
    contacto_imprimir(obtenido);

    printf("Contacto en indice -1 (invalido): ");
    obtenido = obtener(a1, -1);
    if (obtenido == NULL) printf("  -> devuelve NULL correctamente\n");

    /* -------------------------------------------------- */
    separador("6. BUSCAR CONTACTO");
    /* -------------------------------------------------- */

    idx = busca_contacto(a1, c3);
    printf("Buscar c3 (Pedro Sanchez): indice=%d\n", idx);

    idx = busca_contacto(a1, c1_copia);
    printf("Buscar copia de c1 (Luis Garcia): indice=%d\n", idx);

    c_noexiste = crear_contacto("Nadie", "000000000", "no@existe.com");
    idx = busca_contacto(a1, c_noexiste);
    printf("Buscar contacto inexistente: indice=%d %s\n",
           idx, idx == -1 ? "(no encontrado, correcto)" : "");

    /* -------------------------------------------------- */
    separador("7. BORRAR CONTACTO Y REORGANIZAR");
    /* -------------------------------------------------- */

    printf("Agenda ANTES de borrar c2 (Ana Martinez):\n");
    for (i = 0; i < a1->indice; i++) {
        printf("  [%d] ", i); contacto_imprimir(obtener(a1, i));
    }

    borrar_contacto(a1, c2);
    printf("\nAgenda DESPUES de borrar c2 (indice=%d):\n", a1->indice);
    for (i = 0; i < a1->indice; i++) {
        printf("  [%d] ", i); contacto_imprimir(obtener(a1, i));
    }

    printf("\nIntentar borrar contacto que no existe:\n");
    borrar_contacto(a1, c_noexiste);

    /* -------------------------------------------------- */
    separador("8. AGENDAS IGUALES");
    /* -------------------------------------------------- */

    a2 = crear_agenda();
    aniadir_contacto(a2, c1);
    aniadir_contacto(a2, c3);
    aniadir_contacto(a2, c4);

    printf("a1 tiene %d contactos, a2 tiene %d contactos\n",
           a1->indice, a2->indice);
    printf("a1 == a2 ? %s\n",
           agendas_iguales(a1, a2) == 0 ? "SI" : "NO");

    a3 = crear_agenda();
    aniadir_contacto(a3, c1);
    aniadir_contacto(a3, c3);
    aniadir_contacto(a3, c4);

    printf("a1 == a3 (mismos contactos en mismo orden) ? %s\n",
           agendas_iguales(a1, a3) == 0 ? "SI" : "NO");

    /* -------------------------------------------------- */
    separador("9. CASOS BORDE — parametros invalidos");
    /* -------------------------------------------------- */

    printf("aniadir_contacto con agenda NULL:\n");
    aniadir_contacto(NULL, c1);

    printf("aniadir_contacto con contacto NULL:\n");
    aniadir_contacto(a1, NULL);

    printf("busca_contacto con agenda NULL:\n");
    busca_contacto(NULL, c1);

    printf("borrar_contacto con contacto NULL:\n");
    borrar_contacto(a1, NULL);

    /* -------------------------------------------------- */
    separador("10. LIBERAR MEMORIA");
    /* -------------------------------------------------- */

    eliminar_contacto(c2);
    eliminar_contacto(c1_copia);
    eliminar_contacto(c_noexiste);

    free(a1);
    free(a2);
    free(a3);

    eliminar_contacto(c1);
    eliminar_contacto(c3);
    eliminar_contacto(c4);

    printf("Memoria liberada correctamente.\n");

    separador("FIN DEL PROGRAMA");
    return 0;
}