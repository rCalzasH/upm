/*proyecto3 recuperacion junio2025 ;), es mi problema */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../includes/banco.h"  /* banco.h incluye cuenta.h */

/* ─── Constantes ─── */
#define FICHERO_BANCO "datos/banco.txt"
#define MAX_INPUT     256

/* ─── Prototipos auxiliares ─── */
static void limpiar_pantalla(void);
static void pausar(void);
static int  leer_opcion(void);
static void leer_string(char *buf, int max, const char *prompt);
static void separador(const char *titulo);

/* ─── Submenus cuenta ─── */
static void menu_crear_cuenta(Banco *b);
static void menu_eliminar_cuenta(Banco *b);
static void menu_depositar(Banco *b);
static void menu_retirar(Banco *b);
static void menu_transferencia(Banco *b);
static void menu_buscar_iban(Banco *b);
static void menu_es_titular(Banco *b);
static void menu_buen_pagador(Banco *b);

/* ─── Submenus banco ─── */
static void menu_traspasar_cuenta(Banco *b, Banco *b2);
static void menu_mismo_banco(Banco *b, Banco *b2);
static void menu_copiar_banco(Banco *b, Banco *b2);

/* ─── Persistencia ─── */
static void guardar_banco(Banco *b);
static Banco *cargar_banco(void);

/* ════════════════════════════════════════
   MAIN
   ════════════════════════════════════════ */
int main(void) {
    /* C90: todas las declaraciones arriba */
    Banco *b;
    Banco *b2;
    int    opcion;
    int    salir;
    char   dir[MAX_INPUT];
    char   dir2[MAX_INPUT];

    /* Intentamos cargar banco principal desde fichero */
    b = cargar_banco();
    if (b == NULL) {
        printf("No se encontro fichero. Creando banco nuevo.\n");
        leer_string(dir, MAX_INPUT, "Direccion del banco principal: ");
        b = crear_Banco(dir);
        if (b == NULL) {
            fprintf(stderr, "Error fatal al crear banco.\n");
            return 1;
        }
    }

    /* Banco secundario para probar mismo_banco, copiar_banco, traspasar */
    leer_string(dir2, MAX_INPUT, "Direccion del banco secundario: ");
    b2 = crear_Banco(dir2);
    if (b2 == NULL) {
        fprintf(stderr, "Error fatal al crear banco secundario.\n");
        eliminar_banco(b);
        return 1;
    }

    salir = 0;
    while (salir == 0) {
        limpiar_pantalla();
        separador("MINI BANCO — MENU PRINCIPAL");
        printf("  Banco principal:   %s (%d cuentas)\n", b->direccionSu,  b->indice);
        printf("  Banco secundario:  %s (%d cuentas)\n\n", b2->direccionSu, b2->indice);

        printf("  — OPERACIONES DE CUENTA —\n");
        printf("  1.  Crear cuenta\n");
        printf("  2.  Eliminar cuenta del banco\n");
        printf("  3.  Depositar saldo\n");
        printf("  4.  Retirar saldo\n");
        printf("  5.  Hacer transferencia entre cuentas\n");
        printf("  6.  Buscar cuenta por IBAN\n");
        printf("  7.  Comprobar titular de una cuenta\n");
        printf("  8.  Comprobar historial crediticio\n");
        printf("  9.  Mostrar todas las cuentas del banco principal\n");

        printf("\n  — OPERACIONES DE BANCO —\n");
        printf("  10. Traspasar cuenta al banco secundario\n");
        printf("  11. Comprobar si los dos bancos son iguales\n");
        printf("  12. Copiar banco secundario en principal\n");
        printf("  13. Mostrar banco secundario\n");

        printf("\n  — FICHERO —\n");
        printf("  14. Guardar banco principal en .txt\n");
        printf("  15. Guardar y salir\n");
        printf("==========================================\n");
        printf("  Elige una opcion: ");

        opcion = leer_opcion();

        switch (opcion) {
            case 1:  menu_crear_cuenta(b);                   break;
            case 2:  menu_eliminar_cuenta(b);                break;
            case 3:  menu_depositar(b);                      break;
            case 4:  menu_retirar(b);                        break;
            case 5:  menu_transferencia(b);                  break;
            case 6:  menu_buscar_iban(b);                    break;
            case 7:  menu_es_titular(b);                     break;
            case 8:  menu_buen_pagador(b);                   break;
            case 9:
                separador("BANCO PRINCIPAL — TODAS LAS CUENTAS");
                muestra_banco(b);
                pausar();
                break;
            case 10: menu_traspasar_cuenta(b, b2);           break;
            case 11: menu_mismo_banco(b, b2);                break;
            case 12: menu_copiar_banco(b, b2);               break;
            case 13:
                separador("BANCO SECUNDARIO — TODAS LAS CUENTAS");
                muestra_banco(b2);
                pausar();
                break;
            case 14:
                guardar_banco(b);
                pausar();
                break;
            case 15:
                guardar_banco(b);
                salir = 1;
                break;
            default:
                printf("Opcion no valida.\n");
                pausar();
                break;
        }
    }

    eliminar_banco(b);
    eliminar_banco(b2);
    printf("Hasta luego.\n");
    return 0;
}

