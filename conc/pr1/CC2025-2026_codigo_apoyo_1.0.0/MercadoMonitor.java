/*Primera Práctica de Concurrencia - Mercado Monitor
*Autores : Aymara Collado (240215) y Raúl Calzas (240141)
*Última modificación: 2024-05-26 16:02
*/
// Nunca cambia la declaracion del package!
package cc.mercado;

import java.util.*;
import es.upm.babel.cclib.Monitor;

/**
 * Implementación del recurso compartido Carretera con Monitores
 */
public class MercadoMonitor implements Mercado {
  /*Clase interna para representar una oferta*/
  private static class Oferta {
    int precio;
    int tks;
    int d; 
    Oferta(int precio, int tks) {
      this.precio = precio;
      this.tks = tks;
      this.d = 0;
    }
  }

  /*Clase interna para representar una petición aplazada de resultado o alerta*/
  private static class PeticionAplazada {
    Monitor.Cond cond;
    int tipo;    // 0: resultado, 1: alerta bajo, 2: alerta alto 
    int id;      // id de la oferta para peticiones de resultado, -1 para alertas  
    int limite;  // límite de precio para alertas, 0 para peticiones de resultado
    PeticionAplazada(Monitor.Cond c, int tipo, int id, int limite) {
      this.cond = c;
      this.tipo = tipo;
      this.id = id;
      this.limite = limite;
    }
  }
  private Monitor mutex;
  //estructuras de datos axuiliares para ir almacenando todas las ofertas por tipos 
  private Map<Integer, Oferta> compras;
  private Map<Integer, Oferta> ventas;
  //waitLis de conciciones 
  private List<PeticionAplazada> esperando; 
  //atributos descritos en el cTad 
  private int mx;//preicio max
  private int mn;//precio min 
  private int cn;//contador interno de ids 

  public  MercadoMonitor() {
    //creamos monitor de exclusion mutua
    mutex = new Monitor();
    //incicialixamos las estructuras de datos 
    compras = new HashMap<>();
    ventas = new HashMap<>();
    esperando = new ArrayList<>();
    //atributos del mercado 
    mx = Integer.MIN_VALUE;
    mn = Integer.MAX_VALUE;
    cn = 0;
  }


  /*Comprueba si una oferta tiene resultados disponibles*/
  private boolean CPRERes(int id){
    Oferta o = compras.get(id);
    if(o!= null) return o.d > 0 || o.tks == 0;
    o = ventas.get(id);
    return o.d > 0 || o.tks == 0;
  }


  /*Comprueba si el precio mínimo del mercado es menor o igual que el límite dado*/
  private boolean CPREAlertaB(int limite){
    return mn <= limite;
  }


  /*Comprueba si el precio máximo del mercado es mayor o igual que el límite dado*/
  private boolean CPREAlertaA(int limite){
    return mx >= limite;
  }

z
  /*Desbloquea a un hilo que esté esperando una respuesta o alerta, 
  si se cumplen las condiciones para alguna de las peticiones aplazadas*/
  private void desbloqueo(){
    Iterator<PeticionAplazada> it = esperando.iterator();
    while(it.hasNext()){
      PeticionAplazada p = it.next();
      boolean ok = false;
      // comprobamos si se cumplen las condiciones para la petición aplazada p
      switch(p.tipo){
        case 1: ok = CPRERes(p.id); break;
        case 2: ok = CPREAlertaB(p.limite); break;
        case 3: ok = CPREAlertaA(p.limite); break;
      }
      // si se cumplen las condiciones, desbloqueamos al hilo correspondiente 
      // y lo eliminamos de la lista de esperando
      if(ok){
        it.remove();
        p.cond.signal();
        return;
      }
    }
  }


  /* El método venta permite registrar una nueva oferta de venta en el mercado */
  public int venta(int minPrecio, int tks) {
    mutex.enter();
    int id = cn++;
    Oferta o = new Oferta(minPrecio, tks);
    ventas.put(id, o);
  // si la oferta tiene tiempo, intentamos hacerla coincidir con alguna compra existente
    if (o.tks > 0) { 
      int mejor = -1;
      int mejorPrecio = -1;
      // buscamos la compra con el precio más alto que sea mayor o igual al precio mínimo de la venta
      for (Map.Entry<Integer, Oferta> e : compras.entrySet()) {
        Oferta c = e.getValue();
        // solo consideramos compras que aún tienen tiempo, 
        // no tienen resultado y su precio es mayor o igual al mínimo de la venta
        if (c.tks > 0 && c.d == 0 && c.precio >= minPrecio) {
          if (mejor == -1 || c.precio > mejorPrecio) {
            mejor = e.getKey();
            mejorPrecio = c.precio;
          }
        }
      }
      // si encontramos una compra que coincide, 
      // actualizamos el resultado de ambas ofertas y 
      // el precio máximo y mínimo del mercado
      if (mejor != -1) {
        Oferta c = compras.get(mejor);
        int precioFinal = (c.precio + minPrecio) / 2;
        o.d = precioFinal;
        c.d = precioFinal;
        // actualizamos el precio máximo y mínimo del mercado
        mx = Math.max(mx, precioFinal);
        mn = Math.min(mn, precioFinal);
      }
    }
    desbloqueo();
    mutex.leave();
    return id;
  }


