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