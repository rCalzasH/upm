
/*
*Practica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
*

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
Praáctica Baraca con paso de mensajeas CONC;

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
Autor: rCalzas 240141

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
/

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
mport org.jcsp.lang.*;

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
mport es.upm.babel.cclib.ConcIO;
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}


/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
/ CTAD Barcaza

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
ublic class BarcazaCSP implements Barcaza, CSProcess {
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}


/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // Interfaz Barcaza

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // tipo Nacionalidad = Truhan | Bribon

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // public static final int NACIONES = 2;

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // public static final int TRUHAN = 0;

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // public static final int BRIBON = 1;
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}


/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // tipo Estado = Embarcando | Navegando | Desembarcando

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // public static final int EMBARCANDO    = 0;

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // public static final int NAVEGANDO     = 1;

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // public static final int DESEMBARCANDO = 2;

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // canales para comunicación con el servidor

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // un canal de embarcar por cada nacionalidad

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // (replicación de canales)

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   private Any2OneChannel[] chEmbarcar;

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // un solo canal para desembarcar

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   private Any2OneChannel   chDesembarcar;

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // zarpar

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   private One2OneChannel   chZarpar;

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // amarrar

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   private One2OneChannel   chAmarrar;

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // constructor

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   public BarcazaCSP() {
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	// OJO: estado del recurso al servidor
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	// solo creamos los canales:
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	chDesembarcar = Channel.any2one();
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	chZarpar      = Channel.one2one();
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	chAmarrar     = Channel.one2one();
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	chEmbarcar    =
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    new Any2OneChannel[Barcaza.NACIONES];
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   } 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}


/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   public void embarcar (int nacion) {
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	// replicación de canales
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	chEmbarcar[nacion].out().write(null);

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   }
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}


/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   public void desembarcar (int nacion) {
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	chDesembarcar.out().write(nacion);

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   }
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}


/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   public void zarpar () {
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	chZarpar.out().write(null);

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   }
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}


/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   public void amarrar () {
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	chAmarrar.out().write(null);

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   }
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}


/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // //////////////////////////////

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   // SERVIDOR

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   public void run() {
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	// Estado del recurso aquí:	
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	// 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	//
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	//
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	//
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	// Entradas de la select
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	AltingChannelInput[] entradas =
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    new AltingChannelInput[Barcaza.NACIONES+3];
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	// Nombres simbólicos:
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	final int DESEMBARCAR = 2;
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	final int ZARPAR      = 3;
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	final int AMARRAR     = 4;
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	entradas[EMB_TRUHAN] =
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    chEmbarcar[Barcaza.TRUHAN].in();
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	entradas[EMB_BRIBON] =
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    chEmbarcar[Barcaza.BRIBON].in();
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	entradas[DESEMBARCAR] = chDesembarcar.in();
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	entradas[ZARPAR]      = chZarpar.in();
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	entradas[AMARRAR]     = chAmarrar.in();
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	// recepción alternativa
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	Alternative servicios = new Alternative(entradas);
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	// sincronización condicional en la select
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	sincCond[AMARRAR] = true;
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	// bucle de servicio
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	while (true) {
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // sincronización condicional
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // embarcar(nacion)
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // CPRE: self = (Embarcando,o)     /\
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    //       o(Bribon) + o(Truhan) < 4 /\
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    //       o(Bribon) + o(Truhan) = 3 => o(nacion) mod 2 = 1    
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // desembarcar
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // CPRE: self = (Desembarcando,_)
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // zarpar
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // CPRE: self = (Embarcando,{Bribon->b,Truhan->t}) /\ b+t = 4
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    //
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // amarrar
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // CPRE: Cierto
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    // la select
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    switch (servicios.fairSelect(sincCond)) {
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    case EMB_TRUHAN:
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		// 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		// 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		break;
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    case EMB_BRIBON:
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		// 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		// 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		break;
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    case DESEMBARCAR:
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		// 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		// 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		// 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		//
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		break;
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    case ZARPAR:
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		//
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		// 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		break;
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    case AMARRAR:
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		//
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		//
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
		break;
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	    }//switch 
/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
	}//bucle de servicio

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
   }//run servidor

/*
*Práctica Barcaza con paso de mensajes CONC;
*Autor: rCalzas 240141
*/
import org.jcsp.lang.*;
import es.upm.babel.cclib.ConcIO;
 
// CTAD Barcaza
public class BarcazaCSP implements Barcaza, CSProcess {
 
    // Interfaz Barcaza
    
    // tipo Nacionalidad = Truhan | Bribon
    // public static final int NACIONES = 2;
    
    // public static final int TRUHAN = 0;
    // public static final int BRIBON = 1;
 
    // tipo Estado = Embarcando | Navegando | Desembarcando
    // public static final int EMBARCANDO    = 0;
    // public static final int NAVEGANDO     = 1;
    // public static final int DESEMBARCANDO = 2;
    
    // canales para comunicación con el servidor
    // un canal de embarcar por cada nacionalidad
    // (replicación de canales)
    private Any2OneChannel[] chEmbarcar;
    // un solo canal para desembarcar
    private Any2OneChannel   chDesembarcar;
    // zarpar
    private One2OneChannel   chZarpar;
    // amarrar
    private One2OneChannel   chAmarrar;
    
