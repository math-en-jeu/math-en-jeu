﻿
/*******************************************************************
Math en jeu
Copyright (C) 2007 Projet SMAC

Ce programme est un logiciel libre ; vous pouvez le
redistribuer et/ou le modifier au titre des clauses de la
Licence Publique Generale Affero (AGPL), telle que publiee par
Affero Inc. ; soit la version 1 de la Licence, ou (a
votre discretion) une version ulterieure quelconque.

Ce programme est distribue dans l'espoir qu'il sera utile,
mais SANS AUCUNE GARANTIE ; sans meme une garantie implicite de
COMMERCIABILITE ou DE CONFORMITE A UNE UTILISATION
PARTICULIERE. Voir la Licence Publique
Generale Affero pour plus de details.

Vous devriez avoir recu un exemplaire de la Licence Publique
Generale Affero avec ce programme; si ce n'est pas le cas,
ecrivez a Affero Inc., 510 Third Street - Suite 225,
San Francisco, CA 94107, USA.

Changed 2009 Oloieri Lilian
*********************************************************************/

import mx.transitions.Tween;
import mx.transitions.easing.*;
import flash.filters.*;
import mx.controls.Loader;



class Personnage
{
	private var role:Number; // role of user if 1 - simple user , if 2 - master(admin)
	public  var image:MovieClip;
	private var position:Point;
	private var numero:Number;           // ????
	private var prochainePosition:Point;
	private var l:Number;
	private var c:Number;
	private var pointage:Number;
	private var argent:Number;
	private var listeDesObjets:Object;
	private var faireCollision:String;   // sert a savoir s'il y a eu collision apres un deplacement et avec quoi
	private var nom:String;               // name of user that is master of pers
	private var boardCentre:Boolean;
	private var listeSurMagasin:Array;	 // sert a recuperer la liste d'objets du magasin lorsque qu'on va sur une case magasin
	private var minigameLoade:Boolean;
	private var clothesColor:String;
	private var braniacState:Boolean;
	private var repostCases:Boolean;
		
		
	function setRepostCases(repost:Boolean)
	{
		this.repostCases = repost;
	}
	
	function getRepostCases():Boolean
	{
		return this.repostCases;
	}
	
	function setBoardCentre(centre:Boolean)
	{
		this.boardCentre = centre;
	}
	
	function getBoardCentre():Boolean
	{
		return this.boardCentre;
	}
	
	function setColor(n:String)
	{
		clothesColor = n;
	}
	
	function getColor():String
	{
		return clothesColor;
	}
	
	function setRole(n:Number)
	{
		role = n;
	}
	
	function getRole():Number
	{
		return role;
	}
	
	function setBraniac(stateVar:Boolean)
	{
		braniacState = stateVar;
	}
	
	function getBraniac():Boolean
	{
		return braniacState;
	}
   
	////////////////////////////////////////////////////////////
	function obtenirNom():String
	{
		return this.nom;
	}
	       
	////////////////////////////////////////////////////////////
	function acheterObjet(id:Number, objectName:String)
	{
		listeDesObjets[objectName].push(id);
		ajouterImageBanque(objectName);
	}
	
	////////////////////////////////////////////////////////////    
	function afficherObjets()
	{
		var i:Number;
		trace("debut afficherObjets\n**********")
		for(var nom:String in listeDesObjets)
		{
			for(i = 0; i < listeDesObjets[nom].length; i++) 
			trace(listeDesObjets[nom][i] + " - id : ");
		}
		trace("**********\nfin afficherObjets")
	}
	
	////////////////////////////////////////////////////////////   
	function obtenirNombreObjet()
	{
		var total:Number = 0;
		for(var nom:String in listeDesObjets)
		{
			total += listeDesObjets[nom].length; 			
		}
		
		trace("total : " + total);
		return total;
	}
	
	////////////////////////////////////////////////////////////
	function ajouterObjet(id:Number, objectName:String)
	{
		listeDesObjets[objectName].push(id);
		ajouterImageBanque(objectName);
	}
	
	////////////////////////////////////////////////////////////
	function obtenirObjets():Object
	{
		return this.listeDesObjets;
	}
	
	
	function objectInListe(nomObj:String):Boolean
	{
		return this.listeDesObjets[nomObj].length >= 1;
		
	}
	
	////////////////////////////////////////////////////////////
	// - sert a ajouter une objet a la banque d'objets du personnage
	
