package ServeurJeu.Communications;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;
import ServeurJeu.ControleurJeu;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.Evenements.GestionnaireEvenements;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class GestionnaireCommunication 
{
	// D�claration d'une r�f�rence vers le contr�leur de jeu
	private ControleurJeu objControleurJeu;
	
	// D�claration d'une liste de ProtocoleJoueur des clients connect�s au serveur
	private Vector lstProtocoleJoueur;
	
	// D�claration d'un objet qui va permettre de v�rifier l'�tat des connexions
	// entre le serveur et les clients
	private VerificateurConnexions objVerificateurConnexions;
	
	// D�claration d'une r�f�rence vers le gestionnaire d'�v�nements
	private GestionnaireEvenements objGestionnaireEvenements;
	
	// D�claration d'une r�f�rence vers le gestionnaire de bases de donn�es
	private GestionnaireBD objGestionnaireBD;
	
	// Cette variable contient le port sur lequel le serveur va �couter et 
	// recevoir les connexions clientes
	private int intPort;
	
	// D�claration d'un socket pour le serveur
	private ServerSocket objSocketServeur;
	
	/**
	 * Constructeur de la classe GestionnaireCommunication qui permet d'initialiser
	 * le port d'�coute du serveur et la r�f�rence vers le contr�leur de jeu ainsi
	 * que vers le gestionnaire d'�v�nements.
	 */
	public GestionnaireCommunication(ControleurJeu controleur, GestionnaireEvenements gestionnaireEv, 
	        						 GestionnaireBD gestionnaireBD) 
	{
		super();
		
		// On d�finit le port par d�faut � 6100
		intPort = 6100;
		
		// Garder la r�f�rence vers le contr�leur de jeu
		objControleurJeu = controleur;
		
		// Garder la r�f�rence vers le GestionnaireEvenements et vers le GestionnaireBD
		objGestionnaireEvenements = gestionnaireEv;
		objGestionnaireBD = gestionnaireBD;
		
		// Cr�er une liste des ProtocoleJoueur
		lstProtocoleJoueur = new Vector();
		
		// Cr�er le v�rificateur de connexions
		objVerificateurConnexions = new VerificateurConnexions(this);
		
		// Cr�er un thread pour le v�rificateur de connexions
		Thread threadVerificateur = new Thread(objVerificateurConnexions);
		
		// D�marrer le thread du v�rificateur
		threadVerificateur.start();
	}
	
	/**
	 * Cette m�thode permet de d�marrer l'�coute du serveur. Chaque connexion
	 * cr�e un nouveau thread pour g�rer le protocole du joueur.
	 */
	public void ecouterConnexions()
	{
		try
		{
			// Cr�er un socket pour le serveur qui va �couter sur le port d�finit
			// par la variable "intPort"
			objSocketServeur = new ServerSocket(intPort);
		}
		catch (IOException e)
		{
			// L'�coute n'a pas pu �tre d�marr�e
			System.out.println("Il est impossible de demarrer l'ecoute sur le port " + intPort);
			System.out.println("Le serveur va maintenant s'arreter");
			System.exit(-1);
		}
		
		// Boucler ind�finiment en �coutant sur le port "intPort" et en d�marrant un
		// nouveau thread pour chacune des connexions �tablies
		while (true)
		{
			try
			{
				System.out.println("En attente de recevoir connexion");
				
				// Accepter une connexion et cr�er un objet ProtocoleJoueur
				// qui va ex�cuter le protocole pour le joueur
				ProtocoleJoueur objJoueur = new ProtocoleJoueur(objControleurJeu, this, objVerificateurConnexions,
																objSocketServeur.accept());
				
				// Cr�er un thread pour le joueur demandant la connexion
				Thread threadJoueur = new Thread(objJoueur);
				
				// D�marrer le thread du joueur courant
				threadJoueur.start();
				
				// Ajouter le nouveau ProtocoleJoueur dans la liste (ici on n'a 
				// pas besoin de synchroniser la liste puisque le vecteur fait 
				// d�j� cette synchronisation)
				lstProtocoleJoueur.add(objJoueur);
			}
			catch (IOException e)
			{
				// Une erreur est survenue lors de l'acceptation de la connexion
				System.out.println("Il est impossible d'accepter la connexion du client");
				// System.out.println("Le serveur va maintenant s'arr�ter");
				// System.exit(-1);
			}
		}
	}
	
	/**
	 * Cette m�thode permet de supprimer le protocole joueur pass� en param�tres
	 * de la liste des ProtocoleJoueur. Cela signifie que le joueur ne sera plus
	 * connect� physiquement au serveur de jeu. Il faut s'assurer que le 
	 * ProtocoleJoueur supprim� sera d�truit ou terminera prochainement.
	 * 
	 * @param ProtocoleJoueur protocole : le protocole du joueur � supprimer de
	 * 									  la liste
	 */
	public void supprimerProtocoleJoueur(ProtocoleJoueur protocole)
	{
		// Enlever le protocole joueur de la liste des ProtocoleJoueur
		lstProtocoleJoueur.remove(protocole);
	}
	
	/**
	 * Cette fonction permet de retourner la liste des 
	 * ProtocoleJoueur des clients connect�s au serveur de jeu.
	 * 
	 * @return Vector : la liste des ProtocoleJoueur des clients 
	 * 					pr�sentement connect�s au serveur de jeu
	 */
	public Vector obtenirListeProtocoleJoueur()
	{
		return lstProtocoleJoueur;
	}
	
	/**
	 * Cette m�thode est appel�e automatiquement si le programme doit terminer.
	 * Lorsque l'application serveur doit terminer, alors il faut s'assurer que 
	 * tous les threads stopent et que tous les sockets soient ferm�s. La fa�on
	 * la plus rapide est de fermer le socket serveur. Cela aura pour effet de
	 * fermer tous les sockets obtenus par l'acceptation de connexions et comme
	 * ces sockets se fermeront, chaque thread arr�tera car une exception 
	 * surviendra pour chaque thread. 
	 */
	protected void finalize()
	{
		try
		{
			// Fermer le socket du serveur
			objSocketServeur.close();
		}
		catch (IOException e)
		{
			// Le socket du serveur est d�j� ferm�
			System.out.println("Le socket du serveur est deja ferme");
		}
		
		// Vider la liste des protocoles de joueurs
		lstProtocoleJoueur.clear();
		
		// Arr�ter le thread de v�rification des connexions
		objVerificateurConnexions.arreterVerificateurConnexions();
		
		// Arr�ter le thread de gestionnaire d'�v�nements
		objGestionnaireEvenements.arreterGestionnaireEvenements();
		
		// Fermer toutes les connexions ouvertes pour le gestionnaire de base de donn�es
		objGestionnaireBD.arreterGestionnaireBD();
	}
}
