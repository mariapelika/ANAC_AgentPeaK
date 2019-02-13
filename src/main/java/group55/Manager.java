package group55;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TreeMap;


import negotiator.AgentID;
import negotiator.Bid;
import negotiator.BidIterator;
import negotiator.Domain;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.timeline.Timeline;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.issue.*;


public class Manager {
	AdditiveUtilitySpace US;
	Bid maxBid = null;
	Timeline T;
	double prev_util;
	double overallExpectedutil = 0.0;
	boolean FirstOfferGiven = false;
	Bid myNextBidIs = null;
	boolean conceded=false;
	Double tempUtility=(double) 0;
	ArrayList<Double> utils;
	ArrayList<Double> AllIncomingBidUtils;
	ArrayList<Object> myCurrentBiggerBids;
	TreeMap<Double, Bid> AllPossibleBids;
	
	public void ManageIncomingBid(AgentID sender,Bid lastReceivedBid) throws Exception{
		
		 //edw tha briskoume to utility tou bid pou ekane o antipalos
		 tempUtility =US.getUtility(lastReceivedBid);
		
		 
		 //tha to bazoume sti lista
		 AllIncomingBidUtils.add(tempUtility);
		 
		 myNextBidIs=myMinOpMan();	
	}
	
	
	public Bid myMinOpMan() throws Exception{
		conceded=false;
		int number=0;
		double total=0;
		List<Bid> bids = new ArrayList<Bid>();
		Bid pBid=null;
		
		//gia ton epomeno giro 
		for (Double u :AllIncomingBidUtils) {
		    total=total+u; //prosthetoume ola ta utilities apo tous antipalous
		    number++; // briskoume ton sinoloko arithmo bids antipalwn     
		}
		double OpTotal=total/number; //mesos oros utilities
		
		System.out.println("MESOS OROS PROIGOUMENON"+prev_util);
			
		//periptosi pou kanei concede (neo util<paliou)
		if( tempUtility<=prev_util && prev_util!=0) //concede
		{
		
			conceded=true;
			double concede=(prev_util-tempUtility)/4;
			
			while(bids.isEmpty()) { // se periptosi pou sta oria mas den iparxei bid xamilonoume to lowerbound 
				//gia na brethei bid me consession
				bids=getProperBid(tempUtility+concede,prev_util);
				concede=concede-0.05;
			} 		
		}
		//periptosi pou xtipaei xtipame kai emeis
		else if(tempUtility>prev_util && prev_util!=0)
		{			
			double lowerbound=tempUtility;
			while(bids.isEmpty()) { 				
				bids=getProperBid(lowerbound-0.01,1);
				lowerbound=lowerbound-0.1; // omoiws me panw
			}
		}
		else {//prwth fora
			conceded=true;
			double lowerbound=0.9;
			while(bids.isEmpty()) { 				
				bids=getProperBid(lowerbound-0.01,1);
				lowerbound=lowerbound-0.1; // omoiws me panw
			}			
		}		
		Random random=new Random();
		
		pBid=getfinalBid(bids);
		
		prev_util=OpTotal;
		
		
		
		if(pBid!=null)
			return pBid;		
		else 
			return US.getDomain().getRandomBid(random);
	}
	 
	public Bid getfinalBid(List<Bid> bids) { //briskei ton meso oro ton utilities ton pithanon bids
		// kai epilegoume to kontinotero bid = me ton meso oro
		
		Bid myBid=null;
		double lower=0,upper=0,total=0;
		//briskw meso oro
	    for(int i=0;i<bids.size();i++)
	    {
	    	total=total+US.getUtility(bids.get(i));	    	
	    }		
		lower=total/bids.size();
		upper=lower;
		
		//anoigo ta oria ean den iparxei bid konta ston meso oro 
		while(myBid==null) {
			
			for(int i=0;i<bids.size();i++) {
				
				try {
					double util = US.getUtility(bids.get(i));
					if (util >= lower && util <= upper) {
						myBid=bids.get(i);
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(lower>=0.025 && upper<=0.974) {
				lower=lower-0.025;
				upper=upper+0.025;
			}
			else if (upper>0.974) {
				lower=lower-0.025;
				upper=1;
			}
		}	
		
		return myBid;
	}
	
	
	public double CreateThreshold() throws Exception {
				 
		
		double compromisingFactor = GetComprFatctor();
		 
		double myThreshold = US.getUtility(myNextBidIs);
		return myThreshold*compromisingFactor;		
	}
	
	public double GetDiscountFactor() {
        if (this.US.getDiscountFactor() <= 0.001 || this.US.getDiscountFactor() > 1.0) {
            return 1.0;
        }
        return this.US.getDiscountFactor();
	}	 

	public double GetComprFatctor() {	
		 
		double Time = this.T.getTime();
		double DF = this.GetDiscountFactor();
		double myMin = 1.0;
		 
		myMin = DF *(Math.pow(DF, Time)); 
		return myMin;
	 }	     
	 
	 //ipologizw ola ta utility apo ola ta bid pou mporw na kanw sto utility space mou 
	public void findAllmyBidsPossible() throws Exception{
			
		Random random = new Random();
		int numOfPossibleBids = (int) US.getDomain().getNumberOfPossibleBids();
		
		for(int i =0; i < numOfPossibleBids ; i++){ 
			Bid randomBid = US.getDomain().getRandomBid(random);
			if((!AllPossibleBids.containsKey(US.getUtility(randomBid))) || (!AllPossibleBids.containsValue(randomBid))){
				AllPossibleBids.put(US.getUtility(randomBid), randomBid);				
			}
		}			
	 }
	 public Manager(AdditiveUtilitySpace US, Timeline T) throws Exception {
		this.T = T;
        this.US = US;
       
        try {
            double maxBidUtil = US.getUtility(this.maxBid); 
            if (maxBidUtil == 0.0) {
                this.maxBid = this.US.getMaxUtilityBid();
            }
        }
        catch (Exception e) {
            try {
                this.maxBid = this.US.getMaxUtilityBid();
            }
            catch (Exception var5_7) {
               
            }
        }
        
        myNextBidIs = maxBid;
        prev_util=0;
        this.AllIncomingBidUtils = new ArrayList<Double>();
        AllPossibleBids = new TreeMap<Double, Bid>();
        myCurrentBiggerBids = new ArrayList<Object>();
        myCurrentBiggerBids.add(maxBid);
        AllPossibleBids.put(US.getUtility(maxBid), maxBid);
        findAllmyBidsPossible();
	 }
	 
	 
	 /**
		 * Get all bids in a utility range.
		 */
	private List<Bid> getProperBid(double lower, double upper) {

		List<Bid> bids = new ArrayList<Bid>();
		BidIterator myBidIterator = new BidIterator(US.getDomain());
		
		while (myBidIterator.hasNext()) {
			Bid b = myBidIterator.next();
			
			try {
				double util = US.getUtility(b);
				if (util >= lower && util <= upper) {
					bids.add(b);
				
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bids;
	}
		
}
