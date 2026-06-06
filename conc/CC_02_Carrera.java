/*
*Práctica 2 concurrencia 
*Auttor: rCalzas,240141
*/
//aun que en la plantilla del ejercicio lo hace heredadno de thread, lo prefiero hacer así pq se supone que separas mejor responsabilidades y además se puede heredara de otras clase si fuera necesario */
import java.util.ArrayList;
import java.util.List;

public class CC_02_Carrera {

    static final int P = 4;          
    static final int NUM_OPS = 1000;  
    
    static List<Integer> lista = new ArrayList<>();
    
    static class Productor implements Runnable {
        private int id;

        public Productor(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            int valor = id;
            for (int i = 0; i < NUM_OPS; i++) {
                lista.add(valor);  
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
                if (!lista.isEmpty()) {
                    Integer valor = lista.remove(0);
                    System.out.println(valor);
                    leidos++;
                }
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
            e.printStackTrace();
        }
        System.out.println("Si señor hemos rompido la ArrayLIst siuuu");
    }
}