	function ajouterImageBanque(nomObj:String)
	{		
		trace("--- ds ajouterImageBanque ! ---");
			
		_level0.loader.contentHolder.objectMenu[nomObj].countTxt = Number(_level0.loader.contentHolder.objectMenu[nomObj].countTxt) + 1;
				
		function peutUtiliserObjet(nomObjet:String):Boolean
		{
			switch(nomObjet)
			{
				case "pieceFixe":
					return false;
				case "PotionGros":
					return true;
				case "PotionPetit":
					return true;
				case "Banane":
					return true;
				case "Livre":
					{
						if(_level0.loader.contentHolder.objGestionnaireEvenements.obtenirGestionnaireCommunication().obtenirEtatClient() != Etat.ATTENTE_REPONSE_QUESTION.no) return false;
						if((_level0.loader.contentHolder.type_question != "MULTIPLE_CHOICE") && (_level0.loader.contentHolder.type_question != "MULTIPLE_CHOICE_3") && (_level0.loader.contentHolder.type_question != "MULTIPLE_CHOICE_5" )) return false;
						return true;
					}
				case "Boule":
					{
						if(_level0.loader.contentHolder.objGestionnaireEvenements.obtenirGestionnaireCommunication().obtenirEtatClient() != Etat.ATTENTE_REPONSE_QUESTION.no) return false;
						return true;
					}
				default: return true;
			}
			return false;
		}
		
		var listeObjets:Object = this.obtenirObjets();
				
		// function used to make action of the object on that on click
		_level0.loader.contentHolder.objectMenu[nomObj + "_mc"].onRelease = function()
		{
			if(peutUtiliserObjet(nomObj)&& (listeObjets[nomObj].length >= 1))
			{
				if(nomObj == "Banane"){
                      var objID:Object = new Object();
					  objID.id = listeObjets[nomObj][listeObjets[nomObj].length - 1];
					 
			          var bananaClip:MovieClip;
                      bananaClip = _level0.loader.contentHolder.attachMovie("bananaToss", "toss", 2021, objID);
					  
					  bananaClip._x = 22;
                      bananaClip._y = 70;
					  
		        }else{
					_level0.loader.contentHolder.objGestionnaireEvenements.utiliserObjet(listeObjets[nomObj][listeObjets[nomObj].length - 1], "NA");
					_level0.loader.contentHolder.planche.obtenirPerso().enleverObjet(nomObj);
				}
				
				
				_level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._xscale = _level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._yscale = 100;
				_level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._alpha = 100;
			}//end 1st if
		};
		/*
		// fonctions transfered to mathemaquoi for hovers needs
		_level0.loader.contentHolder.objectMenu[nomObj + "_mc"].onRollOver = function()
		{
			var toolTipMess:String = _root.texteSource_xml.firstChild.attributes["objectMessage" + nomObj];

			_level0.loader.contentHolder.objGestionnaireEvenements.drawToolTip(toolTipMess, _level0.loader.contentHolder.objectMenu[nomObj + "_mc"]);
	
	        _level0.loader.contentHolder.objectMenu.onEnterFrame = function(){ 
                _level0.loader.contentHolder.toolTip._x = _xmouse;
                _level0.loader.contentHolder.toolTip._y = _ymouse - 40;
		        _level0.loader.contentHolder.toolTip._visible = true;
			}
			
			if(peutUtiliserObjet(nomObj) && (listeObjets[nomObj].length >= 1))
			{
				_level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._alpha = 60;
				_level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._xscale = _level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._yscale = 120;
				
			}
		};
		
		_level0.loader.contentHolder.objectMenu[nomObj + "_mc"].onRollOut = function()
		{
			
			 _level0.loader.contentHolder.toolTip._visible = false;
	         _level0.loader.contentHolder.toolTip.swapDepths(_level0.loader.contentHolder.objectMenu[nomObj + "_mc"]);
             _level0.loader.contentHolder.toolTip.removeMovieClip();
			 
			 delete _level0.loader.contentHolder.objectMenu.onEnterFrame;
	
   
			if(peutUtiliserObjet(nomObj) && (listeObjets[nomObj].length >= 1))
			{
				_level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._alpha = 100;
				_level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._xscale = _level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._yscale = 100;
			}
		}; */
		
		trace("--- FIN ajouterImageBanque ! ---");
	}
	
	
	
