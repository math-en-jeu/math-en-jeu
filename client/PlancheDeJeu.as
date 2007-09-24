﻿/*******************************************************************
Math en jeu
Copyright (C) 2007 Projet SMAC

Ce programme est un logiciel libre ; vous pouvez le
redistribuer et/ou le modifier au titre des clauses de la
Licence Publique Générale Affero (AGPL), telle que publiée par
Affero Inc. ; soit la version 1 de la Licence, ou (à
votre discrétion) une version ultérieure quelconque.

Ce programme est distribué dans l'espoir qu'il sera utile,
mais SANS AUCUNE GARANTIE ; sans même une garantie implicite de
COMMERCIABILITE ou DE CONFORMITE A UNE UTILISATION
PARTICULIERE. Voir la Licence Publique
Générale Affero pour plus de détails.

Vous devriez avoir reçu un exemplaire de la Licence Publique
Générale Affero avec ce programme; si ce n'est pas le cas,
écrivez à Affero Inc., 510 Third Street - Suite 225,
San Francisco, CA 94107, USA.
*********************************************************************/

import flash.geom.Transform;
import flash.geom.ColorTransform;

import mx.transitions.Tween;
import mx.transitions.easing.*;
import mx.utils.Delegate;
import mx.transitions.Transition;


class PlancheDeJeu
{
    private var mat:Array;
    private var tableauDesCases:Array = new Array();
    private var hauteurDeCase:Number = -1;
    private var largeurDeCase:Number = -1;
    private var perso:Personnage;
    private var monPersonnage:Number = -1;  //  numéro dans le tableau des perso du personnage en cours
    private var nomDeMonPersonnage:String; // nom de notre perso
    private var zoom:Number = 0;
    private var gestionnaireInterface:GestionnaireInterface;
    private var rotation:Number = 0;
    private var tableauDesPersonnages:Array = new Array(); // contient les personnages
	
    
    function obtenirPerso():Personnage
    {
        return this.perso;
    }
	
	function setPerso(p:Personnage):Void
    {
        perso = p;
    }
	
    function obtenirNomDeMonPersonnage():String
    {
        return this.nomDeMonPersonnage;
    }
    
	function getNumeroMagasin(ll:Number, cc:Number):Number
	{
		return this.tableauDesCases[ll][cc].obtenirNumMagasin();
	}
	
	function obtenirTableauDesCases():Array
    {
        return this.tableauDesCases;
    }
    
	function obtenirMat():Array
    {
        return this.mat;
    }
    

    ////////////////////////////////////////////////////////////
    //  CONSTUCTEUR
    ////////////////////////////////////////////////////////////
    function PlancheDeJeu(tab:Array, num:Number, p:GestionnaireInterface)
    {
        var i:Number;
		this.mat = new Array(tab.length)
        for(i=0;i<tab.length;i++)
        {
			this.mat[i] = new Array(tab[i].length);
            this.tableauDesCases.push(new Array());
        }
        definirMat(tab, null);
        monPersonnage = num;
        perso = null;
        gestionnaireInterface= p;
    }
	
	
    function obtenirNombreDeColonnes():Number
    {
        return this.mat[0].length;
    }

   
    // à revoir, pour tout de suite ca sert quand on enleve une piece
    function modifierNumeroCase(l:Number, c:Number, num:Number)
    {
	    // si on vient d'enlever un objet il faut mettre comme nouvelle valeur juste la couleur de la case (deux derniers chiffres)
	    // si on vient d'enlever une piece il faut mettre comme nouvelle valeur juste la couleur de la case (deux derniers chiffres)
	    if(num == -30000 || num == -10000)
	    {
		this.mat[l][c] = this.mat[l][c]%100;    
	    }
	    
	    else
	    {
		    this.mat[l][c] += num;
	    }
    }
    
       
    function enleverPiece(l:Number, c:Number)
    {
        this.tableauDesCases[l][c].effacerPiece();
    }
	
	
	///////////////////////////////////////////////////////////////////////////
	function obtenirObjet(l:Number, c:Number):ObjetSurCase
    {
        return this.tableauDesCases[l][c].obtenirObjet();
    }
	
	
	function enleverObjet(l:Number, c:Number)
    {
        this.tableauDesCases[l][c].effacerObjet();
    }
	

