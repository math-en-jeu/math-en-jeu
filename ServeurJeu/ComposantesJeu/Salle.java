package ServeurJeu.ComposantesJeu;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import Enumerations.RetourFonctions.ResultatEntreeTable;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
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
//NOTE: Les noms de variable en anglais r�f�re aux noms de colonnes dans la BD
//      tandis que ceux en fran�ais r�f�re aux objets du ServerJeu.
/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class Salle
{
    // D�claration d'une r�f�rence vers le gestionnaire d'�v�nements

    private final GestionnaireEvenements objGestionnaireEvenements;
    // D�claration d'une r�f�rence vers le contr�leur de jeu
    private final ControleurJeu objControleurJeu;
    // D�claration d'une r�f�rence vers le gestionnaire de bases de donn�es
    private final GestionnaireBD objGestionnaireBD;
    // Contient le nom de la salle dans chaque langue. La map est construite
    // avec l'information contenu dans la table room_info.
    // BD:  [room_info.language_id --> room_info.name]
    private final Map<Integer, String> mapRoomLanguageIdToName;
    // Contient la description de la salle dans chaque langue. La map est
    // construite avec l'information contenu dans la table room_info.
    // BD:  [room_info.language_id --> room_info.name]
    private final Map<Integer, String> mapRoomLanguageIdToDescription;
    // Contient le mot de passe permettant d'acc�der � la salle
    // BD:  [room.password]
    private final String strPassword;
    // Contient le nom d'utilisateur du cr�ateur de cette salle
    // BD:  [user.username] pour le user avec id [room.user_id]
    private final String strCreatorUsername;
    // Contient la date d'ouverture de la salle. Peut etre null
    // BD:  [room.beginDate]
    private final Date dateBeginDate;
    // Contient la date de fermeture de la salle. Peut etre null
    // BD:  [room.endDate]
    private final Date dateEndDate;
    // Contient le num�ro d'indentification unique pour la salle dans la BD
    // BD:  [room.room_id]
    private final int intRoomId;
    // Contient le numbre de minute pour la dur�e des parties dans la salle
    // BD:  [room.masterTime]
    private final int intMasterTime;
    // Contient la liste des "game types" admis dans la salle.
    // BD:  [room_game_types.game_type_id]
    private final Set<Integer> setGameTypeIds;
    // Contient le type de la salle.
    // BD:  [user.role_id] pour le user avec user_id [room.user_id]
    //      De plus user.role_id == 3 est converti en String "profsType"
    //              user.role_id != 3 sont converti en String "General"
    private final String strRoomType;
    // Contient la liste des keyword_id valide pour la banque de questions
    // de la salle.  Seule les questions associ�es � au moins un keyword_id
    // peuvent �tre utilis�es dans la salle.
    // BD:  [room_keywords.keyword_id]
    private final Set<Integer> setKeywordIds;
    // Cet objet est une liste de num�ros utilis�s pour les tables (sert �
    // g�n�rer de nouvelles tables)
    private final HashSet<Integer> lstNoTables;
    // Cet objet est une liste des joueurs qui sont pr�sentement dans cette salle
    private final HashMap<String, JoueurHumain> lstJoueurs;
    // Cet objet est une liste des tables qui sont pr�sentement dans cette salle
    private final HashMap<Integer, Table> lstTables;
    // Un compteur pour le prochain num�ro de table qui sera cr�� dans cette
    // salle.
    private int prochainNoTable;

    /**
     * Une {@code Salle} est un endroit qui contient une ou plusieurs {@link Table}
     * de jeu similaire.  La salle r�git le types de questions pos�es aux
     * tables ainsi que le temps et type de partie qui peuvent y �tre jou�es.
     * Pour construire une salle, elle doit d�j� exister dans la BD donc
     * seul un room_id est n�cessaire parce que le reste de l'information
     * sera extraite automatiquement par le gestionnaire de BD associ�
     * au contr�leur de jeu pour la salle � construire.
     * @param controleurJeu une r�f�rence au contr�leur de jeu qui devra
     *                      g�rer cette salle.
     * @param roomId le room_id pour cette salle tel que sp�cifi� dans la
     *               table 'room' de la BD.
     * @throws java.sql.SQLException si le gestionnaire de BD associ� ne peut
     *                               extraire l'information n�cessaire dans la
     *                               BD pour construire la salle.
     *                               See {@link GestionnaireBD#}
     */
    public Salle(ControleurJeu controleurJeu, int roomId) throws java.sql.SQLException {
        objGestionnaireEvenements = new GestionnaireEvenements();
        objControleurJeu = controleurJeu;
        objGestionnaireBD = controleurJeu.obtenirGestionnaireBD();

        Map<String, Object> roomData = objGestionnaireBD.getRoomInfo(roomId);
        intRoomId = roomId;
        strPassword = (String)roomData.get("password");
        strCreatorUsername = (String)roomData.get("username");
        Object objDate = roomData.get("beginDate");
        if (objDate == null || objDate.toString().isEmpty())
            dateBeginDate = null;
        else
            dateBeginDate = (Date)objDate;
        objDate = roomData.get("endDate");
        if (objDate == null || objDate.toString().isEmpty())
            dateEndDate = null;
        else
            dateEndDate = (Date)objDate;
        intMasterTime = (Integer)roomData.get("masterTime");
        mapRoomLanguageIdToName = (Map<Integer, String>)roomData.get("names");
        mapRoomLanguageIdToDescription = (Map<Integer, String>)roomData.get("descriptions");
        strRoomType = (String)roomData.get("roomType");
        setKeywordIds = objGestionnaireBD.getRoomKeywordIds(roomId);
        setGameTypeIds = objGestionnaireBD.getRoomGameTypeIds(roomId);
        // Cr�er une nouvelle liste de joueurs, de tables et de num�ros
        lstJoueurs = new HashMap<String, JoueurHumain>();
        lstTables = new HashMap<Integer, Table>();
        lstNoTables = new HashSet<Integer>();
        // Cr�er un thread pour le GestionnaireEvenements et le d�marrer.
        (new Thread(objGestionnaireEvenements, "Salle")).start();
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
    private int genererNoTable() {
        // D�claration d'une variable qui va contenir le num�ro de table
        // g�n�r�
        int intNoTable = prochainNoTable + 1;

        // Boucler tant qu'on n'a pas trouv� de num�ro n'�tant pas utilis�
        while (lstNoTables.contains(intNoTable)) {
            intNoTable++;
        }

        setProchainNoTable(intNoTable);
        return intNoTable;
    }

    /**
     * Cette fonction permet de valider que le mot de passe pour entrer dans la
     * salle est correct. On suppose suppose que le joueur n'est pas dans la
     * salle courante. Cette fonction va avoir pour effet de connecter le joueur
     * dans la salle courante.
     *
     * @param joueur : Le joueur demandant d'entrer dans la salle
     * @param motDePasse : Le mot de passe pour entrer dans la salle
     * @param doitGenererNoCommandeRetour : Permet de savoir si on doit
     * 					g�n�rer un num�ro de commande pour le retour de
     * 					l'appel de fonction
     * @return false : Le mot de passe pour entrer dans la salle n'est pas
     * 		   le bon
     * 	   true  : Le joueur a r�ussi � entrer dans la salle
     *
     * @synchronism Cette fonction est synchronis�e pour �viter que deux
     * 		puissent entrer ou quitter une salle en m�me temps.
     * 		On n'a pas � s'inqui�ter que le joueur soit modifi�
     * 		pendant le temps qu'on ex�cute cette fonction. De plus
     * 		on n'a pas � rev�rifier que la salle existe bien (car
     * 		elle ne peut �tre supprim�e) et que le joueur n'est
     * 		pas toujours dans une autre salle (car le protocole
     * 		ne peut pas ex�cuter plusieurs fonctions en m�me temps)
     */
    public boolean entrerSalle(JoueurHumain joueur, String motDePasse, boolean doitGenererNoCommandeRetour) {
        // Si le mot de passe est le bon, alors on ajoute le joueur dans la liste
        // des joueurs de cette salle et on envoit un �v�nement aux autres
        // joueurs de cette salle pour leur dire qu'il y a un nouveau joueur
        if (strPassword.equals(objGestionnaireBD.controlPWD(motDePasse))) {
            // Emp�cher d'autres thread de toucher � la liste des joueurs de
            // cette salle pendant l'ajout du nouveau joueur dans cette salle
            synchronized (lstJoueurs) {
                // Ajouter ce nouveau joueur dans la liste des joueurs de cette salle
                lstJoueurs.put(joueur.obtenirNomUtilisateur(), joueur);
            }

            // Le joueur est maintenant entr� dans la salle courante
            joueur.definirSalleCourante(this);

            // Si on doit g�n�rer le num�ro de commande de retour, alors
            // on le g�n�re, sinon on ne fait rien (�a devrait toujours
            // �tre vrai, donc on le g�n�re tout le temps)
            if (doitGenererNoCommandeRetour == true) {
                // G�n�rer un nouveau num�ro de commande qui sera
                // retourn� au client
                joueur.obtenirProtocoleJoueur().genererNumeroReponse();
            }

            // Pr�parer l'�v�nement de nouveau joueur dans la salle.
            // Cette fonction va passer les joueurs et cr�er un
            // InformationDestination pour chacun et ajouter l'�v�nement
            // dans la file de gestion d'�v�nements
            preparerEvenementJoueurEntreSalle(joueur.obtenirNomUtilisateur());


            //objGestionnaireBD.fillUserLevels(joueur, this);

            return true;
        }
        return false;
    }

    /**
     * Cette m�thode permet au joueur pass� en param�tres de quitter la salle.
     * On suppose que le joueur est dans la salle et qu'il n'est pas en train
     * de jouer dans aucune table.
     *
     * @param joueur : Le joueur demandant de quitter la salle
     * @param doitGenererNoCommandeRetour : Permet de savoir si on doit
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
    public void quitterSalle(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, boolean detruirePartieCourante) {
        //TODO: Peut-�tre va-t-il falloir ajouter une synchronisation ici
        // 	lorsque la commande sortir joueur de la table sera cod�e
        // Si le joueur est en train de jouer dans une table, alors
        // il doit quitter cette table avant de quitter la salle
        if (joueur.obtenirPartieCourante() != null) {
            // Quitter la table courante avant de quitter la salle
            joueur.obtenirPartieCourante().obtenirTable().quitterTable(joueur, false, detruirePartieCourante);
        }

        // Emp�cher d'autres thread de toucher � la liste des joueurs de
        // cette salle pendant que le joueur quitte cette salle
        synchronized (lstJoueurs) {
            // Enlever le joueur de la liste des joueurs de cette salle
            lstJoueurs.remove(joueur.obtenirNomUtilisateur());

            // Le joueur est maintenant dans aucune salle
            joueur.definirSalleCourante(null);

            // Si on doit g�n�rer le num�ro de commande de retour, alors
            // on le g�n�re, sinon on ne fait rien (�a se peut que ce soit
            // faux)
            if (doitGenererNoCommandeRetour == true) {
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
     * @param joueur : Le joueur demandant de cr�er la table
     * @param name
     * @param tempsPartie : Le temps que doit durer la partie
     * @param doitGenererNoCommandeRetour : Permet de savoir si on doit
     * 					g�n�rer un num�ro de commande pour le retour de
     * 					l'appel de fonction
     * @param intNbLines
     * @param intNbColumns
     * @param gameType
     * @return int : Le num�ro de la nouvelle table cr��e
     *
     * @synchronism Cette fonction est synchronis�e pour la liste des tables
     * 		car on va ajouter une nouvelle table et il ne faut pas
     * 		qu'on puisse d�truire une table ou obtenir la liste des
     * 		tables pendant ce temps. On synchronise �galement la
     * 		liste des joueurs de la salle, car on va passer les
     * 		joueurs de la salle et leur envoyer un �v�nement. La
     * 		fonction entrerTable est synchronis�e automatiquement.
     */
    public int creerTable(JoueurHumain joueur, int tempsPartie, boolean doitGenererNoCommandeRetour, String name, int intNbLines, int intNbColumns, String gameType) {
        // D�claration d'une variable qui va contenir le num�ro de la table
        int intNoTable;

        // Emp�cher d'autres thread de toucher � la liste des tables de
        // cette salle pendant la cr�ation de la table
        synchronized (lstTables) {
            //System.out.println("Salle - cree table : " + intNbLines + " " + intNbColumns);
            // Cr�er une nouvelle table en passant les param�tres appropri�s
            Table objTable = new Table(this, genererNoTable(), joueur, tempsPartie, name, intNbLines, intNbColumns, gameType);

            objTable.creation();

            // Ajouter la table dans la liste des tables
            lstTables.put(new Integer(objTable.obtenirNoTable()), objTable);

            // Ajouter le num�ro de la table dans la liste des num�ros de table
            lstNoTables.add(new Integer(objTable.obtenirNoTable()));

            // Si on doit g�n�rer le num�ro de commande de retour, alors
            // on le g�n�re, sinon on ne fait rien (�a devrait toujours
            // �tre vrai, donc on le g�n�re tout le temps)
            if (doitGenererNoCommandeRetour == true) {
                // G�n�rer un nouveau num�ro de commande qui sera
                // retourn� au client
                joueur.obtenirProtocoleJoueur().genererNumeroReponse();
            }

            // Emp�cher d'autres thread de toucher � la liste des tables de
            // cette salle pendant la cr�ation de la table
            synchronized (lstJoueurs) {
                // Pr�parer l'�v�nement de nouvelle table.
                // Cette fonction va passer les joueurs et cr�er un
                // InformationDestination pour chacun et ajouter l'�v�nement
                // dans la file de gestion d'�v�nements
                preparerEvenementNouvelleTable(objTable, joueur.obtenirNomUtilisateur());
            }

            // Entrer dans la table on ne fait rien avec la liste des
            // personnages
            objTable.entrerTable(joueur, false);

            // Garder le num�ro de table pour le retourner
            intNoTable = objTable.obtenirNoTable();
        }

        return intNoTable;
    }

    /**
     * Cette fonction permet au joueur d'entrer dans la table d�sir�e. On
     * suppose que le joueur n'est pas dans aucune table.
     *
     * @param joueur : Le joueur demandant d'entrer dans la table
     * @param noTable : Le num�ro de la table dans laquelle entrer
     * @param doitGenererNoCommandeRetour : Permet de savoir si on doit
     * 					g�n�rer un num�ro de commande pour le retour de
     * 					l'appel de fonction
     * @return Succes : Le joueur est maintenant dans la table
     *         TableNonExistante : Le joueur a tent� d'entrer dans une
     *                             table non existante
     *         TableComplete : Le joueur a tent� d'entrer dans une
     *                         table ayant d�j� le maximum de joueurs
     *         PartieEnCours : Une partie est d�j� en cours dans la
     *                         table d�sir�e
     *
     * @synchronism Cette fonction est synchronis�e sur la liste des tables
     *		pour �viter qu'un joueur puisse commencer � quitter et
     *		que le joueur courant d�bute son entr�e dans la table
     *		courante qui a des chances d'�tre d�truite si le joueur
     *		qui veut quitter est le dernier de la table.
     */
    public String entrerTable(JoueurHumain joueur, int noTable, boolean doitGenererNoCommandeRetour) {
        // D�claration d'une variable qui va contenir le r�sultat � retourner
        // � la fonction appelante, soit les valeurs de l'�num�ration
        // ResultatEntreeTable
        String strResultatEntreeTable;

        // Emp�cher d'autres thread de toucher � la liste des tables de
        // cette salle pendant que le joueur entre dans la table
        synchronized (lstTables) {
            // Si la table n'existe pas dans la salle o� se trouve le joueur,
            // alors il y a une erreur
            if (lstTables.containsKey(new Integer(noTable)) == false) {
                // La table n'existe pas
                strResultatEntreeTable = ResultatEntreeTable.TableNonExistante;
            } // Si la table est compl�te, alors il y a une erreur (aucune
            // synchronisation suppl�mentaire � faire car elle ne peut devenir
            // compl�te ou ne plus l'�tre que par l'entr�e ou la sortie d'un
            // joueur dans la table. Or ces actions sont synchronis�es avec
            // lstTables, donc �a va.
            else if (((Table)lstTables.get(new Integer(noTable))).estComplete() == true) {
                // La table est compl�te
                strResultatEntreeTable = ResultatEntreeTable.TableComplete;
            } //TODO: Cette validation d�pend de l'�tat de la partie (de la table)
            // 		et lorsque cette partie se terminera ou d�butera, son �tat va changer,
            //		il va donc falloir revoir cette validation
            // Si la table n'est pas compl�te et une partie est en cours,
            // alors il y a une erreur
            else if (((Table)lstTables.get(new Integer(noTable))).estCommencee() == true) {
                // Une partie est en cours
                strResultatEntreeTable = ResultatEntreeTable.PartieEnCours;
            } else {
                // Appeler la m�thode permettant d'entrer dans la table
                ((Table)lstTables.get(new Integer(noTable))).entrerTableAutres(joueur, doitGenererNoCommandeRetour);

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
     * @param tableADetruire : La table � d�truire
     *
     * @synchronism Cette fonction n'est pas synchronis�e car elle l'est par
     * 				la fonction qui l'appelle. On synchronise seulement
     * 				la liste des joueurs de cette salle lorsque va venir
     * 				le temps d'envoyer l'�v�nement que la table est d�truite
     * 				aux joueurs de la salle. On n'a pas � s'inqui�ter que la
     * 				table soit modifi�e pendant le temps qu'on ex�cute cette
     * 				fonction, car il n'y a plus personne dans la table.
     */
    public void detruireTable(Table tableADetruire) {
        Table t = (Table)lstTables.get(new Integer(tableADetruire.obtenirNoTable()));
        if (t != null) {
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
        synchronized (lstJoueurs) {
            // Pr�parer l'�v�nement qu'une table a �t� d�truite.
            // Cette fonction va passer les joueurs et cr�er un
            // InformationDestination pour chacun et ajouter l'�v�nement
            // dans la file de gestion d'�v�nements
            System.out.println(" RTest - table " + tableADetruire.obtenirNoTable());
            preparerEvenementTableDetruite(tableADetruire.obtenirNoTable());
        }
    }

    /**
     * Cette fonction permet d'obtenir la liste des joueurs se trouvant dans la
     * salle courante. La vraie liste de joueurs est retourn�e.
     *
     * @return La liste des joueurs se trouvant dans la salle courante
     *
     * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle doit
     * 				l'�tre par l'appelant de cette fonction tout d�pendant
     * 				du traitement qu'elle doit faire
     */
    public HashMap<String, JoueurHumain> obtenirListeJoueurs() {
        return lstJoueurs;
    }

    /**
     * Cette fonction permet d'obtenir la liste des tables se trouvant dans la
     * salle courante. La vraie liste est retourn�e.
     *
     * @return La liste des tables de la salle courante
     *
     * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle doit
     * 				l'�tre par l'appelant de cette fonction tout d�pendant
     * 				du traitement qu'elle doit faire
     */
    public HashMap<Integer, Table> obtenirListeTables() {
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
     * @param nomUtilisateur : Le nom d'utilisateur du joueur qui
     *                                vient d'entrer dans la salle
     *
     * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle l'est
     * 				par l'appelant (entrerSalle).
     */
    private void preparerEvenementJoueurEntreSalle(String nomUtilisateur) {
        // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs qu'un joueur est entr� dans la salle
        EvenementJoueurEntreSalle joueurEntreSalle = new EvenementJoueurEntreSalle(nomUtilisateur);
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Si le nom d'utilisateur du joueur courant n'est pas celui
            // qui vient d'entrer dans la salle, alors on peut envoyer un
            // �v�nement � cet utilisateur
            if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur)) {
                continue;
            }
            // Obtenir un num�ro de commande pour le joueur courant, cr�er
            // un InformationDestination et l'ajouter � l'�v�nement
            joueurEntreSalle.ajouterInformationDestination(
                    new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                    objJoueur.obtenirProtocoleJoueur()));
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
     * @param nomUtilisateur : Le nom d'utilisateur du joueur qui
     * 			  vient de quitter la salle
     *
     * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle l'est
     * 		par l'appelant (quitterSalle).
     */
    private void preparerEvenementJoueurQuitteSalle(String nomUtilisateur) {
        // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs qu'un joueur a quitt� la salle
        EvenementJoueurQuitteSalle joueurQuitteSalle = new EvenementJoueurQuitteSalle(nomUtilisateur);
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur)) {
                continue;
            }
            // Obtenir un num�ro de commande pour le joueur courant, cr�er
            // un InformationDestination et l'ajouter � l'�v�nement
            joueurQuitteSalle.ajouterInformationDestination(
                    new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                    objJoueur.obtenirProtocoleJoueur()));
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
     * @param noTable : Le num�ro de la table cr��
     * @param tempsPartie : Le temps de la partie
     * @param nomUtilisateur : Le nom d'utilisateur du joueur qui
     * 			  a cr�� la table
     *
     * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle l'est
     * 		par l'appelant (creerTable).
     */
    private void preparerEvenementNouvelleTable(Table objTable, String nomUtilisateur) {
        // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs qu'une table a �t� cr��e
        EvenementNouvelleTable nouvelleTable = new EvenementNouvelleTable(objTable);
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Si le nom d'utilisateur du joueur courant n'est pas celui
            // qui vient de cr�er la table, alors on peut envoyer un
            // �v�nement � cet utilisateur
            if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur)) {
                continue;
            }

            // Obtenir un num�ro de commande pour le joueur courant, cr�er
            // un InformationDestination et l'ajouter � l'�v�nement
            nouvelleTable.ajouterInformationDestination(
                    new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                    objJoueur.obtenirProtocoleJoueur()));
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
     * @param noTable : Le num�ro de la table d�truite
     *
     * @synchronism Cette fonction n'est pas synchronis�e ici, mais elle l'est
     * 		par l'appelant (detruireTable).
     */
    private void preparerEvenementTableDetruite(int noTable) {
        // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement
        // aux joueurs qu'une table a �t� cr��e
        EvenementTableDetruite tableDetruite = new EvenementTableDetruite(noTable);
        for (JoueurHumain objJoueur: lstJoueurs.values()) {
            // Obtenir un num�ro de commande pour le joueur courant, cr�er
            // un InformationDestination et l'ajouter � l'�v�nement
            tableDetruite.ajouterInformationDestination(
                    new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
                    objJoueur.obtenirProtocoleJoueur()));
        }
        // Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
        objGestionnaireEvenements.ajouterEvenement(tableDetruite);
    }

    /**
     * Cette fonction permet de d�terminer si la salle poss�de un mot de passe
     * pour y acc�der ou non.
     *
     * @return boolean : true si la salle est prot�g�e par un mot de passe
     *                   false sinon
     */
    public boolean protegeeParMotDePasse() {
        return !(strPassword == null || strPassword.trim().equals(""));
    }

    /**
     * Retourne le nom de la salle dans la langue sp�cifi�.
     * Les langues reconnues sont: "fr", "en"
     * Si la langue sp�cifi�e est {@code null} ou vide on retourne le
     * nom fran�ais suivi d'un / suivi du nom anglais.
     * Si la langue sp�cifi�e est non reconnu on retourne le nom anglais.
     * Si le nom de la salle n'a pas �t� d�finie dans la langue demand�e
     * on retourne un nom vide: ""
     * @param lang la langue voulu pour la valeur de retour ("fr","en","")
     * @return Le nom de la salle dans la (ou les) langue voulue.
     */
    public String getRoomName(String lang) {
        if (lang == null || lang.trim().length() == 0) {
            String frName = mapRoomLanguageIdToName.get(1);
            String enName = mapRoomLanguageIdToName.get(2);
            if (frName == null) {
                frName = "";
            }
            if (enName == null) {
                enName = "";
            }
            return frName + "/" + enName;
        }
        if (lang.equalsIgnoreCase("fr")) {
            String frName = mapRoomLanguageIdToName.get(1);
            if (frName == null) {
                return "";
            }
            return frName;
        }
        String enName = mapRoomLanguageIdToName.get(2);
        if (enName == null) {
            return "";
        }
        return enName;
    }

    /**
     * Retourne la description de la salle dans la langue sp�cifi�.
     * Les langues reconnues sont: "fr", "en"
     * Si la langue sp�cifi�e est {@code null} ou vide on retourne la
     * description fran�aise suivi d'un / suivi de la description anglaise.
     * Si la langue sp�cifi�e est non reconnu on retourne la description anglaise.
     * Si la description de la salle n'a pas �t� d�finie dans la langue demand�e
     * on retourne une description vide: ""
     * @param lang la langue voulu pour la valeur de retour ("fr","en","")
     * @return La description de la salle dans la (ou les) langue voulue.
     */
    public String getRoomDescription(String lang) {
        if (lang == null || lang.trim().length() == 0) {
            String frDesc = mapRoomLanguageIdToDescription.get(1);
            String enDesc = mapRoomLanguageIdToDescription.get(2);
            if (frDesc == null) {
                frDesc = "";
            }
            if (enDesc == null) {
                enDesc = "";
            }
            return frDesc + "/" + enDesc;
        }
        if (lang.equalsIgnoreCase("fr")) {
            String frDesc = mapRoomLanguageIdToDescription.get(1);
            if (frDesc == null) {
                return "";
            }
            return frDesc;
        }
        String enDesc = mapRoomLanguageIdToDescription.get(2);
        if (enDesc == null) {
            return "";
        }
        return enDesc;
    }

    private void setProchainNoTable(int prochainNoTable) {
        this.prochainNoTable = prochainNoTable;
        if (this.prochainNoTable > 999) {
            this.prochainNoTable = 0;
        }
    }

    public ControleurJeu getObjControleurJeu() {
        return objControleurJeu;
    }

    public Table obtenirTable(int intNoTable) {

        return (Table)lstTables.get(new Integer(intNoTable));
    }

    public String getCreatorUsername() {
        return strCreatorUsername;
    }

    public int getRoomId() {
        return intRoomId;
    }
    public Map<Integer,String> getNameMap() {
        return mapRoomLanguageIdToName;
    }
    public Map<Integer,String> getDescriptionMap() {
        return mapRoomLanguageIdToDescription;
    }
    public int getMasterTime() {
        return intMasterTime;
    }

    public Set<Integer> getKeywordIds() {
        return setKeywordIds;
    }

    public Set<Integer> getGameTypeIds() {
        return setGameTypeIds;
    }

    public String getPassword() {
        return strPassword;
    }

    public String getRoomType() {
        return strRoomType;
    }

    public Date getBeginDate() {
        return dateBeginDate;
    }

    public Date getEndDate() {
        return dateEndDate;
    }
}// end class 