	////////////////////////////////////////////////////////////
	// -sert a enlever un objet a un personnage :
	// on enleve les donnees relatives a cet objet dans les listes
	// du personnage. on enleve ensuite l'image de l'objet
	//
	function enleverObjet(n:String)
	{
		var i:Number;
		
		trace("liste d'objets avant avoir enlever 1 obj :");
		afficherObjets();
		
		listeDesObjets[n].pop();
		_level0.loader.contentHolder.objectMenu[n].countTxt = Number(_level0.loader.contentHolder.objectMenu[n].countTxt) - 1;
		
		
		trace("liste d'objets apres avoir enlever 1 obj :");
		afficherObjets();
	}
	
	////////////////////////////////////////////////////////////
	function zoomer(valeur:Number)
	{		
		image._xscale += valeur;
		image._yscale += valeur;
	}
	
	////////////////////////////////////////////////////////////
	function translater(la:Number, ha:Number)
	{
		image._x += la;
		image._y += ha;
		
		this.position.definirX(position.obtenirX()+la);
		this.position.definirY(position.obtenirY()+ha);
		this.prochainePosition.definirX(prochainePosition.obtenirX()+la);
		this.prochainePosition.definirY(prochainePosition.obtenirY()+ha);
	}
	
	////////////////////////////////////////////////////////////
	function obtenirL():Number
	{
		return l;
	}
	