    function obtenirNombreDeLignes():Number
    {
        return this.mat.length;
    }
   
   	  // cette fonction affiche la planche de jeu initiale,
      // idéalement il faudrait que les 4 coins soient toujours présents et pas plus de 9998 cases
    function afficher()
    {
        var i:Number;
        var j:Number;
        var nouvelleCase:Case;
        var pt:Point = new Point(0, 0);
        var clipTest:MovieClip;
        var x:Number;
        var y:Number;
        var temp:Number;
        // ici on veut juste déterminer la hauteur et la largeur des cases  //////////////////////////
        clipTest= _level0.loader.contentHolder.referenceLayer.attachMovie("case0", "case", 0);
        clipTest._x = -100;
        clipTest._y = -100;
        largeurDeCase = clipTest._width;
        hauteurDeCase = clipTest._height*0.85;
        clipTest.removeMovieClip();
        ////////////////////////////////////////////////////////////////////////////////////////
        for(i=0;i<this.mat.length;i++)
        {
            x = i*largeurDeCase/2+largeurDeCase/2;
            y = 200 + i*hauteurDeCase/2;
            for(j=0;j<this.mat[0].length;j++)
            {
                pt.definirX(x);
                pt.definirY(y);
                if(this.mat[i][j] != 0)
                {
            //trace("dans afficher la planche de jeu, i   j   numéro de la case :  "+i+"   "+j+"   "+this.mat[i][j]);
                    nouvelleCase = new Case(this.mat[i][j], i, j, mat.length, this.mat[0].length);
                    this.tableauDesCases[i][j] = nouvelleCase;
                    nouvelleCase.afficher(pt);
                }
                else
                {
                    if((i==0 && j==0) || (i==this.mat.length-1 && j==0) || (i==0 && j==this.mat[0].length-1) || (i==this.mat.length-1 && j==this.mat[0].length-1))
                    {
                        //mat[i][j] = -1;
                        nouvelleCase = new Case(-1, i, j, mat.length, this.mat[0].length);
                        this.tableauDesCases[i][j] = nouvelleCase;
                        nouvelleCase.afficher(pt);
                    }
                }
                x = x+largeurDeCase/2;
                y = y-hauteurDeCase/2;
            }
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////////////
    function definirMat(tab:Array, tdc:Array)
    {
        var i:Number;
        var j:Number;
        // on initie les tableaux /////////////////////////
        for(i=0;i<tab.length;i++)
        {
            for(j=0;j<tab[0].length;j++)
            {
                this.mat[i][j] = tab[i][j];
                //trace("ds definirMat  mat   tab i  j  mat  tab :    "+i+"   "+j+"   "+this.mat[i][j]+"     "+tab[i][j]);
                if(tdc == null)
                {
                    this.tableauDesCases[i][j] = null;
                }
                else
                {
                    this.tableauDesCases[i][j] = tdc[i][j];
                    this.tableauDesCases[i][j].definirL(i);
                    this.tableauDesCases[i][j].definirC(j);
                }
            }
        }
    }
	
    function translater(direction:String)
    {
        var i:Number;
        var j:Number;
        var dx:Number;
        var dy:Number;
        var la:Number;
        var ha:Number;
        switch(direction)
        {
            case "Est":
                la = -largeurDeCase/2;
                ha = 0;
            break;
            case "Ouest":
                la = largeurDeCase/2;
                ha = 0;
            break;
            case "Nord":
                la = 0;
                ha = hauteurDeCase/2;
            break;
            case "Sud":
                la = 0;
                ha = -hauteurDeCase/2;
            break;
        }
       
	
	// on déplace le clip sur lequel est attaché tous les autres clips
	_level0.loader.contentHolder.referenceLayer._x +=la;		
	_level0.loader.contentHolder.referenceLayer._y +=ha;
	

    }

       
    function zoomer(sens:String):Boolean
    {
		switch(sens)
		{
			case "in":
			
				if(this.zoom < 8)
				{
					this.zoom++;
					
					// on zoom le clip sur lequel est attaché tous les autres clips
					_level0.loader.contentHolder.referenceLayer._xscale +=10;
					_level0.loader.contentHolder.referenceLayer._yscale +=10;
				
					// on déplace le clip sur lequel est attaché tous les autres clips
					_level0.loader.contentHolder.referenceLayer._x -=27;
					_level0.loader.contentHolder.referenceLayer._y -=20;
				}
				break;
				
			case "out":
		
				if(this.zoom > -8)
				{
					this.zoom--; 
					
					// on zoom le clip sur lequel est attaché tous les autres clips
					_level0.loader.contentHolder.referenceLayer._xscale -=10;
					_level0.loader.contentHolder.referenceLayer._yscale -=10;
					
					// on déplace le clip sur lequel est attaché tous les autres clips
					_level0.loader.contentHolder.referenceLayer._x +=27;
					_level0.loader.contentHolder.referenceLayer._y +=20;
				}
				break;
		
			default:
			break;
		}
		if(this.zoom == -8 || this.zoom == 8) return false;
		return true;
    }
    
    
    function obtenirRotation():Number
    {
        return rotation;
    }
    
    
    function obtenirTableauDesPersonnages():Array
    {
        return this.tableauDesPersonnages;
    }
    
    
    
// cette fonction prends en entrée un pt du board original et retourne la ligne et la colonne dans le board tournée
// a appliquer a tous les pt donnés par le serveur
    function calculerPositionTourne(ll:Number, cc:Number):Point
    {
        var pt:Point;
        var i:Number;
        var a:Number;
        var b:Number;
        var temp:Number;
        a = ll;
        b = cc;
        switch(this.rotation)
        {
            case 1:
                temp = b;
                b = a;
                a = mat.length-1-temp;
            break
            case 2:
                temp = b;
                b = a;
                a = mat[0].length-1-temp;
                temp = b;
                b = a;
                a = mat.length-1-temp;
            break
            case 3:
                temp = b;
                b = a;
                a = mat.length-1-temp;
                temp = b;
                b = a;
                a = mat[0].length-1-temp;
                temp = b;
                b = a;
                a = mat.length-1-temp;
            break
        }
        pt = new Point(a,b);
        return pt;
    }
    
    
    
    // cette fonction prends en entrée un pt du board tourné et retourne la ligne et la colonne dans le board original
    // a appliquer a tous les points donné au serveur
    function calculerPositionOriginale(ll:Number, cc:Number):Point
    {
        var pt:Point;
        var i:Number;
        var a:Number;
        var b:Number;
        var temp:Number;
        a = ll;
        b = cc;
        switch(this.rotation)
        {
            case 1:
                temp = a;
                a = b;
                b = mat.length-1-temp;
            break
            case 2:
                temp = a;
                a = b;
                b = mat.length-1-temp;
                temp = a;
                a = b;
                b = mat[0].length-1-temp;
            break
            case 3:
                temp = a;
                a = b;
                b = mat.length-1-temp;
                temp = a;
                a = b;
                b = mat[0].length-1-temp;
                temp = a;
                a = b;
                b = mat.length-1-temp;
            break
        }
        pt = new Point(a,b);
        return pt;
    }

	
    function faireRirePersonnage(p:Personnage)
    {
        p.rire();
    }

    
    function ajouterPersonnage(nom:String, ll:Number,cc:Number,num:Number, idClip:Number)
    {
        var p:Personnage;
        p = new Personnage(nom, 5*tableauDesCases.length*tableauDesCases[0].length+2*num,"Personnage"+idClip,ll, cc, tableauDesCases[ll][cc].obtenirClipCase()._x,tableauDesCases[ll][cc].obtenirClipCase()._y);
        p.afficher();
     //   p.zoomer(zoom*10);
        tableauDesCases[ll][cc].ajouterPersonnage(p);
	
	this.tableauDesPersonnages.push(p);
	
        if(num == monPersonnage)
        {
            perso = p;
	    	nomDeMonPersonnage = nom;
	    	this.centrerPersonnage(ll,cc);
	  	//  p.zoomer(10);
        }
		//else
			p.zoomer(-5);  // POURQUOI?
    }
    
    
    
	// c'est quoi la différence entre ça et recentrerBoard ??
    function centrerPersonnage(l:Number, c:Number)
    {
        var diffX:Number;
        var diffY:Number;
        var i:Number;
        var j:Number;
        diffX = 275-this.tableauDesCases[l][c].obtenirClipCase()._x;
        diffY = 200-this.tableauDesCases[l][c].obtenirClipCase()._y;
        for(i=0;i<this.tableauDesCases.length;i++)
        {
            for(j=0;j<this.tableauDesCases[0].length;j++)
            {
                if(this.tableauDesCases[i][j] != null)
                {
                    this.tableauDesCases[i][j].translater(diffX,diffY);
                }
            }
        }
		
		
    }
    
    
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	function recentrerBoard(l:Number, c:Number):Boolean
	{
		var dx:Number;
		var dy:Number;
		var i:Number;
		var j:Number;
		var pourcent:Number;
		
		
		dx = 275-this.tableauDesCases[l][c].obtenirClipCase()._x;
		dy = 200-this.tableauDesCases[l][c].obtenirClipCase()._y;
		
		if ((dx == 0) && (dy == 0))
		{
			return true;
		}
    
		
		if(Math.abs(dx) > Math.abs(dy))
		{
			if(Math.abs(dx) > 20)
			{
				pourcent = 20/Math.abs(dx);
				dx *= pourcent;
				dy *= pourcent;
			}
		}
		else
		{
			if(Math.abs(dy) > 20)
			{
				pourcent = 20/Math.abs(dy);
				dx *= pourcent;
				dy *= pourcent;
			}
		}
						
		
		
		for(i=0;i<this.tableauDesCases.length;i++)
        {
            for(j=0;j<this.tableauDesCases[0].length;j++)
            {
                if(this.tableauDesCases[i][j] != null)
                {
                    this.tableauDesCases[i][j].translater(dx, dy);
                }
            }
        }
		return false;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	function switchColor(laCase:Case)
	{
		//trace("--- switchColor ---");
		
		var mClip:MovieClip = new MovieClip();
		mClip = laCase.obtenirClipCase().interieur;
		
		var trans:Transform = new Transform(mClip);
		var colorTrans:ColorTransform = new ColorTransform();

		
		if(laCase.obtenirMiniGame())
		{
			colorTrans.rgb = 0xd8ae00;
			trans.colorTransform = colorTrans;
			
			trans = new Transform(laCase.obtenirClipCase().bord);
			trans.colorTransform = colorTrans;
		}
		else
		{
			colorTrans.rgb = 0xd8ae00;
			trans.colorTransform = colorTrans;
		}
	}

	function switchBackColor(laCase:Case)
	{
		//trace("--- switchBackColor ---");
		
		var mClip:MovieClip = new MovieClip();
		mClip = laCase.obtenirClipCase().interieur;
		
		var trans:Transform = new Transform(mClip);
		var colorTrans:ColorTransform = new ColorTransform();

	
		if(laCase.obtenirMiniGame())
		{
			colorTrans.rgb = 0xC61717;
			trans.colorTransform = colorTrans;
			
			trans = new Transform(laCase.obtenirClipCase().bord);
			trans.colorTransform = colorTrans;
		}
		else
		{
			colorTrans.rgb = 0x8ab21d;
			trans.colorTransform = colorTrans;
		}
	}

	
    function afficherCasesPossibles(p:Personnage)
    {

		trace("Debut afficherCasesPossibles");
		
        var i:Number;
        var nb:Number = 0;
        var brille:MovieClip;
		var coordonnees:Point = new Point(0,0);
        var level:Number;
		var temp:Number;
		var twMove:Tween;
		var twMove2:Tween;
		var pointageMin:Number = _level0.loader.contentHolder.objGestionnaireEvenements.obtenirPointageMinimalWinTheGame();
	
		trace(pointageMin);
		
		switchColor(tableauDesCases[p.obtenirL()][p.obtenirC()]);
		
		//trace("ds afficherCasesPossibles");
        for(i=1;i<=Math.min(mat.length-p.obtenirL()-1,6);i++)
        {
			temp = Number(p.obtenirL());
			temp += Number(i);
			// pourquoi il concatene ??????????????
			//trace("ds premier for avant if  i  temp   mat  L   C  :  "+i+"   "+temp+"   "+this.mat[temp][p.obtenirC()]+"   "+p.obtenirL()+"   "+p.obtenirC());
		
            if(this.mat[temp][p.obtenirC()] > 0)
            {

				
				trace(pointageMin);
				if(tableauDesCases[temp][p.obtenirC()].obtenirType() > 40000 && _level0.loader.contentHolder.planche.obtenirPerso().obtenirPointage() < pointageMin)
				{
					trace("pas assez de points pour atteindre le WinTheGame");
				}
				else
				{
					switchColor(tableauDesCases[temp][p.obtenirC()]);
	
					level = (tableauDesCases.length*tableauDesCases[0].length) +(temp*tableauDesCases[0].length)+Number(p.obtenirC()) + Number(1);
					//trace("ds if,  level  "+level);
					brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					brille._x = tableauDesCases[temp][p.obtenirC()].obtenirClipCase()._x;
					brille._y = tableauDesCases[temp][p.obtenirC()].obtenirClipCase()._y;
					brille._alpha = 0;
					brille._width = largeurDeCase;
					brille._height = hauteurDeCase/0.85;
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = temp;
					brille._colonne = p.obtenirC();

					
					brille.onPress = function ()
					{   
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
						_level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees);
					};
					tableauDesCases[temp][p.obtenirC()].definirCasePossible(brille);
			
					_root.objGestionnaireInterface.ajouterBouton(brille, 3);
					
				}
            }
            else
            {
                break;
            }
        }
	
	
        for(i=1;i<=Math.min(p.obtenirL(),6);i++)
        {
			//trace("ds deuxième for avant if  i  mat  :  "+i+"   "+mat[p.obtenirL()-i][p.obtenirC()]);
			temp = p.obtenirL()-i;
           
		   if(mat[temp][p.obtenirC()] > 0)
            {
				if(tableauDesCases[temp][p.obtenirC()].obtenirType() > 40000 && _level0.loader.contentHolder.planche.obtenirPerso().obtenirPointage() < pointageMin)
				{
					trace("pas assez de points pour atteindre le WinTheGame");
				}
				else
				{
					switchColor(tableauDesCases[temp][p.obtenirC()]);
					
					// trace("tableau des cases  vs  mat  : "+ tableauDesCases.length+"   "+mat.length);
					// trace("tableau des cases[0]  vs  mat[]  : "+ tableauDesCases[0].length+"   "+mat[0].length);
					level = (tableauDesCases.length*tableauDesCases[0].length)  +  ((temp)*tableauDesCases[0].length)  +  Number(p.obtenirC()) + Number(1);
					// trace("ds if,  level:  "+level);
					brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					brille._x = tableauDesCases[temp][p.obtenirC()].obtenirClipCase()._x;
					brille._y = tableauDesCases[temp][p.obtenirC()].obtenirClipCase()._y;
					brille._alpha = 0;
					brille._width = largeurDeCase;
					brille._height = hauteurDeCase/0.85;
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = temp;
					brille._colonne = p.obtenirC();
					
					
					brille.onPress = function ()
					{
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
				
						_level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees);
			   
					};
					this.tableauDesCases[temp][p.obtenirC()].definirCasePossible(brille);
			
					_root.objGestionnaireInterface.ajouterBouton(brille, 3);
			
				}
           	}
            else
            {
                break;
            }
        }
	
	
        for(i=1;i<=Math.min(mat[0].length-p.obtenirC()-1,6);i++)
        {
			temp = Number(p.obtenirC());
			temp += Number(i);
			//trace("ds troisieme for avant if  i  temp   mat  L   C  :  "+i+"   "+temp+"    "+mat[p.obtenirL()][temp]+"   "+p.obtenirL()+"   "+p.obtenirC());
            
			if(mat[p.obtenirL()][temp] > 0)
            {
				if(tableauDesCases[p.obtenirL()][temp].obtenirType() > 40000 && _level0.loader.contentHolder.planche.obtenirPerso().obtenirPointage() < pointageMin)
				{
					trace("pas assez de points pour atteindre le WinTheGame");
				}
				else
				{
					switchColor(tableauDesCases[p.obtenirL()][temp]);
			
					level = (tableauDesCases.length*tableauDesCases[0].length) + Number(p.obtenirL()*tableauDesCases[0].length) + temp + Number(1);
					//trace("ds if : level :   "+level);
					brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					brille._x = tableauDesCases[p.obtenirL()][temp].obtenirClipCase()._x;
					brille._y = tableauDesCases[p.obtenirL()][temp].obtenirClipCase()._y;
					brille._alpha = 0;
					brille._width = largeurDeCase;
					brille._height = hauteurDeCase/0.85;
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = p.obtenirL();
					brille._colonne = temp;
				
					
					brille.onPress = function ()
					{
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
				
						_level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees);
			   
					};
					this.tableauDesCases[p.obtenirL()][temp].definirCasePossible(brille);
			
					_root.objGestionnaireInterface.ajouterBouton(brille, 3);
			
				}
			}
            else
            {
                break;
            }
        }
        for(i=1;i<=Math.min(p.obtenirC(),6);i++)
        {
			//trace("ds dernier for avant if  i  mat  :  "+i+"   "+mat[p.obtenirL()][p.obtenirC()-i]);
			temp = p.obtenirC()-i;
			
            if(mat[p.obtenirL()][temp] > 0)
            {
				if(tableauDesCases[p.obtenirL()][temp].obtenirType() > 40000 && _level0.loader.contentHolder.planche.obtenirPerso().obtenirPointage() < pointageMin)
				{
					trace("pas assez de points pour atteindre le WinTheGame");
				}
				else
				{
					switchColor(tableauDesCases[p.obtenirL()][temp]);
			
					level = (tableauDesCases.length*tableauDesCases[0].length) + (Number(p.obtenirL())*tableauDesCases[0].length)+temp+Number(1);
					//trace("ds if,  level: "+level);
					brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					brille._x = tableauDesCases[p.obtenirL()][temp].obtenirClipCase()._x;
					brille._y = tableauDesCases[p.obtenirL()][temp].obtenirClipCase()._y;
					brille._alpha = 0;
					brille._width = largeurDeCase;
					brille._height = hauteurDeCase/0.85;
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = p.obtenirL();
					brille._colonne = temp;
					
					brille.onPress = function ()
					{
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
				
						_level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees);
					};
					this.tableauDesCases[p.obtenirL()][temp].definirCasePossible(brille);
			
					_root.objGestionnaireInterface.ajouterBouton(brille, 3);
			
				}
            }
            else
            {
                break;
            }
        }
	
