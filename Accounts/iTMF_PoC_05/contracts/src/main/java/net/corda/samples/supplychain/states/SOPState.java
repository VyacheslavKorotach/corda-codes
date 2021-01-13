package net.corda.samples.supplychain.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.samples.supplychain.contracts.SOPStateContract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(SOPStateContract.class)
public class SOPState implements ContractState {

    private AnonymousParty pickUpFrom;
    private AnonymousParty patient;
    private String sop;
    private AbstractParty regulator;
    private List<AbstractParty> participants;


    public SOPState(AnonymousParty pickUpFrom, AnonymousParty patient, String sop, AbstractParty regulator) {
        this.pickUpFrom = pickUpFrom;
        this.patient = patient;
        this.sop = sop;
        this.regulator = regulator;
        this.participants = new ArrayList<AbstractParty>();
        participants.add(pickUpFrom);
        participants.add(patient);
        participants.add(regulator);
    }

    public AnonymousParty getPickUpFrom() {
        return pickUpFrom;
    }

    public void setPickUpFrom(AnonymousParty pickUpFrom) {
        this.pickUpFrom = pickUpFrom;
    }

    public AnonymousParty getPatient() {
        return patient;
    }

    public void setPatient(AnonymousParty patient) {
        this.patient = patient;
    }

    public String getSop() {
        return sop;
    }

    public void setSop(String sop) { this.sop = sop; }

    public AbstractParty getRegulator() {
        return regulator;
    }

    public void setRegulator(AnonymousParty regulator) {
        this.regulator = regulator;
    }

//    @NotNull @Override
//    public List<AbstractParty> getParticipants() {
//        return this.participants;
//    }

    @NotNull @Override
    public List<AbstractParty> getParticipants() { return Arrays.asList(regulator, patient, pickUpFrom); }
}