/* ════════════════════════════════════════
   SUBMENUS — CUENTA
   ════════════════════════════════════════ */

static void menu_crear_cuenta(Banco *b) {
    char   titular[MAX_INPUT];
    char   iban[25];
    double saldo;
    Cuenta *c;

    separador("CREAR CUENTA");
    leer_string(titular, MAX_INPUT, "Nombre del titular: ");
    leer_string(iban, 25, "IBAN (24 caracteres, ej: ES9121000418450200051332): ");
    printf("Saldo inicial: ");
    scanf("%lf", &saldo);
    getchar();

    c = cear_cuenta(titular, iban, saldo);
    if (c == NULL) {
        printf("Error: cuenta no creada (IBAN invalido o parametros erroneos).\n");
    } else {
        aniadir_cuenta(b, c);
        printf("Cuenta creada y aniadida:\n");
        imprimir_cuenta(c);
    }
    pausar();
}

static void menu_eliminar_cuenta(Banco *b) {
    char iban[25];
    int  idx;

    separador("ELIMINAR CUENTA");
    leer_string(iban, 25, "IBAN de la cuenta a eliminar: ");

    idx = esta_cuenta_iban(b, iban);
    if (idx == -1) {
        printf("Cuenta no encontrada.\n");
    } else {
        printf("Eliminando: ");
        imprimir_cuenta(b->cuentas[idx]);
        eliminar_cuenta_B(b, b->cuentas[idx]);
        printf("Cuenta eliminada correctamente.\n");
    }
    pausar();
}

static void menu_depositar(Banco *b) {
    char   iban[25];
    double cantidad;
    int    idx;

    separador("DEPOSITAR SALDO");
    leer_string(iban, 25, "IBAN de la cuenta: ");
    idx = esta_cuenta_iban(b, iban);
    if (idx == -1) {
        printf("Cuenta no encontrada.\n");
    } else {
        printf("Cantidad a depositar: ");
        scanf("%lf", &cantidad);
        getchar();
        aniadir_saldo(b->cuentas[idx], cantidad);
        printf("Deposito realizado. Estado actual:\n");
        imprimir_cuenta(b->cuentas[idx]);
    }
    pausar();
}

static void menu_retirar(Banco *b) {
    char   iban[25];
    double cantidad;
    int    idx;

    separador("RETIRAR SALDO");
    leer_string(iban, 25, "IBAN de la cuenta: ");
    idx = esta_cuenta_iban(b, iban);
    if (idx == -1) {
        printf("Cuenta no encontrada.\n");
    } else {
        printf("Cantidad a retirar: ");
        scanf("%lf", &cantidad);
        getchar();
        restar_saldo(b->cuentas[idx], cantidad);
        printf("Operacion realizada. Estado actual:\n");
        imprimir_cuenta(b->cuentas[idx]);
    }
    pausar();
}

