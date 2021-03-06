package net.corda.samples.tictacthor.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.workflows.flows.ShareStateAndSyncAccounts;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.flows.StartableByService;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.samples.tictacthor.states.SopState;

import java.util.Arrays;
import java.util.UUID;

@StartableByRPC
@StartableByService
public class SyncSop extends FlowLogic<String>{
    private String sopId;
    private Party party;

    public SyncSop(String sopId, Party party) {
        this.sopId = sopId;
        this.party = party;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {

        UUID id = UUID.fromString(sopId);
        QueryCriteria.LinearStateQueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria()
                .withUuid(Arrays.asList(id)).withStatus(Vault.StateStatus.UNCONSUMED);
        try {
            StateAndRef<SopState> inputSopStateAndRef = getServiceHub().getVaultService().queryBy(SopState.class,queryCriteria).getStates().get(0);
            subFlow(new ShareStateAndSyncAccounts(inputSopStateAndRef,party));

        }catch (Exception e){
            throw new FlowException("SOPState with id "+sopId+" not found.");
        }
        return "SOP synced";
    }
}