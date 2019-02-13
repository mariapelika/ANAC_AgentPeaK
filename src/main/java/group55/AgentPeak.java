package group55;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.timeline.Timeline;
import negotiator.utility.AdditiveUtilitySpace;
public class AgentPeak  extends AbstractNegotiationParty {

	private Bid lastReceivedBid = null;
	private Manager IM;

	@Override
	public void init(NegotiationInfo info) {

		super.init(info);

		System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
		System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());

		// if you need to initialize some variables, please initialize them
		// below
		 //kanei intiallize to issue manager me vasi to utilspace to timeline kai to opponent model
        try {
			this.IM = new Manager((AdditiveUtilitySpace)this.utilitySpace, (Timeline) this.timeline);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	public Action chooseAction(List<Class<? extends Action>> validActions) {

		// with 50% chance, counter offer
		// if we are the first party, also offer.
		//System.out.println("TIME WHILE ACTIONING    "+this.T.getTime());
		try {
			if (lastReceivedBid == null || !validActions.contains(Accept.class) || this.getUtility(lastReceivedBid)<IM.CreateThreshold() ||  IM.conceded){
				System.out.println(" EKTELESTIKE OFFER");
				return new Offer(getPartyId(),IM.myNextBidIs);
			} else {
				System.out.println("EKTELESTIKE ACCEPT");
				return new Accept(getPartyId(), lastReceivedBid);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("DEN EKTELESTIKE");
		return null;
	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action. Can be null.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		if (action instanceof Offer) {//exw neo offer
			lastReceivedBid = ((Offer) action).getBid();
			try {
				IM.ManageIncomingBid(sender,lastReceivedBid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getDescription() {
		return "AgentPeaK";
	}

}