/*
*Primera Práctica de Concurrencia - MercadoCSP
*Autores : Aymara Collado (240215) y Raúl Calzas (240141)
*Última modificación: 28-05-2026 18:53
*/
//nunca cambio la declaracion del package 
package cc.mercado;

import org.jcsp.lang.*;
import java.util.*;

public class MercadoCSP implements Mercado, CSProcess {

    // Clase para representar una oferta (igual que en el monitor)
    private static class Oferta {
        int precio;//precio de la oferta 
        int tks;//tiempo de la oferta 
        int d;//dinero gastado para cerrar la oferta 
        Oferta(int precio, int tks) {
            this.precio = precio;
            this.tks = tks;
            this.d = 0;//siempre a cero cuando se crea y de ahí luego se introduce el precio pactado para cerrar la oferta 
        }
    }

    // Clases que se usan para transmitir la informacion que viaja por los canales 
    private static class PeticionOferta {
        int precio;//precio de la oferta
        int tks;//tiempo de la oferta 
        ChannelOutput resp;//canal por el que se pasa la informacion(salida) — sin genéricos por compatibilidad con JCSP 2008
        //constructor 
        PeticionOferta(int precio, int tks, ChannelOutput resp) {
            this.precio = precio;
            this.tks = tks;
            this.resp = resp;
        }
    }

    private static class PeticionResultado {
        int id;//id de la petivionResultado 
        ChannelOutput resp;//parte del canal que manda la respuesta(salida) — sin genéricos por compatibilidad con JCSP 2008
        //constructor 
        PeticionResultado(int id, ChannelOutput resp) {
            this.id = id;
            this.resp = resp;
        }
    }

    private static class PeticionAlerta {
        int limite;
        ChannelOutput resp;// sin genéricos por compatibilidad con JCSP 2008
        //constructor 
        PeticionAlerta(int limite, ChannelOutput resp) {
            this.limite = limite;
            this.resp = resp;
        }
    }

    // Clase para peticiones aplazadas dentro del servidor
    private static class PeticionAplazada {
        ChannelOutput resp;// sin genéricos por compatibilidad con JCSP 2008
        int tipo;   // 0: resultado, 1: alertaBajo, 2: alertaAlto
        int id;     //id de la oferta 
        int limite; //limite en tks de la oferta 
        //constructor 
        PeticionAplazada(ChannelOutput resp, int tipo, int id, int limite) {
            this.resp = resp;
            this.tipo = tipo;
            this.id = id;
            this.limite = limite;
        }
    }

    // Canales: Any2One porque varios clientes hablan con un servidor — sin genéricos por compatibilidad con JCSP 2008
    Any2OneChannel cVentas;
    Any2OneChannel cCompras;
    Any2OneChannel cResultadoOferta;
    Any2OneChannel cAlertaPrecioAlto;
    Any2OneChannel cAlertaPrecioBajo;
    Any2OneChannel cTick;
    //constructor 
    public MercadoCSP() {
        cVentas           = Channel.any2one();
        cCompras          = Channel.any2one();
        cResultadoOferta  = Channel.any2one();
        cAlertaPrecioAlto = Channel.any2one();
        cAlertaPrecioBajo = Channel.any2one();
        cTick             = Channel.any2one();

        new ProcessManager(this).start();//inicamos el servidor como dice el comentario de la practica 
    }

    public int venta(int minPrecio, int tks) {
        One2OneChannel resp = Channel.one2one();// sin genéricos por compatibilidad con JCSP 2008
        cVentas.out().write(new PeticionOferta(minPrecio, tks, resp.out()));
        return (Integer) resp.in().read();//toda la logica va en el run, así que no hay que manejar nada aqui
    }

    public int compra(int maxPrecio, int tks) {
        One2OneChannel resp = Channel.one2one();// sin genéricos por compatibilidad con JCSP 2008
        cCompras.out().write(new PeticionOferta(maxPrecio, tks, resp.out()));
        return (Integer) resp.in().read();//toda la logica va en el run, así que no hay que manejar nada aqui
    }

