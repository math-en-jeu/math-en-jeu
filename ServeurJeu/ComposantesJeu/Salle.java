package ServeurJeu.ComposantesJeu;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import Enumerations.RetourFonctions.ResultatEntreeTable;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.Evenements.EvenementJoueurEntreSalle;
import ServeurJeu.Evenements.EvenementJoueurQuitteSalle;
import ServeurJeu.Evenements.EvenementNouvelleTable;
import ServeurJeu.Evenements.EvenementTableDetruite;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;
import ServeurJeu.ControleurJeu;

//TODO: Le mot de passe d'une salle ne doit pas �tre modifi�e pendant le jeu,
//      sinon il va falloir ajouter des synchronisations � chaque fois qu'on
//      fait des validations avec le mot de passe de la salle.
/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class Salle 
{
	// D�claration d'une r�f�rence vers le gestionnaire d'�v�nements
	private GestionnaireEvenements objGestionnaireEvenements;
	
	// D�claration d'une r�f�rence vers le contr�leur de jeu
	private ControleurJeu objControleurJeu;
	
	// D�claration d'une r�f�rence vers le gestionnaire de bases de donn�es
	private GestionnaireBD objGestionnaireBD;
	
	// Cette variable va contenir le nom de la salle
	private String strNomSalle;

	// Cette variable va contenir le mot de passe permettant d'acc�der � la salle
	private String strPassword;
	
	// Cette variable va contenir le nom d'utilisateur du cr�ateur de cette salle
	private String strCreatorUserName;
        
    // Contient le type de jeu (ex. mathEnJeu)
	private String gameType;
	
	//Room short description
	private String roomDescription;
	
	// Cet objet est une liste de num�ros utilis�s pour les tables (sert � 
	// g�n�rer de nouvelles tables)
	private TreeSet<Integer> lstNoTables;
	
	// Cet objet est une liste des joueurs qui sont pr�sentement dans cette salle
	private TreeMap<String, JoueurHumain> lstJoueurs;
	
	// Cet objet est une liste des tables qui sont pr�sentement dans cette salle
	private TreeMap<Integer, Table> lstTables;
	
	// Cet objet permet de d�terminer les r�gles de jeu pour cette salle
	private Regles objRegles;
        
    //private static int maxPossessionPieceEtObjet;   // will be changed to instance var
      
   
	/**
	 * Constructeur de la classe Salle qui permet d'initialiser les membres 
	 * priv�s de la salle. Ce constructeur a en plus un mot de passe permettant
	 * d'acc�der � la salle.
	 * 
	 * @param GestionnaireBD gestionnaireBD : Le gestionnaire de base de donn�es
	 * @param String nomSalle : Le nom de la salle
	 * @param String nomUtilisateurCreateur : Le nom d'utilisateur du cr�ateur
	 * 										  de la salle
	 * @param String motDePasse : Le mot de passe
	 * @param Regles reglesSalle : Les r�gles de jeu pour la salle courante
	 */
	public Salle(String nomSalle, String nomUtilisateurCreateur, String motDePasse, 
				 Regles reglesSalle, ControleurJeu controleurJeu, String gameType)
	{
		super();
		
		// Faire la r�f�rence vers le gestionnaire d'�v�nements et le 
		// gestionnaire de base de donn�es
		objGestionnaireEvenements = new GestionnaireEvenements();
		objGestionnaireBD = controleurJeu.obtenirGestionnaireBD();
		
		// Garder en m�moire le nom de la salle, le nom d'utilisateur du 
		// cr�ateur de la salle et le mot de passe
		strNomSalle = nomSalle;
		 
		strCreatorUserName = nomUtilisateurCreateur;
		strPassword = motDePasse;
                
        // Type de jeu de la salle
        this.gameType = gameType;
		
		// Cr�er une nouvelle liste de joueurs, de tables et de num�ros
		lstJoueurs = new TreeMap <String, JoueurHumain>();
		lstTables = new TreeMap <Integer, Table>();
		lstNoTables = new TreeSet <Integer>();
		
		// D�finir les r�gles de jeu pour la salle courante
		objRegles = reglesSalle;
                
        // Faire la r�f�rence vers le controleur de jeu
		objControleurJeu = controleurJeu;
		
		// Cr�er un thread pour le GestionnaireEvenements
		Thread threadEvenements = new Thread(objGestionnaireEvenements);
		
		// D�marrer le thread du gestionnaire d'�v�nements
		threadEvenements.start();
		
	}

	/**
	 * Cette fonction permet de g�n�rer un nouveau num�ro de table.
	 * 
	 * @return int : Le num�ro de table g�n�r�
	 * 
	 * @synchronism Cette fonction n'a pas besoin d'�tre synchronis�e, car 
	 * 				elle doit l'�tre par la fonction appelante. La 
	 * 				synchronisation devrait se faire sur la liste des tables.
	 */
	private int genererNoTable()
	{
		// D�claration d'une variable qui va contenir le num�ro de table
		// g�n�r�
		int intNoTable = 1;
		
		// Boucler tant qu'on n'a pas trouv� de num�ro n'�tant pas utilis�
		while (lstNoTables.contains(new Integer(intNoTable)) == true)
		{
			intNoTable++;
		}
		
		return intNoTable;
	}
	
	/**
	 * Cette fonction permet de valider que le mot de passe pour entrer dans la
	 * salle est correct. On suppose suppose que le joueur n'est pas dans la
	 * salle courante. Cette fonction va avoir pour effet de connecter le joueur 
	 * dans la salle courante.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant d'entrer dans la salle
	 * @param String motDePasse : Le mot de passe pour entrer dans la salle
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								g�n�rer un num�ro de commande pour le retour de
	 * 								l'appel de fonction
	 * @return false : Le mot de passe pour entrer dans la salle n'est pas
	 * 				   le bon
	 * 		   true  : Le joueur a r�ussi � entrer dans la salle
	 * 
	 * @synchronism Cette fonction est synchronis�e pour �viter que deux 
	 * 				puissent entrer ou quitter une salle en m�me temps.
	 * 				On n'a pas � s'inqui�ter que le joueur soit modifi�
	 * 				pendant le temps qu'on ex�cute cette fonction. De plus
	 * 				on n'a pas � rev�rifier que la salle existe bien (car
	 * 				elle ne peut �tre supprim�e) et que le joueur n'est 
	 * 				pas toujours dans une autre salle (car le protocole
	 * 				ne peut pas ex�cuter plusieurs fonctions en m�me temps)
	 */
	public boolean entrerSalle(JoueurHumain joueur, String motDePasse, boolean doitGenererNoCommandeRetour)
	{
		// Si le mot de passe est le bon, alors on ajoute le joueur dans la liste
		// des joueurs de cette salle et on envoit un �v�nement aux autres
		// joueurs de cette salle pour leur dire qu'il y a un nouveau joueur
		if (strPassword.equals(motDePasse))
		{
		    // Emp�cher d'autres thread de toucher � la liste des joueurs de 
		    // cette salle pendant l'ajout du nouveau joueur dans cette salle
		    synchronized (lstJoueurs)
		    {
				// Ajouter ce nouveau joueur dans la liste des joueurs de cette salle
				lstJoueurs.put(joueur.obtenirNomUtilisateur(), joueur);
				
				// Le joueur est maintenant entr� dans la salle courante
				joueur.definirSalleCourante(this);
				
				// Si on doit g�n�rer le num�ro de commande de retour, alors
				// on le g�n�re, sinon on ne fait rien (�a devrait toujours
				// �tre vrai, donc on le g�n�re tout le temps)
				if (doitGenererNoCommandeRetour == true)
				{
					// G�n�rer un nouveau num�ro de commande qui sera 
				    // retourn� au client
				    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
				}

				// Pr�parer l'�v�nement de nouveau joueur dans la salle. 
				// Cette fonction va passer les joueurs et cr�er un 
				// InformationDestination pour chacun et ajouter l'�v�nement 
				// dans la file de gestion d'�v�nements
				preparerEvenementJoueurEntreSalle(joueur.obtenirNomUtilisateur());
		    }
		
			// On retourne vrai
			return true;
		}
		else
		{
			// On retourne faux
			return false;
		}
	}
	
	/**
	 * Cette m�thode permet au joueur pass� en param�tres de quitter la salle. 
	 * On suppose que le joueur est dans la salle et qu'il n'est pas en train
	 * de jouer dans aucune table.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant de quitter la salle
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								g�n�rer un num�ro de commande pour le retour de
	 * 								l'appel de fonction
	 * 
	 * @synchronism Cette fonction est synchronis�e pour �viter que deux 
	 * 				puissent entrer ou quitter une salle en m�me temps.
	 * 				On n'a pas � s'inqui�ter que le joueur soit modifi�
	 * 				pendant le temps qu'on ex�cute cette fonction. De plus
	 * 				on n'a pas � rev�rifier que la salle existe bien (car
	 * 				elle ne peut �tre supprim�e) et que le joueur n'est 
	 * 				pas toujours dans une autre salle (car le protocole
	 * 				ne peut pas ex�cuter plusieurs fonctions en m�me temps)
	 */
	public void quitterSalle(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, boolean detruirePartieCourante)
	{
		//TODO: Peut-�tre va-t-il falloir ajouter une synchronisation ici
		// 		lorsque la commande sortir joueur de la table sera cod�e
		// Si le joueur est en train de jouer dans une table, alors
		// il doit quitter cette table avant de quitter la salle
		if (joueur.obtenirPartieCourante() != null)
		{
		    // Quitter la table courante avant de quitter la salle
		    joueur.obtenirPartieCourante().obtenirTable().quitterTable(joueur, false, detruirePartieCourante);
		}
	    
	    // Emp�cher d'autres thread de toucher � la liste des joueurs de 
	    // cette salle pendant que le joueur quitte cette salle
	    synchronized (lstJoueurs)
	    {
			// Enlever le joueur de la liste des joueurs de cette salle
			lstJoueurs.remove(joueur.obtenirNomUtilisateur());
			
			// Le joueur est maintenant dans aucune salle
			joueur.definirSalleCourante(null);
			
			// Si on doit g�n�rer le num�ro de commande de retour, alors
			// on le g�n�re, sinon on ne fait rien (�a se peut que ce soit
			// faux)
			if (doitGenererNoCommandeRetour == true)
			{
				// G�n�rer un nouveau num�ro de commande qui sera 
			    // retourn� au client
			    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
			}

			// Pr�parer l'�v�nement qu'un joueur a quitt� la salle. 
			// Cette fonction va passer les joueurs et cr�er un 
			// InformationDestination pour chacun et ajouter l'�v�nement 
			// dans la file de gestion d'�v�nements
			preparerEvenementJoueurQuitteSalle(joueur.obtenirNomUtilisateur());	        
	    }
	}
	
	/**
	 * Cette m�thode permet de cr�er une nouvelle table et d'y faire entrer le
	 * joueur qui en fait la demande. On suppose que le joueur n'est pas dans 
	 * aucune autre table.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant de cr�er la table
	 * @param int tempsPartie : Le temps que doit durer la partie
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								g�n�rer un num�ro de commande pour le retour de
	 * 								l'appel de fonction
	 * @return int : Le num�ro de la nouvelle table cr��e
	 * 
	 * @synchronism Cette fonction est synchronis�e pour la liste des tables
	 * 				car on va ajouter une nouvelle table et il ne faut pas 
	 * 				qu'on puisse d�truire une table ou obtenir la liste des
	 * 				tables pendant ce temps. On synchronise �galement la 
	 * 				liste des joueurs de la salle, car on va passer les 
	 * 				joueurs de la salle et leur envoyer un �v�nement. La
	 * 				fonction entrerTable est synchronis�e automatiquement.
	 */
	public int creerTable(JoueurHumain joueur, int tempsPartie, boolean doitGenererNoCommandeRetour, String name)
	{
		// D�claration d'une variable qui va contenir le num�ro de la table
		int intNoTable;
		
	    // Emp�cher d'autres thread de toucher � la liste des tables de 
	    // cette salle pendant la cr�ation de la table
	    synchronized (lstTables)
	    {
	    	
	    	// Cr�er une nouvelle table en passant les param�tres appropri�s
	    	Table objTable = new Table( this, genererNoTable(), joueur, tempsPartie, objControleurJeu, name);
	    		    	
	    	objTable.creation();
	    		    	
	    	// Ajouter la table dans la liste des tables
	    	lstTables.put(new Integer(objTable.obtenirNoTable()), objTable);
	    	
	    	// Ajouter le num�ro de la table dans la liste des num�ros de table
	    	lstNoTables.add(new Integer(objTable.obtenirNoTable()));
	    	
			// Si on doit g�n�rer le num�ro de commande de retour, alors
			// on le g�n�re, sinon on ne fait rien (�a devrait toujours
			// �tre vrai, donc on le g�n�re tout le temps)
			if (doitGenererNoCommandeRetour == true)
			{
				// G�n�rer un nouveau num�ro de commande qui sera 
			    // retourn� au client
			    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
			}

		    // Emp�cher d'autres thread de toucher � la liste des tables de 
		    // cette salle pendant la cr�ation de la table
		    synchronized (lstJoueurs)
		    {
				// Pr�parer l'�v�nement de nouvelle table. 
				// Cette fonction va passer les joueurs et cr�er un 
				// InformationDestination pour chacun et ajouter l'�v�nement 
				// dans la file de gestion d'�v�nements
				preparerEvenementNouvelleTable(objTable.obtenirNoTable(), tempsPartie, joueur.obtenirNomUtilisateur(), objTable.getTableName());
		    }

		    // Entrer dans la table on ne fait rien avec la liste des 
		    // personnages
		    objTable.entrerTable(joueur, false, new TreeMap<String,Integer>(), new TreeMap<String, Integer>());
		    
		    // Garder le num�ro de table pour le retourner
		    intNoTable = objTable.obtenirNoTable();
	    }
	    
	    return intNoTable;
	}
	
	/**
	 * Cette fonction permet au joueur d'entrer dans la table d�sir�e. On 
	 * suppose que le joueur n'est pas dans aucune table.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant d'entrer dans la table
	 * @param int noTable : Le num�ro de la table dans laquelle entrer
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								g�n�rer un num�ro de commande pour le retour de
	 * 								l'appel de fonction
	 * @param TreeMap listePersonnageJoueurs : La liste des joueurs dont la cl� 
	 * 								est le nom d'utilisateur du joueur et le contenu 
	 * 								est le Id du joueur 
	 * 
	 * @return String : Succes : Le joueur est maintenant dans la table
	 * 		   			TableNonExistante : Le joueur a tent� d'entrer dans une
	 * 										table non existante
	 * 					TableComplete : Le joueur a tent� d'entrer dans une 
	 * 									table ayant d�j� le maximum de joueurs
	 * 					PartieEnCours : Une partie est d�j� en cours dans la 
	 * 									table d�sir�e
	 * 
	 * @synchronism Cette fonction est synchronis�e sur la liste des tables
	 * 				pour �viter qu'un joueur puisse commencer � quitter et 
	 * 				que le joueur courant d�bute son entr�e dans la table 
	 * 				courante qui a des chances d'�tre d�truite si le joueur 
	 * 				qui veut quitter est le dernier de la table.
	 */
	public String entrerTable(JoueurHumain joueur, int noTable, boolean doitGenererNoCommandeRetour, TreeMap<String, Integer> listePersonnageJoueurs, TreeMap<String, Integer> listeRoleJoueurs)
	{
	    // D�claration d'une variable qui va contenir le r�sultat � retourner
	    // � la fonction appelante, soit les valeurs de l'�num�ration 
	    // ResultatEntreeTable
	    String strResultatEntreeTable;
	    
	    // Emp�cher d'autres thread de toucher � la liste des tables de 
	    // cette salle pendant que le joueur entre dans la table
	    synchronized (lstTables)
	    {
			// Si la table n'existe pas dans la salle o� se trouve le joueur, 
			// alors il y a une erreur
			if (lstTables.containsKey(new Integer(noTable)) == false)
			{
				// La table n'existe pas
				strResultatEntreeTable = ResultatEntreeTable.TableNonExistante;
			}
			// Si la table est compl�te, alors il y a une erreur (aucune 
			// synchronisation suppl�mentaire � faire car elle ne peut devenir 
			// compl�te ou ne plus l'�tre que par l'entr�e ou la sortie d'un 
			// joueur dans la table. Or ces actions sont synchronis�es avec 
			// lstTables, donc �a va.
			else if (((Table) lstTables.get(new Integer(noTable))).estComplete() == true)
			{
				// La table est compl�te
				strResultatEntreeTable = ResultatEntreeTable.TableComplete;
			}
			//TODO: Cette validation d�pend de l'�tat de la partie (de la table)
			// 		et lorsque cette partie se terminera ou d�butera, son �tat va changer,
			//		il va donc falloir revoir cette validation
			// Si la table n'est pas compl�te et une partie est en cours, 
			// alors il y a une erreur
			else if (((Table) lstTables.get(new Integer(noTable))).estCommencee() == true)
			{
				// Une partie est en cours
				strResultatEntreeTable = ResultatEntreeTable.PartieEnCours;
			}
			else
			{
				// Appeler la m�thode permettant d'entrer dans la table
				((Table) lstTables.get(new Integer(noTable))).entrerTable(joueur, doitGenererNoCommandeRetour, listePersonnageJoueurs, listeRoleJoueurs);
				
				// Il n'y a eu aucun probl�me pour entrer dans la table
				strResultatEntreeTable = ResultatEntreeTable.Succes;
			}
	    }
	    
	    return strResultatEntreeTable;
	}

	/**
	 * Cette m�thode permet de d�truire la table pass�e en param�tres. 
	 * On suppose que la table n'a plus aucuns joueurs.
	 * 
	 * @param Table tableADetruire : La table � d�truire
	 * 
	 * @synchronism Cette fonction n'est pas synchronis�e car elle l'est par
	 * 				la fonction qui l'appelle. On synchronise seulement
	 * 				la liste des joueurs de cette salle lorsque va venir
	 * 				le temps d'envoyer l'�v�nement que la table est d�truite
	 * 				aux joueurs de la salle. On n'a pas � s'inqui�ter que la 
	 * 				table soit modifi�e pendant le temps qu'on ex�cute cette 
	 * 				fonction, car il n'y a plus personne dans la table.
	 */
	public void detruireTable(Table tableADetruire)
	{
		Table t = (Table)lstTables.get( new Integer(tableADetruire.obtenirNoTable() ) );
		if( t != null )
		{
			t.destruction();
		}
		// Enlever la table de la liste des tables de cette salle
		lstTables.remove(new Integer(tableADetruire.obtenirNoTable()));
		
		// On enl�ve le num�ro de la table dans la liste des num�ros de table
		// pour le rendre disponible pour une autre table
		lstNoTables.remove(new Integer(tableADetruire.obtenirNoTable()));
		
		// Emp�cher d'autres thread de toucher � la liste des joueurs de 
	    // cette salle pendant qu'on parcourt tous les joueurs de la salle
		// pour leur envoyer un �v�nement
	    synchronized (lstJoueurs)
	    {
			// Pr�parer l'�v�nement qu'une table a �t� d�truite. 
			// Cette fonction va passer les joueurs et cr�er un 
			// InformationDestination pour chacun et ajouter l'�v�nement 
			// dans la file de gestion d'�v�nements
			preparerEvenementTableDetruite(tableADetruire.obtenirNoTable());	    	
	    }
	}
	
	/**
	 * Cette fonction permet d'obtenir la liste des joueurs se trouvant dans la
	 * salle courante. La vraie liste de joueurs est retourn�e.
	 * 
	 * @return TreeMap : La liste des joueurs se trouvant dans la salle courante
	 * 
	 * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle doit
	 * 				l'�tre par l'appelant de cette fonction tout d�pendant
	 * 				du traitement qu'elle doit faire
	 */
	public TreeMap<String, JoueurHumain> obtenirListeJoueurs()
	{
		return lstJoueurs;
	}
	
	/**
	 * Cette fonction permet d'obtenir la liste des tables se trouvant dans la
	 * salle courante. La vraie liste est retourn�e.
	 * 
	 * @return TreeMap : La liste des tables de la salle courante
	 * 
	 * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle doit
	 * 				l'�tre par l'appelant de cette fonction tout d�pendant
	 * 				du traitement qu'elle doit faire
	 */
	public TreeMap<Integer, Table> obtenirListeTables()
	{
		return lstTables;
	}
	
	/**
	 * Cette m�thode permet de pr�parer l'�v�nement de l'entr�e d'un joueur 
	 * dans la salle courante. Cette m�thode va passer tous les joueurs 
	 * de cette salle et pour ceux devant �tre avertis (tous sauf le joueur 
	 * courant pass� en param�tre), on va obtenir un num�ro de commande, on 
	 * va cr�er un InformationDestination et on va ajouter l'�v�nement dans 
	 * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronis�e.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient d'entrer dans la salle
	 * 
	 * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle l'est
	 * 				par l'appelant (entrerSalle).
	 */
	private void preparerEvenementJoueurEntreSalle(String nomUtilisateur)
	{
	    // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs qu'un joueur est entr� dans la salle
	    EvenementJoueurEntreSalle joueurEntreSalle = new EvenementJoueurEntreSalle(nomUtilisateur);
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// lstJoueurs (chaque �l�ment est un Map.Entry)
		Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient d'entrer dans la salle, alors on peut envoyer un 
			// �v�nement � cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
			    // un InformationDestination et l'ajouter � l'�v�nement
			    joueurEntreSalle.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
		objGestionnaireEvenements.ajouterEvenement(joueurEntreSalle);
	}

	/**
	 * Cette m�thode permet de pr�parer l'�v�nement du depart d'un joueur 
	 * de la salle courante. Cette m�thode va passer tous les joueurs 
	 * de cette salle et pour ceux devant �tre avertis (tous sauf le joueur 
	 * courant pass� en param�tre), on va obtenir un num�ro de commande, on 
	 * va cr�er un InformationDestination et on va ajouter l'�v�nement dans 
	 * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronis�e.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient de quitter la salle
	 * 
	 * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle l'est
	 * 				par l'appelant (quitterSalle).
	 */
	private void preparerEvenementJoueurQuitteSalle(String nomUtilisateur)
	{
	    // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs qu'un joueur a quitt� la salle
	    EvenementJoueurQuitteSalle joueurQuitteSalle = new EvenementJoueurQuitteSalle(nomUtilisateur);
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// lstJoueurs (chaque �l�ment est un Map.Entry)
		Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de quitter la salle, alors on peut envoyer un 
			// �v�nement � cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
			    // un InformationDestination et l'ajouter � l'�v�nement
			    joueurQuitteSalle.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
		objGestionnaireEvenements.ajouterEvenement(joueurQuitteSalle);
	}
	
	/**
	 * Cette m�thode permet de pr�parer l'�v�nement de la cr�ation d'une 
	 * nouvelle table dans la salle courante. Cette m�thode va passer tous 
	 * les joueurs de cette salle et pour ceux devant �tre avertis (tous 
	 * sauf le joueur courant pass� en param�tre), on va obtenir un num�ro 
	 * de commande, on va cr�er un InformationDestination et on va ajouter 
	 * l'�v�nement dans la file d'�v�nements du gestionnaire d'�v�nements. 
	 * Lors de l'appel de cette fonction, la liste des joueurs est 
	 * synchronis�e.
	 *
	 * @param int noTable : Le num�ro de la table cr��
	 * @param int tempsPartie : Le temps de la partie
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  a cr�� la table
	 * 
	 * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle l'est
	 * 				par l'appelant (creerTable).
	 */
	private void preparerEvenementNouvelleTable(int noTable, int tempsPartie, String nomUtilisateur, String tablName)
	{
	    // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs qu'une table a �t� cr��e
	    EvenementNouvelleTable nouvelleTable = new EvenementNouvelleTable(noTable, tempsPartie, tablName);
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// lstJoueurs (chaque �l�ment est un Map.Entry)
		Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de cr�er la table, alors on peut envoyer un 
			// �v�nement � cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
			    // un InformationDestination et l'ajouter � l'�v�nement
				nouvelleTable.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
		objGestionnaireEvenements.ajouterEvenement(nouvelleTable);
	}
	
	/**
	 * Cette m�thode permet de pr�parer l'�v�nement de la destruction d'une 
	 * table dans la salle courante. Cette m�thode va passer tous les joueurs 
	 * de cette salle et pour ceux devant �tre avertis (tous sauf le joueur 
	 * courant pass� en param�tre), on va obtenir un num�ro de commande, on 
	 * va cr�er un InformationDestination et on va ajouter l'�v�nement dans 
	 * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel de 
	 * cette fonction, la liste des joueurs est synchronis�e.
	 *
	 * @param int noTable : Le num�ro de la table d�truite
	 * 
	 * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle l'est
	 * 				par l'appelant (detruireTable).
	 */
	private void preparerEvenementTableDetruite(int noTable)
	{
	    // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs qu'une table a �t� cr��e
	    EvenementTableDetruite tableDetruite = new EvenementTableDetruite(noTable);
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// lstJoueurs (chaque �l�ment est un Map.Entry)
		Set<Map.Entry<String,JoueurHumain>> lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator<Entry<String, JoueurHumain>> objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry<String,JoueurHumain>)(objIterateurListe.next())).getValue());
			
		    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
		    // un InformationDestination et l'ajouter � l'�v�nement
			tableDetruite.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
		            											objJoueur.obtenirProtocoleJoueur()));
		}
		
		// Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
		objGestionnaireEvenements.ajouterEvenement(tableDetruite);
	}

	/**
	 * Cette fonction permet de retourner le nom de la salle courante.
	 * If the room has two languages on return name in the needed lang
	 * if not on return the name in existing lang, or if the lang isn't 
	 * known on return the existing string 
	 * @return String : Le nom de la salle
	 */
	public String getRoomName(String lang)
	{
		if(lang.equals(""))
			return strNomSalle;
		
		boolean exist = false; 
		for(int i = 0; i < strNomSalle.length(); i++)
		{
			if(strNomSalle.charAt(i) == '/')
				exist = true;
		}
	
		if(exist)
		{
		   StringTokenizer nomSalle = new StringTokenizer(strNomSalle, "/");
		   String nomFr = nomSalle.nextToken().trim();
		   String nomEng = nomSalle.nextToken().trim();
		   return lang.equalsIgnoreCase("fr")? nomFr : nomEng;
		}
		return strNomSalle;
	}//end methode
	
	public Regles getRegles()
	{
	   return objRegles;
	}
	
	/**
	 * Cette fonction permet de d�terminer si la salle poss�de un mot de passe
	 * pour y acc�der ou non.
	 * 
	 * @return boolean : true si la salle est prot�g�e par un mot de passe
	 * 					 false sinon
	 */
	public boolean protegeeParMotDePasse()
	{
		return !(strPassword == null || strPassword.equals(""));
	}


	public String getGameType()
	{
		return gameType;
	}


	public void setRoomDescription(String roomDescription) {
		this.roomDescription = roomDescription;
	}

	// return room description
	public String getRoomDescription(String lang) {
		if(lang.equals(""))
			return roomDescription;

		boolean exist = false; 
		for(int i = 0; i < roomDescription.length(); i++)
		{
			if(roomDescription.charAt(i) == '/')
				exist = true;
		}

		if(exist)
		{
			StringTokenizer descRoom = new StringTokenizer(roomDescription, "/");
			String descFr = descRoom.nextToken().trim();
			String descEng = descRoom.nextToken().trim();
			return lang.equalsIgnoreCase("fr")? descFr : descEng;
		}
		return roomDescription;
	}

}// end class 