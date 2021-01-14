package net.corda.samples.tictacthor.states;

import net.corda.samples.tictacthor.contracts.CargoStateContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(CargoStateContract.class)
public class CargoState implements ContractState {

    private AnonymousParty pickUpFrom;
    private AnonymousParty deliverTo;
    private String cargo;
    private String cargo1;
    private AbstractParty shipper;
    private List<AbstractParty> participants;


    public CargoState(AnonymousParty pickUpFrom, AnonymousParty deliverTo, String cargo, String cargo1, AbstractParty shipper) {
        this.pickUpFrom = pickUpFrom;
        this.deliverTo = deliverTo;
        this.cargo = cargo;
        this.cargo1 = cargo1;
        this.shipper = shipper;
        this.participants = new ArrayList<AbstractParty>();
        participants.add(pickUpFrom);
        participants.add(deliverTo);
        participants.add(shipper);
    }

    public AnonymousParty getPickUpFrom() {
        return pickUpFrom;
    }

    public void setPickUpFrom(AnonymousParty pickUpFrom) {
        this.pickUpFrom = pickUpFrom;
    }

    public AnonymousParty getDeliverTo() {
        return deliverTo;
    }

    public void setDeliverTo(AnonymousParty deliverTo) {
        this.deliverTo = deliverTo;
    }

    public String getCargo() {
        return cargo;
    }

    public String getCargo1() {
        return cargo1;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public void setCargo1(String cargo1) {
        this.cargo1 = cargo1;
    }

    public AbstractParty getShipper() {
        return shipper;
    }

    public void setShipper(AnonymousParty shipper) {
        this.shipper = shipper;
    }

    @NotNull @Override
    public List<AbstractParty> getParticipants() {
        return this.participants;
    }
}