package net.corda.samples.supplychain.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.samples.supplychain.contracts.SOPStateContract;
import org.jetbrains.annotations.NotNull;
//import net.corda.core.serialization.ConstructorForDeserialization;
//import net.corda.core.serialization.CordaSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(SOPStateContract.class)
public class SOPState implements ContractState {
//    private UniqueIdentifier sopUniqueNum;
//    private String sopId;
    private final int sopStepNum;
    private AnonymousParty paramedic;
    private AnonymousParty patient;
    private String sopDescription;
    private AbstractParty regulator;
    private boolean isParamedicTurn;
    private List<AbstractParty> participants;
//    private Status status;


    public SOPState(int sopStepNum, AnonymousParty paramedic, AnonymousParty patient, String sopDescription, AbstractParty regulator) {
        this.sopStepNum = sopStepNum;
        this.paramedic = paramedic;
        this.patient = patient;
        this.sopDescription = sopDescription;
        this.regulator = regulator;
        this.participants = new ArrayList<AbstractParty>();
        participants.add(paramedic);
        participants.add(patient);
        participants.add(regulator);
    }

//    @ConstructorForDeserialization
//    public SopState(UniqueIdentifier playerO, UniqueIdentifier playerX,
//                      AnonymousParty me, AnonymousParty competitor,
//                      boolean isPlayerXTurn, UniqueIdentifier linearId,
//                      char[][] board, Status status) {
//        this.playerO = playerO;
//        this.playerX = playerX;
//        this.me = me;
//        this.competitor = competitor;
//        this.isPlayerXTurn = isPlayerXTurn;
//        this.sopUniqueNum = sopUniqueNum;
//        this.board = board;
//        this.status = status;
//    }

//    @NotNull @Override
//    public UniqueIdentifier getSopUniqueNum() { return this.sopUniqueNum; }

//    @CordaSerializable
//    public enum Status {
//        SOP_IN_PROGRESS, SOP_OVER
//    }

    public int getSopStepNum() { return sopStepNum; }

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

    public String getSopDescription() {
        return sopDescription;
    }

    public void setSopDescription(String sopDescription) { this.sopDescription = sopDescription; }

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
    public List<AbstractParty> getParticipants() { return Arrays.asList(regulator, patient, paramedic); }
}