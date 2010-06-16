package ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables;

import java.util.Timer;
import ClassesUtilitaires.BraniacTask;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;

/**
 * @author Oloieri Lilian
 *
 */

public class Braniac extends ObjetUtilisable {

	public static final int PRIX = 1;

	// Cette constante affirme que l'objet courant n'est pas limit� 
	// lorsqu'on l'ach�te (c'est-�-dire qu'un magasin n'�puise jamais 
	// son stock de cet objet)
	public static final boolean EST_LIMITE = false;

	// Cette constante affirme que l'objet courant ne peut �tre arm� 
	// et d�pos� sur une case pour qu'un autre joueur tombe dessus. Elle 
	// ne peut seulement �tre utilis�e imm�diatement par le joueur
	public static final boolean PEUT_ETRE_ARME = false;

	// Cette constante d�finit le nom de cet objet
	public static final String TYPE_OBJET = "Braniac";
	
	//private static final long Seconds = 90;

	/**
	 * Constructeur de la classe Braniac qui permet de d�finir les propri�t�s 
	 * propres � l'objet courant.
	 *
	 * @param in id : Le num�ro d'identification de l'objet
	 * @param boolean estVisible : Permet de savoir si l'objet doit �tre visible ou non
	 */
	public Braniac(int id, boolean estVisible)
	{
		// Appeler le constructeur du parent
		super(id, estVisible, UID_OU_BRANIAC, PRIX, EST_LIMITE, PEUT_ETRE_ARME, TYPE_OBJET);
	}

	public static BraniacTask utiliserBraniac(JoueurHumain player, long delay)
	{
		// player under Braniac
		// Create TimerTask and Timer.
		BraniacTask bTask = new BraniacTask(player);
		Timer bTimer = new Timer();
					
		// effect of Braniac - the rest in the BraniacState
		player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() + 1);
								
		// used timer to take out effects of braniac after the needed time
		bTimer.schedule(bTask, delay);
		//System.out.println("BraniacTask !!!! " + bTimer + " " + Seconds + " " + bTask);
		return bTask;
			
	}
	
	
	
	public static BraniacTask utiliserBraniac(JoueurVirtuel player, long delay)
	{		
		
		// Create TimerTask and Timer.
		BraniacTask bTask = new BraniacTask(player);
		Timer bTimer = new Timer();
		
		player.getBraniacState().setInBraniac(true);
		// used timer to take out effects of braniac after the needed time
		bTimer.schedule(bTask, delay);
		
		return bTask;

	}// end method
}// end class