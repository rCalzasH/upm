
/*
*Práctica: Concurrencia 1
*Autor: rCalzas, 240141
*/

public class CC_01_Threads {

    static class Hilo implements Runnable {
        private int name ;

        public Hilo(int name) {
            this.name  = name;
        }

        @Override
        public void run() {
            System.out.println("Hola, soy el hilo " + name);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("NO");
            }
            System.out.println("El hilo " + name + " ha fallecido");
        }
    }

    public static void main(String[] args) {

        int N = 8;
        Thread[] hilos = new Thread[N];

        for (int i = 0; i < N; i++) {
            Hilo tarea = new Hilo(i);
            hilos[i] = new Thread(tarea);
            hilos[i].start();
        }

        for (int i = 0; i < N; i++) {
            try {
                hilos[i].join();
            } catch (InterruptedException e) {
                System.out.println("NO se han unido los hilos puñeta");
            }
        }

        System.out.println("Todos mis hijos se han muerto");
    }
}
