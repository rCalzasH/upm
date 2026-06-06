/*
*Práctica 4 concurrencia 
*Auttor: rCalzas,240141
*/

// TODO: importar la clase de los semáforos.
// 

// Sauna de dos intensidades con control de acceso

class Sauna {
    // Dos intensidades:
    static final int ALTA  = 0;
    static final int MEDIA = 1;
    // La capacidad es 3, de momento
    static final int CAPAC = 3;
    Semaphore altos = new Semaphore(CAPAC);
    Semaphore bajos = new Semaphore(CAPAC);//empiezan los dos on ya que si no podria morirse esperando el hilo si no es del tipo qe justo es el incial
    //de momento solo 2 varaible de estado que habla de si está lleno
    int tipoAct=0;
    int capAct=0;
    public Sauna() {
    }
    
    public void entrar(int tipo) {
        switch (tipo) {
	case ALTA:
        altos.await();
        capAct++;
        tipoAct=tipo;
        altos.signal();
	    break;
	case MEDIA:
        bajos.await();
        capAct++;
        tipoAct=tipo;
        bajos.signal();
	    break;
	}
    }

    public void salir(int tipo) {
	switch (tipo) {
	case ALTA:
        capAct--;//ha salido alguien
        if(capAct==0){//si vacío aviso otros
            bajos.signal();
        }
        else{
            altos.signal();//solo aviso
        }
        break;
	case MEDIA:
        capAct--;//sale alguien
        if(capAct==0){//si vacío aviso otros
            altos.signal();
        }
        else{
            bajos.signal();//solo aviso
        }
	    break;
	}
    }
}