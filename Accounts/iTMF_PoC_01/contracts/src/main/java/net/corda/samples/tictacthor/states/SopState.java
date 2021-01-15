package net.corda.samples.tictacthor.states;

import kotlin.Pair;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import net.corda.samples.tictacthor.contracts.SopContract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(SopContract.class)
public class SopState implements LinearState {


    private UniqueIdentifier paramedic;
    private UniqueIdentifier patient;
    private AnonymousParty me;
    private AnonymousParty counterparty;
//    private AnonymousParty investigation;
    private boolean isPlayerXTurn;
    private char[][] sop ;
    private UniqueIdentifier linearId;
    private Status status;

    public SopState(UniqueIdentifier paramedic, UniqueIdentifier patient, AnonymousParty me, AnonymousParty counterparty) {
        //dynamic
        this.paramedic = paramedic;
        this.patient = patient;
        this.me = me;
        this.counterparty = counterparty;

        //fixed
        this.isPlayerXTurn = false;
        this.sop = new char[][]{{'E','E','E'},{'E','E','E'},{'E','E','E'}};
        this.linearId = new UniqueIdentifier();
        this.status = Status.SOP_IN_PROGRESS;
    }

    @ConstructorForDeserialization
    public SopState(UniqueIdentifier paramedic, UniqueIdentifier patient,
                    AnonymousParty me, AnonymousParty counterparty,
                    boolean isPlayerXTurn, UniqueIdentifier linearId,
                    char[][] sop, Status status) {
        this.paramedic = paramedic;
        this.patient = patient;
        this.me = me;
        this.counterparty = counterparty;
        this.isPlayerXTurn = isPlayerXTurn;
        this.linearId = linearId;
        this.sop = sop;
        this.status = status;
    }

    @NotNull @Override
    public UniqueIdentifier getLinearId() { return this.linearId; }

    @NotNull @Override
    public List<AbstractParty> getParticipants() { return Arrays.asList(me, counterparty); }


    @CordaSerializable
    public enum Status {
        SOP_IN_PROGRESS, SOP_COMPLITED
    }

    // Returns the party of the current player
    public UniqueIdentifier getCurrentPlayerParty(){
        if(isPlayerXTurn){
            return patient;
        }else{
            return paramedic;
        }
    }

    public char[][] deepCopy(){
        char[][] newsop = new char[3][3];
        for(int i=0; i<this.sop.length; i++) {
            for (int j = 0; j < this.sop[i].length; j++) {
                newsop[i][j] = this.sop[i][j];
            }
        }
        return newsop;
    }

    public SopState returnNewSopAfterMove(Pair<Integer,Integer> pos, AnonymousParty me, AnonymousParty competitor){
        if((pos.getFirst() > 2) ||(pos.getSecond()> 2)){
            throw new IllegalStateException("Invalid sop index.");
        }
        char[][] newborad = this.deepCopy();
        if(isPlayerXTurn){
            newborad[pos.getFirst()][pos.getSecond()] = 'X';
        }else{
            newborad[pos.getFirst()][pos.getSecond()] = 'O';
        }
        if(SopContract.SopUtils.isSopCompleted(newborad)){
            SopState b = new SopState(this.paramedic,this.patient,me,competitor,!this.isPlayerXTurn,this.linearId, newborad, Status.SOP_COMPLITED);
            return b;
        }else{
            SopState b = new SopState(this.paramedic,this.patient,me,competitor,!this.isPlayerXTurn, this.linearId, newborad, Status.SOP_IN_PROGRESS);
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

    public boolean isPlayerXTurn() {
        return isPlayerXTurn;
    }

    public char[][] getSop() {
        return sop;
    }

    public Status getStatus() {
        return status;
    }

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

    public void setPlayerXTurn(boolean playerXTurn) {
        isPlayerXTurn = playerXTurn;
    }

    public void setSop(char[][] sop) {
        this.sop = sop;
    }

    public void setLinearId(UniqueIdentifier linearId) {
        this.linearId = linearId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}