	//trace("fin afficher case possible");    
    }
    
    
    function effacerCasesPossibles(p:Personnage)
    {
        var i:Number;
	var temp:Number;
	
	trace("Debut effacerCasesPossibles");
	
	switchBackColor(tableauDesCases[p.obtenirL()][p.obtenirC()]);
	
        for(i=1;i<=Math.min(mat.length-p.obtenirL()-1,6);i++)
        {
		temp = Number(Number(p.obtenirL()) + i);   
		//trace("ds premier for    i     L    temp"+i+"    "+p.obtenirL()+"    "+temp);
		
            if(mat[temp][p.obtenirC()] > 0)
            {
		    //trace("ds if premier for");
                tableauDesCases[temp][p.obtenirC()].effacerCasePossible();
				switchBackColor(tableauDesCases[temp][p.obtenirC()]);
            }
            else
            {
                break;
            }
        }
        for(i=1;i<=Math.min(p.obtenirL(),6);i++)
        {
		//trace("ds deuxième for");
		
            if(mat[p.obtenirL()-i][p.obtenirC()] > 0)
            {
		   // trace("ds if deuxième for");
                tableauDesCases[p.obtenirL()-i][p.obtenirC()].effacerCasePossible();
				switchBackColor(tableauDesCases[p.obtenirL()-i][p.obtenirC()]);
            }
            else
            {
                break;
            }
        }
        for(i=1;i<=Math.min(mat[0].length-p.obtenirC()-1,6);i++)
        {
		temp = Number(Number(p.obtenirC())+i);   
		//trace("ds troisieme for    i     L    temp"+i+"    "+p.obtenirC()+"    "+temp);
		
            if(mat[p.obtenirL()][temp] > 0)
            {
		    //trace("ds if troisieme for");
                tableauDesCases[p.obtenirL()][temp].effacerCasePossible();
				switchBackColor(tableauDesCases[p.obtenirL()][temp]);
            }
            else
            {
                break;
            }
        }
        for(i=1;i<=Math.min(p.obtenirC(),6);i++)
        {
		//trace("ds quatrieme for");
		
            if(mat[p.obtenirL()][p.obtenirC()-i] > 0)
            {
		    //trace("ds if quatrieme for");
                tableauDesCases[p.obtenirL()][p.obtenirC()-i].effacerCasePossible();
				switchBackColor(tableauDesCases[p.obtenirL()][p.obtenirC()-i]);
            }
            else
            {
                break;
            }
        }
	
	_root.objGestionnaireInterface.deleterCasesSpeciales();  
		//trace("Fin effacerCasesPossibles");
		//trace("****************************");
    }	
	
    ////////////////////////////////////////////////////////////////////////////
    function effacerPiece(ll:Number, cc:Number)
    {
        tableauDesCases[ll][cc].effacerPiece();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    function effacerObjet(ll:Number, cc:Number)
    {
        tableauDesCases[ll][cc].effacerObjet();
    }
  
  
    // num est le numéro du personnage   str est le type de collision
    function teleporterPersonnage(nom:String, ancienL:Number, ancienC:Number, nouveauL:Number, nouveauC:Number, str:String)
    {
	    trace(" dans teleporterPersonnage, parametres :  "+nom+"   "+ancienL+"   "+ancienC+"   "+nouveauL+"   "+nouveauC+"   "+str);
	    var listeTemporaire:Array;
	    //var p:Point = new Point(this.tableauDesCases[nouveauL][nouveauC].obtenirClipCase()._x, this.tableauDesCases[nouveauL][nouveauC].obtenirClipCase()._y);
	
	     var p:Point = new Point(nouveauL,nouveauC);
	
	    
	    //trace(" le point :  "+p.obtenirX()+"     "+p.obtenirY());
	    listeTemporaire = this.tableauDesCases[ancienL][ancienC].obtenirListeDesPersonnages();
	    
	    for(var i:Number = 0; i< listeTemporaire.length; i++)
	    {
		    if(listeTemporaire[i].obtenirNom() == nom)
		    {
			    trace("juste avant de définir la prochaine poisition");
			    //listeTemporaire[i].definirPosition(p, nouveauL, nouveauC);  // on le met si on veut téléportation, mais probleme avec les collisions...
			    this.tableauDesCases[ancienL][ancienC].obtenirListeDesPersonnages()[i].definirProchainePosition(p,str);
			    break;
		    }
	    }
	    
	    trace("fin de teleporterPersonnage");
    }
    
    
    
    function estCaseSpeciale(lig:Number, col:Number):Boolean
    {
	    //trace("est dans estCaseSpeciale   "+lig+"   "+col);
	if(tableauDesCases[lig][col].obtenirType()%100 > 90)
        {
		trace("ds le if");
            return true;
        }
	
	return false;
    }
        
    
}
