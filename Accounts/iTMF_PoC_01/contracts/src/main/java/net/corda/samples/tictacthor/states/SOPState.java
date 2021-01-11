package net.corda.samples.tictacthor.states;

import kotlin.Pair;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import net.corda.samples.tictacthor.contracts.SOPContract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(SOPContract.class)
public class SOPState implements LinearState {


    private UniqueIdentifier playerO;
    private UniqueIdentifier playerX;
    private AnonymousParty me;
    private AnonymousParty competitor;
    private AnonymousParty investigation;
    private boolean isPlayerXTurn;
    private char[][] sop ;
    private UniqueIdentifier linearId;
    private Status status;

    public SOPState(UniqueIdentifier playerO, UniqueIdentifier playerX, AnonymousParty me, AnonymousParty competitor) {
        //dynamic
        this.playerO = playerO;
        this.playerX = playerX;
        this.me = me;
        this.competitor = competitor;

        //fixed
        this.isPlayerXTurn = false;
        this.sop = new char[][]{{'E','E','E'},{'E','E','E'},{'E','E','E'}};
        this.linearId = new UniqueIdentifier();
        this.status = Status.GAME_IN_PROGRESS;
    }

    @ConstructorForDeserialization
    public SOPState(UniqueIdentifier playerO, UniqueIdentifier playerX,
                    AnonymousParty me, AnonymousParty competitor,
                    boolean isPlayerXTurn, UniqueIdentifier linearId,
                    char[][] sop, Status status) {
        this.playerO = playerO;
        this.playerX = playerX;
        this.me = me;
        this.competitor = competitor;
        this.isPlayerXTurn = isPlayerXTurn;
        this.linearId = linearId;
        this.sop = sop;
        this.status = status;
    }

    @NotNull @Override
    public UniqueIdentifier getLinearId() { return this.linearId; }

    @NotNull @Override
    public List<AbstractParty> getParticipants() { return Arrays.asList(me,competitor); }


    @CordaSerializable
    public enum Status {
        GAME_IN_PROGRESS, GAME_OVER
    }

    // Returns the party of the current player
    public UniqueIdentifier getCurrentPlayerParty(){
        if(isPlayerXTurn){
            return playerX;
        }else{
            return playerO;
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

    public SOPState returnNewSOPAfterMove(Pair<Integer,Integer> pos, AnonymousParty me, AnonymousParty competitor){
        if((pos.getFirst() > 2) ||(pos.getSecond()> 2)){
            throw new IllegalStateException("Invalid sop index.");
        }
        char[][] newborad = this.deepCopy();
        if(isPlayerXTurn){
            newborad[pos.getFirst()][pos.getSecond()] = 'X';
        }else{
            newborad[pos.getFirst()][pos.getSecond()] = 'O';
        }
        if(SOPContract.SOPUtils.isGameOver(newborad)){
            SOPState b = new SOPState(this.playerO,this.playerX,me,competitor,!this.isPlayerXTurn,this.linearId, newborad, Status.GAME_OVER);
            return b;
        }else{
            SOPState b = new SOPState(this.playerO,this.playerX,me,competitor,!this.isPlayerXTurn, this.linearId, newborad, Status.GAME_IN_PROGRESS);
            return b;
        }
    }


    //getter setter
    public UniqueIdentifier getPlayerO() {
        return playerO;
    }

    public UniqueIdentifier getPlayerX() {
        return playerX;
    }

    public AnonymousParty getMe() {
        return me;
    }

    public AnonymousParty getCompetitor() {
        return competitor;
    }

    public boolean isPlayerXTurn() {
        return isPlayerXTurn;
    }

    public char[][] getSOP() {
        return sop;
    }

    public Status getStatus() {
        return status;
    }

    public void setPlayerO(UniqueIdentifier playerO) {
        this.playerO = playerO;
    }

    public void setPlayerX(UniqueIdentifier playerX) {
        this.playerX = playerX;
    }

    public void setMe(AnonymousParty me) {
        this.me = me;
    }

    public void setCompetitor(AnonymousParty competitor) {
        this.competitor = competitor;
    }

    public void setPlayerXTurn(boolean playerXTurn) {
        isPlayerXTurn = playerXTurn;
    }

    public void setSOP(char[][] sop) {
        this.sop = sop;
    }

    public void setLinearId(UniqueIdentifier linearId) {
        this.linearId = linearId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}