/*
*Práctica 4 concurrencia 
*Auttor: rCalzas,240141
*/
//aun que en la plantilla del ejercicio lo hace heredadno de thread, lo prefiero hacer así pq se supone que separas mejor responsabilidades y además se puede heredara de otras clase si fuera necesario */
import java.util.ArrayList;
import java.util.List;
import es.upm.babel.cclib.Semaphore;


public class CC_04_ColaSem {

    static final int P = 4;
    static final int NUM_OPS = 1000;

    static List<Integer> lista = new ArrayList<>();

    static Semaphore semaf1 = new Semaphore(1);//semaforo binario
    static Semaphore elementos = new Semaphore(0);//contador

    static class Productor implements Runnable {
        private int id;

        public Productor(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            int valor = id;
            for (int i = 0; i < NUM_OPS; i++) {
                semaf1.await();//esperando
                lista.add(valor);
                semaf1.signal();//sñal de semaf1
                elementos.signal();//señal de contador 
                valor += P;
            }
        }
    }

    static class Consumidor implements Runnable {

        @Override
        public void run() {
            int leidos = 0;
            int total = P * NUM_OPS;

            while (leidos < total) {
                elementos.await();//esperando
                semaf1.await();//esperando
                Integer valor = lista.remove(0);
                semaf1.signal();//señal del contador
                System.out.println(valor);
                leidos++;
            }
        }
    }

    public static void main(String[] args) {

        Thread[] productores = new Thread[P];
        Thread consumidor = new Thread(new Consumidor());

        for (int i = 0; i < P; i++) {
            productores[i] = new Thread(new Productor(i));
        }

        consumidor.start();
        for (int i = 0; i < P; i++) {
            productores[i].start();
        }

        try {
            for (int i = 0; i < P; i++) {
                productores[i].join();
            }
            consumidor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();//esto lo hago para depurar por si acaso casca en algún punto, que de ehcho ha sido bastante útil compilando en local
        }
        System.out.println("Ahora ya no se rompe la lista pq usamos semaforos superchachiguays");
    }
}
