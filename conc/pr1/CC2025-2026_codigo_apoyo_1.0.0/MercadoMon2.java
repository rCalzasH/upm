/*
*Práctica 1 concurrencia 
Autores: rCalzas (testeo Lógica paralela con otras estructuras de datos)

*/
// Nunca cambia la declaracion del package!
package cc.mercado;
import es.upm.aedlib.*;
import es.upm.babel.cclib.Monitor;
// al lio///



//implementamos un comparator proipio para filtrar ofertas, ya que debemos seguir un doble criterio. Usamos las expresiones lambas pq son más compatas 
private //TODO
/**
 * Implementación del recurso compartido MERCADO  con Monitores (ponia carretera, pero seríauna errata, ya que la del año pasado era de carreteras)
 */
public class MercadoMonitor implements Mercado {
 /*
 *La gestión de los ticks hemos decidido que no vamos a estar comprobando todo el rato, ya que no se puede actualizar pq es seccion critica mientras está en otro método
 por esa misa raazon es simplemente que el que se encarga de revisar y acabar las ofertas */ 
  private class Oferta {
  //calse que modela el tipo oferta en funcion de lo expliucado en el enunciado
  private int tiempoMaximo;
  private int precioDeseado;
  private int dineroGastado;//
  private final int id; //el id es final pq no cambia  
  private Entry <Integer, Oferta> entry;
  //constructor 
  public oferta(int t, int p,int id){ /*int precioGAstado (otra cosa que no tiene sentido, si pido un parametro apra que lo voy a inciializar siempre a 0)*/ ){
    this.tiempoMaximo=t;
    this.precioDeseado=p;
    //this.precioGastado=0; no tiene sentido ????
    this.id=id;
  }
}
//Atributos MercadoMon2 
  private Monitor mutex;                          // exclusion mutua
  private Monitor.Cond precioAlto;               // para alertaPrecioAlto
  private Monitor.Cond precioBajo;               // para alertaPrecioBajo
  private Map<Integer, Monitor.Cond> condOfertas; // una condicion por oferta
  //estructuras de datos auxiliares para poder guardar las ofertas   
  private HeapPriorityQueue<Integer, Oferta> heapVentas;  // min precio primero
  private HeapPriorityQueue<Integer, Oferta> heapCompras; // max precio primero
  private Map<Integer, Oferta> todasOfertas;              // todas las ofertas por id  
  //
  private int mx;  // precio maximo acordado en intervalo actual
  private int mi;  // precio minimo acordado en intervalo actual
  private int idContador; // contador para asignar ids unicos
  //constructor 
 public MercadoMonitor() {
    this.mutex = new Monitor();
    this.precioAlto = mutex.newCond();
    this.precioBajo = mutex.newCond();
    this.condOfertas = new HashTableMap<>();
    this.ventas = new HeapPriorityQueue<>();
    this.compras = new HeapPriorityQueue<>((a, b) -> b - a); // invertido usamos una lamba expresion pq es más compacto y legible 
    this.todasOfertas = new HashTableMap<>();
    this.mx = Integer.MIN_VALUE;  // -infinito
    this.mi = Integer.MAX_VALUE;  // +infinito
    this.idContador = 0;
}

  //matches de compra-ventas; no hago aquí uso de monitores ya que al ser métodos auxiliares y los invoco solamente yo, se donde se invocan e iran siemrpe protegidos con mutex
  private Oferta matchV(int minPrecio) {
    if (compras.isEmpty()) return null;
    Oferta of = compras.first().getValue(); 
    if(of.tiempoMaximo==0) retunr null;//comprobacion momentanea 
    return of.precio >= minPrecio ? of : null;  //deveulo la oferta si es el caso de que puedo firmar la compraaventa o null
}

private Oferta matchC(int maxPrecio) {
    if (ventas.isEmpty()) return null;//compruebo si no esta vacío 
    Oferta of = ventas.first().getValue();
    if(of.tiempoMaximo==0) retunr null;//comprobacion momentanea 
    return of.precio <= maxPrecio ? of : null;  //deveulo la oferta si es el caso de que puedo firmar la compraaventa o null
}
  public int venta(int minPrecio, int tks) {
    mutex.enter();
    // creamos la oferta y actualizamos el contador
    Oferta of = new Oferta(tks, minPrecio, this.idContador);
    idContador++;
    // creamos la condicion para esta oferta
    Monitor.Cond cond = mutex.newCond();
    condOfertas.put(of.id, cond);
    // buscamos match
    Oferta of1 = matchV(of.precioDeseado);
    if (of1 != null) {
        // hay match, cerramos la transaccion
        int precio = (of.precioDeseado + of1.precioDeseado) / 2;
        of.dineroGastado = precio;
        of1.dineroGastado = precio;
        // actualizamos precios del intervalo actual
        this.mx = Math.max(this.mx, precio);
        this.mi = Math.min(this.mi, precio);
        // añadimos ambas ofertas a todasOfertas
        todasOfertas.put(of.id, of);
        todasOfertas.put(of1.id, of1);
        // sacamos la compra del heap de compras
        Compras.remove(of1.entry);
        // despertamos al comprador
        condOfertas.get(of1.id).signal();
        
    } else {
        // no hay match, enColamos la oferta
        todasOfertas.put(of.id, of);
        of.entry = ventas.enqueue(of.precioDeseado, of);
    }
    //terminamos
    mutex.leave();
    return of.id;
}

  public int compra(int maxPrecio, int tks) {
   mutex.enter();
    // creamos la oferta y actualizamos el contador
    Oferta of = new Oferta(tks, minPrecio, this.idContador);
    idContador++;
    // creamos la condicion para esta oferta
    Monitor.Cond cond = mutex.newCond();
    condOfertas.put(of.id, cond);
    // buscamos match
    Oferta of1 = matchC(of.precioDeseado);
    if (of1 != null) {
        // hay match, cerramos la transaccion
        int precio = (of.precioDeseado + of1.precioDeseado) / 2;
        of.dineroGastado = precio;
        of1.dineroGastado = precio;
        // actualizamos precios del intervalo actual
        this.mx = Math.max(this.mx, precio);
        this.mi = Math.min(this.mi, precio);
        // añadimos ambas ofertas a todasOfertas
        todasOfertas.put(of.id, of);
        todasOfertas.put(of1.id, of1);
        // sacamos la compra del heap de compras
        Compras.remove(of1.entry);
        // despertamos al comprador
        condOfertas.get(of1.id).signal();
        
    } else {
        // no hay match, enColamos la oferta
        todasOfertas.put(of.id, of);
        of.entry = ventas.enqueue(of.precioDeseado, of);
    }
    //terminamos
    mutex.leave();
    return of.id;
}


  }

  public int resultadoOferta(int id) {
    // TODO: implementar resultadoOferta
    return -1;
  }

  public void alertaPrecioBajo(int limite) {
    // TODO: implementar alertaPrecioBajo
  }
  
  public void alertaPrecioAlto(int limite) {
    // TODO: implementar alertaPrecioAlto
  }

  public void tick() {
    // TODO: implementar tick
  }
}