    // constructor
    public BarcazaCSP() {
	// OJO: estado del recurso al servidor
	// solo creamos los canales:
	chDesembarcar = Channel.any2one();
	chZarpar      = Channel.one2one();
	chAmarrar     = Channel.one2one();
	chEmbarcar    =
	    new Any2OneChannel[Barcaza.NACIONES];
	chEmbarcar[Barcaza.TRUHAN] = Channel.any2one();
	chEmbarcar[Barcaza.BRIBON] = Channel.any2one();
    } 
 
    public void embarcar (int nacion) {
	// replicación de canales
	chEmbarcar[nacion].out().write(null);
    }
 
    public void desembarcar (int nacion) {
	chDesembarcar.out().write(nacion);
    }
 
    public void zarpar () {
	chZarpar.out().write(null);
    }
 
    public void amarrar () {
	chAmarrar.out().write(null);
    }
 
    // //////////////////////////////
    // SERVIDOR
    public void run() {
	// Estado del recurso aquí:
	// fase actual de la barcaza
	int estado = Barcaza.EMBARCANDO;
	// ocupantes de cada nación a bordo
	int[] ocupantes = new int[Barcaza.NACIONES];
	ocupantes[Barcaza.TRUHAN] = 0;
	ocupantes[Barcaza.BRIBON] = 0;
 
	// Entradas de la select
	AltingChannelInput[] entradas =
	    new AltingChannelInput[Barcaza.NACIONES+3];
 
	// Nombres simbólicos:
	final int EMB_TRUHAN  = 0; // =Barcaza.TRUHAN
	final int EMB_BRIBON  = 1; // =Barcaza.BRIBON
	final int DESEMBARCAR = 2;
	final int ZARPAR      = 3;
	final int AMARRAR     = 4;
 
	entradas[EMB_TRUHAN] =
	    chEmbarcar[Barcaza.TRUHAN].in();
	entradas[EMB_BRIBON] =
	    chEmbarcar[Barcaza.BRIBON].in();
	entradas[DESEMBARCAR] = chDesembarcar.in();
	entradas[ZARPAR]      = chZarpar.in();
	entradas[AMARRAR]     = chAmarrar.in();
 
	// recepción alternativa
	Alternative servicios = new Alternative(entradas);
	// sincronización condicional en la select
	boolean[] sincCond = new boolean[Barcaza.NACIONES+3];
	
	// bucle de servicio
	while (true) {
	    int total = ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON];
 
	    // embarcar(TRUHAN)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[TRUHAN] mod 2 = 1)
	    //   es decir: si hay 3 a bordo, el 4º debe ser impar en truhanes
	    //   => el 4º debe ser del mismo tipo que el que ya tiene impar
	    //   Reescrito: total < 4 /\ (total < 3 \/ ocupantes[TRUHAN] % 2 == 1)
	    sincCond[EMB_TRUHAN] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.TRUHAN] % 2 == 1);
 
	    // embarcar(BRIBON)
	    // CPRE: estado = EMBARCANDO /\ total < 4 /\
	    //       (total < 3 \/ ocupantes[BRIBON] mod 2 = 1)
	    sincCond[EMB_BRIBON] =
		(estado == Barcaza.EMBARCANDO) &&
		(total < 4) &&
		(total < 3 || ocupantes[Barcaza.BRIBON] % 2 == 1);
 
	    // desembarcar
	    // CPRE: estado = DESEMBARCANDO
	    sincCond[DESEMBARCAR] =
		(estado == Barcaza.DESEMBARCANDO);
 
	    // zarpar
	    // CPRE: estado = EMBARCANDO /\ total = 4
	    sincCond[ZARPAR] =
		(estado == Barcaza.EMBARCANDO) &&
		(total == 4);
 
	    // amarrar
	    // CPRE: Cierto (siempre habilitado)
	    sincCond[AMARRAR] = true;
 
	    // la select
	    switch (servicios.fairSelect(sincCond)) {
	    case EMB_TRUHAN:
		entradas[EMB_TRUHAN].read();
		ocupantes[Barcaza.TRUHAN]++;
		break;
 
	    case EMB_BRIBON:
		entradas[EMB_BRIBON].read();
		ocupantes[Barcaza.BRIBON]++;
		break;
 
	    case DESEMBARCAR:
		int nacion = (Integer) entradas[DESEMBARCAR].read();
		ocupantes[nacion]--;
		// si ya han desembarcado todos, volvemos a estado EMBARCANDO
		if (ocupantes[Barcaza.TRUHAN] + ocupantes[Barcaza.BRIBON] == 0) {
		    estado = Barcaza.EMBARCANDO;
		}
		break;
 
	    case ZARPAR:
		entradas[ZARPAR].read();
		estado = Barcaza.NAVEGANDO;
		break;
 
	    case AMARRAR:
		entradas[AMARRAR].read();
		estado = Barcaza.DESEMBARCANDO;
		break;
	    }//switch 
	}//bucle de servicio
    }//run servidor
}