static void menu_transferencia(Banco *b) {
    char   iban_orig[25];
    char   iban_dest[25];
    double cantidad;
    int    idx_orig;
    int    idx_dest;

    separador("TRANSFERENCIA ENTRE CUENTAS");
    leer_string(iban_orig, 25, "IBAN cuenta origen:  ");
    leer_string(iban_dest, 25, "IBAN cuenta destino: ");

    idx_orig = esta_cuenta_iban(b, iban_orig);
    idx_dest = esta_cuenta_iban(b, iban_dest);

    if (idx_orig == -1 || idx_dest == -1) {
        printf("Una o ambas cuentas no encontradas.\n");
    } else {
        printf("Cantidad a transferir: ");
        scanf("%lf", &cantidad);
        getchar();
        hacer_transferencia(b->cuentas[idx_orig], b->cuentas[idx_dest], cantidad);
        printf("Transferencia realizada.\n");
        printf("Origen:  "); imprimir_cuenta(b->cuentas[idx_orig]);
        printf("Destino: "); imprimir_cuenta(b->cuentas[idx_dest]);
    }
    pausar();
}

static void menu_buscar_iban(Banco *b) {
    char iban[25];
    int  idx;

    separador("BUSCAR CUENTA POR IBAN");
    leer_string(iban, 25, "IBAN a buscar: ");
    idx = esta_cuenta_iban(b, iban);
    if (idx == -1) {
        printf("Cuenta no encontrada.\n");
    } else {
        printf("Cuenta encontrada en posicion %d:\n", idx);
        imprimir_cuenta(b->cuentas[idx]);
        printf("Es buen pagador: %s\n", es_buen_pagador(b->cuentas[idx]) ? "SI" : "NO");
    }
    pausar();
}

static void menu_es_titular(Banco *b) {
    char iban[25];
    char titular[MAX_INPUT];
    int  idx;

    separador("COMPROBAR TITULAR");
    leer_string(iban,    25,        "IBAN de la cuenta: ");
    leer_string(titular, MAX_INPUT, "Nombre a comprobar: ");

    idx = esta_cuenta_iban(b, iban);
    if (idx == -1) {
        printf("Cuenta no encontrada.\n");
    } else {
        printf("%s %s titular de la cuenta %s\n",
               titular,
               es_titular(b->cuentas[idx], titular) ? "ES" : "NO ES",
               iban);
    }
    pausar();
}

static void menu_buen_pagador(Banco *b) {
    char iban[25];
    int  idx;

    separador("HISTORIAL CREDITICIO");
    leer_string(iban, 25, "IBAN de la cuenta: ");
    idx = esta_cuenta_iban(b, iban);
    if (idx == -1) {
        printf("Cuenta no encontrada.\n");
    } else {
        imprimir_cuenta(b->cuentas[idx]);
        printf("Historial crediticio: %s\n",
               es_buen_pagador(b->cuentas[idx]) ? "BUENO" : "MALO");
    }
    pausar();
}

/* ════════════════════════════════════════
   SUBMENUS — BANCO
   ════════════════════════════════════════ */

static void menu_traspasar_cuenta(Banco *b, Banco *b2) {
    char iban[25];
    int  idx;

    separador("TRASPASAR CUENTA AL BANCO SECUNDARIO");
    leer_string(iban, 25, "IBAN de la cuenta a traspasar: ");
    idx = esta_cuenta_iban(b, iban);
    if (idx == -1) {
        printf("Cuenta no encontrada en el banco principal.\n");
    } else {
        traspasar_cuenta(b, b2, b->cuentas[idx]);
        printf("Cuenta traspasada correctamente.\n");
        printf("Banco principal ahora tiene %d cuentas.\n",  b->indice);
        printf("Banco secundario ahora tiene %d cuentas.\n", b2->indice);
    }
    pausar();
}

