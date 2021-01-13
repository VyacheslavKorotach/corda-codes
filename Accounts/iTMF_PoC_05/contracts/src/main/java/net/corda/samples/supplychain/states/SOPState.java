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

    private AnonymousParty paramedic;
    private AnonymousParty patient;
    private String sopSubStepDescription;
    private AbstractParty regulator;
    private List<AbstractParty> participants;


    public SOPState(AnonymousParty paramedic, AnonymousParty patient, String sopSubStepDescription, AbstractParty regulator) {
        this.paramedic = paramedic;
        this.patient = patient;
        this.sopSubStepDescription = sopSubStepDescription;
        this.regulator = regulator;
        this.participants = new ArrayList<AbstractParty>();
        participants.add(paramedic);
        participants.add(patient);
        participants.add(regulator);
    }

    public AnonymousParty getParamedic() {
        return paramedic;
    }

    public void setParamedic(AnonymousParty paramedic) {
        this.paramedic = paramedic;
    }

    public AnonymousParty getPatient() {
        return patient;
    }

    public void setPatient(AnonymousParty patient) {
        this.patient = patient;
    }

    public String getSopSubStepDescription() {
        return sopSubStepDescription;
    }

    public void setSopSubStepDescription(String sopSubStepDescription) {
        this.sopSubStepDescription = sopSubStepDescription;
    }

    public AbstractParty getRegulator() {
        return regulator;
    }

    public void setRegulator(AnonymousParty regulator) {
        this.regulator = regulator;
    }

    @NotNull @Override
    public List<AbstractParty> getParticipants() {
        return this.participants;
    }

//    @NotNull @Override
//    public List<AbstractParty> getParticipants() { return Arrays.asList(regulator, patient, paramedic); }
}