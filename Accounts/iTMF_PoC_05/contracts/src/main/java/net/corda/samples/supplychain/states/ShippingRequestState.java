package net.corda.samples.supplychain.states;

import net.corda.core.serialization.CordaSerializable;
import net.corda.samples.supplychain.contracts.ShippingRequestStateContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(ShippingRequestStateContract.class)
public class ShippingRequestState implements ContractState {

    private AnonymousParty pickUpFrom;
    private AnonymousParty deliverTo;
    private Party shippper;
    private String cargo;
    private List<AbstractParty> participants;
    private Status status;

    public ShippingRequestState(AnonymousParty pickUpFrom, AnonymousParty deliverTo, Party shippper, String cargo) {
        this.pickUpFrom = pickUpFrom;
        this.deliverTo = deliverTo;
        this.shippper = shippper;
        this.cargo = cargo;
        this.status = Status.GAME_IN_PROGRESS;
        this.participants = new ArrayList<AbstractParty>();
        participants.add(pickUpFrom);
        participants.add(shippper);
        participants.add(deliverTo);
    }

    @CordaSerializable
    public enum Status {
        GAME_IN_PROGRESS, GAME_OVER
    }

    public AnonymousParty getPickUpFrom() {
        return pickUpFrom;
    }

    public void setPickUpFrom(AnonymousParty pickUpFrom) {
        this.pickUpFrom = pickUpFrom;
    }

    public AnonymousParty getDeliverTo() {  return deliverTo; }

    public void setDeliverTo(AnonymousParty deliverTo) {
        this.deliverTo = deliverTo;
    }

    public Party getShippper() {
        return shippper;
    }

    public void setShippper(Party shippper) {
        this.shippper = shippper;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    @NotNull @Override
    public List<AbstractParty> getParticipants() {
        return this.participants;
    }
}