static void menu_mismo_banco(Banco *b, Banco *b2) {
    separador("COMPARAR BANCOS");
    printf("Banco principal:  %s (%d cuentas)\n", b->direccionSu,  b->indice);
    printf("Banco secundario: %s (%d cuentas)\n", b2->direccionSu, b2->indice);
    printf("Son el mismo banco: %s\n",
           mismo_banco(b, b2) == 1 ? "SI" : "NO");
    pausar();
}

static void menu_copiar_banco(Banco *b, Banco *b2) {
    separador("COPIAR BANCO SECUNDARIO EN PRINCIPAL");
    printf("ANTES — Principal: %d cuentas | Secundario: %d cuentas\n",
           b->indice, b2->indice);
    copiar_banco(b, b2);
    printf("DESPUES — Principal: %d cuentas\n", b->indice);
    muestra_banco(b);
    pausar();
}

/* ════════════════════════════════════════
   PERSISTENCIA .TXT
   Formato CSV: titular;iban;saldo;histCred
   ════════════════════════════════════════ */

static void guardar_banco(Banco *b) {
    FILE *fp;
    int   i;

    fp = fopen(FICHERO_BANCO, "w");
    if (fp == NULL) {
        perror("Error al abrir fichero para guardar");
        return;
    }

    fprintf(fp, "%s\n", b->direccionSu);  /* linea 1: direccion */

    for (i = 0; i < b->indice; i++) {
        fprintf(fp, "%s;%s;%.2f;%d\n",
                b->cuentas[i]->titular,
                b->cuentas[i]->num_C,
                b->cuentas[i]->saldo,
                b->cuentas[i]->histCred);
    }

    fclose(fp);
    printf("Banco guardado en %s (%d cuentas).\n", FICHERO_BANCO, b->indice);
}

static Banco *cargar_banco(void) {
    FILE   *fp;
    Banco  *b;
    char    linea[MAX_INPUT];
    char    dir[MAX_INPUT];
    char    titular[MAX_INPUT];
    char    iban[25];
    double  saldo;
    int     histCred;
    Cuenta *c;

    fp = fopen(FICHERO_BANCO, "r");
    if (fp == NULL) return NULL;

    if (fgets(dir, MAX_INPUT, fp) == NULL) { fclose(fp); return NULL; }
    dir[strcspn(dir, "\n")] = '\0';  /* quitar \n */

    b = crear_Banco(dir);
    if (b == NULL) { fclose(fp); return NULL; }

    while (fgets(linea, MAX_INPUT, fp) != NULL) {
        linea[strcspn(linea, "\n")] = '\0';
        if (sscanf(linea, "%255[^;];%24[^;];%lf;%d",
                   titular, iban, &saldo, &histCred) == 4) {
            c = cear_cuenta(titular, iban, saldo);
            if (c != NULL) {
                c->histCred = histCred;
                aniadir_cuenta(b, c);
            }
        }
    }

    fclose(fp);
    printf("Banco cargado: %s (%d cuentas)\n", b->direccionSu, b->indice);
    return b;
}

/* ════════════════════════════════════════
   UTILIDADES
   ════════════════════════════════════════ */

static void limpiar_pantalla(void) {
    printf("\033[2J\033[H");
}

static void pausar(void) {
    printf("\nPulsa ENTER para continuar...");
    getchar();
}

static int leer_opcion(void) {
    int op;
    if (scanf("%d", &op) != 1) op = -1;
    getchar();
    return op;
}

static void leer_string(char *buf, int max, const char *prompt) {
    printf("%s", prompt);
    if (fgets(buf, max, stdin) != NULL)
        buf[strcspn(buf, "\n")] = '\0';
}

static void separador(const char *titulo) {
    printf("\n==========================================\n");
    printf("  %s\n", titulo);
    printf("==========================================\n");
}
