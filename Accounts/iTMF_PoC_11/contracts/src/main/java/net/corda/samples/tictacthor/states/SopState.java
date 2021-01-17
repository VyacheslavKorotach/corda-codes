package net.corda.samples.tictacthor.states;

import net.corda.core.crypto.TransactionSignature;
import net.corda.samples.tictacthor.contracts.SopContract;
//import javafx.util.Pair;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

// **********
// * State *
// *********
@BelongsToContract(SopContract.class)
public class SopState implements LinearState {


    private UniqueIdentifier paramedic;
    private UniqueIdentifier patient;
    private AnonymousParty me;
    private AnonymousParty counterparty;
    private boolean isPatientTurn;
    private int sop;
    private UniqueIdentifier linearId;
    private Status status;
    //SOP parameters
    private String sopID;
    private String paramedicName;
    private String patientName;
    private String subStepDescription;
    private float temperatureValue;
    //SOP SubSteps
    private String[] subSteps = {"The Paramedic chose the Patient and started the SOP",
            "Patient confirmed the SOP with the Paramedic",
            "Paramedic successfully measured the Patient temperature and put down the value",
            "The Patient confirmed the value of the Temperature measurement. SOP is successfully finished",
            "The SOP was canceled"};

    public SopState(UniqueIdentifier paramedic, UniqueIdentifier patient, AnonymousParty me, AnonymousParty counterparty) {
        //dynamic
        this.paramedic = paramedic;
        this.patient = patient;
        this.me = me;
        this.counterparty = counterparty;

        //fixed
        this.isPatientTurn = false;
        this.sop = 0;
        this.linearId = new UniqueIdentifier();
        this.status = Status.SOP_IN_PROGRESS;

        //SOP parameters
        this.sopID = "Temperature measurement";
        this.paramedicName = "";
        this.patientName = "";
        this.subStepDescription = subSteps[0];
        this.temperatureValue = 0.0F;
    }

    @ConstructorForDeserialization
    public SopState(UniqueIdentifier paramedic, UniqueIdentifier patient,
                    AnonymousParty me, AnonymousParty counterparty,
                    boolean isPatientTurn, UniqueIdentifier linearId,
                    int sop, Status status,
                    String sopID, String paramedicName, String patientName,
                    String subStepDescription, float temperatureValue) {
        this.paramedic = paramedic;
        this.patient = patient;
        this.me = me;
        this.counterparty = counterparty;
        this.isPatientTurn = isPatientTurn;
        this.linearId = linearId;
        this.sop = sop;
        this.status = status;
        this.sopID = sopID;
        this.paramedicName = paramedicName;
        this.patientName = patientName;
        this.subStepDescription = subStepDescription;
        this.temperatureValue = temperatureValue;
    }

    @NotNull @Override
    public UniqueIdentifier getLinearId() { return this.linearId; }

    @NotNull @Override
    public List<AbstractParty> getParticipants() { return Arrays.asList(me, counterparty); }


    @CordaSerializable
    public enum Status {
        SOP_IN_PROGRESS, SOP_COMPLETED
    }

    // Returns the party of the current player
    public UniqueIdentifier getCurrentPlayerParty(){
        if(isPatientTurn){
            return patient;
        }else{
            return paramedic;
        }
    }

//    public int deepCopy(){
//        int newbsop = this.sop;
//        return newbsop;
//    }

    public SopState returnNewSopAfterMove(Integer pos, AnonymousParty me, AnonymousParty competitor){
//        int newsop = this.deepCopy();
        int newsop = pos;
        if(isPatientTurn){
        }else{
        }
        if(SopContract.SopUtils.isSOPOver(newsop)){
            SopState b = new SopState(this.paramedic,this.patient,me,competitor,!this.isPatientTurn,this.linearId, newsop,Status.SOP_COMPLETED, this.sopID, this.paramedicName, this.patientName, this.subStepDescription, this.temperatureValue);
            return b;
        }else{
            SopState b = new SopState(this.paramedic,this.patient,me,competitor,!this.isPatientTurn, this.linearId, newsop,Status.SOP_IN_PROGRESS, this.sopID, this.paramedicName, this.patientName, this.subStepDescription, this.temperatureValue);
            return b;
        }
    }


    //getter setter
    public UniqueIdentifier getParamedic() {
        return paramedic;
    }

    public UniqueIdentifier getPatient() {
        return patient;
    }

    public AnonymousParty getMe() {
        return me;
    }

    public AnonymousParty getCounterparty() {
        return counterparty;
    }

    public boolean isPatientTurn() {
        return isPatientTurn;
    }

    public int getSop() {
        return sop;
    }

    public Status getStatus() {
        return status;
    }

    public String getSopID() { return sopID; }

    public String getParamedicName() { return paramedicName; }

    public String getPatientName() { return patientName; }

    public String getSubStepDescription() { return subStepDescription; }

    public float getTemperatureValue(){ return temperatureValue;}

    public void setParamedic(UniqueIdentifier paramedic) {
        this.paramedic = paramedic;
    }

    public void setPatient(UniqueIdentifier patient) {
        this.patient = patient;
    }

    public void setMe(AnonymousParty me) {
        this.me = me;
    }

    public void setCounterparty(AnonymousParty counterparty) {
        this.counterparty = counterparty;
    }

    public void setPatientTurn(boolean patientTurn) {
        isPatientTurn = patientTurn;
    }

    public void setSop(int sop) {
        this.sop = sop;
        this.subStepDescription = this.subSteps[sop];
    }

    public void setLinearId(UniqueIdentifier linearId) {
        this.linearId = linearId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setSopID(String sopID) { this.sopID = sopID; }

    public void setParamedicName(String paramedicName) { this.paramedicName = paramedicName; }

    public void setPatientName(String patientName) { this.patientName = patientName; }

    public void setSubStepDescription(String subStepDescription) { this.subStepDescription = subStepDescription; }

    public void setTemperatureValue(float temperatureValue) { this.temperatureValue = temperatureValue; }

}