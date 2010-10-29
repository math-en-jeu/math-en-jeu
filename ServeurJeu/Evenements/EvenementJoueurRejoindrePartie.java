package ServeurJeu.Evenements;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ClassesUtilitaires.UtilitaireXML;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Oloieri Lilian
 */

public class EvenementJoueurRejoindrePartie extends Evenement{
	// D�claration d'une variable qui va garder le nom d'utilisateur du 
	// joueur qui a rejoindre la partie
    private String strNomUtilisateur;
    
	// D�claration d'une variable qui va garder le num�ro Id du personnage 
	// choisi par le joueur
    private int intIdPersonnage;
    
    // D�claration d'une variable qui va garder les points du joueur
    private int intPointage;
    
    // D�claration d'une variable qui va garder le role du joueur
    private int userRole;
    
    // D�claration d'une variable qui va garder la clocolor du joueur
    private String userColor;
    
    /**
     * Constructeur de la classe EvenementJoueurDemarrePartie qui permet 
     * d'initialiser le num�ro Id du personnage et le nom d'utilisateur du 
     * joueur qui vient de d�marrer la partie. 
     */
    public EvenementJoueurRejoindrePartie(String nomUtilisateur, int idPersonnage, int pointage, int role, String color)
    {
        // D�finir le num�ro Id du personnage et le nom d'utilisateur du joueur 
    	// qui a d�marr� la partie
    	intIdPersonnage = idPersonnage;
        strNomUtilisateur = nomUtilisateur;
        intPointage = pointage;
        userRole = role;
        userColor = color;
    }
	
	/**
	 * Cette fonction permet de g�n�rer le code XML de l'�v�nement d'un joueur
	 * qui rejoindre une partie et de le retourner.
	 * 
	 * @param InformationDestination information : Les informations � qui 
	 * 					envoyer l'�v�nement
	 * @return String : Le code XML de l'�v�nement � envoyer
	 */
	protected String genererCodeXML(InformationDestination information)
	{
	    // D�claration d'une variable qui va contenir le code XML � retourner
	    String strCodeXML = "";
	    
		try
		{
	        // Appeler une fonction qui va cr�er un document XML dans lequel 
		    // on peut ajouter des noeuds
	        Document objDocumentXML = UtilitaireXML.obtenirDocumentXML();

			// Cr�er le noeud de commande � retourner
			Element objNoeudCommande = objDocumentXML.createElement("commande");
			
			// Cr�er les noeuds de param�tre
			Element objNoeudParametreIdPersonnage = objDocumentXML.createElement("parametre");
			Element objNoeudParametreNomUtilisateur = objDocumentXML.createElement("parametre");
			Element objNoeudParametrePointage = objDocumentXML.createElement("parametre");
			Element objNoeudParametreCloColor = objDocumentXML.createElement("parametre");
			Element objNoeudParametreRole = objDocumentXML.createElement("parametre");
			
			// Cr�er des noeuds texte contenant le num�ro Id du personnage et le 
			// nom d'utilisateur des noeuds param�tre
			Text objNoeudTexteIdPersonnage = objDocumentXML.createTextNode(Integer.toString(intIdPersonnage));
			Text objNoeudTexteNomUtilisateur = objDocumentXML.createTextNode(strNomUtilisateur);
			Text objNoeudTextePointage = objDocumentXML.createTextNode(Integer.toString(intPointage));
			Text objNoeudTexteRole = objDocumentXML.createTextNode(Integer.toString(userRole));
			Text objNoeudTexteColor = objDocumentXML.createTextNode(userColor);
			
			// D�finir les attributs du noeud de commande
			objNoeudCommande.setAttribute("no", Integer.toString(information.obtenirNoCommande()));
			objNoeudCommande.setAttribute("type", "Evenement");
			objNoeudCommande.setAttribute("nom", "JoueurRejoindrePartie");
			
			// On ajoute un attribut type qui va contenir le type
			// du param�tre
			objNoeudParametreIdPersonnage.setAttribute("type", "IdPersonnage");
			objNoeudParametreNomUtilisateur.setAttribute("type", "NomUtilisateur");
			objNoeudParametrePointage.setAttribute("type", "Pointage");
			objNoeudParametreCloColor.setAttribute("type", "Color");
			objNoeudParametreRole.setAttribute("type", "Role");
			
			// Ajouter les noeuds texte aux noeuds des param�tres
			objNoeudParametreIdPersonnage.appendChild(objNoeudTexteIdPersonnage);
			objNoeudParametreNomUtilisateur.appendChild(objNoeudTexteNomUtilisateur);
			objNoeudParametrePointage.appendChild(objNoeudTextePointage);
			objNoeudParametreRole.appendChild(objNoeudTexteRole);
			objNoeudParametreCloColor.appendChild(objNoeudTexteColor);
			
			// Ajouter les noeuds param�tres au noeud de commande
			objNoeudCommande.appendChild(objNoeudParametreNomUtilisateur);
			objNoeudCommande.appendChild(objNoeudParametreIdPersonnage);
			objNoeudCommande.appendChild(objNoeudParametrePointage);
			objNoeudCommande.appendChild(objNoeudParametreRole);
			objNoeudCommande.appendChild(objNoeudParametreCloColor);
			
			// Ajouter le noeud de commande au noeud racine dans le document
			objDocumentXML.appendChild(objNoeudCommande);

			// Transformer le document XML en code XML
			strCodeXML = UtilitaireXML.transformerDocumentXMLEnString(objDocumentXML);
		}
		catch (TransformerConfigurationException tce)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_transformation"));
		}
		catch (TransformerException te)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_conversion"));
		}
		
		return strCodeXML;
	}

	
}
