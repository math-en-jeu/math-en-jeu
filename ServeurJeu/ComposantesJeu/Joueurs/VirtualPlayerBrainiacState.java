package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.Brainiac;
import ServeurJeu.Temps.BrainiacVirtuelTask;


/**
 * Used to treat the Braniac's applyed to users
 * both Virtual and Humain
 * We can't have two braniac's in the same time, so we 
 * cancel the old one and put another one with the 
 * sheduled time = + of the rest of first and 90 s 
 * for the second  
 * 
 * @author Oloieri Lilian
 * date 10 March 2010
 */
public class VirtualPlayerBrainiacState {
	
	// timertask actually applayed to player
	private BrainiacVirtuelTask bTask;
	
	// time to end of the actual braniac
	private long taskDate;
	
	// is in Braniac our player?
	private boolean isInBrainiac;
	
	//our player
	private JoueurVirtuel vplayer;
	
	private static long BRAINTIME = 60000;

	// constructor - in the first time we are not in the Braniac
	public VirtualPlayerBrainiacState(JoueurVirtuel vplayer) {
		super();
		//this.setInBrainiac(false);
		this.vplayer = vplayer;
	}


	// setters and getters 
	public void setBTask(BrainiacVirtuelTask bTask) {
		this.bTask = bTask;
	}

	public BrainiacVirtuelTask getBTask() {
		return bTask;
	}

	public void setTaskDate(long taskDate) {
		this.taskDate = taskDate;
	}

	public long getTaskDate() {
		return taskDate;
	}

	public void setInBrainiac(boolean isInBrainiac) {
		this.isInBrainiac = isInBrainiac;
	}

	public boolean isInBrainiac() {
		return isInBrainiac;
	}
	
	
	/*
	 * Method used to set a Braniac to player with all the
	 * side effets applayed
	 */
	public void putTheOneBrainiac()
	{
		 if(vplayer != null){

			if(this.isInBrainiac == false){

				this.isInBrainiac = true;
				this.bTask = Brainiac.utiliserBrainiac(vplayer, BRAINTIME);
				this.taskDate = System.currentTimeMillis() + BRAINTIME;
			}else
			{
				this.bTask.cancel();
				long tempDate = this.taskDate  + BRAINTIME;
				this.bTask = Brainiac.utiliserBrainiac(vplayer, tempDate);
				this.taskDate = tempDate;
				//System.out.println("BraniacTask !!!! " + tempDate + " " + " " + bTask);

			}	
		}	
	}// end of method
	
	public void destruction()
	{
		if(this.bTask != null){
		   this.bTask.cancelTask();		   
		}
		this.vplayer = null;
	}


	/*
	 *  Used to set off the effects off Brainiac...
	 *  Now for the case if Banana is used on player
	 */
	public void setOffBrainiac() {
		if(this.isInBrainiac){
			this.isInBrainiac = false;
			this.bTask.cancelTask();
		}
	}
	
	public int getTaskTime()
	{
		if(this.isInBrainiac)
		  return (int) ((this.taskDate - System.currentTimeMillis())/1000);
		else
		  return 0;
	}
	
	

}//end of class 