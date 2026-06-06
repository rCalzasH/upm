/*proyecto1 recuperacion junio2025 ;), es mi problema */
#include "calculadora.h"
#include <stdio.h>
#include <stdlib.h>

int main(void) {
    int sigueOp = 0;
    int opEleg;
    double x, y, z;

    while (sigueOp == 0) {
        printf("Elige operacion de esta nuestra calculadora: suma(1) resta(2) multiplica(3) divide(4) salir(5)\n");
        scanf("%d", &opEleg);

        switch (opEleg) {
            case 1:
                printf("Elige los dos numeros con los que quieras operar: ");
                scanf("%lf %lf", &x, &y);
                z = suma(x, y);
                fprintf(stdout, "la suma es %f\n", z);
                break;
            case 2:
                printf("Elige los dos numeros con los que quieras operar: ");
                scanf("%lf %lf", &x, &y);
                z = resta(x, y);
                fprintf(stdout, "la resta es %f\n", z);
                break;
            case 3:
                printf("Elige los dos numeros con los que quieras operar: ");
                scanf("%lf %lf", &x, &y);
                z = multiplica(x, y);
                fprintf(stdout, "el producto es %f\n", z);
                break;
            case 4:
                printf("Elige los dos numeros con los que quieras operar: ");
                scanf("%lf %lf", &x, &y);
                z = divide(x, y);
                fprintf(stdout, "la division es %f\n", z);
                break;
            case 5:
                sigueOp = 1;
                break;
            default:
                printf("no te he entendido, que marron\n");
                break;
        }
    }

    return 0;
}