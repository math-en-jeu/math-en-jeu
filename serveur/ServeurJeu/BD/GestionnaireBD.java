package ServeurJeu.BD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import Enumerations.Visibilite;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.BoiteQuestions;
import ServeurJeu.ComposantesJeu.Langue2;
import ServeurJeu.ComposantesJeu.Question;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseCouleur;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesCaseSpeciale;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesMagasin;
import ServeurJeu.ComposantesJeu.ReglesJeu.ReglesObjetUtilisable;
import ServeurJeu.Configuration.GestionnaireConfiguration;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class GestionnaireBD 
{
  
  /*
  private static class GestionnaireBDHolder {
    private static final GestionnaireBD INSTANCE = new GestionnaireBD();
  }
  */
  
  // D�claration d'une r�f�rence vers le contr�leur de jeu
  //@Deprecated
  //private ControleurJeu objControleurJeu;

  // Objet Connection n�cessaire pour le contact avec le serveur MySQL
  //private Connection connexion;

  // Objet Statement n�cessaire pour envoyer une requ�te au serveur MySQL
  //private Statement requete;

  static private Logger objLogger = Logger.getLogger( GestionnaireBD.class );

  private static final String strValeurGroupeAge = "valeurGroupeAge";

  private static Connection mConnection;
  
  public GestionnaireBD(Connection pConnection) {
    mConnection = pConnection;
  }

  


  /**
   * Cette fonction permet de chercher dans la BD si le joueur dont le nom
   * d'utilisateur et le mot de passe sont pass�s en param�tres existe.
   * 
   * @param String nomUtilisateur : Le nom d'utilisateur du joueur
   * @param String motDePasse : Le mot de passe du joueur
   * @return true  : si le joueur existe et que son mot de passe est correct
   *       false : si le joueur n'existe pas ou que son mot de passe n'est 
   *           pas correct
   */
  public boolean joueurExiste(String nomUtilisateur, String motDePasse)
  {

    GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
    String codeErreur = config.obtenirString( "gestionnairebd.code_erreur_inactivite" );

    int count=0;  //compteur du nombre d'essai de la requ�te

    //boucler la requ�te jusqu'� 5 fois si la connexion � MySQL
    //a �t� interrompu du � un manque d'activit� de la connexion
    while(count<5)
    {
      try
      {
        if(count!=0)
        {
          //connexionDB();
        }
        //synchronized( requete )
        //{
          Statement stmt = mConnection.createStatement();
          ResultSet rs = stmt.executeQuery("SELECT * FROM joueur WHERE alias = '" + nomUtilisateur + "' AND motDePasse = '" + motDePasse + "';");
          return rs.next();
        //}
      }
      catch (SQLException e)
      {
        //on v�rifie l'�tat de l'exception 
        //si l'�tat est �gal au codeErreur
        //on peut r�esayer la connexion
        if(e.getSQLState().equals(codeErreur))
        {
          count++;
        }
        else
        {
          // Une erreur est survenue lors de l'ex�cution de la requ�te
          objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
          objLogger.error(GestionnaireMessages.message("bd.trace"));
          objLogger.error( e.getMessage() );
          e.printStackTrace();
          return false; 
        }
      }
    }
    return false;
  }

  /**
   * Cette fonction permet de chercher dans la BD le joueur et de remplir
   * les champs restants du joueur.
   * 
   * @param JoueurHumain joueur : Le joueur duquel il faut trouver les
   *                informations et les d�finir dans l'objet
   */
  public void remplirInformationsJoueur(JoueurHumain joueur)
  {
    
    try
    {
      Statement requete = mConnection.createStatement();

        ResultSet rs = requete.executeQuery("SELECT cleJoueur, prenom, nom, cleNiveau, peutCreerSalles FROM joueur WHERE alias = '" + joueur.obtenirNomUtilisateur() + "';");
        if (rs.next())
        {
          if (rs.getInt("peutCreerSalles") != 0)
          {
            joueur.definirPeutCreerSalles(true);
          }
          String prenom = rs.getString("prenom");
          String nom = rs.getString("nom");
          int cle = Integer.parseInt(rs.getString("cleJoueur"));
          String cleNiveau = rs.getString( "cleNiveau" );
          joueur.definirPrenom(prenom);
          joueur.definirNomFamille(nom);
          joueur.definirCleJoueur(cle);
          joueur.definirCleNiveau( cleNiveau );
        }
      
    }
    catch (SQLException e)
    {
      // Une erreur est survenue lors de l'ex�cution de la requ�te
      objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();      
    }
  }

  
  //TODO : load questions using the groups for the current rooms
  public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String niveau ) {
    String strRequeteSQL = "SELECT question.*,question_details.* , typereponse.nomType FROM question, question_details" +
      ",typereponse WHERE typereponse.cleType = question.typeReponse and question_details.valide = 1 " +
      "and FichierFlashQuestion is not NULL and FichierFlashReponse is not NULL and " +
      "question.cleQuestion = question_details.id and langue_id = " + boiteQuestions.obtenirLangue().getId();
    
    strRequeteSQL += " and " + strValeurGroupeAge + niveau + " > 0";
    
    
    remplirBoiteQuestions( boiteQuestions, niveau, strRequeteSQL );
    
  }
  
  
  public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String niveau, int intDifficulte )
  {
    // Noter qu'on ne tient plus compte de la cat�gorie!!
    String nomTable = "question"; //boiteQuestions.obtenirLangue().obtenirNomTableQuestionsBD();

    String strRequeteSQL = "SELECT " + nomTable + ".*,typereponse.nomType FROM " + nomTable + ",typereponse " +
    "WHERE typereponse.cleType = " + nomTable + ".typeReponse and " + nomTable + ".valide = 1 " +
    "and FichierFlashQuestion is not NULL and FichierFlashReponse is not NULL ";

    /*
    strRequeteSQL += "and cleQuestion >= " +
    boiteQuestions.obtenirLangue().obtenirCleQuestionMin() + " and cleQuestion <= " +
    boiteQuestions.obtenirLangue().obtenirCleQuestionMax() + " and ";

    */
    strRequeteSQL += strValeurGroupeAge + niveau + " = " + intDifficulte;
    
    remplirBoiteQuestions( boiteQuestions, niveau, strRequeteSQL );
  }
  
  
  // This method fills a Question box with only the player's level
  /*
  public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String niveau )
  {
      
    
    String nomTable = boiteQuestions.obtenirLangue().obtenirNomTableQuestionsBD();
    String strRequeteSQL = "SELECT " + nomTable + ".*,typereponse.nomType FROM " + nomTable +
    ",typereponse WHERE typereponse.cleType = " + nomTable + ".typeReponse and " + nomTable + ".valide = 1 " +
    "and FichierFlashQuestion is not NULL and FichierFlashReponse is not NULL and ";


    strRequeteSQL += "cleQuestion >= " + boiteQuestions.obtenirLangue().obtenirCleQuestionMin()
    + " and cleQuestion <= " + boiteQuestions.obtenirLangue().obtenirCleQuestionMax()
    + " and ";

    strRequeteSQL += strValeurGroupeAge + niveau + " > 0";
    
    

    remplirBoiteQuestions( boiteQuestions, niveau, strRequeteSQL );
  }
*/
  /*
  // This function fills a Question box with the player's level, a specified difficulty and a question category
  public void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String niveau, int intCategorie, int intDifficulte )
  {
    // Noter qu'on ne tient plus compte de la cat�gorie!!
    String nomTable = boiteQuestions.obtenirLangue().obtenirNomTableQuestionsBD();

    String strRequeteSQL = "SELECT " + nomTable + ".*,typereponse.nomType FROM " + nomTable + ",typereponse " +
    "WHERE typereponse.cleType = " + nomTable + ".typeReponse and " + nomTable + ".valide = 1 " +
    "and FichierFlashQuestion is not NULL and FichierFlashReponse is not NULL ";

    strRequeteSQL += "and cleQuestion >= " +
    boiteQuestions.obtenirLangue().obtenirCleQuestionMin() + " and cleQuestion <= " +
    boiteQuestions.obtenirLangue().obtenirCleQuestionMax() + " and ";

    strRequeteSQL += strValeurGroupeAge + niveau + " = " + intDifficulte;
    remplirBoiteQuestions( boiteQuestions, niveau, strRequeteSQL );
  }
  */

  // This function follows one of the two previous functions. It queries the database and
  // does the actual filling of the question box.
  private void remplirBoiteQuestions( BoiteQuestions boiteQuestions, String niveau, String strRequeteSQL )
  { 
    String lUrl = GestionnaireConfiguration.obtenirInstance().obtenirString("controleurjeu.url-question");
    try
    {
      Statement requete = mConnection.createStatement();
        ResultSet rs = requete.executeQuery( strRequeteSQL );
        while(rs.next())
        {
          int codeQuestion = rs.getInt("cleQuestion");
          //String typeQuestion = TypeQuestion.ChoixReponse; //TODO aller chercher code dans bd
          String typeQuestion = rs.getString( "nomType" );
          String question = rs.getString( "FichierFlashQuestion" );
          String reponse = rs.getString("bonneReponse");
          String explication = rs.getString("FichierFlashReponse");
          int difficulte = rs.getInt( strValeurGroupeAge + niveau );
          //TODO la categorie???
          boiteQuestions.ajouterQuestion(new Question(codeQuestion, typeQuestion, difficulte, lUrl+question, reponse, lUrl+explication));
        }
      
    }
    catch (SQLException e)
    {
      // Une erreur est survenue lors de l'ex�cution de la requ�te
      objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();      
    }
    catch( RuntimeException e)
    {
      //Une erreur est survenue lors de la recherche de la prochaine question
      objLogger.error(GestionnaireMessages.message("bd.erreur_prochaine_question"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();
    }
  }

  // This function queries the DB to find the player's musical preferences
  // and returns a Vector containing URLs of MP3s the player might like
  public Vector obtenirListeURLsMusique(int cleJoueur)
  {
    Vector<String> liste = new Vector<String>();
    
    String URLMusique = GestionnaireConfiguration.obtenirInstance().obtenirString("musique.url");
    String strRequeteSQL = "SELECT musique_Fichiers.nomFichier FROM musique_Fichiers,musique_Fichiers_Categories,musique_Categories,musique_Categorie_Joueur WHERE ";
    strRequeteSQL       += "musique_Fichiers.cleFichier = musique_Fichiers_Categories.cleFichier AND ";
    strRequeteSQL       += "musique_Fichiers_Categories.cleCategorie = musique_Categories.cleCategorie AND ";
    strRequeteSQL       += "musique_Categories.cleCategorie = musique_Categorie_Joueur.cleCategorie AND ";
    strRequeteSQL       += "musique_Categorie_Joueur.cleJoueur = " + Integer.toString(cleJoueur);
    try
    {
      Statement requete = mConnection.createStatement();

        ResultSet rs = requete.executeQuery(strRequeteSQL);
        while(rs.next())
        {
          liste.add(URLMusique + rs.getString("nomFichier"));
        }
      
    }
    catch (SQLException e)
    {
      // Une erreur est survenue lors de l'ex�cution de la requ�te
      objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();      
    }
    catch( RuntimeException e)
    {
      // Ce n'est pas le bon message d'erreur mais ce n'est pas grave
      objLogger.error(GestionnaireMessages.message("bd.error_music"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();
    }
    return liste;
  }

  // This method updates a player's information in the DB
  public void mettreAJourJoueur( JoueurHumain joueur, int tempsTotal )
  {
    try
    {
      Statement requete = mConnection.createStatement();

        ResultSet rs = requete.executeQuery("SELECT partiesCompletes, meilleurPointage, tempsPartie FROM joueur WHERE alias = '" + joueur.obtenirNomUtilisateur() + "';");
        if (rs.next())
        {
          int partiesCompletes = rs.getInt( "partiesCompletes" ) + 1;
          int meilleurPointage = rs.getInt( "meilleurPointage" );
          int pointageActuel = joueur.obtenirPartieCourante().obtenirPointage();
          if( meilleurPointage < pointageActuel )
          {
            meilleurPointage = pointageActuel;
          }

          int tempsPartie = tempsTotal + rs.getInt("tempsPartie");

          //mise-a-jour
          int result = requete.executeUpdate( "UPDATE joueur SET partiesCompletes=" + partiesCompletes + ",meilleurPointage=" + meilleurPointage + ",tempsPartie=" + tempsPartie + " WHERE alias = '" + joueur.obtenirNomUtilisateur() + "';");
        }
    }
    catch (SQLException e)
    {
      // Une erreur est survenue lors de l'ex�cution de la requ�te
      objLogger.error(GestionnaireMessages.message("bd.erreur_exec_requete"));
      objLogger.error(GestionnaireMessages.message("bd.trace"));
      objLogger.error( e.getMessage() );
      e.printStackTrace();      
    }
  }


  /* Cette fonction permet d'ajouter les information sur une partie dans 
   * la base de donn�es dans la table partie. 
   *
   * Retour: la cl� de partie qui servira pour la table partieJoueur
   */
  public int ajouterInfosPartiePartieTerminee(Date dateDebut, int dureePartie)
  {

    SimpleDateFormat objFormatDate = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat objFormatHeure = new SimpleDateFormat("HH:mm:ss");

    String strDate = objFormatDate.format(dateDebut);
    String strHeure = objFormatHeure.format(dateDebut);

    // Cr�ation du SQL pour l'ajout
    String strSQL = "INSERT INTO partie(datePartie, heurePartie, dureePartie) VALUES ('" + 
    strDate + "','" + strHeure + "'," + dureePartie + ")";

    try
    {
      Statement requete = mConnection.createStatement();


        // Ajouter l'information pour cette partie
        requete.executeUpdate(strSQL, Statement.RETURN_GENERATED_KEYS);

        // Aller chercher la cl� de partie qu'on vient d'ajouter
        ResultSet  rs = requete.getGeneratedKeys();

        // On retourne la cl� de partie
        rs.next();
        return Integer.parseInt(rs.getString("GENERATED_KEY"));
      
    }
    catch (Exception e)
    {
      System.out.println(GestionnaireMessages.message("bd.erreur_ajout_infos") + e.getMessage());
    }

    // Au cas o� il y aurait erreur, on retourne -1
    return -1;
  }

  /* Cette fonction permet d'ajouter les informations sur une partie pour
   * un joueur dans la table partieJoueur;
   *
   */
  public void ajouterInfosJoueurPartieTerminee(int clePartie, int cleJoueur, int pointage, boolean gagner)
  {
    int intGagner = 0;
    if (gagner == true)
    {
      intGagner = 1;
    }

    // Cr�ation du SQL pour l'ajout
    String strSQL = "INSERT INTO partiejoueur(clePartie, cleJoueur, pointage, gagner) VALUES " +
    "(" + clePartie + "," + cleJoueur + "," + pointage + "," + intGagner + ");";

    try
    {

      Statement requete = mConnection.createStatement();

        // Ajouter l'information pour ce joueur
        requete.executeUpdate(strSQL);
    }
    catch (Exception e)
    {
    }
  }
  
  /**
   * Load a langue given its short name
   * @param pShortName the short name of a langage (ex : fr,en,jp
   * @return a langue object
   */
  public Langue2 loadLangue(String pShortName) {
    Langue2 lResult = null;
    
    String lSql = "select * from langues where nom_court = '" + pShortName + "'";
    
    try {
      Statement requete = mConnection.createStatement();
      synchronized (requete) {
        ResultSet lRs = requete.executeQuery(lSql);
        
        if (lRs.next()) {
          lResult = new Langue2(lRs.getInt("id"), lRs.getString("nom"),lRs.getString("nom_court"));
          
        }
      }

    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    }
    
    return lResult;
  }
    
  
  /**
   * Add the rules for the color squares and return the modified object
   * @param pRules
   * @return the modified TreeSet containing the rules for the color squares
   */
  private TreeSet<ReglesCaseCouleur> loadRuleColorSquare(Regles pRules) {
    
    TreeSet<ReglesCaseCouleur> lResult = pRules.obtenirListeCasesCouleurPossibles();
    
    Statement lStatement = null;
    String lSql = "select * from regles_case_couleur";
    try {
      
      lStatement = mConnection.createStatement();
      ResultSet lRs = lStatement.executeQuery(lSql);
        
      while (lRs.next()) {
        lResult.add(new ReglesCaseCouleur(lRs.getInt("priorite"), lRs.getInt("type")));
      }

    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }
    
    return lResult;
  }
  
  /**
   * Add the rules for the special squares and return the modified object
   * @param pRules
   * @return the modified TreeSet containing the rules for the special squares
   */
  private TreeSet<ReglesCaseSpeciale> loadRuleSpecialSquare(Regles pRules) {
        
    TreeSet<ReglesCaseSpeciale> lResult = pRules.obtenirListeCasesSpecialesPossibles();
    
    Statement lStatement = null;
    String lSql = "select * from regles_case_special";
    try {
      lStatement = mConnection.createStatement();
      ResultSet lRs = lStatement.executeQuery(lSql);

      while (lRs.next()) {
        lResult.add(new ReglesCaseSpeciale(lRs.getInt("priorite"), lRs.getInt("type")));
      }
      
    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }
    
    return lResult;
  }
  
  
  /**
   * Rooms are loaded directly in the ControleurJeu list of rooms 
   * @param pGameType the type of game to load room for
   */
  public void loadRooms(String pGameType) {
    
    String lSql = "select r.*, s.id as salle_id, s.nom as table_name, s.password,j.alias "
                  + " from salles s, type_jeu tj, joueur j, regles r " 
                  + " where s.type_jeu_id = tj.id and j.cleJoueur = s.joueur_id and s.regle_id = r.id and tj.nom = '" + pGameType + "'"
                  + " order by s.nom";
    
    Statement lStatement = null;
    Statement lStatShop = null;
    Statement lStatObject = null;
    Statement lStatDescription = null;
    

    try {

      lStatement = mConnection.createStatement();
      lStatShop = mConnection.createStatement();
      lStatObject = mConnection.createStatement();
      lStatDescription = mConnection.createStatement();
      ResultSet lRs = lStatement.executeQuery(lSql);
      
      while (lRs.next()) {
        
        //check if the room is not already loaded
        if (!ControleurJeu.getInstance().salleExiste(lRs.getString("table_name"))) {
        
          Regles objReglesSalle = new Regles();
          TreeSet<ReglesObjetUtilisable> objetsUtilisables = objReglesSalle.obtenirListeObjetsUtilisablesPossibles();
          TreeSet<ReglesMagasin> magasins = objReglesSalle.obtenirListeMagasinsPossibles();
  
          loadRuleColorSquare(objReglesSalle);
          loadRuleSpecialSquare(objReglesSalle);
          
          objReglesSalle.definirPermetChat(lRs.getInt("chat") == 1 ? true : false);
          objReglesSalle.definirRatioTrous(lRs.getFloat("ratio_trou"));
          objReglesSalle.definirRatioMagasins(lRs.getFloat("ratio_magasin"));
          objReglesSalle.definirRatioCasesSpeciales(lRs.getFloat("ratio_case_special"));
          objReglesSalle.definirRatioPieces(lRs.getFloat("ratio_piece"));
          objReglesSalle.definirRatioObjetsUtilisables(lRs.getFloat("ratio_objet_utilisable"));
          objReglesSalle.definirValeurPieceMaximale(lRs.getInt("valeur_maximal_piece"));
          objReglesSalle.definirTempsMinimal(lRs.getInt("temps_minimal"));
          objReglesSalle.definirTempsMaximal(lRs.getInt("temps_maximal"));
          objReglesSalle.definirDeplacementMaximal(lRs.getInt("deplacement_maximal"));
          objReglesSalle.definirMaxObjet(lRs.getInt("max_possession_objets_pieces"));
          
          //load the usable object for this room
          
          lSql = "select o.nom, so.priorite from objets o, salles_objets so, salles s where " 
            + "o.id = so.objet_id and so.salle_id = s.id and s.id=" + lRs.getInt("salle_id");
          ResultSet lRsObjet = lStatShop.executeQuery(lSql);
          while (lRsObjet.next()) {
            objetsUtilisables.add(new ReglesObjetUtilisable(lRsObjet.getInt("priorite"), lRsObjet.getString("nom"), Visibilite.Aleatoire));
          }
  
          //load the shops
          lSql = "select m.nom, sm.priorite from salles s, magasins m, salles_magasins sm where "
            + "m.id = sm.magasin_id and sm.salle_id = s.id and s.id=" + lRs.getInt("salle_id");
  
          ResultSet lRsMagasin = lStatObject.executeQuery(lSql);
          while (lRsMagasin.next()) {
            magasins.add(new ReglesMagasin(lRsMagasin.getInt("priorite"), lRsMagasin.getString("nom")));
          }
          
          //load the description for this room
          lSql = "select sd.description, l.nom_court from langues l, salles s, salle_detail sd "
                 + " where sd.salle_id = s.id and l.id = sd.langue_id and s.id =" + lRs.getInt("salle_id");
          
          ResultSet lRsDescription = lStatDescription.executeQuery(lSql);
          Map<String, String> lDescriptions = new TreeMap<String, String>();
          while (lRsDescription.next()) {
            lDescriptions.put(lRsDescription.getString("nom_court"), lRsDescription.getString("description"));
          }
          
          Salle lSalle = new Salle(lRs.getString("table_name"), 
                                   lRs.getString("alias"), 
                                   lRs.getString("password"),
                                   lDescriptions,
                                   objReglesSalle,
                                   pGameType);
          
          
          
          ControleurJeu.getInstance().ajouterNouvelleSalle(lSalle);
          
        }

      }
      
    } catch (SQLException e) {
      objLogger.log(Level.FATAL, e.getMessage(), e);
    } finally {
      if (lStatement != null) {
        try {
          lStatement.close();
        } catch (SQLException e) {
          objLogger.log(Level.FATAL, e.getMessage(), e);
        }
      }
    }
    
  }
  

}