  /* El método compra permite registrar una nueva oferta de compra en el mercado 
  * La lógica es similar a la de venta, pero buscando coincidencias con ofertas de venta existentes
  */
  public int compra(int maxPrecio, int tks) {
    mutex.enter();
    int id = cn++;
    Oferta o = new Oferta(maxPrecio, tks);
    compras.put(id, o);
    if (o.tks > 0) {
      int mejor = -1;
      int mejorPrecio = Integer.MAX_VALUE;
      for (Map.Entry<Integer, Oferta> e : ventas.entrySet()) {
        Oferta v = e.getValue();
        if (v.tks > 0 && v.d == 0 && v.precio <= maxPrecio) {
          if (mejor == -1 || v.precio < mejorPrecio) {
            mejor = e.getKey();
            mejorPrecio = v.precio;
          }
        }
      }
      if (mejor != -1) {
        Oferta v = ventas.get(mejor);
        int precioFinal = (v.precio + maxPrecio) / 2;
        o.d = precioFinal;
        v.d = precioFinal;
        mx = Math.max(mx, precioFinal);
        mn = Math.min(mn, precioFinal);
        }
    }
    desbloqueo();
    mutex.leave();
    return id;
    }


/* El método resultadoOferta permite obtener el resultado de una oferta en el mercado */
  public int resultadoOferta(int id) {
    mutex.enter();
    // si el resultado de la oferta no está disponible, 
    // bloqueamos al hilo hasta que lo esté
    if (!CPRERes(id)) {
      Monitor.Cond c = mutex.newCond();
      esperando.add(new PeticionAplazada(c, 1, id, 0));
      c.await();
    }
    // una vez que se desbloquea, obtenemos el resultado de la oferta correspondiente
    Oferta o = compras.get(id);
    if (o == null) o = ventas.get(id);
    int res = o.d;
    desbloqueo();
    mutex.leave();
    return res;
  }


  /* El método alertaPrecioBajo permite establecer una alerta para cuando el precio mínimo del mercado cae por debajo de un límite */
  public void alertaPrecioBajo(int limite) {
    mutex.enter();
    // si el precio mínimo del mercado no es menor o igual al límite, 
    // bloqueamos al hilo hasta que lo sea
    if (!CPREAlertaB(limite)) {
      Monitor.Cond c = mutex.newCond();
      // añadimos una petición aplazada para esta alerta a la lista de esperando
      esperando.add(new PeticionAplazada(c, 2, -1, limite));
      c.await();
    }
    desbloqueo();
    mutex.leave();
  }


  /* El método alertaPrecioAlto permite establecer una alerta para cuando el precio máximo del mercado sube por encima de un límite */
  public void alertaPrecioAlto(int limite) {
    mutex.enter();
    if (!CPREAlertaA(limite)) {
      Monitor.Cond c = mutex.newCond();
      esperando.add(new PeticionAplazada(c, 3, -1, limite));
      c.await();
    }
    desbloqueo();
    mutex.leave();
  }


/* El método tick se llama cada segundo para actualizar el estado del mercado,
reducir el tiempo de las ofertas y eliminar las que se han agotado. */
  public void tick() {
    mutex.enter();
    // actualizamos el precio máximo y mínimo del mercado, 
    // y reducimos el tiempo de las ofertas
    mx = Integer.MIN_VALUE;
    mn = Integer.MAX_VALUE;
    // recorremos todas las ofertas de compra y venta para actualizar el precio máximo y mínimo,
    // y reducir el tiempo de las ofertas que aún tienen tiempo
    for (Oferta o : compras.values()) {
      if (o.tks > 0) o.tks--;
    }
    for (Oferta o : ventas.values()) {
      if (o.tks > 0) o.tks--;
    }
    desbloqueo();
    mutex.leave();
    }
}
