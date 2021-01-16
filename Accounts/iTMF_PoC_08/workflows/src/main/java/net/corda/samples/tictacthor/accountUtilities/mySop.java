package net.corda.samples.tictacthor.accountUtilities;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import net.corda.samples.tictacthor.states.SopState;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.flows.StartableByService;
import net.corda.core.node.services.vault.QueryCriteria;

import java.util.*;

@StartableByRPC
@StartableByService
public class mySop extends FlowLogic<SopState>{

    private String whoAmI;
    public mySop(String whoAmI) {
        this.whoAmI = whoAmI;
    }

    @Override
    @Suspendable
    public SopState call() throws FlowException {
        AccountService accountService = getServiceHub().cordaService(KeyManagementBackedAccountService.class);
        AccountInfo myAccount = accountService.accountInfo(whoAmI).get(0).getState().getData();
        QueryCriteria.VaultQueryCriteria criteria = new QueryCriteria.VaultQueryCriteria().withExternalIds(Arrays.asList(myAccount.getIdentifier().getId()));
        SopState b = getServiceHub().getVaultService().queryBy(SopState.class,criteria).getStates().get(0).getState().getData();
        return b;
    }
}