    public int resultadoOferta(int id) {
        One2OneChannel resp = Channel.one2one();// sin genéricos por compatibilidad con JCSP 2008
        cResultadoOferta.out().write(new PeticionResultado(id, resp.out()));
        return (Integer) resp.in().read();//toda la logica va en el run, así que no hay que manejar nada aqui
    }

    public void alertaPrecioBajo(int limite) {
        One2OneChannel resp = Channel.one2one();// sin genéricos por compatibilidad con JCSP 2008
        cAlertaPrecioBajo.out().write(new PeticionAlerta(limite, resp.out()));
        resp.in().read(); // bloqueamos hasta que el servidor responda
        //toda la logica va en el run, así que no hay que manejar nada aqui
    }

    public void alertaPrecioAlto(int limite) {
        One2OneChannel resp = Channel.one2one();// sin genéricos por compatibilidad con JCSP 2008
        cAlertaPrecioAlto.out().write(new PeticionAlerta(limite, resp.out()));
        resp.in().read();//toda la logica va en el run, así que no hay que manejar nada aqui
    }

    public void tick() {
        cTick.out().write(null); // tick no necesita respuesta
    }

    public void run() {
        // Estado interno del servidor simulado 
        Map compras = new HashMap();// sin genéricos por compatibilidad con JCSP 2008
        Map ventas  = new HashMap();// sin genéricos por compatibilidad con JCSP 2008
        int cn = 0;
        int mx = Integer.MIN_VALUE;
        int mn = Integer.MAX_VALUE;
        List esperando = new ArrayList();// sin genéricos por compatibilidad con JCSP 2008

        // Array de canales para el Alternative
        Alternative servicios = new Alternative(new Guard[]{
            cVentas.in(),           // case 0
            cCompras.in(),          // case 1
            cResultadoOferta.in(),  // case 2
            cAlertaPrecioBajo.in(), // case 3
            cAlertaPrecioAlto.in(), // case 4
            cTick.in()              // case 5
        });
                  /*
                  *La lógica de todas las operaciones es la misma que en mercadoMonitor, simplemenete eliminando las conditions de los monitores
                  */
        while (true) {
            switch (servicios.fairSelect()) {
                //el switch implementa la lógica que permite diferenciar lo que salga desde el fairSelect, difereniando por el tipo de lo que sale 
                case 0: { // venta
                    PeticionOferta p = (PeticionOferta) cVentas.in().read();
                    int id = cn++;
                    Oferta o = new Oferta(p.precio, p.tks);
                    ventas.put(id, o);
                    if (o.tks > 0) {
                        int mejor = -1, mejorPrecio = -1;
                        for (Iterator it = compras.entrySet().iterator(); it.hasNext();) {
                            Map.Entry e = (Map.Entry) it.next();
                            Oferta c = (Oferta) e.getValue();
                            if (c.tks > 0 && c.d == 0 && c.precio >= p.precio) {
                                if (mejor == -1 || c.precio > mejorPrecio) {
                                    mejor = (Integer) e.getKey();
                                    mejorPrecio = c.precio;
                                }
                            }
                        }
                        if (mejor != -1) {
                            Oferta c = (Oferta) compras.get(mejor);
                            int precioFinal = (c.precio + p.precio) / 2;
                            o.d = precioFinal;
                            c.d = precioFinal;
                            mx = Math.max(mx, precioFinal);
                            mn = Math.min(mn, precioFinal);
                        }
                    }
                    p.resp.write(id);
                    esbloqueo(esperando, compras, ventas, mx, mn);
                    break;
                }

                case 1: { // compra
                    PeticionOferta p = (PeticionOferta) cCompras.in().read();
                    int id = cn++;
                    Oferta o = new Oferta(p.precio, p.tks);
                    compras.put(id, o);
                    if (o.tks > 0) {
                        int mejor = -1, mejorPrecio = Integer.MAX_VALUE;
                        for (Iterator it = ventas.entrySet().iterator(); it.hasNext();) {
                            Map.Entry e = (Map.Entry) it.next();
                            Oferta v = (Oferta) e.getValue();
                            if (v.tks > 0 && v.d == 0 && v.precio <= p.precio) {
                                if (mejor == -1 || v.precio < mejorPrecio) {
                                    mejor = (Integer) e.getKey();
                                    mejorPrecio = v.precio;
                                }
                            }
                        }
                        if (mejor != -1) {
                            Oferta v = (Oferta) ventas.get(mejor);
                            int precioFinal = (v.precio + p.precio) / 2;
                            o.d = precioFinal;
                            v.d = precioFinal;
                            mx = Math.max(mx, precioFinal);
                            mn = Math.min(mn, precioFinal);
                        }
                    }
                    p.resp.write(id);
                    desbloqueo(esperando, compras, ventas, mx, mn);
                    break;
                }

                case 2: { // resultadoOferta
                    PeticionResultado p = (PeticionResultado) cResultadoOferta.in().read();
                    if (CPRERes(p.id, compras, ventas)) {
                        Oferta o = (Oferta) compras.get(p.id);
                        if (o == null) o = (Oferta) ventas.get(p.id);
                        p.resp.write(o.d);
                    } else {
                        esperando.add(new PeticionAplazada(p.resp, 0, p.id, 0));
                    }
                    break;
                }

                case 3: { // alertaPrecioBajo
                    PeticionAlerta p = (PeticionAlerta) cAlertaPrecioBajo.in().read();
                    if (mn <= p.limite) {
                        p.resp.write(0); // respondemos para desbloquear al cliente
                    } else {
                        esperando.add(new PeticionAplazada(p.resp, 1, -1, p.limite));
                    }
                    break;
                }

                case 4: { // alertaPrecioAlto
                    PeticionAlerta p = (PeticionAlerta) cAlertaPrecioAlto.in().read();
                    if (mx >= p.limite) {
                        p.resp.write(0);
                    } else {
                        esperando.add(new PeticionAplazada(p.resp, 2, -1, p.limite));
                    }
                    break;
                }

                case 5: { // tick
                    cTick.in().read();
                    for (Iterator it = compras.values().iterator(); it.hasNext();) {
                        Oferta o = (Oferta) it.next();
                        if (o.tks > 0) o.tks--;
                    }
                    for (Iterator it = ventas.values().iterator(); it.hasNext();) {
                        Oferta o = (Oferta) it.next();
                        if (o.tks > 0) o.tks--;
                    }
                    desbloqueo(esperando, compras, ventas, mx, mn);
                    break;
                }
            }
        }
    }

    //metodos auxiliares del servidor simulado como e mercadoMonitor;

    private boolean CPRERes(int id, Map compras, Map ventas) {
        Oferta o = (Oferta) compras.get(id);
        if (o == null) o = (Oferta) ventas.get(id);
        return o != null && (o.d > 0 || o.tks == 0);
    }

    private void desbloqueo(List esperando, Map compras, Map ventas, int mx, int mn) {
        Iterator it = esperando.iterator();
        while (it.hasNext()) {
            PeticionAplazada p = (PeticionAplazada) it.next();
            boolean ok = false;
            switch (p.tipo) {
                case 0: ok = CPRERes(p.id, compras, ventas); break;
                case 1: ok = mn <= p.limite; break;
                case 2: ok = mx >= p.limite; break;
            }
            if (ok) {
                it.remove();
                if (p.tipo == 0) {
                    Oferta o = (Oferta) compras.get(p.id);
                    if (o == null) o = (Oferta) ventas.get(p.id);
                    p.resp.write(o.d);
                } else {
                    p.resp.write(0); // desbloquea al cliente de la alerta
                }
            }
        }
    }
}