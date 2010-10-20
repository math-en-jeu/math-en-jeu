package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Banane;
import ServeurJeu.Temps.BananaHumainTask;

/**
 * Used to treat the Banana's applied to users
 * both Virtual and Human
 * We can't have two banana's task in the same time, so we 
 * cancel the old one and put another one with the 
 * Scheduled time = + of the rest of first and 90 s 
 * for the second  
 * 
 * @author Oloieri Lilian
 * date 10 March 2010
 */

public class HumainPlayerBananaState {
	
	// timertask actually applied to player
	private BananaHumainTask bTask;
	
	// time to end of the actual banana
	private long taskDate;
	
	// is Banana applied to our player?
	private boolean isUnderBananaEffects;
	
	// is the state to one of them
	private JoueurHumain player;
		
	private static long bananaTime = 90000;

	// constructor - in the first time we are not in the Banana
	public HumainPlayerBananaState(JoueurHumain player) {
		//super();
		//this.setisUnderBananaEffects(false);
		this.player = player;
	}
	
	// setters and getters 
	public void setBTask(BananaHumainTask bTask) {
		this.bTask = bTask;
	}

	public BananaHumainTask getBTask() {
		return bTask;
	}

	public void setTaskDate(long taskDate) {
		this.taskDate = taskDate;
	}

	public long getTaskDate() {
		return taskDate;
	}

	/**
	 * @return the isUnderBananaEffects
	 */
	public boolean isUnderBananaEffects() {
		return isUnderBananaEffects;
	}

	/**
	 * @param isBananaOn the isBananaOn to set
	 */
	public void setisUnderBananaEffects(boolean isBananaOn) {
		this.isUnderBananaEffects = isBananaOn;
	}
	
	
	/*
	 * Method used to set a Banana to player with all the
	 * side effets applayed
	 */
	public void bananaIsTossed()
	{
		if(player != null){
			if(this.isUnderBananaEffects == false){
				
				this.isUnderBananaEffects = true;
			    this.bTask = Banane.utiliserBanane(player, bananaTime);
			    this.taskDate = System.currentTimeMillis() + bananaTime;
			    
			}else
			{
				this.bTask.cancel();
				long tempDate = this.taskDate  + bananaTime;
				this.bTask = Banane.utiliserBanane(player, tempDate);
				this.taskDate = tempDate;
				//System.out.println("BraniacTask !!!! " + tempDate + " " + " " + bTask);
				
								
			}	
		}
		
	}// end of method
	
	public void destruction()
	{
		if(this.bTask != null)
		   this.bTask.cancel();
		this.player = null;
	}
	


}// end of the class