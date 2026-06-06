/*
*Práctica 5 concurrencia 
*Auttor: rCalzas,240141
*/ 
import es.upm.babel.cclib.Semaphore;
// Almacen concurrente para un dato
class Almacen1 {
    // Producto a almacenar
    private int almacenado;
    private Semaphore prod = new Semaphore(1);//como el constructor está vacío, inicializamos este a 1 para que lo priero que pueda pasar sea siempre que se guarde 
    private Semaphore cons = new Semaphore(0);//este es el otro semáforo, el que se usa para consumir, y por ello simepre empiza a 0, para que espere un signal antes de intetnar sacar;
    //

    public Almacen1() {
    }
    
    public void almacenar(int producto) {
	//sincro
    prod.await();
    //SC, donde metemos algo
    almacenado = producto;
	// salimos señalando que ya hay algo dentro 
    cons.signal();
    }

    public int extraer() {
	int result;
    //sincronizamos 
    cons.await(); 
	// SC, solo se entra cuando hay algo dentro
	result = almacenado;
    //salimos señalando que no hay nada dentro y luego devolvemos lo guardado
	prod.signal();
	return result;
    }
}
//   :)