	////////////////////////////////////////////////////////////
	function definirL(ll:Number)
	{
		l = ll;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirC():Number
	{
		return c;
	}
	
	////////////////////////////////////////////////////////////
	function definirC(cc:Number)
	{
		c = cc;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirNumero():Number
	{
		return numero;
	}
	
	////////////////////////////////////////////////////////////
	function definirNumero(n:Number)
	{
		numero = n;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirPointage():Number
	{
		return pointage;
	}
	
	////////////////////////////////////////////////////////////
	function modifierPointage(x:Number)
	{
		pointage = x;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirArgent():Number
	{
		return argent;
	}
	
	
	////////////////////////////////////////////////////////////
	function modifierArgent(x:Number)
	{
		argent = x;
		_level0.loader.contentHolder.objectMenu["piece"].countTxt = x;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirImage():MovieClip
	{
		return image;
	}
	
	////////////////////////////////////////////////////////////
	function definirImage(i:MovieClip)
	{
		image = i;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirPosition():Point
	{
		return position;
	}
	
	
	////////////////////////////////////////////////////////////
	function definirPosition(p:Point, ll:Number, cc:Number)
	{
		image._x = p.obtenirX();
		image._y = p.obtenirY();
		this.position.definirX(p.obtenirX());
		this.position.definirY(p.obtenirY());
		this.l = ll;
		this.c = cc;
	}

	////////////////////////////////////////////////////////////
	function obtenirProchainePosition():Point
	{
		return prochainePosition;
	}
	
	////////////////////////////////////////////////////////////
	// retourne la liste des objets que le magasin ou on se trouve contient
	//
	function obtenirMagasin():Array
	{
		return listeSurMagasin;
	}
	
	////////////////////////////////////////////////////////////
	// fixe les objets contenus dans un magasin
	//
	function definirMagasin(mag:Array)
	{
		listeSurMagasin = mag;
	}
	
		
	////////////////////////////////////////////////////////////
	// cette fonction change l'echelle d'une image lorsqu'on utilise
	// une potion ( bleue ou rouge : grandit ou rapetisse )
	// la potion fait effet pour 30 secondes. apres, on rappelle la
	// la fonction qui retourne a la normale.
	// on utilise des tween pour une transition plus amusante !
	//
	function shrinkBonhommeSpecial(mClip1:MovieClip, x:Number, y:Number)
	{
		var twMove1:Tween;
		var twMove2:Tween;
		var intervalId:Number;
		var cpt:Number = 0;
		var fctPerso:Object = this;	// on garde en memoire qu'on est dans la classe personnage pour etre capable d'utiliser ses fonctions

		intervalId = setInterval(attendre, 1000, cpt);	// sert pour attendre la jusqu'a la fin de l'effet de la potion
		
		twMove1 = new Tween(mClip1, "_xscale", Bounce.easeIn, 100, x, 2, true);
		twMove2 = new Tween(mClip1, "_yscale", Bounce.easeIn, 100, y, 2, true);
		
		// cette fonction attend jusqu'au signal du compteur
		// et appelle le retour au format normal
		function attendre():Void
		{
			var maxCpt:Number = 30;
			
			if(cpt >= maxCpt) 
			{
				clearInterval(intervalId);
				fctPerso.shrinkBonhommeNormal(mClip1, x, y);
			} 
			cpt++;
		}
	}

	////////////////////////////////////////////////////////////
	// Cette fonction ramene la taille du mClip a sa taille normale
	// prend les valeurs x et y pour l'echelle
	// - le tween permet une transition plus amusante !
	//
	function shrinkBonhommeNormal(mClip1:MovieClip, x:Number, y:Number)
	{
		var twMove1:Tween;
		var twMove2:Tween;
		
		twMove1 = new Tween(mClip1, "_xscale", Bounce.easeIn, x, 100, 2, true);
		twMove2 = new Tween(mClip1, "_yscale", Bounce.easeIn, y, 100, 2, true);
	}

	////////////////////////////////////////////////////////////
	// cette fonction transforme des donnees stockees dans un array
	// dans un XMLNode.
	// ca permet de garder en memoire les objets contenus dans un magasin,
	// leur ID et leur cout
	//
	function genererListeMagasinXML(lstObjMagasin:Array):Object
	{
		var objMagasin:Object = new Object();
		
		for(var j:Number = 0; j<lstObjMagasin.length; j++)
		{
			var objNoeudObjMagasin:XMLNode = lstObjMagasin[j];
									
			objMagasin["objet"+j] = new Object();
			objMagasin["objet"+j].cout = objNoeudObjMagasin.attributes.cout;
			objMagasin["objet"+j].id = objNoeudObjMagasin.attributes.id;
			objMagasin["objet"+j].type = objNoeudObjMagasin.attributes.type;
		
			trace("id : " + objMagasin["objet"+j].id);
		}
		return objMagasin;
	}
	
	function genererListeMagasinXML2(lstObjMagasin:Array, nouveauID:Number, vieuxID:Number):Object
	{
		var objMagasin:Object = new Object();
		
		//trace("newID : " + nouveauID);
		//trace("vieuxID : " + vieuxID);
		
		for(var j:Number = 0; j<lstObjMagasin.length; j++)
		{
			var objNoeudObjMagasin:XMLNode = lstObjMagasin[j];
									
			objMagasin["objet"+j] = new Object();
			objMagasin["objet"+j].cout = objNoeudObjMagasin.attributes.cout;
			objMagasin["objet"+j].type = objNoeudObjMagasin.attributes.type;
			
			if(vieuxID == objNoeudObjMagasin.attributes.id)
			{
				objMagasin["objet"+j].id = nouveauID;
			}
			else
			{
				objMagasin["objet"+j].id = objNoeudObjMagasin.attributes.id;
			}
		
			//trace("id ds generer #2 : " + objMagasin["objet"+j].id);
		}
		return objMagasin;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	//	CONSTRUCTEUR
	//////////////////////////////////////////////////////////////////////////////////////
	function Personnage(nom:String, role:Number, niveau:Number, nomClip:Number, ll:Number, cc:Number, xx:Number, yy:Number, cloColor:String, mag:Array)
	{
		this.l = ll;
		this.c = cc;
		this.numero = niveau;
		this.position = new Point(xx,yy);
		this.prochainePosition = new Point( xx , yy );
		this.clothesColor = cloColor;
		
		// to load the perso .. use ClipLoader to know the moment of complet load
		var myLoader:MovieClipLoader = new MovieClipLoader();
	    //myLoader.addListener(image);
		
		var mclListener:Object = new Object();
        mclListener.onLoadComplete = function(target_mc:MovieClip) {
            
		    target_mc.nom = nom;
			
			// assure que le clip a la bonne orientation
			target_mc._xscale = - Math.abs(target_mc._xscale);
			//target_mc.dtNom._xscale = - Math.abs(target_mc._xscale);
			target_mc.dtNom._x = 42;
			
			target_mc.clothesCol = cloColor;
			target_mc.gotoAndPlay("rest");
			//trace(" new color 2!!! " + target_mc.clothesCol)
			
        };
		myLoader.addListener(mclListener);


		
		if(!(role == 2 && _level0.loader.contentHolder.objGestionnaireEvenements.typeDeJeu == "Tournament")){
  
           image =  _level0.loader.contentHolder.referenceLayer.createEmptyMovieClip("Personnage" + niveau, niveau);
		    myLoader.loadClip("perso" + nomClip + ".swf", image); 
		
		}
		
		
		
		//image.dtNom._visible = true;
		//image.dtNom.text = nom;
		//image.nom = nom;
		//trace(image.nom + " : ici perso2" );
		//trace(image + " : ici perso2" );
		
		this.pointage = 0;
		this.argent = 0;
		this.listeDesObjets = new Object();
		
		
		// each array will contain the ID's of objects in possession
		//this.listeDesObjets["piece"] = new Array();
		this.listeDesObjets["Banane"] = new Array();
		this.listeDesObjets["Livre"] = new Array();
		this.listeDesObjets["Boule"] = new Array();
		
		this.faireCollision = null;
		this.nom = nom;
		this.listeSurMagasin = mag;
		this.minigameLoade = false;
		this.role = role;
	    
	}// end constr
	
	
	////////////////////////////////////////////////////////////
	function deplacePersonnage()
	{
		var pourcent:Number;
		var dx:Number;
		var dy:Number;
		var reafficher1:Boolean = true;
		var reafficher2:Boolean = false;
		
		
		
		dx = this.prochainePosition.obtenirX() - this.position.obtenirX();  
		dy = this.prochainePosition.obtenirY() - this.position.obtenirY();
		//trace("ds deplacePersonnage " + dx/dy);
		if( boardCentre ) //dx == 0 && dy == 0 && image._currentFrame == 1) 
		{
			return;
		}
		
		if ((dx == 0) && (dy == 0))
		{
			if(image._currentFrame != 1 && image._currentFrame < 90)
			{
				// place le personnage au repos et de face
				image.gotoAndStop(1);
		
				switch(this.faireCollision)
				{
					case "piece":
						// l'argent est ajoute au personnage dans les fichiers gestEve et gestComm
						// en gros, ici, on enleve l'image du board
															
						
	
						_level0.loader.contentHolder.planche.enleverPiece(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -10000);
						this.faireCollision = null;
					break;
				
					case "magasin":
						//si notre personnage tombe sur un magasin, on charge le GUI_magasin
						if(this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
						{
							this.minigameLoade = true;
							reafficher1 = false;
							_level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
							_root.objGestionnaireInterface.effacerBoutons(1);			
							
							// on charge le magasin : on ajuste sa position a l'ecran
							// on indique qu'il ne s'agit pas d'un mini-game.
						
							var minigame:MovieClip;   
							minigame = _level0.loader.contentHolder.miniGameLayer.attachMovie("GUI_magasin", "magasin", 9997);
							minigame._x = 0;
							minigame._y = 0;
							minigame._xscale = 100;
							minigame._yscale = 100;
							//minigame._width = 550;
							//minigame._height= 400;
							minigame._visible = false;
						
						
							//variables qui servent pour un fade-in/fade-out a l'entree du magasin
							var oListener:Object = new Object();  
							var twMove:Tween;
							var twMove2:Tween;
							var mClip1:MovieClip;
	
							mClip1 = _level0.loader.contentHolder.attachMovie("masque", "masqueA", 1,{_x:270, _y: 200});
							mClip1._alpha = 0;
						
							twMove = new Tween(mClip1, "_alpha", Regular.easeOut, 0, 100, 1, true);
							twMove.addListener(oListener);
	
							oListener.onMotionFinished = function():Void 
							{ 
								twMove2 = new Tween(mClip1, "_alpha", Regular.easeOut, 100, 0, 1, true);
								_level0.loader.contentHolder.menuOutils._visible = false;
								_level0.loader.contentHolder.enteteHolder._visible = false;
								_level0.loader.contentHolder.horloge._visible = false;
								minigame._visible = true;
							}
						
							this.faireCollision = null;
						}
						else
						{
							faireCollision = null;
						}
					break;
				
					// Pour tous les objets, on se comporte de la meme maniere :
					// on enleve l'objet (meme si on en a 10 : parce que le serveur nous dit qu'on l'enleve)
					// si necessaire, on l'ajoute a nos objets
					
					case "Livre":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
				       
					break;
				
					case "Telephone":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
                        
					break;
				
					case "Papillon":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
                       
					break;
				
					case "Boule":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
                      
					break;
				
					case "PotionGros":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
                       
					break;
				
					case "PotionPetit":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
                       
					break;
				
					case "Banane":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
                        
					break;
					
					case "Braniac":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
						
						// with Braniac a little different - we use it instantly
						// so the player in the state of Braniac at the time to reach the case
						// 
						getBraniacAnimaton();
                        
					break;

				
					default :
						//trace("pas de collision");	
					break;
				}// switch(this.faireCollision)
			
				if(this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
				{
					if(_level0.loader.contentHolder.planche.estCaseSpeciale(this.l, this.c) &&  _level0.loader.contentHolder.sortieDunMinigame == false)
					{
						this.minigameLoade = true;
						 _level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_root.objGestionnaireInterface.effacerBoutons(1);
					
						_level0.loader.contentHolder.minigameToLoad = _level0.loader.contentHolder.url_Minigames[Math.floor(Math.random() * _level0.loader.contentHolder.url_Minigames.length)];

						//variables qui servent pour un fade-in/fade-out a l'entree du magasin
						var oListener:Object = new Object();  
						var twMove:Tween;
						var twMove2:Tween;
						var mClip1:MovieClip;
	
						mClip1 = _level0.loader.contentHolder.attachMovie("masque", "masqueA", 9998,{_x:270, _y: 200});
						mClip1._alpha = 0;
						
						var minigame:MovieClip;
						minigame = _level0.loader.contentHolder.miniGameLayer.attachMovie("GUI_minigame", "Minigame", 9997);
						minigame._visible = false;
					
						switch(_level0.loader.contentHolder.minigameToLoad)
						{
							case "ileTresor.swf":
								minigame._x = 40;
								minigame._y = -40;
								minigame._width = 670;
								minigame._height= 500;
							break;
							
							case "balleAuMur2.swf":
								minigame._x = 0;
								minigame._y = 40;
								minigame._width = 550;
								minigame._height= 400;
							break;
							
							default:
								minigame._x = 15;
								minigame._y = - 40;
								minigame._width = 485;
								minigame._height= 385;
							break;
						}
						
						twMove = new Tween(mClip1, "_alpha", Regular.easeOut, 0, 100, 1, true);
						twMove.addListener(oListener);
	
						oListener.onMotionFinished = function():Void 
						{ 
							twMove2 = new Tween(mClip1, "_alpha", Regular.easeOut, 100, 0, 1, true);
							minigame._visible = true;
						}
					} // if(_level0.loader.contentHolder.planche.estCaseSpeciale(this.l, this.c) &&  _level0.loader.contentHolder.sortieDunMinigame == false)
					else reafficher2 = true;
				}// if(this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
			}// if(image._currentFrame != 1 && image._currentFrame < 90)
			
			if(reafficher1 && reafficher2) this.minigameLoade = false;
			
			//Si le perso est le mien et qu'il est au repos, mais que le board n'est pas centre
			if(this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
			{
				if(_level0.loader.contentHolder.planche.recentrerBoard(this.l, this.c, true))
				{
					if(!this.minigameLoade)
					{
						_level0.loader.contentHolder.planche.afficherCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						
					}
					boardCentre = true;
					//trace(_level0.loader.contentHolder.horlogeNum + "temps restant");
					
				}
			} 
			
			return; // pour ne pas faire le reste des verifications inutilement si dx == dy == 0
		} // if ((dx == 0) && (dy == 0))
		
		//pour le reste, ((dx == 0)&&(dy == 0) != 1)
		
		if(Math.abs(dx) > Math.abs(dy))
		{
			if(Math.abs(dx) > 10)
			{
				pourcent = 10/Math.abs(dx);
				dx *= pourcent;
				dy *= pourcent;
			}
		}
		else
		{
			if(Math.abs(dy) > 10)
			{
				pourcent = 10/Math.abs(dy);
				dx *= pourcent;
				dy *= pourcent;
			}
		}

        //trace("trace  dx : " + dx + " dy : " + dy + " control : " + dx/dy);
		if (dy < 0)
		{
			if(image._currentFrame < 70)
			{
				image.gotoAndPlay(70);
				//trace("perso " + image._currentFrame);
			}
			
		}
		else
		{
			if(image._currentFrame > 69)
			{
				image.gotoAndPlay(10);
			}
		}
				
		if (dx >= 0)
		{
			// assure que le clip a la bonne orientation
			image._xscale = - Math.abs(image._xscale);
			image.dtNom._xscale = - Math.abs(image._xscale);
			image.dtNom._x = 42;
		}
		else
		{
			// flip le clip pour aller vers la gauche
			image._xscale =  Math.abs(image._xscale);
			image.dtNom._xscale =   Math.abs(image._xscale);
			image.dtNom._x = - 42;
		}
		
		
		// deplace le clip
		image._x += dx;  
		image._y += dy;
		position.definirX(position.obtenirX()+dx);
		position.definirY(position.obtenirY()+dy);
	
		// Si le deplacement voulu n'est pas nul mais que le personnage est au repos
		//trace("avant le if le frame  :   "+this.image._currentFrame);
		if (((dx != 0) || (dy != 0)) && (this.image._currentFrame == 1 || this.image._currentFrame > 89))
		{
			// place le clip du personnage au debut de la sequence de deplacement
			image.gotoAndPlay(10);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	function afficher()
	{
		image._visible = true;
		image._x = position.obtenirX();  
		image._y = position.obtenirY();
	}
	
	function afficherAutreDir()
	{
	       // assure que le clip a la bonne orientation
			image._xscale = - Math.abs(image._xscale);
			image.dtNom._xscale = - Math.abs(image._xscale);
			image.dtNom._x = 42;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	function cachePersonnage()
	{
		image._visible = false;
	}	
	
	//////////////////////////////////////////////////////////////////////////////////////
	////  pt contient la ligne et la colonne PAS LES X et Y
	function definirProchainePosition(pt:Point, str:String)
	{
		_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].retirerPersonnage(this);
		
		this.l = pt.obtenirX();
		this.c = pt.obtenirY();
 
        
 
		_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].ajouterPersonnage(this);
		
		this.prochainePosition.definirX(_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].obtenirClipCase()._x);
		this.prochainePosition.definirY(_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].obtenirClipCase()._y);
		
		this.faireCollision = str;
		this.boardCentre = false;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	function obtenirX():Number
	{
		return position.obtenirX();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	function obtenirY():Number
	{
		return position.obtenirY();
	}
		
	//////////////////////////////////////////////////////////////////////////////////////
	function rire()
	{
		image.gotoAndPlay("rire");
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	function tossBanana()
	{
		this.image.gotoAndPlay("tossing");
		trace("tossing !!!!!!!!!!!!!!!!!");
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	function slippingBanana()
	{
		this.image.gotoAndPlay("slipping");
		trace("slipping !!!!!!!!!!!!!!!!!");
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	function rest()
	{
		this.image.gotoAndPlay("rest");
	}
	
	// used to put the Braniac animation on the player  for the 90 sec.
	function getBraniacAnimaton()
	{
	   var playerThat:String = this.nom;
	   	  	    
	   image.braniacState = "begin";
		
	   var intervalIDBegin = setInterval(etape2Bran, 3000, playerThat);	// to pass to phase 2 of Braniac
		
		function etape2Bran():Void
		{
			//_level0.loader.contentHolder.objGestionnaireEvenements.setBraniacState("in");
			_level0.loader.contentHolder.planche.getPersonnageByName(playerThat).obtenirImage().braniacState = "in";
			//trace("control bran in set " + _level0.loader.contentHolder.planche.getPersonnageByName(playerThat).obtenirImage().braniacState);
		 	clearInterval(intervalIDBegin);
		}
			
	} // end of getBraniacAnimation
	
	function setOutBraniac()
	{
		var playerUnder:String = this.nom;
		
		var restedTime = 89;
	   	  	    
	    var intervalIDEnd = setInterval(etapeEndBran, 1000, playerUnder);	// to pass to phase 3 of Braniac
		
		function etapeEndBran():Void
		{    
		   restedTime--;	  
		  
		   if( restedTime == 2)
		   {
			  _level0.loader.contentHolder.planche.getPersonnageByName(playerUnder).obtenirImage().braniacState = "end";
		   }
		   else if(restedTime == 0)
	       {
			  _level0.loader.contentHolder.planche.getPersonnageByName(playerUnder).setBraniac(false);
			 				
		   }  
		   
		   // to end the Braniac and remove the timer box
		   if( restedTime < 0)
		   { 
		      _level0.loader.contentHolder.planche.getPersonnageByName(playerUnder).obtenirImage().braniacState = "out";
              clearInterval(intervalIDEnd);
		   }
			
		}// end method
	}//end
	
	
	
}